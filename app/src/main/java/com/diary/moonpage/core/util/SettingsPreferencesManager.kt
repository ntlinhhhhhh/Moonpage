package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

internal val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val PASSCODE_KEY = stringPreferencesKey("passcode")
        private val IS_PASSCODE_ENABLED_KEY = booleanPreferencesKey("is_passcode_enabled")
        private val IS_BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("is_biometric_enabled")
        private val REMINDER_TIME_KEY = stringPreferencesKey("reminder_time")
        private val IS_REMINDER_ENABLED_KEY = booleanPreferencesKey("is_reminder_enabled")
        private val IS_TUTORIAL_COMPLETED_KEY = booleanPreferencesKey("is_tutorial_completed")
        
        // Special blocks
        private val IS_MUSIC_ENABLED_KEY = booleanPreferencesKey("is_music_enabled")
        private val IS_SLEEP_ENABLED_KEY = booleanPreferencesKey("is_sleep_enabled")
        private val IS_STEPS_ENABLED_KEY = booleanPreferencesKey("is_steps_enabled")
        private val IS_MENSTRUATION_ENABLED_KEY = booleanPreferencesKey("is_menstruation_enabled")
    }

    val language: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    val isTutorialCompleted: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_TUTORIAL_COMPLETED_KEY] ?: false
    }

    val reminderTime: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[REMINDER_TIME_KEY] ?: "21:00"
    }

    val isReminderEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_REMINDER_ENABLED_KEY] ?: false
    }

    val passcode: Flow<String?> = context.settingsDataStore.data.map { preferences ->
        preferences[PASSCODE_KEY]
    }

    val isPasscodeEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_PASSCODE_ENABLED_KEY] ?: false
    }

    val isBiometricEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED_KEY] ?: false
    }

    val isMusicEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_MUSIC_ENABLED_KEY] ?: true // Default true
    }

    val isSleepEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_SLEEP_ENABLED_KEY] ?: true // Default true
    }

    val isStepsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_STEPS_ENABLED_KEY] ?: true // Default true
    }

    val isMenstruationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_MENSTRUATION_ENABLED_KEY] ?: true // Default true
    }

    suspend fun setReminderTime(time: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[REMINDER_TIME_KEY] = time
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_REMINDER_ENABLED_KEY] = enabled
        }
    }

    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
        // Also save to SharedPreferences for synchronous access in attachBaseContext
        context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("language", language)
            .apply()
    }

    suspend fun setPasscode(passcode: String?) {
        context.settingsDataStore.edit { preferences ->
            if (passcode == null) {
                preferences.remove(PASSCODE_KEY)
                preferences[IS_PASSCODE_ENABLED_KEY] = false
            } else {
                preferences[PASSCODE_KEY] = passcode
                preferences[IS_PASSCODE_ENABLED_KEY] = true
            }
        }
    }

    suspend fun setPasscodeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_PASSCODE_ENABLED_KEY] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_TUTORIAL_COMPLETED_KEY] = completed
        }
    }

    suspend fun setSpecialBlocksEnabled(music: Boolean, sleep: Boolean, steps: Boolean, menstruation: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_MUSIC_ENABLED_KEY] = music
            preferences[IS_SLEEP_ENABLED_KEY] = sleep
            preferences[IS_STEPS_ENABLED_KEY] = steps
            preferences[IS_MENSTRUATION_ENABLED_KEY] = menstruation
        }
    }

    suspend fun clearAll() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
