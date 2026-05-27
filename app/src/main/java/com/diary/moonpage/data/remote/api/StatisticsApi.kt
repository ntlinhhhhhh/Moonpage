package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StatisticsApi {
    @GET("api/statistics/summary")
    suspend fun getStatisticsSummary(
        @Query("year") year: Int,
        @Query("month") month: Int?
    ): Response<StatisticsResponse>

    @GET("api/statistics/summary")
    suspend fun getGlobalSummary(): Response<StatisticsResponse>
}
