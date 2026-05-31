package com.diary.moonpage.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dailyLogPhotoDataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_log_photo_cache")

/**
 * Manages local file caching for Daily Log photos.
 *
 * Stores a mapping of remote URL → local permanent file path so that
 * photos are loaded instantly from disk (no network required) after the
 * first upload, and saves() can skip re-downloading already-uploaded images.
 */
@Singleton
class DailyLogPhotoManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private val LOCAL_PATHS_KEY = stringPreferencesKey("daily_log_local_paths")
        private const val PHOTOS_DIR = "daily_log_photos"
    }

    /** Returns a Flow of remote URL → local absolute file path. */
    fun getLocalPaths(): Flow<Map<String, String>> {
        return context.dailyLogPhotoDataStore.data.map { prefs ->
            parsePaths(prefs[LOCAL_PATHS_KEY])
        }
    }

    /**
     * Returns the cached local path for [remoteUrl], or null if not cached /
     * the file no longer exists on disk.
     */
    suspend fun getLocalPath(remoteUrl: String): String? {
        val paths = context.dailyLogPhotoDataStore.data.map { prefs ->
            parsePaths(prefs[LOCAL_PATHS_KEY])
        }
        return paths.first().let { map ->
            val path = map[remoteUrl] ?: return@let null
            if (File(path).exists()) path else null
        }
    }

    /**
     * Saves a compressed source [file] to the permanent photos directory and
     * records the mapping of [remoteUrl] → permanent local path.
     *
     * @return The permanent [File] on disk.
     */
    suspend fun savePhoto(remoteUrl: String, file: File): File {
        val photosDir = File(context.filesDir, PHOTOS_DIR)
        photosDir.mkdirs()
        val fileName = "dl_${remoteUrl.hashCode()}_${file.name}"
        val permanentFile = File(photosDir, fileName)
        file.copyTo(permanentFile, overwrite = true)
        addPath(remoteUrl, permanentFile.absolutePath)
        return permanentFile
    }

    /**
     * Saves a new locally-picked photo to permanent storage and records a
     * preliminary mapping keyed by [tempKey] (e.g. content URI string) so we
     * can resolve it immediately.  After upload, call [migratePath] to replace
     * the temp key with the real remote URL.
     *
     * @return The permanent [File] on disk.
     */
    suspend fun saveLocalPhoto(tempKey: String, file: File): File {
        val photosDir = File(context.filesDir, PHOTOS_DIR)
        photosDir.mkdirs()
        val fileName = "dl_local_${System.currentTimeMillis()}_${file.name}"
        val permanentFile = File(photosDir, fileName)
        file.copyTo(permanentFile, overwrite = true)
        addPath(tempKey, permanentFile.absolutePath)
        return permanentFile
    }

    /**
     * Re-keys an existing path entry from [oldKey] to [newKey] (real server URL).
     * Useful after upload: replace a temp-URI key with the actual remote URL.
     */
    suspend fun migratePath(oldKey: String, newKey: String) {
        context.dailyLogPhotoDataStore.edit { prefs ->
            val map = parsePaths(prefs[LOCAL_PATHS_KEY])
            val path = map.remove(oldKey) ?: return@edit
            map[newKey] = path
            prefs[LOCAL_PATHS_KEY] = gson.toJson(map)
        }
    }

    /** Removes paths whose local files no longer exist (garbage-collect orphans). */
    suspend fun cleanupOrphans() {
        context.dailyLogPhotoDataStore.edit { prefs ->
            val map = parsePaths(prefs[LOCAL_PATHS_KEY])
            val cleaned = map.filter { (_, path) -> File(path).exists() }
            prefs[LOCAL_PATHS_KEY] = gson.toJson(cleaned)
        }
    }

    /** Removes the mapping and optionally the local file for the given [remoteUrl]. */
    suspend fun removePath(remoteUrl: String, deleteFile: Boolean = false) {
        context.dailyLogPhotoDataStore.edit { prefs ->
            val map = parsePaths(prefs[LOCAL_PATHS_KEY])
            val path = map.remove(remoteUrl)
            if (deleteFile && path != null) File(path).delete()
            prefs[LOCAL_PATHS_KEY] = gson.toJson(map)
        }
    }

    // ─── private helpers ──────────────────────────────────────────────────────

    private suspend fun addPath(key: String, localPath: String) {
        context.dailyLogPhotoDataStore.edit { prefs ->
            val map = parsePaths(prefs[LOCAL_PATHS_KEY])
            map[key] = localPath
            prefs[LOCAL_PATHS_KEY] = gson.toJson(map)
        }
    }

    private fun parsePaths(json: String?): MutableMap<String, String> {
        if (json.isNullOrEmpty()) return mutableMapOf()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return (gson.fromJson<Map<String, String>>(json, type) ?: emptyMap()).toMutableMap()
    }
}
