package com.diary.moonpage.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("https://api.openweathermap.org/data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    companion object {
        const val API_KEY = "895284fb2d2c1d87c12662f3a61d670a" // Placeholder, user should provide their own
    }
}

data class WeatherResponse(
    val weather: List<WeatherDescription>,
    val main: WeatherMain,
    val name: String
)

data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)

data class WeatherMain(
    val temp: Double,
    val humidity: Int
)
