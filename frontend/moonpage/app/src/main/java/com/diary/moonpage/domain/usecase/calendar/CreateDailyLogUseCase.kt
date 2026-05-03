package com.diary.moonpage.domain.usecase.calendar

import com.diary.moonpage.domain.repository.DailyLogRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateDailyLogUseCase @Inject constructor(
    private val repository: DailyLogRepository
) {
    suspend operator fun invoke(
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
        return repository.createDailyLog(
            dateStr, baseMoodId, date, note, sleepHours, isMenstruation, menstruationPhase, activityIds, dailyPhotos
        )
    }
}
