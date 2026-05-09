package com.diary.moonpage.domain.usecase.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyLogsByMonthUseCase @Inject constructor(
    private val repository: DailyLogRepository
) {
    operator fun invoke(yearMonth: String): Flow<List<DailyLog>> {
        return repository.getDailyLogsByMonth(yearMonth)
    }
}
