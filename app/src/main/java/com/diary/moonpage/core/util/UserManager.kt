package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.diary.moonpage.data.remote.dto.auth.UserResponseDto
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private val USER_DATA_KEY = stringPreferencesKey("user_data")
        private val LOCAL_AVATAR_PATH_KEY = stringPreferencesKey("local_avatar_path")
    }

    suspend fun saveUser(user: UserResponseDto) {
        val userJson = gson.toJson(user)
        context.userDataStore.edit { preferences ->
            preferences[USER_DATA_KEY] = userJson
        }
    }

    fun getUser(): Flow<UserResponseDto?> {
        return context.userDataStore.data.map { preferences ->
            val userJson = preferences[USER_DATA_KEY]
            if (userJson != null) {
                gson.fromJson(userJson, UserResponseDto::class.java)
            } else {
                null
            }
        }
    }

    suspend fun saveLocalAvatarPath(path: String?) {
        context.userDataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(LOCAL_AVATAR_PATH_KEY)
            } else {
                preferences[LOCAL_AVATAR_PATH_KEY] = path
            }
        }
    }

    fun getLocalAvatarPath(): Flow<String?> {
        return context.userDataStore.data.map { preferences ->
            preferences[LOCAL_AVATAR_PATH_KEY]
        }
    }

    suspend fun clearUser() {
        context.userDataStore.edit { preferences ->
            preferences.remove(USER_DATA_KEY)
            preferences.remove(LOCAL_AVATAR_PATH_KEY)
        }
    }
}
