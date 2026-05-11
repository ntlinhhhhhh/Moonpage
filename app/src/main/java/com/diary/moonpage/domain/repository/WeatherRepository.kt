package com.diary.moonpage.domain.repository

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherData>
}

data class WeatherData(
    val condition: String,
    val description: String,
    val temp: Double,
    val cityName: String,
    val iconUrl: String
)
