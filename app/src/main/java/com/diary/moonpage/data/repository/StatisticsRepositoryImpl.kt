package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.StatisticsApi
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.repository.StatisticsRepository
import retrofit2.Response
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsApi: StatisticsApi
) : StatisticsRepository {
    override suspend fun getStatisticsSummary(year: Int, month: Int): Response<StatisticsResponse> {
        return statisticsApi.getStatisticsSummary(year, month)
    }
}
