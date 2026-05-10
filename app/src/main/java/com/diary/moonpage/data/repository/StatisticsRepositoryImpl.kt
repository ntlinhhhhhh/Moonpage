package com.diary.moonpage.data.repository

import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.data.local.dao.StatisticsDao
import com.diary.moonpage.data.local.entity.StatisticsEntity
import com.diary.moonpage.data.remote.api.StatisticsApi
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.repository.StatisticsRepository
import retrofit2.Response
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsApi: StatisticsApi,
    private val statisticsDao: StatisticsDao,
    private val tokenManager: TokenManager
) : StatisticsRepository {
    override suspend fun getStatisticsSummary(year: Int, month: Int, isMonthly: Boolean): Response<StatisticsResponse> {
        val userId = tokenManager.getUserId() ?: "unknown"
        
        // Try to get from cache first for speed
        val cached = statisticsDao.getStatistics(userId, year, month, isMonthly)
        if (cached != null) {
            return Response.success(cached.response)
        }

        return try {
            val response = statisticsApi.getStatisticsSummary(year, month)
            if (response.isSuccessful && response.body() != null) {
                statisticsDao.insertStatistics(
                    StatisticsEntity(userId, year, month, isMonthly, response.body()!!)
                )
            }
            response
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getGlobalSummary(): Response<StatisticsResponse> {
        return try {
            statisticsApi.getGlobalSummary()
        } catch (e: Exception) {
            throw e
        }
    }
}
