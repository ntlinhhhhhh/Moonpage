package com.diary.moonpage.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.diary.moonpage.core.util.DailyLogPhotoManager
import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.domain.repository.StatisticsRepository
import com.diary.moonpage.domain.usecase.notification.CheckAndTriggerNotificationsUseCase
import com.diary.moonpage.widget.glance.MoonpageWidgets
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okio.buffer
import okio.sink
import java.io.File
import java.util.UUID

@HiltWorker
class DailyLogUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DailyLogRepository,
    private val photoManager: DailyLogPhotoManager,
    private val statisticsRepository: StatisticsRepository,
    private val checkAndTriggerNotificationsUseCase: CheckAndTriggerNotificationsUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val date = inputData.getString(KEY_DATE) ?: return Result.failure()
        val baseMoodId = inputData.getInt(KEY_BASE_MOOD_ID, 0)
        if (baseMoodId == 0) return Result.failure()

        val photoPaths = inputData.getStringArray(KEY_PHOTO_PATHS).orEmpty().toList()
        val photoKeys = inputData.getStringArray(KEY_PHOTO_KEYS).orEmpty().toList()
        val photoEntries = photoKeys.mapIndexedNotNull { index, key ->
            resolvePhotoFile(key, photoPaths.getOrNull(index))?.let { file -> key to file }
        }
        if (photoEntries.size != photoKeys.size) {
            android.util.Log.w("DailyLogUploadWorker", "Missing daily log photo files for $date")
            return Result.retry()
        }
        val photoFiles = photoEntries.map { it.second }

        val result = repository.createDailyLog(
            baseMoodId = baseMoodId,
            date = date,
            note = inputData.getString(KEY_NOTE),
            sleepHours = nullableDouble(KEY_SLEEP_HOURS),
            sleepStartTime = inputData.getString(KEY_SLEEP_START_TIME),
            isMenstruation = inputData.getBoolean(KEY_IS_MENSTRUATION, false),
            menstruationPhase = inputData.getString(KEY_MENSTRUATION_PHASE),
            activityIds = inputData.getStringArray(KEY_ACTIVITY_IDS)?.toList(),
            dailyPhotos = photoFiles.takeIf { it.isNotEmpty() },
            steps = nullableInt(KEY_STEPS),
            musicTitle = inputData.getString(KEY_MUSIC_TITLE),
            artistName = inputData.getString(KEY_ARTIST_NAME),
            albumArtUrl = inputData.getString(KEY_ALBUM_ART_URL),
            calories = nullableInt(KEY_CALORIES),
            distance = nullableDouble(KEY_DISTANCE),
            wakeupTime = inputData.getString(KEY_WAKEUP_TIME),
            weather = inputData.getString(KEY_WEATHER),
            temperature = nullableDouble(KEY_TEMPERATURE)
        )

        return result.fold(
            onSuccess = {
                updatePhotoCache(date, photoEntries)
                statisticsRepository.triggerRefresh()
                MoonpageWidgets.refreshAll(applicationContext)
                runCatching { checkAndTriggerNotificationsUseCase() }
                Result.success()
            },
            onFailure = { error ->
                android.util.Log.e("DailyLogUploadWorker", "Daily log upload failed", error)
                Result.retry()
            }
        )
    }

    private suspend fun resolvePhotoFile(photoKey: String, inputPath: String?): File? {
        inputPath
            ?.takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.takeIf { it.exists() }
            ?.let { return it }

        photoManager.getLocalPath(photoKey)
            ?.let(::File)
            ?.takeIf { it.exists() }
            ?.let { return it }

        return if (photoKey.startsWith("http", ignoreCase = true)) {
            downloadAndCacheRemotePhoto(photoKey)
        } else {
            null
        }
    }

    private suspend fun downloadAndCacheRemotePhoto(url: String): File? {
        val request = okhttp3.Request.Builder().url(url).build()
        return try {
            okhttp3.OkHttpClient().newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) return@use null

                val tempFile = File(applicationContext.cacheDir, "retained_photo_${UUID.randomUUID()}.jpg")
                tempFile.sink().buffer().use { sink ->
                    sink.writeAll(body.source())
                }
                val cachedFile = photoManager.savePhoto(url, tempFile)
                tempFile.delete()
                cachedFile
            }
        } catch (e: Exception) {
            android.util.Log.w("DailyLogUploadWorker", "Failed to cache retained photo: $url", e)
            null
        }
    }

    private suspend fun updatePhotoCache(date: String, photoEntries: List<Pair<String, File>>) {
        val uploadedPhotos = repository.getDailyLogByDate(date)
            .getOrNull()
            ?.dailyPhotos
            .orEmpty()
            .mapNotNull(::normalizeAppImageUrl)

        uploadedPhotos.zip(photoEntries).forEach { (remoteUrl, localPhoto) ->
            val (localKey, file) = localPhoto
            if (localKey != remoteUrl) {
                photoManager.migratePath(localKey, remoteUrl)
            }
            if (photoManager.getLocalPath(remoteUrl) == null && file.exists()) {
                photoManager.savePhoto(remoteUrl, file)
            }
        }
    }

    private fun nullableInt(key: String): Int? {
        return inputData.getInt(key, NULL_INT).takeUnless { it == NULL_INT }
    }

    private fun nullableDouble(key: String): Double? {
        return inputData.getDouble(key, Double.NaN).takeUnless { it.isNaN() }
    }

    companion object {
        const val UNIQUE_WORK_PREFIX = "daily_log_upload_"
        const val KEY_DATE = "date"
        const val KEY_BASE_MOOD_ID = "base_mood_id"
        const val KEY_NOTE = "note"
        const val KEY_SLEEP_HOURS = "sleep_hours"
        const val KEY_SLEEP_START_TIME = "sleep_start_time"
        const val KEY_IS_MENSTRUATION = "is_menstruation"
        const val KEY_MENSTRUATION_PHASE = "menstruation_phase"
        const val KEY_ACTIVITY_IDS = "activity_ids"
        const val KEY_PHOTO_KEYS = "photo_keys"
        const val KEY_PHOTO_PATHS = "photo_paths"
        const val KEY_STEPS = "steps"
        const val KEY_MUSIC_TITLE = "music_title"
        const val KEY_ARTIST_NAME = "artist_name"
        const val KEY_ALBUM_ART_URL = "album_art_url"
        const val KEY_CALORIES = "calories"
        const val KEY_DISTANCE = "distance"
        const val KEY_WAKEUP_TIME = "wakeup_time"
        const val KEY_WEATHER = "weather"
        const val KEY_TEMPERATURE = "temperature"

        private const val NULL_INT = Int.MIN_VALUE

        fun buildRequest(data: Data): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DailyLogUploadWorker>()
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        }
    }
}
