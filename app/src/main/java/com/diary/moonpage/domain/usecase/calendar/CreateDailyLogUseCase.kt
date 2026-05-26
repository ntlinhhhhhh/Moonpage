package com.diary.moonpage.domain.usecase.calendar

import com.diary.moonpage.domain.model.DailyLog
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
        wakeupTime: String? = null,
        weather: String? = null,
        temperature: Double? = null
    ): Result<Unit> {
        return repository.createDailyLog(
            baseMoodId, date, note, sleepHours, sleepStartTime, 
            isMenstruation, menstruationPhase, activityIds, dailyPhotos, 
            steps, musicTitle, artistName, albumArtUrl, calories, distance, wakeupTime, weather, temperature
        )
    }
}
