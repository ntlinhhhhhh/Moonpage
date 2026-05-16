package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.weather.OpenMeteoResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("daily") daily: String? = "weathercode,temperature_2m_max,windspeed_10m_max",
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("timezone") timezone: String = "auto"
    ): Response<OpenMeteoResponseDto>

    @GET("https://archive-api.open-meteo.com/v1/archive")
    suspend fun getArchive(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("daily") daily: String = "weathercode,temperature_2m_max,windspeed_10m_max",
        @Query("timezone") timezone: String = "auto"
    ): Response<OpenMeteoResponseDto>
}
