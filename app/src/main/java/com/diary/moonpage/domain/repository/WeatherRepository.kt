package com.diary.moonpage.domain.repository

import java.time.LocalDate

interface WeatherRepository {
    suspend fun getWeatherConditions(lat: Double, lon: Double, date: LocalDate): Result<List<String>>
}
