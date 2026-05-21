package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_prefs")

@Singleton
class LanguagePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language_code")
    }

    val languageCode: Flow<String> = context.languageDataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: "en" // Default to English
    }

    suspend fun setLanguage(languageCode: String) {
        context.languageDataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = languageCode
        }
    }
}
