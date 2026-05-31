package com.diary.moonpage.widget.glance

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.diary.moonpage.R
import kotlinx.coroutines.flow.first

private const val PHOTO_ROTATE_INTERVAL_MS = 5 * 60_000L
private const val PHOTO_ROTATE_WINDOW_MS = 60_000L
private const val PHOTO_ROTATE_ACTION = "com.diary.moonpage.widget.PHOTO_MOMENT_ROTATE"
private const val PHOTO_ROTATE_REQUEST_CODE = 5102

class PhotoMomentWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = MoonpageWidgetDataSource(context)
        val snapshot = dataSource.loadTodaySnapshot()
        val photoCount = snapshot.photoUris.size
        if (photoCount > 1) {
            PhotoMomentRotationScheduler.schedule(context)
        } else {
            PhotoMomentRotationScheduler.cancel(context)
        }
        val resolvedIndex = if (photoCount == 0) {
            0
        } else {
            ((System.currentTimeMillis() / PHOTO_ROTATE_INTERVAL_MS) % photoCount).toInt()
        }
        val bitmap = dataSource.loadBitmap(snapshot.photoUris.getOrNull(resolvedIndex))
        val isNight = dataSource.isNightMode()
        val palette = snapshot.palette
        val preferences = dataSource.getWidgetPreferences()

        val openAppAction = actionStartActivity(
            MoonpageWidgets.openAppIntent(context, MoonpageWidgets.ROUTE_CAMERA)
        )

        // Read preferences once — avoids collectAsState keeping a live Flow session
        // that gets cancelled every time widget.update() is called.
        val showStreak = preferences.showPhotoStreak.first()
        val displayMode = preferences.photoDisplayMode.first()
        val streakText = context.getString(R.string.streak_badge, snapshot.streakCount)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp)
                    .background(
                        ColorProvider(if (isNight) palette.nightSurface else palette.daySurface)
                    )
                    .clickable(openAppAction),
                contentAlignment = Alignment.TopStart
            ) {
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        contentScale = if (displayMode == "FIT") ContentScale.Fit else ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.logo),
                            contentDescription = null,
                            modifier = GlanceModifier.size(60.dp)
                        )
                    }
                }

                if (showStreak) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize().padding(top = 12.dp, end = 12.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = streakText,
                            modifier = GlanceModifier
                                .cornerRadius(50.dp)
                                .background(ColorProvider(if (isNight) palette.nightBadge else palette.dayBadge))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            style = TextStyle(
                                color = ColorProvider(if (isNight) palette.nightBadgeText else palette.dayBadgeText),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

private object PhotoMomentRotationScheduler {
    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = pendingIntent(context)
        val triggerAt = SystemClock.elapsedRealtime() + PHOTO_ROTATE_INTERVAL_MS

        alarmManager.setWindow(
            AlarmManager.ELAPSED_REALTIME,
            triggerAt,
            PHOTO_ROTATE_WINDOW_MS,
            pendingIntent
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, PhotoMomentAutoRotateReceiver::class.java).apply {
            action = PHOTO_ROTATE_ACTION
        }
        return PendingIntent.getBroadcast(
            context,
            PHOTO_ROTATE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class PhotoMomentAutoRotateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != PHOTO_ROTATE_ACTION) return

        PhotoMomentRotationScheduler.schedule(context)
        MoonpageWidgetRefreshManager.requestRefresh(
            context = context,
            reason = MoonpageWidgetRefreshManager.Reason.PHOTO_ROTATION
        )
    }
}

class PhotoMomentWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhotoMomentWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        PhotoMomentRotationScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        PhotoMomentRotationScheduler.cancel(context)
        super.onDisabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            PhotoMomentRotationScheduler.schedule(context)
        }
    }
}
