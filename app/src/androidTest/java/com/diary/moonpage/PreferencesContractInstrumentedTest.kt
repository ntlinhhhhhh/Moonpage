package com.diary.moonpage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diary.moonpage.core.util.SettingsPreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesContractInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val settings = SettingsPreferencesManager(context)

    @Before
    fun clearBefore() = runBlocking {
        settings.clearAll()
    }

    @After
    fun clearAfter() = runBlocking {
        settings.clearAll()
    }

    @Test
    fun tc01Tc07Tc08SettingsDefaultsMatchFirstRunContracts() = runBlocking {
        assertEquals("en", settings.language.first())
        assertFalse(settings.isTutorialCompleted.first())
        assertEquals("21:00", settings.reminderTime.first())
        assertFalse(settings.isReminderEnabled.first())
        assertNull(settings.passcode.first())
        assertFalse(settings.isPasscodeEnabled.first())
        assertFalse(settings.isBiometricEnabled.first())
        assertTrue(settings.isMusicEnabled.first())
        assertTrue(settings.isSleepEnabled.first())
        assertTrue(settings.isStepsEnabled.first())
        assertTrue(settings.isMenstruationEnabled.first())
    }

    @Test
    fun tc07Tc10PasscodeAndBiometricSettingsPersistAndCanBeCleared() = runBlocking {
        settings.setPasscode("1234")
        settings.setBiometricEnabled(true)

        assertEquals("1234", settings.passcode.first())
        assertTrue(settings.isPasscodeEnabled.first())
        assertTrue(settings.isBiometricEnabled.first())

        settings.setPasscode(null)
        settings.setBiometricEnabled(false)

        assertNull(settings.passcode.first())
        assertFalse(settings.isPasscodeEnabled.first())
        assertFalse(settings.isBiometricEnabled.first())
    }

    @Test
    fun tc02DailyLogSpecialBlockTogglesPersist() = runBlocking {
        settings.setSpecialBlocksEnabled(
            music = false,
            sleep = true,
            steps = false,
            menstruation = true
        )

        assertFalse(settings.isMusicEnabled.first())
        assertTrue(settings.isSleepEnabled.first())
        assertFalse(settings.isStepsEnabled.first())
        assertTrue(settings.isMenstruationEnabled.first())
    }

    @Test
    fun tc07Tc13LanguageSettingPersistsToDataStoreAndSharedPreferences() = runBlocking {
        settings.setLanguage("vi")

        assertEquals("vi", settings.language.first())
        assertEquals("vi", context.getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE).getString("language", null))
    }
}
