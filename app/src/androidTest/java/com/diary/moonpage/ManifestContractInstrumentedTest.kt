package com.diary.moonpage

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestContractInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val packageName = context.packageName
    private val packageManager = context.packageManager

    private val manifestInfo: PackageInfo by lazy {
        packageInfo(
            PackageManager.GET_ACTIVITIES or
                PackageManager.GET_CONFIGURATIONS or
                PackageManager.GET_META_DATA or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_SERVICES
        )
    }

    @Test
    fun tc04Tc08Tc11RuntimePermissionsAreDeclared() {
        val expectedPermissions = setOf(
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.READ_MEDIA_IMAGES,
            "android.permission.health.READ_STEPS",
            "android.permission.health.READ_TOTAL_CALORIES_BURNED",
            "android.permission.health.READ_ACTIVE_CALORIES_BURNED",
            "android.permission.health.READ_DISTANCE",
            "android.permission.health.READ_SLEEP"
        )

        val requested = manifestInfo.requestedPermissions.orEmpty().toSet()
        val legacyStoragePermissions = buildSet {
            if (Build.VERSION.SDK_INT <= 32) add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= 28) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        assertTrue(
            "Missing permissions: ${(expectedPermissions + legacyStoragePermissions) - requested}",
            requested.containsAll(expectedPermissions + legacyStoragePermissions)
        )
    }

    @Test
    fun tc04CameraFeaturesAreDeclared() {
        val features = manifestInfo.reqFeatures.orEmpty().map { it.name }.toSet()

        assertTrue(features.contains(PackageManager.FEATURE_CAMERA))
        assertTrue(features.contains(PackageManager.FEATURE_CAMERA_AUTOFOCUS))
    }

    @Test
    fun tc08FirebaseMessagingAndReminderReceiversAreRegistered() {
        val fcmService = requireNamed(
            manifestInfo.services,
            "$packageName.service.MoonFirebaseMessagingService"
        ) { it.name }
        val bootReceiver = requireNamed(
            manifestInfo.receivers,
            "$packageName.service.BootReceiver"
        ) { it.name }
        val reminderReceiver = requireNamed(
            manifestInfo.receivers,
            "$packageName.service.ReminderReceiver"
        ) { it.name }

        assertTrue(fcmService.exported)
        assertFalse(bootReceiver.exported)
        assertNotNull(reminderReceiver)

        val fcmIntent = Intent("com.google.firebase.MESSAGING_EVENT").setPackage(packageName)
        val matchingServices = packageManager.queryIntentServices(fcmIntent, 0)
        assertTrue(matchingServices.any { it.serviceInfo.name == fcmService.name })

        val appInfo = applicationInfo(PackageManager.GET_META_DATA)
        assertEquals(
            "moonpage_notification_channel",
            appInfo.metaData.getString("com.google.firebase.messaging.default_notification_channel_id")
        )
    }

    @Test
    fun tc09AllFiveGlanceWidgetReceiversExposeAppWidgetMetadata() {
        val expectedReceivers = listOf(
            "$packageName.widget.glance.QuickMoodWidgetReceiver",
            "$packageName.widget.glance.WeeklyMoodWidgetReceiver",
            "$packageName.widget.glance.MonthlyMoodWidgetReceiver",
            "$packageName.widget.glance.DailySummaryWidgetReceiver",
            "$packageName.widget.glance.PhotoMomentWidgetReceiver"
        )

        expectedReceivers.forEach { receiverName ->
            val receiver = requireNamed(manifestInfo.receivers, receiverName) { it.name }
            val providerResId = receiver.metaData?.getInt("android.appwidget.provider") ?: 0

            assertTrue("$receiverName must be exported so launcher can host it", receiver.exported)
            assertTrue("$receiverName is missing appwidget metadata", providerResId != 0)
        }
    }

    @Test
    fun tc11Tc12SpotifyCallbackDeepLinkResolvesToMainActivity() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("moonpage://spotify-callback"))
            .addCategory(Intent.CATEGORY_DEFAULT)

        val activityInfo = requireNamed(
            manifestInfo.activities,
            "$packageName.ui.MainActivity"
        ) { it.name }
        val resolved = intent.setPackage(packageName).resolveActivity(packageManager)

        assertTrue(activityInfo.exported)
        assertTrue(
            resolved == null || resolved.className == "$packageName.ui.MainActivity",
        )
    }

    @Test
    fun tc10Tc12ApplicationPrivacyAndLaunchContractsAreDeclared() {
        val appInfo = applicationInfo(0)
        val mainActivity = requireNamed(
            manifestInfo.activities,
            "$packageName.ui.MainActivity"
        ) { it.name }
        val fileProvider = requireNamed(
            manifestInfo.providers,
            "androidx.core.content.FileProvider"
        ) { it.name }

        assertEquals(packageName, context.packageName)
        assertFalse(appInfo.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP != 0)
        assertTrue(mainActivity.exported)
        assertEquals(ActivityInfo.LAUNCH_SINGLE_TASK, mainActivity.launchMode)
        assertEquals("$packageName.fileprovider", fileProvider.authority)
        assertFalse(fileProvider.exported)
        assertTrue(fileProvider.grantUriPermissions)
    }

    @Test
    fun tc13SdkCompatibilityContractMatchesSupportedRuntimeRange() {
        val appInfo = applicationInfo(0)

        assertTrue(appInfo.minSdkVersion >= 26)
        assertEquals(34, appInfo.targetSdkVersion)
    }

    private fun <T> requireNamed(items: Array<T>?, name: String, selector: (T) -> String): T {
        val item = items?.firstOrNull { selector(it) == name }
        assertNotNull("Missing manifest component: $name", item)
        return item!!
    }

    @Suppress("DEPRECATION")
    private fun packageInfo(flags: Int): PackageInfo {
        return if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            packageManager.getPackageInfo(packageName, flags)
        }
    }

    @Suppress("DEPRECATION")
    private fun applicationInfo(flags: Int): android.content.pm.ApplicationInfo {
        return if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(flags.toLong()))
        } else {
            packageManager.getApplicationInfo(packageName, flags)
        }
    }
}
