package com.steve.weatherquest.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.steve.weatherquest.R
import com.steve.weatherquest.WeatherMenu
import com.steve.weatherquest.models.ForecastPeriodModel
import com.steve.weatherquest.models.ForecastWholeDayModel
import com.steve.weatherquest.repository.WeatherApiStatus

private val TAG = "WeatherInfoScreen"


@Composable
fun WeatherInfoScreen(
    currentWeather: ForecastPeriodModel?,
    onRequestRefresh: (Boolean) -> Unit,
    suggestions: List<AnnotatedString>,
    weatherApiStatus: WeatherApiStatus,
    onSuggestionClicked: (Int) -> Unit,
    forecastWholeDay: List<ForecastWholeDayModel>?,
    switchUnits: () -> Unit,
    isShowingFocused: Boolean,
    forecastDayFocused: List<ForecastPeriodModel>?,
    onCancelFocus: () -> Unit,
    onDayClicked: (Int) -> Unit,
    wideness: Dp,
    tallness: Dp,
    isLocationLoading: Boolean,
    isDroppedDown: Boolean,
    showSearch: (Boolean) -> Unit,
    isMetric: Boolean,
    isMenuShown: Boolean,
    showMenu: () -> Unit,
    modifier: Modifier =  Modifier

    ) {


    Box() {



        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {


            // LaunchedEffect to display weather at startup
            LaunchedEffect(Unit) {
                Log.d(TAG, "LaunchedEffect launched!")
                // Fetch the data so it can be displayed on startup, pass in false to allow
                //the repository do decide whether to get fresh data from the network
                onRequestRefresh(false)
            }


//



            // Search result display
            if (wideness < 840.dp) {
                AnimatedVisibility(isDroppedDown) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {


                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        items(suggestions) { suggestion ->
                            SearchSuggestionCard(
                                location = suggestion,
                                onClick = {
                                    onSuggestionClicked(suggestions.indexOf(suggestion))
                                    showSearch(false)
                                }
                            )
                        }
                    }
                    }
                }
            }

            // If there is nothing to display, prompt the user to choose a location
            if (currentWeather == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(id = R.string.choose_location_prompt),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.3f
                            )
                        ),
                        modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center
                    )
                }

            }



            val lazyListState = rememberLazyListState()


            Box( // Box to hold the forecast display so we can overlay a progress indicator
                modifier = Modifier.fillMaxWidth()
            ) {



                if (currentWeather != null) {
                    if (wideness <= 400.dp) {
                        CurrentWeatherDisplaySmall(
                            currentWeather = currentWeather,
                            switchUnits = { switchUnits() },
                            lazyListState = lazyListState
                        )
                    }
                    if (wideness > 400.dp && wideness <= 840.dp) {
                        CurrentWeatherDisplay(
                            currentWeather = currentWeather,
                            switchUnits = { switchUnits() },
                            lazyListState = lazyListState,
                            tallness = tallness
                        )
                    }
                }
                if (weatherApiStatus == WeatherApiStatus.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(75.dp)
                            .align(Alignment.Center)
                    )
                }
                if (isLocationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(75.dp)
                            .align(Alignment.Center)
                    )
                }
            }


            androidx.compose.animation.AnimatedVisibility(
                visible = !isShowingFocused,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                if (forecastWholeDay != null) {
                    if (wideness <= 400.dp) {
                        FiveDaysForecastDisplaySmall(
                            wholeDays = forecastWholeDay,
                            switchUnits = { switchUnits() },
                            onDayClicked = { onDayClicked(it) },
                            lazyListState = lazyListState
                        )
                    }
                    if (wideness > 400.dp && wideness <= 840.dp) {
                        FiveDaysForecastDisplay(
                            wholeDays = forecastWholeDay,
                            switchUnits = { switchUnits() },
                            onDayClicked = { onDayClicked(it) },
                            lazyListState = lazyListState,
                            tallness = tallness
                        )
                    }

                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = isShowingFocused,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                if (forecastDayFocused != null) {
                    if (wideness <= 400.dp) {
                        ForecastDayFocusedDisplaySmall(
                            periods = forecastDayFocused,
                            switchUnits = { switchUnits() },
                            onClickBack = { onCancelFocus() },
                            isBackEnabled = isShowingFocused,
                            lazyListState = lazyListState
                        )
                    }
                    if (wideness > 400.dp && wideness <= 840.dp) {
                        ForecastDayFocusedDisplay(
                            periods = forecastDayFocused,
                            switchUnits = { switchUnits() },
                            onClickBack = { onCancelFocus() },
                            isBackEnabled = isShowingFocused,
                            lazyListState = lazyListState
                        )
                    }
                }
            }
        }

        if (wideness > 840.dp) {
            // Expanded width screen
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {

                    AnimatedVisibility(isDroppedDown) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            items(suggestions) { suggestion ->
                                SearchSuggestionCard(
                                    location = suggestion,
                                    onClick = {
                                        onSuggestionClicked(suggestions.indexOf(suggestion))
                                        showSearch(false)
                                    }
                                )
                            }
                        }
                    }



                    Box {
                        if (currentWeather != null) {
                            CurrentWeatherDisplayExpanded(
                                currentWeather = currentWeather,
                                switchUnits = { switchUnits() },
                            )

                        }

                        if (isLocationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(75.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }

                if (currentWeather != null) {
                Divider( // Middle
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                )
            }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Column {
                        Box {  // Right
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isShowingFocused,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {

                                if (forecastWholeDay != null) {
                                    FiveDaysForecastDisplayExpanded(
                                        wholeDays = forecastWholeDay,
                                        switchUnits = { switchUnits() },
                                        onDayClicked = { onDayClicked(it) },
                                    )
                                }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isShowingFocused,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {

                                if (forecastDayFocused != null) {
                                    ForecastDayFocusedDisplayExpanded(
                                        periods = forecastDayFocused,
                                        switchUnits = { switchUnits() },
                                        onClickBack = { onCancelFocus() },
                                        isBackEnabled = isShowingFocused,
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }


        // Menu
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 16.dp)
        ) {
            AnimatedVisibility(
                visible = isMenuShown,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                WeatherMenu(
                    switchUnits = { switchUnits() },
                    isMetric = isMetric,
                    showMenu = { showMenu() },
                )
            }
        }

    }

//    /**
//     * New Stuff
//     */
//        if (currentWeather != null) {
//
//            // Check whether the large current weather display has scrolled off screen
//            val isLargeCurrentOnScreen by remember {
//                derivedStateOf {
//                    lazyListState.layoutInfo.visibleItemsInfo.any { it.key == LARGE_CURRENT_KEY }
//                }
//            }
//
//
//            // val theKey = remember { derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo.last { it.key == LARGE_CURRENT_KEY } }.value.offset > 0 } }
//
//
//            if (wideness <= 400.dp) {
//
//            } else {
//
//                Column() {
//
//                    // Current weather title or small current weather display
//                    AnimatedVisibility(
//                        visible = isLargeCurrentOnScreen,
//                        enter = fadeIn(animationSpec = tween(800)),
//                      //  exit = fadeOut(animationSpec = tween(500))
//                    )
////                    if (isLargeCurrentOnScreen)
//                     {
//                        Column(
//                            modifier = Modifier.fillMaxWidth(),
//                                //.animateContentSize(animationSpec = tween(400)),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//
//                            Text(
//                                text = stringResource(id = R.string.current_weather),
//                                style = MaterialTheme.typography.headlineLarge,
//                            )
//                        }
//                    }
//                    AnimatedVisibility(
//                        visible = !isLargeCurrentOnScreen,
//                        enter = fadeIn(animationSpec = tween(800)),
//                       // exit = fadeOut(animationSpec = tween(100))
//                    )
////                    if (!isLargeCurrentOnScreen)
//                     {
//                        CurrentSmaller(
//                            currentWeather = currentWeather,
//                            switchUnits = { switchUnits() },
//                        )
//                    }
//
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        state = lazyListState
//                    ) {
//                        item(key = LARGE_CURRENT_KEY) {
//                            CurrentWeatherDisplay(
//                                currentWeather = currentWeather,
//                                switchUnits = { switchUnits() },
//                              //  isLargeCurrentOnScreen = isLargeCurrentOnScreen
//                            )
//                        }
//                        stickyHeader {// Header for Forecast display
//                            Surface() {
//                                Column(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Text(
//                                        text = stringResource(id = R.string.forecast),
//                                        style = MaterialTheme.typography.headlineLarge,
//                                    )
//                                    if (isShowingFocused) {
//                                        if (forecastDayFocused != null) {
//                                            Text(
//                                                text = forecastDayFocused[0].date,
//                                                style = MaterialTheme.typography.headlineSmall,
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        // Forecast Whole Days Display
//                        if (!isShowingFocused) {
//                            if (forecastWholeDay != null) {
//                                itemsIndexed(forecastWholeDay) { index, item ->
//                                    ForecastFullDayDisplay(
//                                        day = item,
//                                        switchUnits = { switchUnits() },
//                                        onClick = { onDayClicked(it) },
//                                        index = index
//                                    )
//                                    if (index < forecastWholeDay.lastIndex) {
//                                        Divider(thickness = 1.dp)
//                                    }
//                                }
//                            }
//                        }
//                        // Forecast Day Periods display
//                        if (isShowingFocused) {
//                            if (forecastDayFocused != null) {
//                                itemsIndexed(forecastDayFocused) { index, item ->
//                                    PeriodDisplay(
//                                        period = item,
//                                        switchPeriodUnits = { switchUnits() },
//                                        onClickBack = { onCancelFocus() },
//                                        isBackEnabled = isShowingFocused,
//                                    )
//                                    if (index < forecastDayFocused.lastIndex) {
//                                        Divider(thickness = 1.dp)
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//
//                }
//
//
//            }// end wideness normal phone
//        }
}


/**
 * Custom Alert Dialog to explain why location is needed (as per developer guidelines)
 */
@Composable
fun PermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Permission Needed")
        },
        text = {
            Text(text = stringResource(id = R.string.permission_rationale_message))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
