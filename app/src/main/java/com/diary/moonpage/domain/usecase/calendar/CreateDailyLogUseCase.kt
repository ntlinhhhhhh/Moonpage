package com.diary.moonpage.domain.usecase.calendar

import com.diary.moonpage.domain.repository.DailyLogRepository
import java.io.File
import javax.inject.Inject

class CreateDailyLogUseCase @Inject constructor(
    private val repository: DailyLogRepository
) {
    suspend operator fun invoke(
        baseMoodId: Int,
        date: String,
        note: String?,
        sleepHours: Double?,
        isMenstruation: Boolean,
        menstruationPhase: String?,
        activityIds: List<String>?,
        dailyPhotos: List<File>?
    ): Result<Unit> {
        return repository.createDailyLog(
            baseMoodId, date, note, sleepHours, isMenstruation, menstruationPhase, activityIds, dailyPhotos
        )
    }
}
