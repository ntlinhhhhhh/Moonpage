package com.diary.moonpage.data.local.dao

import androidx.room.*
import com.diary.moonpage.data.local.entity.DailyLogEntity

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE date LIKE :yearMonth || '%'")
    suspend fun getLogsByMonth(yearMonth: String): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: String): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<DailyLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE date = :date")
    suspend fun deleteLogByDate(date: String)

    @Query("DELETE FROM daily_logs WHERE date LIKE :yearMonth || '%'")
    suspend fun deleteLogsByMonth(yearMonth: String)
}
