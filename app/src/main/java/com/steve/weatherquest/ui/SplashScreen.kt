package com.steve.weatherquest.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.steve.weatherquest.R

private const val SplashWaitTime: Long = 1000

@Composable
fun WeatherSplashScreen(modifier: Modifier = Modifier, onTimeout: () -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {
        // This will always refer to the latest onTimeout function that
        // LandingScreen was recomposed with
        val currentOnTimeout by rememberUpdatedState(onTimeout)

        // Create an effect that matches the lifecycle of LandingScreen.
        // If LandingScreen recomposes or onTimeout changes, the delay shouldn't start again.
        LaunchedEffect(true) {
            delay(SplashWaitTime)
            currentOnTimeout()
        }
        Image(painterResource(id = R.drawable.mostly_clear_day), contentDescription = null,
            Modifier
                .size(200.dp)
                .align(Alignment.Center)
        )
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)
        ) {
            Text( text = stringResource(id = R.string.weather_icons),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding( end = 8.dp)
            )

            if (isSystemInDarkTheme()) {
                Image(
                    painter = painterResource(id = R.drawable.powered_by_tomorrow_black),
                    contentDescription = null
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.powered_by_tomorrow_white),
                    contentDescription = null
                )
            }
        }
    }
}