package com.steve.weatherquest

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.*
import com.steve.weatherquest.data.IsoCodes
import com.steve.weatherquest.data.LocationRepository
import com.steve.weatherquest.data.PlayServicesAvailabilityChecker
import com.steve.weatherquest.data.WeatherDatabaseDao
import com.steve.weatherquest.models.ForecastPeriodModel
import com.steve.weatherquest.models.ForecastWholeDayModel
import com.steve.weatherquest.models.HereAddress
import com.steve.weatherquest.models.OpWeMaGeocodeResponseModel
import com.steve.weatherquest.repository.AutoCompleteRepository
import com.steve.weatherquest.repository.OpWeMaGeocodeRepository
import com.steve.weatherquest.repository.OpenWeatherRepository
import com.steve.weatherquest.repository.SettingsDataStoreRepository
import com.steve.weatherquest.repository.WeatherApiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    playServicesAvailabilityChecker: PlayServicesAvailabilityChecker,
    locationRepository: LocationRepository,
    autoCompleteRepository: AutoCompleteRepository,
    geocodeRepository: OpWeMaGeocodeRepository,
    openWeatherRepository: OpenWeatherRepository,
    dataStore: SettingsDataStoreRepository,
    weatherDao: WeatherDatabaseDao
) : ViewModel() {


    // Current location (Could be null)
    val deviceLocation = locationRepository.currentLocation


    // Location selected by the user either by device location or search
    private var _weatherLocation: OpWeMaGeocodeResponseModel? = null


    private val _isShowSnackbar = MutableStateFlow(false)
    val isShowSnackbar = _isShowSnackbar.asStateFlow()

    private val _snackbarMessage = MutableStateFlow("")
    val snackbarMessage = _snackbarMessage.asStateFlow()


    // AutoComplete result
    val myAutoCompleteRepository = autoCompleteRepository

    // Loading state of the location in case it takes a long time
    val isLocationLoading = locationRepository.isLocationLoading

    // Trigger for clearing searchbar focus
    private val _clearTrigger = MutableStateFlow(0)
    val clearTrigger = _clearTrigger.asStateFlow()

    // Trigger for focusing searchbar
    private val _focusTrigger = MutableStateFlow(0)
    val focusTrigger = _focusTrigger.asStateFlow()

    private val myGeocodeRepository = geocodeRepository

    private val _isShowSearch = MutableStateFlow(false)
    val isShowSearch = _isShowSearch.asStateFlow()

    private val _isMetric = MutableStateFlow(true)
    val isMetric = _isMetric.asStateFlow()

    private val _isMenuShown = MutableStateFlow(false)
    val isMenuShown = _isMenuShown.asStateFlow()


    private val _currentWeatherDisplayable = MutableStateFlow<ForecastPeriodModel?>(null)
    val currentWeatherDisplayable = _currentWeatherDisplayable.asStateFlow()

    private var _forecastWholeDaysDisplayable = MutableStateFlow<List<ForecastWholeDayModel>?>(null)
    val forecastWholeDaysDisplayable = _forecastWholeDaysDisplayable.asStateFlow()


    private val _showingForecastFocused = MutableStateFlow(false)
    val showingForecastFocused = _showingForecastFocused.asStateFlow()

    private val _focusedForecastDay = MutableStateFlow<List<ForecastPeriodModel>?>(null)
    val focusedForecastDay = _focusedForecastDay.asStateFlow()

    private val _weatherApiStatus = MutableStateFlow(WeatherApiStatus.DONE)
    val weatherApiStatus = _weatherApiStatus.asStateFlow()

    // The list of location suggestions from searching in the searchbar
    private val _suggestions = MutableStateFlow<List<AnnotatedString>>(listOf())
    val suggestions = _suggestions.asStateFlow()


    private val myOpenWeatherRepository = openWeatherRepository

    private val myDataStore = dataStore

    private val myWeatherDao = weatherDao


    private var metric = true


    // Collect the data from the dataStore
    var newMet = viewModelScope.launch(Dispatchers.IO) {
        dataStore.weatherSettingsFlow.collect { settings ->
            metric = settings.isMetric
            openWeatherRepository.isMetric = settings.isMetric
            _isMetric.value = settings.isMetric

        }
    }


    // String to hold the location information for the geocode call
    private var geocodeableLocation: String = "kamloops"

    // Text of the location search bar
    private val _searchedLocation = MutableStateFlow<String>("")
    val searchedLocation = _searchedLocation.asStateFlow()

    init {
        viewModelScope.launch {
            setLocationFromDatabase()
        }
    }


    fun showMenu() {
        _isMenuShown.value = !_isMenuShown.value
    }

    fun triggerSnackbar(message: String) {
        _snackbarMessage.value = message
        _isShowSnackbar.value = true
    }

    // Called once the snackbar has displayed
    fun snackBarDone() {
        _isShowSnackbar.value = false
    }

    private fun setWeatherApiStatus(status: WeatherApiStatus) {
        _weatherApiStatus.value = status
    }

    fun onForecastDayClicked(index: Int) {
        _focusedForecastDay.value = myOpenWeatherRepository.setupPeriodDisplayable(index)
        _showingForecastFocused.value = true
    }

    fun onCancelShowFocused() {
        _showingForecastFocused.value = false
    }

    // Show or hide the search view in the topAppBar
    fun showSearch(isShow: Boolean) {
        _isShowSearch.value = isShow
        if (isShow) {
            _focusTrigger.value++
        }
    }

    private fun clearSuggestions() {
        _suggestions.value = listOf()
    }


    private suspend fun setLocationFromDatabase() {
        withContext(Dispatchers.IO) {
            _weatherLocation = myWeatherDao.getGeocodeDatabaseData()
        }
        Log.d("MainViewModel", "WeatherLocation: $_weatherLocation")
    }


    // Called when the text of the search bar changes
    fun onSearchFieldValueChange(str: String) {
        _searchedLocation.value = str
        // Call the autoComplete api when the search is 3 or more characters
        if (_searchedLocation.value.length >= 3) {
            doTheAutoCompleteNetworkCall()
        } else {
            // Clear suggestions if the user backspaces the search below 3 characters
            clearSuggestions()
        }
    }


    // Called when the user clicks on a suggestion in the location search bar
    fun onSuggestionClicked(index: Int) {

        // Geocode the selected location with the OpenWeather api so we can make the call to
        // get the weather info
        val selectedResponse = myAutoCompleteRepository.autoCompleteResponse!!.items[index].address
        geocodeableLocation = makeGeocodeString(selectedResponse)
        doTheGeocodeNetworkCall()
        Log.d("SuggestionClicked", geocodeableLocation)
        clearSuggestions()
        _searchedLocation.value = ""
        _clearTrigger.value++
    }

    // Create the string for the geocode query
    private fun makeGeocodeString(address: HereAddress): String {
        // Convert the three-letter country code of the autoComplete result to a 2-letter code
        val twoLetterCountry = IsoCodes().convertThreeToTwoLetter(address.countryCode) ?: ""
        // Create a list of strings of the city, state, and country of the selected location
        val builderList = mutableListOf<String>()
        builderList.add(address.city.toString())
        // Only include the state code if it is in USA or Canada
        if (twoLetterCountry == "CA" || twoLetterCountry == "US") {
            builderList.add(address.stateCode.toString())
        }
        builderList.add(twoLetterCountry)
        // Join the list items into a string with a comma as a separator
        return builderList.joinToString(separator = ",")
    }

    // Set and store a new location
    private fun setWeatherLocation(location: OpWeMaGeocodeResponseModel) {
        _weatherLocation = location
        // Set the location in the database
        viewModelScope.launch(Dispatchers.IO) {
            myWeatherDao.insertDatabaseGeocode(location)
            Log.d("MainViewModel", "Location inserted to database")
        }
    }

    // Create a weather location object with the device location and set it as _weatherLocation
    fun setWeatherLocationFromDeviceLocation() {
        val deviceLoc = OpWeMaGeocodeResponseModel(
            name = "Unknown",
            lat = deviceLocation.value!!.latitude,
            lon = deviceLocation.value!!.longitude,
            country = null,
            state = null
        )
        setWeatherLocation(deviceLoc)
        // Trigger the repository to fetch the data for the new location
        viewModelScope.launch {
            updateBothWeathers(true)
        }
    }

    // Emit a state based on whether Google Play Services are available
    val playServicesAvailableState = flow {
        emit(
            if (playServicesAvailabilityChecker.isGooglePlayServicesAvailable()) {
                PlayServicesAvailableState.PlayServicesAvailable
            } else {
                PlayServicesAvailableState.PlayServicesUnavailable
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlayServicesAvailableState.Initializing)


    private fun doTheAutoCompleteNetworkCall() {
        viewModelScope.launch {
            _suggestions.value = myAutoCompleteRepository.callAutoComplete(_searchedLocation.value,
                onError = {
                    triggerSnackbar("Network Error")
                    clearSuggestions()
                    myAutoCompleteRepository.clearResponse()
                })

        }
    }

    private fun doTheGeocodeNetworkCall() {
        viewModelScope.launch {
            myGeocodeRepository.callOpWeMaGeocode(
                qry = geocodeableLocation,
                onCallFinished = { updateBothWeathers(true) },
                setGeoLocation = { setWeatherLocation(it) },
                onError = { triggerSnackbar("Network Error") }
            )
        }
    }


    private fun doTheOpenWeatherCurrentCall() {
        viewModelScope.launch {
            _currentWeatherDisplayable.value = myOpenWeatherRepository.callCurrentWeather(
                lat = _weatherLocation?.lat.toString(),
                lon = _weatherLocation?.lon.toString(),
                onError = { triggerSnackbar("Network Error. New weather data not available.") },
                setStatus = { setWeatherApiStatus(it) }
            )
        }
    }

    private fun doTheOpenWeatherForecastCall() {
        viewModelScope.launch {
            _forecastWholeDaysDisplayable.value = myOpenWeatherRepository.callForecastWeather(
                lat = _weatherLocation?.lat.toString(),
                lon = _weatherLocation?.lon.toString(),
                onError = { triggerSnackbar("Network Error. New weather data not available.") },
                onCancelShowFocused = { onCancelShowFocused() },
                setStatus = { setWeatherApiStatus(it) }
            )
        }
    }


    // Call for an update of weather data; the repository will decide whether data
    // should be fetched from the network or not
    fun updateBothWeathers(isNewQuery: Boolean) {
        if (_weatherLocation != null) {
            myOpenWeatherRepository.isNewCurrentQuery = isNewQuery
            myOpenWeatherRepository.isNewForecastQuery = isNewQuery
            doTheOpenWeatherCurrentCall()
            doTheOpenWeatherForecastCall()
        }
    }


    fun switchMetric() {
        val allWeathersDisplayable = myOpenWeatherRepository.switchUnits(!metric)
        _currentWeatherDisplayable.value = allWeathersDisplayable.current
        _forecastWholeDaysDisplayable.value = allWeathersDisplayable.forecastWholeDays
        _focusedForecastDay.value = allWeathersDisplayable.forecastFocused
        setIsMetric(!metric)
    }


    private fun setIsMetric(metric: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            myDataStore.saveIsMetric(metric)
        }
    }

}


enum class PlayServicesAvailableState {
    Initializing, PlayServicesUnavailable, PlayServicesAvailable
}

