package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.DailyLogApi
import com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.data.local.dao.DailyLogDao
import com.diary.moonpage.data.local.entity.DailyLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class DailyLogRepositoryImpl @Inject constructor(
    private val api: DailyLogApi,
    private val dao: DailyLogDao
) : DailyLogRepository {

    override suspend fun createDailyLog(
        dateStr: String,
        baseMoodId: RequestBody,
        date: RequestBody,
        note: RequestBody?,
        sleepHours: RequestBody?,
        isMenstruation: RequestBody?,
        menstruationPhase: RequestBody?,
        activityIds: List<MultipartBody.Part>?,
        dailyPhotos: List<MultipartBody.Part>?
    ): Result<Unit> {
        return try {
            val response = api.createDailyLog(
                baseMoodId, date, note, sleepHours, isMenstruation, menstruationPhase, activityIds, dailyPhotos
            )
            if (response.isSuccessful) {
                val getResponse = api.getDailyLogByDate(dateStr)
                if (getResponse.isSuccessful && getResponse.body() != null) {
                    val log = getResponse.body()!!
                    dao.insertLog(DailyLogEntity.fromResponse(log))
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create DailyLog: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyLogByDate(date: String): Result<DailyLogResponseDto> {
        return try {
            val cached = dao.getLogByDate(date)
            
            val response = api.getDailyLogByDate(date)
            if (response.isSuccessful && response.body() != null) {
                val log = response.body()!!
                dao.insertLog(DailyLogEntity.fromResponse(log))
                Result.success(log)
            } else {
                cached?.let { Result.success(it.toResponse()) } 
                    ?: Result.failure(Exception("Failed to get DailyLog for date $date: ${response.code()}"))
            }
        } catch (e: Exception) {
            val cached = dao.getLogByDate(date)
            cached?.let { Result.success(it.toResponse()) } ?: Result.failure(e)
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

    override fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLogResponseDto>> = flow {
        val cachedLogs = dao.getLogsByMonth(yearMonth).map { it.toResponse() }
        emit(cachedLogs)
        
        try {
            val response = api.getDailyLogsByMonth(yearMonth)
            if (response.isSuccessful && response.body() != null) {
                val networkLogs = response.body()!!
                dao.deleteLogsByMonth(yearMonth)
                dao.insertLogs(networkLogs.map { DailyLogEntity.fromResponse(it) })
                emit(networkLogs)
            }
        } catch (e: Exception) {
        }
    }.flowOn(Dispatchers.IO)
}
