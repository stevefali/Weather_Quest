package com.steve.weatherquest.ui

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.steve.weatherquest.R
import com.steve.weatherquest.ui.theme.NewComposeWeatherTestsTheme

/**
 * Screen to show location stuff for testing.
 * Clicking the button will first check for location permission, and request it if necessary,
 * before launching the code to get the location, which for now is simply displayed in a string
 * as longitude and latitude, or "null" if it is null.
 */
@Composable
fun LocationInfoScreen(
    showDegradedExperience: Boolean,
    needsPermissionRationale: Boolean,
    onButtonClick: () -> Unit,
    isLoading: Boolean,
    location: Location?
) {
    var showRationaleDialog by remember { mutableStateOf(false) }
    // Compose will trigger this for recomposition :)
    if (showRationaleDialog) {
        PermissionRationaleDialog(
            onConfirm = {
                showRationaleDialog = false
                onButtonClick()
            },
            onDismiss = { showRationaleDialog = false }
        )
    }


    // Customize the onClick behavior here so the hoisting function doesn't have to worry
    // about the RationaleDialog
    fun onClick() {
        if (needsPermissionRationale) {
            showRationaleDialog = true
        } else {
            onButtonClick()
        }
    }

    // Set the text based on whether permission has been granted and whether the location is null
    val locationMessage = when {
        // ShowDegradedExperience means that permission is not granted
        showDegradedExperience -> stringResource(id = R.string.no_permission)
        // If permission is granted, set the string to the location (or null)
        else -> if (location != null) {
            stringResource(
                id = R.string.location_lat_long,
                location.latitude,
                location.longitude
            )
        } else {
            stringResource(id = R.string.no_location)
        }
    }

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the location, or a loading indicator if it is taking a long time
        LocationMessageWithLoadingIndicator(
            locationMessage = locationMessage,
            isLoading = isLoading,
        )
        Button(onClick = { onClick() }) {
            Text(text = "Get Location")
        }
    }
}




@Composable
fun LocationMessageWithLoadingIndicator(
    locationMessage: String,
    isLoading: Boolean
) {
    Box(contentAlignment = Alignment.Center) {
        if (!isLoading) {
            Text(
                text = locationMessage,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Getting Location...",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                CircularProgressIndicator()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LocationInfoScreenPreview() {
    NewComposeWeatherTestsTheme {
        LocationInfoScreen(
            showDegradedExperience = false,
            needsPermissionRationale = false,
            onButtonClick = { },
            isLoading = false,
            location = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocationLoadingIndicatorPreview() {
    LocationMessageWithLoadingIndicator(locationMessage = "Location: ...", isLoading = true)
}

@Preview(showBackground = true)
@Composable
fun PermissionRationaleDialogPreview() {
    PermissionRationaleDialog(onConfirm = { }) {
    }
}