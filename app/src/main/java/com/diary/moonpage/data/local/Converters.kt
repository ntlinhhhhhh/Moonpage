package com.diary.moonpage.data.local

import androidx.room.TypeConverter
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromStatisticsResponse(value: StatisticsResponse): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStatisticsResponse(value: String): StatisticsResponse {
        return Gson().fromJson(value, StatisticsResponse::class.java)
    }
}
