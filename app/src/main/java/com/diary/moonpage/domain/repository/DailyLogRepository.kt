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
        sleepStartTime: String? = null,
        isMenstruation: Boolean,
        menstruationPhase: String?,
        activityIds: List<String>?,
        dailyPhotos: List<File>?,
        steps: Int? = null,
        musicRecord: String? = null,
        calories: Int? = null,
        distance: Double? = null,
        wakeupTime: String? = null
    ): Result<Unit>

    suspend fun getDailyLogByDate(date: String): Result<DailyLog>
    
    fun getDailyLogByDateFlow(date: String): Flow<DailyLog?>
    
    suspend fun deleteDailyLog(date: String): Result<Unit>
    
    fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLog>>

    suspend fun clearCache()
}
