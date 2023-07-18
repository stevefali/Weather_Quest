package com.steve.weatherquest.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.steve.weatherquest.ui.theme.NewComposeWeatherTestsTheme

@Composable
fun InitializingScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Initializing",
            style = MaterialTheme.typography.headlineLarge
        )
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun InitializingScreenPreview() {
    NewComposeWeatherTestsTheme {
        InitializingScreen()
    }
}