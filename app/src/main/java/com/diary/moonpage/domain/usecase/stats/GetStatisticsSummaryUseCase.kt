package com.diary.moonpage.domain.usecase.stats

import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.repository.StatisticsRepository
import retrofit2.Response
import javax.inject.Inject

class GetStatisticsSummaryUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    suspend operator fun invoke(year: Int, month: Int, isMonthly: Boolean): Response<StatisticsResponse> {
        return repository.getStatisticsSummary(year, month, isMonthly)
    }
}
