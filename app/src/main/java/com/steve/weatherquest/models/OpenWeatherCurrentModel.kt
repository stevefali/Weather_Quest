package com.steve.weatherquest.models

import com.squareup.moshi.Json


data class OpenWeatherCurrentModel(
    val weather: List<CurrentWeather?>?,
    val main: CurrentMainWeather?,
    val visibility: Int?,
    val wind: Wind?,
    val clouds: Cloudiness?,
    val rain: Precipitation?,
    val snow: Precipitation?,
    val dt: Long?,
    val sys: Sys?,
    val timezone: Long?,
    val name: String?

)

data class CurrentWeather(
    val id: Int?,
    val main: String?,
    val description: String?
)

data class CurrentMainWeather(
    val temp: Double?,
    @Json(name = "feels_like") val feelsLike: Double?,
    val pressure: Double?,
    val humidity: Double?,
    @Json(name = "temp_min") val tempMin: Double?,
    @Json(name = "temp_max") val tempMax: Double?,
)

data class Sys(
    val sunrise: Long?,
    val sunset: Long?
)