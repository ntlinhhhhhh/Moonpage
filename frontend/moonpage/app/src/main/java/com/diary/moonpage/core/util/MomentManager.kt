package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.diary.moonpage.domain.model.Moment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.momentDataStore: DataStore<Preferences> by preferencesDataStore(name = "moment_cache")

@Singleton
class MomentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private val MOMENT_LIST_KEY = stringPreferencesKey("cached_moments")
        private val LOCAL_PATHS_KEY = stringPreferencesKey("local_paths")
    }

    suspend fun saveMoments(moments: List<Moment>) {
        val json = gson.toJson(moments)
        context.momentDataStore.edit { preferences ->
            preferences[MOMENT_LIST_KEY] = json
        }
    }

    fun getMoments(): Flow<List<Moment>> {
        return context.momentDataStore.data.map { preferences ->
            val json = preferences[MOMENT_LIST_KEY]
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<Moment>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        }
    }

    suspend fun saveLocalPaths(paths: Map<String, String>) {
        val json = gson.toJson(paths)
        context.momentDataStore.edit { preferences ->
            preferences[LOCAL_PATHS_KEY] = json
        }
    }

    suspend fun addLocalPath(imageUrl: String, localPath: String) {
        context.momentDataStore.edit { preferences ->
            val currentJson = preferences[LOCAL_PATHS_KEY]
            val currentPaths: MutableMap<String, String> = if (!currentJson.isNullOrEmpty()) {
                val type = object : TypeToken<Map<String, String>>() {}.type
                gson.fromJson(currentJson, type)
            } else {
                mutableMapOf()
            }
            currentPaths[imageUrl] = localPath
            preferences[LOCAL_PATHS_KEY] = gson.toJson(currentPaths)
        }
    }

    fun getLocalPaths(): Flow<Map<String, String>> {
        return context.momentDataStore.data.map { preferences ->
            val json = preferences[LOCAL_PATHS_KEY]
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<Map<String, String>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyMap()
            }
        }
    }

    suspend fun clearCache() {
        context.momentDataStore.edit { preferences ->
            preferences.remove(MOMENT_LIST_KEY)
            preferences.remove(LOCAL_PATHS_KEY)
        }
    }
}
