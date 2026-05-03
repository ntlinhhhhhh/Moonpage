package com.diary.moonpage.domain.usecase.calendar

import com.diary.moonpage.domain.repository.DailyLogRepository
import javax.inject.Inject

class DeleteDailyLogUseCase @Inject constructor(
    private val repository: DailyLogRepository
) {
    suspend operator fun invoke(date: String): Result<Unit> {
        return repository.deleteDailyLog(date)
    }
}
