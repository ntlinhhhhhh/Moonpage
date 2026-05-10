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
    }

    val language: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
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
}
