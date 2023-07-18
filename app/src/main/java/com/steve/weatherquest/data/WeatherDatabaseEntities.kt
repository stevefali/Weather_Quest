package com.steve.weatherquest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This represents the data of the [ForecastPeriod] object of the [OpenWeatherForecastModel] response
@Entity(tableName = "forecast_database_entity")
data class ForecastDatabaseEntity(
    @PrimaryKey val dt: Long,
    val temp: Double?,
    val feelsLike: Double?,
    val tempMin: Double?,
    val tempMax: Double?,
    val humidity: Double?,
    val id: Int,
    val main: String,
    val description: String,
    val clouds: Int?,
    val speed: Double?,
    val deg: Int?,
    val gust: Double?,
    val visibility: Int?,
    val pop: Double?,
    val rainOneHour: Double?,
    val rainThreeHours: Double?,
    val snowOneHour:Double?,
    val snowThreeHours: Double?,
    val partOfDay: String?
)

// This represents the data of the [City] object of the [OpenWeatherForecastModel] response
@Entity(tableName = "city_database_entity")
data class CityDatabaseEntity(
    @PrimaryKey
    val name: String,
    val timezone: Int?,
    val sunrise: Long?,
    val sunset: Long?,

)


// This represents the data of the [OpenWeatherCurrentModel] response
@Entity(tableName = "current_database_entity")
data class CurrentDatabaseEntity(
    @PrimaryKey
    val dt: Long,
    val id: Int?,
    val main: String?,
    val description: String?,
    val temp: Double?,
    val feelsLike: Double?,
    val pressure: Double?,
    val humidity: Double?,
    val tempMin: Double?,
    val tempMax: Double?,
    val visibility: Int?,
    val speed: Double?,
    val deg: Int?,
    val gust: Double?,
    val clouds: Int?,
    val rainOneHour: Double?,
    val rainThreeHours: Double?,
    val snowOneHour:Double?,
    val snowThreeHours: Double?,
    val sunrise: Long?,
    val sunset: Long?,
    val timezone: Long?,
    val name: String?
)