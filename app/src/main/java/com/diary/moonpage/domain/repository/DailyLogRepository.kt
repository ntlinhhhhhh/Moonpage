package com.diary.moonpage.domain.repository

import com.diary.moonpage.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DailyLogRepository {
    suspend fun createDailyLog(
        baseMoodId: Int,
        date: String,
        note: String?,
        sleepHours: Double?,
        isMenstruation: Boolean,
        menstruationPhase: String?,
        activityIds: List<String>?,
        dailyPhotos: List<File>?
    ): Result<Unit>

    suspend fun getDailyLogByDate(date: String): Result<DailyLog>
    
    suspend fun deleteDailyLog(date: String): Result<Unit>
    
    fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLog>>
}
