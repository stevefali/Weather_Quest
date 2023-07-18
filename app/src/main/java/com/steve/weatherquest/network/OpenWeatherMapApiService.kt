package com.steve.weatherquest.network


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.steve.weatherquest.models.OpenWeatherCurrentModel
import com.steve.weatherquest.models.OpenWeatherForecastModel
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

// Moshi adapter
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Retrofit
private val retrofitOpenWeatherMap = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface OpenWeatherMapApiService {

    // Current weather
    @GET("weather")
    suspend fun getCurrentWeatherResponse(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String,
        @Query("appid") appid: String
    ): OpenWeatherCurrentModel



    // 5-day forecast
    @GET("forecast")
    suspend fun getFiveDayForecastResponse(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String,
        @Query("appid") appid: String
    ): OpenWeatherForecastModel


}

object OpenWeatherMapApi {
    val retrofitService: OpenWeatherMapApiService by lazy {
        retrofitOpenWeatherMap.create(OpenWeatherMapApiService::class.java)
    }
}
