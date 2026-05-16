package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.WeatherApi
import com.diary.moonpage.domain.repository.WeatherData
import com.diary.moonpage.domain.repository.WeatherRepository
import com.diary.moonpage.domain.repository.WeatherResult
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getWeatherConditions(lat: Double, lon: Double, date: LocalDate): Result<WeatherResult> {
        return try {
            val dateStr = date.toString()
            val today = LocalDate.now()

            val response = if (date.isBefore(today)) {
                // Use Archive API for past dates
                api.getArchive(
                    latitude = lat,
                    longitude = lon,
                    startDate = dateStr,
                    endDate = dateStr
                )
            } else {
                // Use Forecast API for today or future dates
                api.getForecast(
                    latitude = lat,
                    longitude = lon,
                    currentWeather = (date == today),
                    startDate = dateStr,
                    endDate = dateStr
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val conditions = mutableListOf<String>()
                var temp = 0.0

                if (date == today && body.currentWeather != null) {
                    // Use real-time current weather for today if available
                    val current = body.currentWeather!!
                    temp = current.temperature
                    conditions.add(mapWeatherCodeToName(current.weathercode))
                    if (current.temperature > 30) conditions.add("Hot")
                    if (current.temperature < 15) conditions.add("Cold")
                    if (current.windspeed > 20) conditions.add("Windy")
                } else if (body.daily != null && body.daily!!.time.isNotEmpty()) {
                    // Use daily summary
                    val daily = body.daily!!
                    temp = (daily.temperatureMax[0] + daily.temperatureMin[0]) / 2.0
                    conditions.add(mapWeatherCodeToName(daily.weathercode[0]))
                    if (daily.temperatureMax[0] > 30) conditions.add("Hot")
                    if (daily.temperatureMin[0] < 15) conditions.add("Cold")
                    if (daily.windspeedMax[0] > 20) conditions.add("Windy")
                }

                Result.success(WeatherResult(conditions.distinct(), temp))
            } else {
                Result.failure(Exception("Weather API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            val response = api.getForecast(lat, lon, true, LocalDate.now().toString(), LocalDate.now().toString())
            if (response.isSuccessful && response.body()?.currentWeather != null) {
                val current = response.body()!!.currentWeather!!
                Result.success(WeatherData(
                    condition = mapWeatherCodeToName(current.weathercode),
                    description = "Current Weather",
                    temp = current.temperature,
                    cityName = "Current Location",
                    iconUrl = ""
                ))
            } else {
                Result.failure(Exception("Failed to fetch current weather"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapWeatherCodeToName(code: Int): String {
        return when (code) {
            0 -> "Sunny"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Cloudy" // Fog
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Rainy"
            71, 73, 75, 77, 85, 86 -> "Snowy"
            95, 96, 99 -> "Stormy"
            else -> "Sunny"
        }
    }
}
