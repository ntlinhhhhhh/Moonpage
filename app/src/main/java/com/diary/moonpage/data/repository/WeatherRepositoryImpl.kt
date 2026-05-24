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

    private var cachedWeather: WeatherResult? = null
    private var cachedWeatherDate: LocalDate? = null

    override fun getCachedWeather(date: LocalDate): WeatherResult? {
        return if (cachedWeatherDate == date) cachedWeather else null
    }

    override fun setCachedWeather(date: LocalDate, result: WeatherResult) {
        cachedWeather = result
        cachedWeatherDate = date
    }

    override suspend fun getWeatherConditions(lat: Double, lon: Double, date: LocalDate): Result<WeatherResult> {
        return try {
            val dateStr = date.toString()
            val today = LocalDate.now()

            val response = if (date.isBefore(today)) {
                api.getArchive(
                    latitude = lat,
                    longitude = lon,
                    startDate = dateStr,
                    endDate = dateStr
                )
            } else {
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
                var temp: Double

                if (date == today && body.currentWeather != null) {
                    val current = body.currentWeather!!
                    temp = current.temperature
                    conditions.add(mapWeatherCodeToName(current.weathercode))
                    if (current.temperature > 30) conditions.add("Hot")
                    if (current.temperature < 15) conditions.add("Cold")
                    if (current.windspeed > 20) conditions.add("Windy")
                } else if (body.daily != null && !body.daily!!.time.isNullOrEmpty()) {
                    val daily = body.daily!!
                    
                    val maxTemp = daily.temperatureMax?.firstOrNull() ?: 0.0
                    val minTemp = daily.temperatureMin?.firstOrNull() ?: maxTemp
                    temp = (maxTemp + minTemp) / 2.0
                    
                    val code = daily.weathercode?.firstOrNull() ?: 0
                    conditions.add(mapWeatherCodeToName(code))
                    
                    if (maxTemp > 30) conditions.add("Hot")
                    if (minTemp < 15) conditions.add("Cold")
                    
                    val wind = daily.windspeedMax?.firstOrNull() ?: 0.0
                    if (wind > 20) conditions.add("Windy")
                } else {
                    return Result.failure(Exception("No weather data available in response"))
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
            45, 48 -> "Cloudy"
            51, 53, 55, 61, 63, 65 -> "Rainy"
            80, 81, 82 -> "Rainy"
            71, 73, 75, 77, 85, 86 -> "Snowy"
            95 -> "Stormy"
            96, 99 -> "Stormy"
            else -> if (code < 40) "Sunny" else "Cloudy"
        }
    }
}
