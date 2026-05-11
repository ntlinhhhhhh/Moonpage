package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.WeatherApi
import com.diary.moonpage.domain.repository.WeatherData
import com.diary.moonpage.domain.repository.WeatherRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            val response = weatherApi.getCurrentWeather(lat, lon, WeatherApi.API_KEY)
            if (response.isSuccessful) {
                val body = response.body()!!
                val weather = body.weather.firstOrNull()
                Result.success(
                    WeatherData(
                        condition = weather?.main ?: "Unknown",
                        description = weather?.description ?: "",
                        temp = body.main.temp,
                        cityName = body.name,
                        iconUrl = "https://openweathermap.org/img/wn/${weather?.icon}@2x.png"
                    )
                )
            } else {
                Result.failure(Exception("Weather fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
