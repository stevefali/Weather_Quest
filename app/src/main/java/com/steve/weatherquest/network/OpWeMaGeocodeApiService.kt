package com.steve.weatherquest.network

import com.steve.weatherquest.models.OpWeMaGeocodeResponseModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "https://api.openweathermap.org/geo/1.0/"

// Moshi adapter
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Retrofit
private val retrofitOpWeMaGeocode = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface OpWeMaGeocodeApiService {
    @GET("direct")
    suspend fun getOpWeMaGeocodeResponse(
        @Query("q") q: String,
        @Query("appid") appid: String
    ): List<OpWeMaGeocodeResponseModel>
}


object OpWeMaGeocodeApi {
    val retrofitService: OpWeMaGeocodeApiService by lazy {
        retrofitOpWeMaGeocode.create(OpWeMaGeocodeApiService::class.java)
    }
}