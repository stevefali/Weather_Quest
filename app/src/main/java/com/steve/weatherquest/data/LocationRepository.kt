package com.steve.weatherquest.data


import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationRepository @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()


    // Value to track if location is loading if it's going to take a long time
    private var _isLocationLoading = MutableStateFlow(false)
    var isLocationLoading = _isLocationLoading.asStateFlow()


    @SuppressLint("MissingPermission") // This will only be called when there is permission
    fun provideTheLocation(onGpsSuccessResult: () -> Unit) {
        // First, try getting the lastLocation
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { lastLocation: Location? ->
                if (lastLocation != null) {
                    _currentLocation.value = lastLocation
                    onGpsSuccessResult()
                } else { // The lastLocation is null, so request a fresh one
                    // Set the loading state so the loading animation can be shown
                    _isLocationLoading.value = true
                    // Request current location and assign it to '_currentLocation'
                    fusedLocationProviderClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY, null
                    ).addOnSuccessListener { location: Location? ->
                        _currentLocation.value = location
                        onGpsSuccessResult() // Trigger viewModel to set location to device location
                        _isLocationLoading.value = false
                    }
                }
            }

    }


}