package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.diary.moonpage.presentation.theme.MoonThemeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val THEME_TYPE_KEY = stringPreferencesKey("theme_type")
        private val DARK_MODE_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("dark_mode")
    }

    val themeType: Flow<MoonThemeType> = context.themeDataStore.data.map { prefs ->
        val themeName = prefs[THEME_TYPE_KEY] ?: MoonThemeType.DEFAULT.name
        try {
            MoonThemeType.valueOf(themeName)
        } catch (e: Exception) {
            MoonThemeType.DEFAULT
        }
    }

    val isDarkMode: Flow<Boolean?> = context.themeDataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY]
    }

    suspend fun setThemeType(themeType: MoonThemeType) {
        context.themeDataStore.edit { prefs ->
            prefs[THEME_TYPE_KEY] = themeType.name
        }
    }

    suspend fun setDarkMode(isDark: Boolean?) {
        context.themeDataStore.edit { prefs ->
            if (isDark == null) {
                prefs.remove(DARK_MODE_KEY)
            } else {
                prefs[DARK_MODE_KEY] = isDark
            }
        }
    }
}
