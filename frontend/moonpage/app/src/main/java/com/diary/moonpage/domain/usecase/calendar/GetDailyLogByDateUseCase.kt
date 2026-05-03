package com.diary.moonpage.domain.usecase.calendar

import com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto
import com.diary.moonpage.domain.repository.DailyLogRepository
import javax.inject.Inject

class GetDailyLogByDateUseCase @Inject constructor(
    private val repository: DailyLogRepository
) {
    suspend operator fun invoke(date: String): Result<DailyLogResponseDto> {
        return repository.getDailyLogByDate(date)
    }
}
