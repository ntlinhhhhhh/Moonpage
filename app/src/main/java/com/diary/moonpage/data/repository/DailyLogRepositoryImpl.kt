package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.DailyLogApi
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.data.local.dao.DailyLogDao
import com.diary.moonpage.data.local.entity.DailyLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class DailyLogRepositoryImpl @Inject constructor(
    private val api: DailyLogApi,
    private val dao: DailyLogDao
) : DailyLogRepository {

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
        musicRecord: String?,
        calories: Int?,
        distance: Double?
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
            val musicRecordBody = musicRecord?.toRequestBody("text/plain".toMediaTypeOrNull())
            val caloriesBody = calories?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val distanceBody = distance?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val activityParts = activityIds?.map { id ->
                MultipartBody.Part.createFormData("ActivityIds", id)
            }

            val photoParts = dailyPhotos?.map { file ->
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("DailyPhotos", file.name, requestFile)
            }

            val response = api.createDailyLog(
                baseMoodIdBody, dateBody, noteBody, sleepHoursBody, sleepStartTimeBody, isMenstruationBody, menstruationPhaseBody, stepsBody, musicRecordBody, activityParts, photoParts,
                caloriesBody, distanceBody
            )
            
            if (response.isSuccessful) {
                // Refresh local cache for this date
                val getResponse = api.getDailyLogByDate(date)
                if (getResponse.isSuccessful && getResponse.body() != null) {
                    dao.insertLog(DailyLogEntity.fromResponse(getResponse.body()!!))
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create DailyLog: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyLogByDate(date: String): Result<DailyLog> {
        return try {
            val cached = dao.getLogByDate(date)
            
            val response = api.getDailyLogByDate(date)
            if (response.isSuccessful && response.body() != null) {
                val logDto = response.body()!!
                dao.insertLog(DailyLogEntity.fromResponse(logDto))
                Result.success(logDto.toDomain())
            } else {
                cached?.let { Result.success(it.toDomain()) } 
                    ?: Result.failure(Exception("Failed to get DailyLog for date $date: ${response.code()}"))
            }
        } catch (e: Exception) {
            val cached = dao.getLogByDate(date)
            cached?.let { Result.success(it.toDomain()) } ?: Result.failure(e)
        }
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

    override fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLog>> = flow {
        val cachedLogs = dao.getLogsByMonth(yearMonth).map { it.toDomain() }
        emit(cachedLogs)
        
        try {
            val response = api.getDailyLogsByMonth(yearMonth)
            if (response.isSuccessful && response.body() != null) {
                val networkLogs = response.body()!!
                dao.deleteLogsByMonth(yearMonth)
                dao.insertLogs(networkLogs.map { DailyLogEntity.fromResponse(it) })
                emit(networkLogs.map { it.toDomain() })
            }
        } catch (e: Exception) {
            // Error handled by the flow consumer (if needed)
        }
    }.flowOn(Dispatchers.IO)
}
