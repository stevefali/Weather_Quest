package com.steve.weatherquest.data

import androidx.room.*
import com.steve.weatherquest.models.OpWeMaGeocodeResponseModel

@Dao
interface WeatherDatabaseDao {

    /**
     * Forecast
     */
    // Get the forecast data
    @Query("SELECT * FROM forecast_database_entity ORDER BY dt ASC")
    suspend fun getForecastDatabaseData(): List<ForecastDatabaseEntity>

    // Get the soonest date from the forecast list
    @Query("SELECT dt FROM forecast_database_entity ORDER BY dt ASC LIMIT 1")
    suspend fun getSoonestForecastDate(): Long?

    // Delete the existing forecast data
    @Query("DELETE FROM forecast_database_entity")
    suspend fun clearForecastDatabase()

    // Insert the new forecast data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecastItem: ForecastDatabaseEntity)


    /**
     * City
     */
    // Get the City data
    @Query("SELECT * FROM city_database_entity")
    suspend fun getCityDatabaseData(): CityDatabaseEntity?

    // Delete the existing city data
    @Query("DELETE FROM city_database_entity")
    suspend fun clearCityDatabase()

    // Insert the new City data
    @Insert
    suspend fun insertCity(city: CityDatabaseEntity)


    /**
     * Current
     */
    // Get the current weather data
    @Query("SELECT * FROM current_database_entity")
    suspend fun getCurrentWeatherDatabaseData(): CurrentDatabaseEntity?

    // Get the date of the current weather in the database
    @Query("SELECT dt FROM current_database_entity")
    suspend fun getCurrentDatabaseDate(): Long?

    // Delete the existing data from the current weather database
    @Query("DELETE FROM current_database_entity")
    suspend fun clearCurrentWeatherDatabase()

    // Insert the new current weather data
    @Insert
    suspend fun insertCurrentWeather(currentWeather: CurrentDatabaseEntity)


    /**
     * Geocode
     */
    // Get the geocode data
    @Query("SELECT * FROM geocode_database_entity")
    fun getGeocodeDatabaseData(): OpWeMaGeocodeResponseModel

    // Insert the new geocode data, replacing the old data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDatabaseGeocode(geocodeData: OpWeMaGeocodeResponseModel)


}