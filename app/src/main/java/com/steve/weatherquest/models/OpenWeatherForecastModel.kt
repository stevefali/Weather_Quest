package com.steve.weatherquest.models

import com.squareup.moshi.Json


// Main response object list
data class OpenWeatherForecastModel(
    val  list: List<ForecastPeriod>?,
    val city: City?
)

data class ForecastPeriod(
    val dt: Long?,
    val main: MainWeather?,
    val weather: List<Weather>?,
    val clouds: Cloudiness?,
    val wind: Wind?,
    val visibility: Int?,
    val pop: Double?,
    val rain: Precipitation?,
    val snow: Precipitation?,
    val sys: PartOfDay?,
    @Json(name = "dt_txt") val dtTxt: String?



)

data class City(
    val name: String,
    val coord: Coordinates?,
    val country: String?,
    val timezone: Int?,
    val sunrise: Long?,
    val sunset: Long?
)

data class Coordinates(
    val lat: Double?,
    val lon: Double?
)


data class MainWeather(
    val temp: Double?,
    @Json(name = "feels_like") val feelsLike: Double?,
    @Json(name = "temp_min") val tempMin: Double?,
    @Json(name = "temp_max") val tempMax: Double?,
    val humidity: Double?
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String
)

data class Cloudiness(
    val all: Int?
)

data class Wind(
    val speed: Double?,
    val deg: Int?,
    val gust: Double?
)

data class Precipitation(
    @Json(name = "1h") val oneHour: Double?,
    @Json(name = "3h") val threeHours: Double?
)

data class PartOfDay(
    val pod: String?
)