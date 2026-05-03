package com.diary.moonpage.domain.repository

import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import retrofit2.Response

interface StatisticsRepository {
    suspend fun getStatisticsSummary(year: Int, month: Int): Response<StatisticsResponse>
}
