package com.steve.weatherquest.models

import androidx.annotation.DrawableRes


data class ForecastDisplayableModel(
    val date: String,
    val time: String,
    val pod: String,
    val code: Int,
    @DrawableRes val iconResId: Int,
    val description: String,
    val temp: Double,
    val feelsLike: Double,
    val humidity: Double,
    val cloudiness: Int,
    val windSpeed: Double,
    val windGust: Double?,
    val rain: Precip?,
    val snow: Precip?,
    val visibility: Int,
    val pop: Int
//    val cityName: String,
//    val sunrise: Long,
//    val sunset: Long

)

data class Precip(
    val oneHour: Double?,
    val threeHour: Double?
)

data class ForecastWholeDayModel(
    val date: String,
    @DrawableRes val iconResId: Int,
    val dayDescription: String,
    val tempHiBig: String,
    val tempHiSmall: String,
    val tempLoBig: String,
    val tempLoSmall: String,
    val symbolBig: String,
    val symbolSmall: String,
    val maxWind: String,
    val maxGust: String?,
    val speedSymbol: String,
    val maxPop: Int
)

data class WeatherCity(
    val name: String,
    val timezone: Int?,
    val sunrise: Long,
    val sunset: Long
)

data class ForecastPeriodModel(
    val dayIndex: Int,
    val time: String,
    val date: String,
    @DrawableRes val iconResId: Int,
    @DrawableRes val background: Int?,
    val description: String,
    val tempBig: String,
    val tempSmall: String,
    val feelsLikeBig: String,
    val feelsLikeSmall: String,
    val humidity: String,
    val cloudiness: String,
    val windSpeed: String,
    val windGust: String?,
    val symbolBig: String,
    val symbolSmall: String,
    val speedSymbol: String,
    val distanceSymbol: String,
    val rain: String?,
    val snow: String?,
    val precipSymbol: String,
    val visibility: String,
    val pop: String,
    val name: String?
)