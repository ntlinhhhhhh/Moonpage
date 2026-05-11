package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val TOKEN_KEY     = stringPreferencesKey("jwt_token")
        private val SPOTIFY_TOKEN_KEY = stringPreferencesKey("spotify_token")
        private val SPOTIFY_VERIFIER_KEY = stringPreferencesKey("spotify_verifier")
        private val SPOTIFY_STATE_KEY = stringPreferencesKey("spotify_state")
        private val USER_ID_KEY   = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    suspend fun saveSpotifyToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[SPOTIFY_TOKEN_KEY] = token
        }
    }

    fun getSpotifyToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[SPOTIFY_TOKEN_KEY]
        }
    }

    suspend fun saveSpotifyAuthData(verifier: String, state: String) {
        context.dataStore.edit { preferences ->
            preferences[SPOTIFY_VERIFIER_KEY] = verifier
            preferences[SPOTIFY_STATE_KEY] = state
        }
    }

    suspend fun getSpotifyVerifier(): String? {
        return context.dataStore.data.map { it[SPOTIFY_VERIFIER_KEY] }.first()
    }

    suspend fun getSpotifyAuthState(): String? {
        return context.dataStore.data.map { it[SPOTIFY_STATE_KEY] }.first()
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }.first()
    }

    suspend fun getUserName(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }.first()
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
