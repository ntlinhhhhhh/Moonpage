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
            val previousMoments = parseMoments(preferences[MOMENT_LIST_KEY])
            val currentPaths = parsePaths(preferences[LOCAL_PATHS_KEY])
            preferences[MOMENT_LIST_KEY] = json
            val migratedPaths = migrateLocalPaths(
                currentPaths = currentPaths,
                previousMoments = previousMoments,
                updatedMoments = moments
            )
            if (migratedPaths.isNotEmpty()) {
                preferences[LOCAL_PATHS_KEY] = gson.toJson(migratedPaths)
            }
        }
    }

    fun getMoments(): Flow<List<Moment>> {
        return context.momentDataStore.data.map { preferences ->
            parseMoments(preferences[MOMENT_LIST_KEY])
        }
    }

    suspend fun saveLocalPaths(paths: Map<String, String>) {
        val json = gson.toJson(paths)
        context.momentDataStore.edit { preferences ->
            preferences[LOCAL_PATHS_KEY] = json
        }
    }

    suspend fun addLocalPath(momentId: String, imageUrl: String?, localPath: String) {
        context.momentDataStore.edit { preferences ->
            val currentPaths = parsePaths(preferences[LOCAL_PATHS_KEY])
            currentPaths[momentId] = localPath
            imageUrl
                ?.takeUnless { it == momentId }
                ?.let { legacyKey -> currentPaths.remove(legacyKey) }
            preferences[LOCAL_PATHS_KEY] = gson.toJson(currentPaths)
        }
    }

    fun getLocalPaths(): Flow<Map<String, String>> {
        return context.momentDataStore.data.map { preferences ->
            parsePaths(preferences[LOCAL_PATHS_KEY])
        }
    }

    suspend fun clearCache() {
        context.momentDataStore.edit { preferences ->
            preferences.remove(MOMENT_LIST_KEY)
            preferences.remove(LOCAL_PATHS_KEY)
        }
    }

    private fun parseMoments(json: String?): List<Moment> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<Moment>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun parsePaths(json: String?): MutableMap<String, String> {
        if (json.isNullOrEmpty()) return mutableMapOf()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return (gson.fromJson<Map<String, String>>(json, type) ?: emptyMap()).toMutableMap()
    }

    private fun migrateLocalPaths(
        currentPaths: Map<String, String>,
        previousMoments: List<Moment>,
        updatedMoments: List<Moment>
    ): Map<String, String> {
        if (currentPaths.isEmpty()) return emptyMap()

        val previousById = previousMoments.associateBy { it.id }
        val migratedPaths = currentPaths.toMutableMap()

        updatedMoments.forEach { moment ->
            val candidateKeys = buildList {
                add(moment.id)
                add(moment.imageUrl)
                previousById[moment.id]?.imageUrl?.let(::add)
            }.distinct()

            val resolvedPath = candidateKeys.firstNotNullOfOrNull(currentPaths::get) ?: return@forEach
            migratedPaths[moment.id] = resolvedPath
            candidateKeys
                .filter { it != moment.id }
                .forEach(migratedPaths::remove)
        }

        return migratedPaths
    }
}
