package com.steve.weatherquest.models

// For returning displayable weather data to the viewModel when units(metric/imperial) are updated
data class AllWeathersDisplayable(
    val current: ForecastPeriodModel?,
    val forecastWholeDays: List<ForecastWholeDayModel>?
)
