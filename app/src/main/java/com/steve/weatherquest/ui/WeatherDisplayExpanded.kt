package com.steve.weatherquest.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.steve.weatherquest.R
import com.steve.weatherquest.models.ForecastPeriodModel
import com.steve.weatherquest.models.ForecastWholeDayModel
import com.steve.weatherquest.util.capitalizeEachWord


@Composable
fun CurrentWeatherDisplayExpanded(
    currentWeather: ForecastPeriodModel,
    switchUnits: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Column {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(id = R.string.current_weather),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            Column(Modifier.padding(4.dp)) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentWeather.description.capitalizeEachWord(),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth(3/5f),
                        minLines = 1,
                        maxLines = 3,
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(id = R.string.as_of))
                        Text(
                            text = currentWeather.time,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    TemperatureDisplay(
                        tempHiBig = currentWeather.tempBig,
                        tempLoBig = currentWeather.feelsLikeBig,
                        tempHiSmall = currentWeather.tempSmall,
                        tempLoSmall = currentWeather.feelsLikeSmall,
                        symbolBig = currentWeather.symbolBig,
                        symbolSmall = currentWeather.symbolSmall,
                        feelsLike = true,
                        onClick = { switchUnits() },
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = currentWeather.iconResId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                        )
                    }

                    Column {
                        ParameterLabelRow(
                            label = stringResource(id = R.string.wind_plain),
                            param = currentWeather.windSpeed,
                            unit = currentWeather.speedSymbol,
                            onUnitsClick = { switchUnits() }
                        )
                        ParameterLabelRowOrNull(
                            label = stringResource(id = R.string.wind_gust_plain),
                            param = currentWeather.windGust,
                            unit = currentWeather.speedSymbol,
                            onUnitsClick = { switchUnits() }
                        )
                        ParameterLabelRow(
                            label = stringResource(id = R.string.visibility),
                            param = currentWeather.visibility,
                            unit = currentWeather.distanceSymbol,
                            onUnitsClick = { switchUnits() }
                        )
                        ParameterLabelRow(
                            label = stringResource(id = R.string.humidity),
                            param = currentWeather.humidity,
                            unit = "%",
                            onUnitsClick = {} // do nothing
                        )

                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {

                    ParameterLabelRowOrNull(label = stringResource(id = R.string.rain),
                        param = currentWeather.rain,
                        unit = currentWeather.precipSymbol,
                        onUnitsClick = { switchUnits() }
                    )
                    ParameterLabelRowOrNull(label = stringResource(id = R.string.snow),
                        param = currentWeather.snow,
                        unit = currentWeather.precipSymbol,
                        onUnitsClick = { switchUnits() }
                    )
                }
            }
        }
    }
}


