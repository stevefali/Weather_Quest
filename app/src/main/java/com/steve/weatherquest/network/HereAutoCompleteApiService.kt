package com.steve.weatherquest.network

import com.steve.weatherquest.models.AutoCompleteResponseModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://autocomplete.search.hereapi.com/v1/"


// Moshi adapter
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Retrofit
private val retrofitHereAutoComplete = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface HereAutoCompleteApiService {
    @GET("autocomplete")
    // @GET("autocomplete?q=new&apiKey=0P2flTgAd4muE9l_3wZ4IKXeJdoC1uz9kHM1eP2QQEQ")
    suspend fun getHereAutoCompleteResponse(
        @Query("q") q: String,
        @Query("types") types: String,
        @Query("apiKey") apiKey: String
    ): AutoCompleteResponseModel

}


object HereAutoCompleteApi {
    val retrofitService: HereAutoCompleteApiService by lazy {
        retrofitHereAutoComplete.create(HereAutoCompleteApiService::class.java)
    }
}