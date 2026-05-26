package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.DailyLogApi
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.data.local.dao.DailyLogDao
import com.diary.moonpage.data.local.entity.DailyLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyLogRepositoryImpl @Inject constructor(
    private val api: DailyLogApi,
    private val dao: DailyLogDao
) : DailyLogRepository {

    override fun getAllDailyLogsFlow(): Flow<List<DailyLog>> {
        return dao.getAllLogsFlow().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createDailyLog(
        baseMoodId: Int,
        date: String,
        note: String?,
        sleepHours: Double?,
        sleepStartTime: String?,
        isMenstruation: Boolean,
        menstruationPhase: String?,
        activityIds: List<String>?,
        dailyPhotos: List<File>?,
        steps: Int?,
        musicTitle: String?,
        artistName: String?,
        albumArtUrl: String?,
        calories: Int?,
        distance: Double?,
        wakeupTime: String?,
        weather: String?,
        temperature: Double?
    ): Result<Unit> {
        return try {
            val baseMoodIdBody = baseMoodId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val dateBody = date.toRequestBody("text/plain".toMediaTypeOrNull())
            val noteBody = note?.toRequestBody("text/plain".toMediaTypeOrNull())
            val sleepHoursBody = sleepHours?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val sleepStartTimeBody = sleepStartTime?.toRequestBody("text/plain".toMediaTypeOrNull())
            val isMenstruationBody = isMenstruation.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val menstruationPhaseBody = menstruationPhase?.toRequestBody("text/plain".toMediaTypeOrNull())
            val stepsBody = steps?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val musicTitleBody = musicTitle?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
            val artistNameBody = artistName?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
            val albumArtUrlBody = albumArtUrl?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
            val caloriesBody = calories?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val distanceBody = distance?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val wakeupTimeBody = wakeupTime?.toRequestBody("text/plain".toMediaTypeOrNull())
            val weatherBody = weather?.toRequestBody("text/plain".toMediaTypeOrNull())
            val temperatureBody = temperature?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val activityParts = activityIds?.map { id ->
                MultipartBody.Part.createFormData("ActivityIds", id)
            }

            val photoParts = dailyPhotos?.map { file ->
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("DailyPhotos", file.name, requestFile)
            }

            val response = api.createDailyLog(
                baseMoodIdBody, dateBody, noteBody, sleepHoursBody, sleepStartTimeBody, 
                isMenstruationBody, menstruationPhaseBody, stepsBody, musicTitleBody, artistNameBody, albumArtUrlBody,
                caloriesBody, distanceBody, wakeupTimeBody, weatherBody, temperatureBody,
                photoParts, activityParts
            )
            
            if (response.isSuccessful) {
                // Background fetch to ensure local cache is exactly same as server
                getDailyLogByDate(date)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create DailyLog: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyLogByDate(date: String): Result<DailyLog> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        return@withContext try {
            val cached = dao.getLogByDate(date)
            
            // Fetch network in background for single source of truth
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = api.getDailyLogByDate(date)
                    if (response.isSuccessful && response.body() != null) {
                        val logDto = response.body()!!
                        dao.insertLog(DailyLogEntity.fromResponse(logDto))
                    }
                } catch (e: Exception) {
                    // Ignore background error
                }
            }

            if (cached != null) {
                Result.success(cached.toDomain())
            } else {
                val response = api.getDailyLogByDate(date)
                if (response.isSuccessful && response.body() != null) {
                    val logDto = response.body()!!
                    dao.insertLog(DailyLogEntity.fromResponse(logDto))
                    Result.success(logDto.toDomain())
                } else {
                    Result.failure(Exception("DailyLog not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDailyLogByDateFlow(date: String): Flow<DailyLog?> {
        // trigger background sync for this specific date
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getDailyLogByDate(date)
                if (response.isSuccessful && response.body() != null) {
                    dao.insertLog(DailyLogEntity.fromResponse(response.body()!!))
                }
            } catch (e: Exception) {}
        }
        return dao.getLogByDateFlow(date).map { it?.toDomain() }.flowOn(Dispatchers.IO)
    }

    override suspend fun deleteDailyLog(date: String): Result<Unit> {
        return try {
            val response = api.deleteDailyLog(date)
            if (response.isSuccessful) {
                dao.deleteLogByDate(date)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete DailyLog: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLog>> {
        // Trigger background sync
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getDailyLogsByMonth(yearMonth)
                if (response.isSuccessful && response.body() != null) {
                    val networkLogs = response.body()!!
                    dao.insertLogs(networkLogs.map { DailyLogEntity.fromResponse(it) })
                }
            } catch (e: Exception) {}
        }

        return dao.getLogsByMonthFlow(yearMonth).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun clearCache() {
        dao.clearAllDailyLogs()
    }
}
