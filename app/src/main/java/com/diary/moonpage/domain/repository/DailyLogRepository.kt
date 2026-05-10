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
        dailyPhotos: List<File>?,
        songTitle: String? = null,
        artistName: String? = null,
        albumArtUrl: String? = null,
        steps: Int? = null,
        calories: Int? = null,
        distance: Double? = null
    ): Result<Unit>

    suspend fun getDailyLogByDate(date: String): Result<DailyLog>
    
    suspend fun deleteDailyLog(date: String): Result<Unit>
    
    fun getDailyLogsByMonth(yearMonth: String): Flow<List<DailyLog>>
}
