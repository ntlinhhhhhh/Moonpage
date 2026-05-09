package com.diary.moonpage.domain.usecase.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import javax.inject.Inject

class GetDailyLogByDateUseCase @Inject constructor(
    private val repository: DailyLogRepository
) {
    suspend operator fun invoke(date: String): Result<DailyLog> {
        return repository.getDailyLogByDate(date)
    }
}
