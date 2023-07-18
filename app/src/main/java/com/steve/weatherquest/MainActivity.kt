package com.steve.weatherquest

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.steve.weatherquest.data.LocationRepository
import com.steve.weatherquest.ui.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.steve.weatherquest.location.LocationPermissionState
import com.steve.weatherquest.models.ForecastPeriodModel
import com.steve.weatherquest.ui.ExpandableSearchView
import com.steve.weatherquest.ui.InitializingScreen
import com.steve.weatherquest.ui.LocationInfoScreen
import com.steve.weatherquest.ui.PermissionRationaleDialog
import com.steve.weatherquest.ui.ServicesUnavailableScreen
import com.steve.weatherquest.ui.WeatherInfoScreen
import com.steve.weatherquest.ui.theme.NewComposeWeatherTestsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private val TAG = "MainActivity"

private const val PRIVACY_POLICY_ADDRESS = "https://doc-hosting.flycricket.io/weather-quest-privacy-policy/06ec81c4-b2a6-459e-bce7-a6f9fe9bd35a/privacy"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var locationRepository: LocationRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        val locationPermissionState = LocationPermissionState(this) {
            // Triggered by 'onResult' when permission request is launched
            if (it.hasPermission()) {
                checkSettingsAndRequestLocation()
            }
        }


        setContent {

            NewComposeWeatherTestsTheme {

                WeatherDisplay(
                    locationPermissionState = locationPermissionState,
                    viewModel = viewModel,
                )

            }
        }
    }


    // This is only ever triggered by LocationPermissionState's 'onResult' when permission is granted.
    fun getLocation() {
        locationRepository.provideTheLocation(onGpsSuccessResult = { viewModel.setWeatherLocationFromDeviceLocation() })
        //  Toast.makeText(this, "Location requested", Toast.LENGTH_SHORT).show()
    }

    /**
     * Location might be turned off in the device settings, so we need to check that
     * before requesting the location. If it is turned off, show the system dialog
     * asking the user to turn it on. Then we can go ahead and get the location.
     */
    fun checkSettingsAndRequestLocation() {
        // The LocationSettingsRequest requires a LocationRequest in order to work
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10_000
        ).build()
        // Build the LocationSettingsRequest and add the LocationRequest to it
        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(request)

        // Check the device settings using the LocationSettingsRequest
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> =
            client.checkLocationSettings(settingsBuilder.build())

        // Get the result of the request (success or failure)
        task.addOnSuccessListener {
            // Location settings are good. Go ahead and get the location.
            getLocation()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but can be fixed by showing the user a dialog
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult()
                    exception.startResolutionForResult(this@MainActivity, 0x1)
                } catch (sendX: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }

    }

    /**
     * Get the result of the dialog that prompted the user to turn location settings on.
     * This function is deprecated, but I can't find a better way to do it right now:(
     */
    @SuppressLint("MissingSuperCall")
    @Deprecated("Not sure how else to do it")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            when (resultCode) {
                RESULT_OK -> {
                    // User has elected to turn location on; now we can get it
                    getLocation()
                }

                RESULT_CANCELED -> {
                    Toast.makeText(this, "Device location must be turned on.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }


}


@Composable
fun LocationTestDisplay(
    viewModel: MainViewModel,
    locationPermissionState: LocationPermissionState,
    modifier: Modifier
) {
    val uiState by viewModel.playServicesAvailableState.collectAsState()
    val currentLocation by viewModel.deviceLocation.collectAsState()
    val isLoading by viewModel.isLocationLoading.collectAsState()

    // Determine which screen to show
    when (uiState) {
        PlayServicesAvailableState.Initializing -> InitializingScreen()
        PlayServicesAvailableState.PlayServicesUnavailable -> ServicesUnavailableScreen()
        PlayServicesAvailableState.PlayServicesAvailable -> {
            LocationInfoScreen(
                showDegradedExperience = locationPermissionState.showDegradedExperience,
                needsPermissionRationale = locationPermissionState.shouldShowRationale(),
                onButtonClick = { locationPermissionState.requestPermission() },
                isLoading = isLoading,
                location = currentLocation
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDisplay(
    locationPermissionState: LocationPermissionState,
    viewModel: MainViewModel,
    // goToPolicy: () -> Unit
) {

    // Weather
    val currentWeather by viewModel.myOpenWeatherRepository.currentWeatherDisplayable.collectAsState()
    val wholeDayForecast by viewModel.myOpenWeatherRepository.forecastWholeDaysDisplayable.collectAsState()
    val weatherCity by viewModel.myOpenWeatherRepository.weatherCity.collectAsState()
    val focusedForecast by viewModel.myOpenWeatherRepository.focusedForecastDay.collectAsState()
    val weatherApiStatus by viewModel.weatherApiStatus.collectAsState()
    // AutoComplete
    val searchedText by viewModel.searchedLocation.collectAsState()
    val suggestions by viewModel.myAutoCompleteRepository.suggestions.collectAsState()

    val showingFocused by viewModel.myOpenWeatherRepository.showingForecastFocused.collectAsState()

    // SearchBar
    val clearTrigger by viewModel.clearTrigger.collectAsState()
    val focusTrigger by viewModel.focusTrigger.collectAsState()
    var isDroppedDown by remember {
        mutableStateOf(false)
    }
    isDroppedDown = suggestions.isNotEmpty()

    // Measure the screen width
    val width = LocalConfiguration.current.screenWidthDp.dp
    val height = LocalConfiguration.current.screenHeightDp.dp


    // Device location
    val playServicesAvailableState by viewModel.playServicesAvailableState.collectAsState()
    val isLocationLoading by viewModel.isLocationLoading.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }
    val isShowSnackbar by viewModel.isShowSnackbar.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    // TopAppBar
    val isSearchShown by viewModel.isShowSearch.collectAsState()

    // Menu
    val isMetric by viewModel.isMetric.collectAsState()
    val isMenuShown by viewModel.isMenuShown.collectAsState()



    Scaffold(
        topBar = {
            WeatherTopBar(
                suggestions = suggestions,
                onSuggestionClick = { viewModel.onSuggestionClicked(it) },
                searchedLocation = searchedText,
                onSearchChange = { viewModel.onSearchFieldValueChange(it) },
                onGpsClick = { locationPermissionState.requestPermission() },
                clearTrigger = clearTrigger,
                isSearchShown = isSearchShown,
                showSearch = { viewModel.showSearch(it) },
                currentWeather = currentWeather,
                onRequestRefresh = { viewModel.updateBothWeathers(it) },
                showMenu = { viewModel.showMenu() },
                playServices = playServicesAvailableState,
                needsPermissionRationale = locationPermissionState.shouldShowRationale(),
                showSnackBar = { viewModel.triggerSnackbar(it) },
                focusTrigger = focusTrigger
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        ) { contentPadding ->
        LaunchedEffect(isShowSnackbar) {
            if (isShowSnackbar) {
                snackbarHostState.showSnackbar(snackbarMessage)
                viewModel.snackBarDone() // Reset showSnackbar to false
            }
        }

        Surface(
            modifier = Modifier.padding(contentPadding)
        ) {

            if (currentWeather != null) {
                Image(
                    painter = painterResource(currentWeather!!.background!!),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            WeatherInfoScreen(
                currentWeather = currentWeather,
                onRequestRefresh = { viewModel.updateBothWeathers(it) },
                suggestions = suggestions,
                weatherApiStatus = weatherApiStatus,
                onSuggestionClicked = { viewModel.onSuggestionClicked(it) },
                forecastWholeDay = wholeDayForecast,
                switchUnits = { viewModel.switchMetric() },
                isShowingFocused = showingFocused,
                forecastDayFocused = focusedForecast,
                onCancelFocus = { viewModel.myOpenWeatherRepository.onCancelShowFocused() },
                onDayClicked = { viewModel.myOpenWeatherRepository.onForecastDayClicked(it) },
                wideness = width,
                tallness = height,
                isLocationLoading = isLocationLoading,
                isDroppedDown = isDroppedDown,
                showSearch = { viewModel.showSearch(it) },
                isMetric = isMetric,
                isMenuShown = isMenuShown,
                showMenu = { viewModel.showMenu() },
            )

        }
    }
}


//@OptIn(ExperimentalAnimationApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    suggestions: List<AnnotatedString>,
    onSuggestionClick: (Int) -> Unit,
    searchedLocation: String,
    onSearchChange: (String) -> Unit,
    onGpsClick: () -> Unit,
    clearTrigger: Int,
    isSearchShown: Boolean,
    showSearch: (Boolean) -> Unit,
    currentWeather: ForecastPeriodModel?,
    onRequestRefresh: (Boolean) -> Unit,
    showMenu: () -> Unit,
    needsPermissionRationale: Boolean,
    showSnackBar: (String) -> Unit,
    playServices: PlayServicesAvailableState,
    focusTrigger: Int,
) {

    /**
     * Device location stuff
     */
    var showRationaleDialog by remember { mutableStateOf(false) }
    // Compose will trigger this for recomposition :)
    if (showRationaleDialog) {
        PermissionRationaleDialog(
            onConfirm = {
                showRationaleDialog = false
                onGpsClick()
            },
            onDismiss = {
                showRationaleDialog = false
                showSnackBar("Please allow permission to access device location")
            }
        )
    }
    // Customize the device location button onClick behavior here so the hoisting function doesn't
    // have to worry about the RationaleDialog
    fun onGpsButtonClick() {
        if (playServices == PlayServicesAvailableState.PlayServicesUnavailable) {
            showSnackBar("Google Play Services are unavailable on your device :(")
        }
        if (needsPermissionRationale) {
            showRationaleDialog = true
        } else {
            onGpsClick()
        }
    }



    Surface {

        TopAppBar(title = {
            AnimatedVisibility(visible = isSearchShown) {
                ExpandableSearchView(
                    searchedLocation = searchedLocation,
                    onSearchChange = { onSearchChange(it) },
                    clearTrigger = clearTrigger,
                    focusTrigger = focusTrigger
                )
            }
            AnimatedVisibility(visible = !isSearchShown) {
                Text(
                    text = if (currentWeather != null) {
                        currentWeather.name!!
                    } else {
                        stringResource(id = R.string.app_name)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.clickable { showSearch(true) }
                )
            }
        },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    3.dp
                )
            ),
            navigationIcon = {
                AnimatedVisibility(visible = !isSearchShown) {
                    IconButton(onClick = { showSearch(true) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_search_24),
                            contentDescription = null,
                        )
                    }
                }
                AnimatedVisibility(visible = isSearchShown) {
                    IconButton(onClick = { showSearch(false) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_keyboard_backspace_24),
                            contentDescription = null
                        )
                    }
                }
            },
            actions = {
                AnimatedVisibility(visible = suggestions.isEmpty()) {
                    IconButton(onClick = { onGpsClick() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_my_location_24),
                            contentDescription = "Use device location"
                        )
                    }
                }
                AnimatedVisibility(visible = currentWeather != null) {
                    IconButton(onClick = { onRequestRefresh(true) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_refresh_24),
                            contentDescription = "Refresh weather data"
                        )
                    }
                }
                IconButton(
                    onClick = {
                        showMenu()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_menu_24),
                        contentDescription = "Menu button"
                    )
                }
            })
    }
}


@Composable
fun WeatherMenu(
    switchUnits: () -> Unit,
    isMetric: Boolean,
    showMenu: () -> Unit,
    //goToPolicy: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(10.dp)) {
        Surface(
//            tonalElevation = 5.dp,
            modifier = Modifier.width(200.dp)
        ) {

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.menu),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { showMenu() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_close_24),
                            contentDescription = null
                        )
                    }
                }
                IsMetricRadioGroup(
                    switchUnits = { switchUnits() },
                    isMetric = isMetric,
                    showMenu = { showMenu() })

                Divider()

                val policyUrl: Uri = Uri.parse(PRIVACY_POLICY_ADDRESS)
                val privacyPolicyIntent = Intent(Intent.ACTION_VIEW, policyUrl)
                val context = LocalContext.current
                TextButton(onClick = {
                    startActivity(context, privacyPolicyIntent, null)
                    showMenu()
                }) {
                    Text(text = stringResource(id = R.string.privacy_policy))
                }

            }
        }
    }
}


@Composable
fun IsMetricRadioGroup(
    switchUnits: () -> Unit,
    isMetric: Boolean,
    showMenu: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) { // Metric button
            RadioButton(selected = isMetric, onClick = {
                if (!isMetric) {
                    switchUnits()
                }
                showMenu()
            })
            Text(
                text = stringResource(id = R.string.metric),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) { // Imperial button
            RadioButton(selected = !isMetric, onClick = {
                if (isMetric) {
                    switchUnits()
                }
                showMenu()
            })
            Text(
                text = stringResource(id = R.string.imperial),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }

}


// Custom Modifier to clear focus from hoisted state
fun Modifier.clearFocusFromHoistedState(
    clearTrigger: Int
): Modifier = composed {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(clearTrigger) {
        focusManager.clearFocus()
    }
    onFocusEvent {
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NewComposeWeatherTestsTheme {
        //  WeatherMenu()
    }
}