@Composable
fun FiveDaysForecastDisplayExpanded(
    wholeDays: List<ForecastWholeDayModel>,
    switchUnits: () -> Unit,
    onDayClicked: (Int) -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = stringResource(id = R.string.forecast),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            itemsIndexed(wholeDays) { index, item ->
                ForecastFullDayDisplayExpanded(
                    day = item,
                    switchUnits = { switchUnits() },
                    onClick = { onDayClicked(it) },
                    index = index
                )
//                if (index < wholeDays.lastIndex) {
//                    Divider(thickness = 1.dp)
//                }
            }
            item {
                Text(
                    text = "",
                    style = MaterialTheme.typography.displayLarge
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {

                    Text(
                        text = stringResource(id = R.string.weather_icons),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    if (isSystemInDarkTheme()) {
                        Image(
                            painter = painterResource(id = R.drawable.powered_by_tomorrow_white),
                            contentDescription = null
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.powered_by_tomorrow_black),
                            contentDescription = null
                        )
                    }
                }
            }
            item {
                Text(
                    text = "",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}


@Composable
fun ForecastFullDayDisplayExpanded(
    day: ForecastWholeDayModel,
    switchUnits: () -> Unit,
    onClick: (Int) -> Unit,
    index: Int
) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {

        Column(
            modifier = Modifier
                .clickable { onClick(index) }
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.dayDescription.capitalizeEachWord(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(3/5f),
                    minLines = 1,
                    maxLines = 3,
                )
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = day.iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
                TemperatureDisplay(
                    tempHiBig = day.tempHiBig,
                    tempLoBig = day.tempLoBig,
                    tempHiSmall = day.tempHiSmall,
                    tempLoSmall = day.tempLoSmall,
                    symbolBig = day.symbolBig,
                    symbolSmall = day.symbolSmall,
                    feelsLike = false,
                    onClick = { switchUnits() }
                )
                Column {
                    ParameterLabelRow(
                        label = stringResource(id = R.string.pop_plain),
                        param = day.maxPop.toString(), unit = "%",
                        onUnitsClick = {} // do nothing
                    )
                    ParameterLabelRow(
                        label = stringResource(id = R.string.wind_plain),
                        param = day.maxWind,
                        unit = day.speedSymbol,
                        onUnitsClick = { switchUnits() }
                    )
                    ParameterLabelRowOrNull(
                        label = stringResource(id = R.string.wind_gust_plain),
                        param = day.maxGust,
                        unit = day.speedSymbol,
                        onUnitsClick = { switchUnits() }
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastDayFocusedDisplayExpanded(
    periods: List<ForecastPeriodModel>,
    switchUnits: () -> Unit,
    onClickBack: () -> Unit,
    isBackEnabled: Boolean
) {
    BackHandler(enabled = isBackEnabled,
        onBack = { onClickBack() })

    Column {
        Card(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box {
                IconButton(
                    onClick = { onClickBack() },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_keyboard_backspace_24),
                        contentDescription = null
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.forecast),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = periods[0].date,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            itemsIndexed(periods) { index, item ->
                PeriodDisplayExpanded(
                    period = item,
                    switchPeriodUnits = { switchUnits() },
                )
//                if (index < periods.lastIndex) {
//                    Divider(thickness = 1.dp)
//                }
            }
            item {
                Text(
                    text = "",
                    style = MaterialTheme.typography.displayLarge
                )
            }
            item {
                Text(
                    text = "",
                    style = MaterialTheme.typography.displaySmall
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {

                    Text(
                        text = stringResource(id = R.string.weather_icons),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    if (isSystemInDarkTheme()) {
                        Image(
                            painter = painterResource(id = R.drawable.powered_by_tomorrow_white),
                            contentDescription = null
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.powered_by_tomorrow_black),
                            contentDescription = null
                        )
                    }
                }
            }
            item {
                Text(
                    text = "",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
fun PeriodDisplayExpanded(
    period: ForecastPeriodModel,
    switchPeriodUnits: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
    ) {

        Column(Modifier.padding(4.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = period.description.capitalizeEachWord(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(3/5f),
                    minLines = 1,
                    maxLines = 3,
                )
                Text(
                    text = period.time,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = period.iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }

                TemperatureDisplay(
                    tempHiBig = period.tempBig,
                    tempLoBig = period.feelsLikeBig,
                    tempHiSmall = period.tempSmall,
                    tempLoSmall = period.feelsLikeSmall,
                    symbolBig = period.symbolBig,
                    symbolSmall = period.symbolSmall,
                    feelsLike = true,
                    onClick = { switchPeriodUnits() }
                )

                Column {
                    ParameterLabelRow(
                        label = stringResource(id = R.string.pop_plain),
                        param = period.pop, unit = "%",
                        onUnitsClick = {} // do nothing
                    )
                    ParameterLabelRow(
                        label = stringResource(id = R.string.wind_plain),
                        param = period.windSpeed,
                        unit = period.speedSymbol,
                        onUnitsClick = { switchPeriodUnits() }
                    )
                    ParameterLabelRowOrNull(
                        label = stringResource(id = R.string.wind_gust_plain),
                        param = period.windGust,
                        unit = period.speedSymbol,
                        onUnitsClick = { switchPeriodUnits() }
                    )
                    ParameterLabelRow(
                        label = stringResource(id = R.string.visibility),
                        param = period.visibility,
                        unit = period.distanceSymbol,
                        onUnitsClick = { switchPeriodUnits() }
                    )
                    ParameterLabelRow(
                        label = stringResource(id = R.string.humidity),
                        param = period.humidity,
                        unit = "%",
                        onUnitsClick = {} // do nothing
                    )

                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                ParameterLabelRowOrNull(label = stringResource(id = R.string.rain),
                    param = period.rain,
                    unit = period.precipSymbol,
                    onUnitsClick = { switchPeriodUnits() }
                )

                ParameterLabelRowOrNull(label = stringResource(id = R.string.snow),
                    param = period.snow,
                    unit = period.precipSymbol,
                    onUnitsClick = { switchPeriodUnits() }
                )
            }
        }

    }
}
