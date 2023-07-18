package com.steve.weatherquest.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


// This could also be an array of multiple permissions (eg. coarse location and fine location)
private val locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

internal fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

internal fun Activity.shouldShowRationaleFor(permission: String): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

/**
 * State holder class for holding location permissions as state so they can trigger recomposition.
 * This will be implemented in the MainActivity and passed down as hoisted state via
 * the setContent() method.
 */
class LocationPermissionState(
    private val activity: ComponentActivity,
    private val onResult: (LocationPermissionState) -> Unit
) {

    // Whether permission was granted
    var accessLocationGranted by mutableStateOf(false)
        private set

    // Whether to show a rationale for permission to access location
    var accessLocationNeedsRationale by mutableStateOf(false)
        private set

    // Whether to show a degraded experience (after permission denied)
    var showDegradedExperience by mutableStateOf(false)
        private set


    private val permissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            updateState()
            showDegradedExperience = !hasPermission()
            onResult(this)
        }

    init {
        updateState()
    }


    private fun updateState() {
        accessLocationGranted = activity.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        accessLocationNeedsRationale =
            activity.shouldShowRationaleFor(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Launch the permission request
     */
    fun requestPermission() {
        permissionLauncher.launch(locationPermission)
    }

    fun hasPermission(): Boolean = accessLocationGranted

    fun shouldShowRationale(): Boolean = !hasPermission() && (
            accessLocationNeedsRationale)
}