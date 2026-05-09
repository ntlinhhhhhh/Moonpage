package com.diary.moonpage.data.local.entity

import androidx.room.Entity
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse

@Entity(tableName = "statistics", primaryKeys = ["userId", "year", "month", "isMonthly"])
data class StatisticsEntity(
    val userId: String,
    val year: Int,
    val month: Int,
    val isMonthly: Boolean,
    val response: StatisticsResponse,
    val timestamp: Long = System.currentTimeMillis()
)
