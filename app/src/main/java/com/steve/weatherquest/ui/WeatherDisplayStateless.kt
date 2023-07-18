package com.steve.weatherquest.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.steve.weatherquest.R
import com.steve.weatherquest.models.ForecastPeriodModel
import com.steve.weatherquest.models.ForecastWholeDayModel
import com.steve.weatherquest.util.capitalizeEachWord


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CurrentWeatherDisplay(
    currentWeather: ForecastPeriodModel,
    switchUnits: () -> Unit,
    lazyListState: LazyListState,
    tallness: Dp
) {

    /**
     * Track the lazyListState to create a larger viewport for the forecast list when the user
     * scrolls up by displaying a smaller current weather display.
     */
    var isScrolled by remember {
        mutableStateOf(false)
    }
    isScrolled =
        (remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }.value > 10 || remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }.value != 0)

    // Display the smaller current weather display when the list is scrolled up
    AnimatedVisibility(
        visible = isScrolled || tallness <= 400.dp,
        enter = fadeIn(animationSpec = tween(1200)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            CurrentSmaller(currentWeather = currentWeather, switchUnits = { switchUnits() })
        }
    }


    // Display the normal current weather display when the list is not scrolled up
    AnimatedVisibility(
        visible = !isScrolled && tallness > 400.dp,
        enter = expandVertically(animationSpec = tween(700),
            initialHeight = { it / 4 }),
        exit = shrinkVertically(animationSpec = tween(700),
            targetHeight = { it / 4 })
    ) {


        // Custom modifier for when we want a temperature display to slide out
        val tempSlideMod = Modifier.animateEnterExit(
            enter = slideInHorizontally(),
            exit = slideOutHorizontally(targetOffsetX = { it * -2 })
        )

        Card(
            modifier = Modifier
                .animateEnterExit(
                    enter = fadeIn(),
                    exit = fadeOut()
                )
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column() {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = stringResource(id = R.string.current_weather),
                        style = MaterialTheme.typography.headlineLarge,

                    )
                }



                Column(modifier = Modifier.padding(4.dp)) {


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
                                style = MaterialTheme.typography.headlineSmall,
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
                            animationModifier = tempSlideMod
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.animateEnterExit(
                                enter = slideInHorizontally(initialOffsetX = { it * 2 }),
                                exit = slideOutHorizontally(targetOffsetX = { it * 2 })
                            )
                        ) {
                            Image(
                                painter = painterResource(id = currentWeather.iconResId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .animateEnterExit(
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier.animateEnterExit(
                                enter = slideInHorizontally(initialOffsetX = { it / 2 }),
                                exit = slideOutHorizontally(targetOffsetX = { it * 2 })
                            )
                        ) {
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
//                    Divider(
//                        thickness = 3.dp,
//                        modifier = Modifier
//                            .padding(vertical = 8.dp)
//                            .animateEnterExit(
//                                enter = fadeIn(animationSpec = tween(600)),
//                                exit = fadeOut(animationSpec = tween(600))
//                            )
//                    )
            }

        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FiveDaysForecastDisplay(
    wholeDays: List<ForecastWholeDayModel>,
    switchUnits: () -> Unit,
    onDayClicked: (Int) -> Unit,
    lazyListState: LazyListState,
    tallness: Dp
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (tallness > 400.dp) {
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
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState
        ) {
            itemsIndexed(wholeDays) { index, item ->
                ForecastFullDayDisplay(
                    day = item,
                    switchUnits = { switchUnits() },
                    onClick = { onDayClicked(it) },
                    index = index
                )
//                    if (index < wholeDays.lastIndex) {
//                        Divider(thickness = 1.dp)
//                    }
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
fun ForecastFullDayDisplay(
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
fun ForecastDayFocusedDisplay(
    periods: List<ForecastPeriodModel>,
    switchUnits: () -> Unit,
    onClickBack: () -> Unit,
    isBackEnabled: Boolean,
    lazyListState: LazyListState,
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
                .fillMaxWidth(),
            state = lazyListState
        ) {
            itemsIndexed(periods) { index, item ->
                PeriodDisplay(
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
fun PeriodDisplay(
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


@Composable
fun ParameterLabelRow(
    label: String,
    param: String,
    unit: String,
    onUnitsClick: () -> Unit
) {

    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Text(
            text = param,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Bottom)
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Top)
                .clickable { onUnitsClick() }
        )
    }
}

/**
 * ParameterLabelRow that only displays if the parameter is not null,
 * to avoid having to write all those null checks individually
 */
@Composable
fun ParameterLabelRowOrNull(
    label: String,
    param: String?,
    unit: String,
    onUnitsClick: () -> Unit
) {
    if (param != null) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = param,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Bottom)
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Top)
                    .clickable { onUnitsClick() }
            )
        }
    }
}


@Composable
fun TemperatureDisplay(
    tempHiBig: String,
    tempLoBig: String,
    tempHiSmall: String,
    tempLoSmall: String,
    symbolBig: String,
    symbolSmall: String,
    feelsLike: Boolean,
    onClick: () -> Unit,
    animationModifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // modifier = Modifier.clickable { onClick() }
    ) {
        Row {
            Text(
                text = tempHiBig,
                style = MaterialTheme.typography.displayLarge
            )
            Column {
                Text(
                    text = stringResource(id = R.string.degrees, symbolBig),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.clickable { onClick() }
                )
                Text(
                    text = stringResource(id = R.string.temp_and_degrees, tempHiSmall, symbolSmall),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 1.dp)
                )
            }
        }
        Column(modifier = animationModifier) {// Modifier for enter/exit animations
            if (feelsLike) {
                Text(
                    text = stringResource(id = R.string.feels_like),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Row {
                Text(
                    text = tempLoBig,
                    style = MaterialTheme.typography.headlineLarge
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.degrees, symbolBig),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { onClick() }
                    )
                    Text(
                        text = stringResource(
                            id = R.string.temp_and_degrees,
                            tempLoSmall,
                            symbolSmall
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 1.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CurrentSmaller(
    currentWeather: ForecastPeriodModel,
    switchUnits: () -> Unit,
) {

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // modifier = Modifier.clickable { onClick() }
            ) {

                Row {
                    Text(
                        text = currentWeather.tempBig,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Column {
                        Text(
                            text = stringResource(id = R.string.degrees, currentWeather.symbolBig),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.clickable { switchUnits() }
                        )
                        Text(
                            text = stringResource(
                                id = R.string.temp_and_degrees,
                                currentWeather.tempSmall,
                                currentWeather.symbolSmall
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = currentWeather.description.capitalizeEachWord(),
                style = MaterialTheme.typography.headlineSmall,
            )
            Image(
                painter = painterResource(id = currentWeather.iconResId),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }
//        Divider(
//            thickness = 3.dp,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
    }

}


