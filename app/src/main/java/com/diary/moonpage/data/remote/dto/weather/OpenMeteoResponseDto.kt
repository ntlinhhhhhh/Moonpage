package com.diary.moonpage.data.remote.dto.weather

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponseDto(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeatherDto?,
    @SerializedName("daily")
    val daily: DailyWeatherDataDto?
)

data class CurrentWeatherDto(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)

data class DailyWeatherDataDto(
    val time: List<String>,
    @SerializedName("weathercode")
    val weathercode: List<Int>,
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerializedName("windspeed_10m_max")
    val windspeedMax: List<Double>
)
