package com.steve.weatherquest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.steve.weatherquest.models.OpWeMaGeocodeResponseModel
import javax.inject.Singleton

@Singleton
@Database(
    entities = [ForecastDatabaseEntity::class, CityDatabaseEntity::class, CurrentDatabaseEntity::class, OpWeMaGeocodeResponseModel::class],
    version = 3
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDatabaseDao(): WeatherDatabaseDao
}