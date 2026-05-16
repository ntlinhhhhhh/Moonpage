package com.diary.moonpage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diary.moonpage.data.local.entity.StatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics WHERE userId = :userId AND year = :year AND month = :month AND isMonthly = :isMonthly")
    suspend fun getStatistics(userId: String, year: Int, month: Int, isMonthly: Boolean): StatisticsEntity?

    @Query("SELECT * FROM statistics WHERE userId = :userId AND year = :year AND month = :month AND isMonthly = :isMonthly")
    fun getStatisticsFlow(userId: String, year: Int, month: Int, isMonthly: Boolean): Flow<StatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: StatisticsEntity)

    @Query("DELETE FROM statistics WHERE userId = :userId AND year = :year AND month = :month AND isMonthly = :isMonthly")
    suspend fun deleteStatistics(userId: String, year: Int, month: Int, isMonthly: Boolean)

    @Query("DELETE FROM statistics")
    suspend fun clearAllStatistics()
}
