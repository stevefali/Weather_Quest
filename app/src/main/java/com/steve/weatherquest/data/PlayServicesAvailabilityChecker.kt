package com.steve.weatherquest.data

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class PlayServicesAvailabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleApiAvailability: GoogleApiAvailability
) {
    suspend fun isGooglePlayServicesAvailable(): Boolean = withContext(Dispatchers.Default) {
        when (googleApiAvailability.isGooglePlayServicesAvailable(context)) {
            ConnectionResult.SUCCESS -> true
            else -> false
        }
    }
}