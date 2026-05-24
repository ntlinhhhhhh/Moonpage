package com.diary.moonpage.data.repository

import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.data.local.dao.StatisticsDao
import com.diary.moonpage.data.local.entity.StatisticsEntity
import com.diary.moonpage.data.remote.api.StatisticsApi
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsApi: StatisticsApi,
    private val statisticsDao: StatisticsDao,
    private val tokenManager: TokenManager
) : StatisticsRepository {
    private val _refreshTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
    override val refreshTrigger = _refreshTrigger.asSharedFlow()

    override fun triggerRefresh() {
        kotlinx.coroutines.MainScope().launch {
            clearCache()
            _refreshTrigger.emit(Unit)
        }
    }
    override suspend fun getStatisticsSummary(year: Int, month: Int?, isMonthly: Boolean): Response<StatisticsResponse> {
        val userId = tokenManager.getUserId() ?: "unknown"
        val m = month ?: 0

        return try {
            val response = statisticsApi.getStatisticsSummary(year, month, isMonthly)
            if (response.isSuccessful && response.body() != null) {
                statisticsDao.insertStatistics(
                    StatisticsEntity(userId, year, m, isMonthly, response.body()!!)
                )
                response
            } else {
                val cached = statisticsDao.getStatistics(userId, year, m, isMonthly)
                cached?.let { Response.success(it.response) } ?: response
            }
        } catch (e: Exception) {
            val cached = statisticsDao.getStatistics(userId, year, m, isMonthly)
            cached?.let { Response.success(it.response) } ?: throw e
        }
    }

    override suspend fun getGlobalSummary(): Response<StatisticsResponse> {
        return try {
            statisticsApi.getGlobalSummary()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun clearCache() {
        statisticsDao.clearAllStatistics()
    }
}
