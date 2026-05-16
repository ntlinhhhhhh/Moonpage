package com.diary.moonpage.domain.repository

import java.time.LocalDate

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherData>
    suspend fun getWeatherConditions(lat: Double, lon: Double, date: java.time.LocalDate): Result<List<String>>
}

data class WeatherData(
    val condition: String,
    val description: String,
    val temp: Double,
    val cityName: String,
    val iconUrl: String
)
