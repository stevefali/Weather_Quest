package com.steve.weatherquest.repository

import android.util.Log
import com.steve.weatherquest.models.OpWeMaGeocodeResponseModel
import com.steve.weatherquest.network.OpWeMaGeocodeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

private const val APIKEY = "Private"

@Singleton
class OpWeMaGeocodeRepository {

    private val TAG = "OpWeMaGeocodeRepository"


    // The location geocoded by the OpenWeatherMap server
    private var _geocodedLocation: OpWeMaGeocodeResponseModel? = null

    /**
     * Since this is only called after the user selects a location from the search bar suggestions,
     * we can immediately call for weather data for that location.
     */
    suspend fun callOpWeMaGeocode(
        qry: String,
        onCallFinished: () -> Unit,
        setGeoLocation: (OpWeMaGeocodeResponseModel) -> Unit,
        onError: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                _geocodedLocation = OpWeMaGeocodeApi.retrofitService.getOpWeMaGeocodeResponse(
                    q = qry,
                    appid = APIKEY
                )[0]
            } catch (e: Exception) {
                Log.d(TAG, "Error in geocode network call!")
                e.printStackTrace()
                onError() // Trigger snackbar
            }
            if (_geocodedLocation != null) {
                // Pass the location up to the viewModel
                setGeoLocation(_geocodedLocation!!)
                // Now trigger the weather update via the viewModel
                onCallFinished()
            }
        }
    }
}