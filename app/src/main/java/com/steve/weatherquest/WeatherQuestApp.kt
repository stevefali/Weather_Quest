package com.steve.weatherquest

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.steve.weatherquest.data.WeatherDatabase
import com.steve.weatherquest.repository.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.steve.weatherquest.data.WeatherDatabaseDao
import com.steve.weatherquest.repository.AutoCompleteRepository
import com.steve.weatherquest.repository.OpWeMaGeocodeRepository
import com.steve.weatherquest.repository.OpenWeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class WeatherQuestApp : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        application: Application
    ) = LocationServices.getFusedLocationProviderClient(application)


    @Provides
    @Singleton
    fun provideGoogleApiAvailability() = GoogleApiAvailability.getInstance()



    @Provides
    @Singleton
    fun provideAutoCompleteRepository() = AutoCompleteRepository()


    @Provides
    @Singleton
    fun provideOpWeMaGeocodeRepository() = OpWeMaGeocodeRepository()


    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext appContext: Context):
            WeatherDatabase {
        return Room.databaseBuilder(
            appContext,
            WeatherDatabase::class.java,
            "weather_database"
        )
            //.fallbackToDestructiveMigration()
            .build()
    }


    @Provides
    fun provideWeatherDatabaseDao(weatherDatabase: WeatherDatabase): WeatherDatabaseDao {
        return weatherDatabase.weatherDatabaseDao()
    }

    @Provides
    @Singleton
    fun provideOpenWeatherRepository(weatherDatabaseDao: WeatherDatabaseDao) =
        OpenWeatherRepository(weatherDatabaseDao)

}

