package com.steve.weatherquest.repository

import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.DrawableRes
import com.steve.weatherquest.R
import com.steve.weatherquest.data.CityDatabaseEntity
import com.steve.weatherquest.models.*
import com.steve.weatherquest.data.CurrentDatabaseEntity
import com.steve.weatherquest.data.ForecastDatabaseEntity
import com.steve.weatherquest.data.WeatherDatabaseDao
import com.steve.weatherquest.models.City
import com.steve.weatherquest.models.Cloudiness
import com.steve.weatherquest.models.CurrentMainWeather
import com.steve.weatherquest.models.CurrentWeather
import com.steve.weatherquest.models.ForecastDisplayableModel
import com.steve.weatherquest.models.ForecastPeriod
import com.steve.weatherquest.models.ForecastPeriodModel
import com.steve.weatherquest.models.ForecastWholeDayModel
import com.steve.weatherquest.models.MainWeather
import com.steve.weatherquest.models.OpenWeatherCurrentModel
import com.steve.weatherquest.models.OpenWeatherForecastModel
import com.steve.weatherquest.models.PartOfDay
import com.steve.weatherquest.models.Precip
import com.steve.weatherquest.models.Precipitation
import com.steve.weatherquest.models.Sys
import com.steve.weatherquest.models.Weather
import com.steve.weatherquest.models.WeatherCity
import com.steve.weatherquest.models.Wind
import com.steve.weatherquest.network.OpenWeatherMapApi
import com.steve.weatherquest.util.trimToTwoDecimals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

enum class WeatherApiStatus { LOADING, ERROR, DONE }

private const val APIKEY = "Private"
private const val UNITS_METRIC = "metric"

private const val DATE_SKELETON = "EEE d MMM"
private const val TIME_SKELETON = "h:mm a"

@Singleton
class OpenWeatherRepository @Inject constructor(
    weatherDatabaseDao: WeatherDatabaseDao
) {


    private val TAG = "OpenWeatherRepository"


    private val databaseDao = weatherDatabaseDao

    // Current weather
    private var _currentWeatherResponse: OpenWeatherCurrentModel? = null
    val currentWeatherResponse get() = _currentWeatherResponse

    // Forecast weather
    private var _forecastWeatherResponse: OpenWeatherForecastModel? = null
    val forecastWeatherResponse get() = _forecastWeatherResponse

    // Current weather displayable
    private val _currentWeatherDisplayable = MutableStateFlow<ForecastPeriodModel?>(null)
    val currentWeatherDisplayable = _currentWeatherDisplayable.asStateFlow()

    // Forecast weather displayable
    private var _forecastWeatherDisplayable: List<ForecastDisplayableModel>? = null

    // Forecast displayable separated by date
    private val _sortedForecastDisplayable =
        MutableStateFlow<List<List<ForecastDisplayableModel>>?>(null)
    val sortedForecastDisplayable = _sortedForecastDisplayable.asStateFlow()

    // Forecast whole days displayable
    private val _forecastWholeDaysDisplayable = MutableStateFlow<List<ForecastWholeDayModel>?>(null)
    val forecastWholeDaysDisplayable = _forecastWholeDaysDisplayable.asStateFlow()


    private val _weatherApiStatus = MutableStateFlow<WeatherApiStatus>(WeatherApiStatus.DONE)
    val weatherApiStatus = _weatherApiStatus.asStateFlow()


    // City info
    private val _weatherCity = MutableStateFlow<WeatherCity?>(null)
    val weatherCity = _weatherCity.asStateFlow()

    private val _showingForecastFocused = MutableStateFlow(false)
    val showingForecastFocused = _showingForecastFocused.asStateFlow()

    private val _focusedForecastDay = MutableStateFlow<List<ForecastPeriodModel>?>(null)
    val focusedForecastDay = _focusedForecastDay.asStateFlow()

    // Boolean to tell us if the refresh call was triggered by the user entering a new location
    var isNewCurrentQuery = false
    var isNewForecastQuery = false


    private var symbolBig: String? = null
    private var symbolSmall: String? = null
    private var speedSymbol: String? = null
    private var distanceSymbol: String? = null
    private var precipSymbol: String? = null


    var isMetric: Boolean = true


    // Store the index here so we can use it without having to hoist it up again
    private var dayIndex = 0

    fun onForecastDayClicked(index: Int) {
        dayIndex = index
        setupPeriodDisplayable()
        _showingForecastFocused.value = true
    }

    fun onCancelShowFocused() {
        _showingForecastFocused.value = false
    }


    suspend fun callCurrentWeather(lat: String, lon: String, onError: () -> Unit) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Current weather network called")

            _weatherApiStatus.value = WeatherApiStatus.LOADING

            var instead = false
            var fetchNewData = false

            if (databaseDao.getCurrentDatabaseDate() == null) { // Database empty?
                Log.d(TAG, "Current Weather database returned null!")
                fetchNewData = true
            } else if (isNewCurrentQuery) { // Is this just a refresh or a new query?
                fetchNewData = true
            } else if (hasEnoughTimePassedCurrent(databaseDao.getCurrentDatabaseDate()!!)) { // Is data recent?
                fetchNewData = true
            }
            if (fetchNewData) { // Get fresh data from the network
                Log.d(TAG, "Fetching new currentWeather data from network")
                try {
                    _currentWeatherResponse =
                        OpenWeatherMapApi.retrofitService.getCurrentWeatherResponse(
                            lat = lat,
                            lon = lon,
                            units = UNITS_METRIC,
                            appid = APIKEY
                        )
                } catch (e: Exception) {
                    _weatherApiStatus.value = WeatherApiStatus.ERROR
                    Log.d(TAG, "Error in current weather network call!")
                    e.printStackTrace()
                    // Snackbar notifying of network error
                    onError()
                    // Fetch from database instead
                    instead = true
                    fetchCurrentWeatherFromDatabase()
                }

                if (_currentWeatherResponse != null) {
                    if (!instead) { // Don't do this twice
                        // Setup display data
                        setupCurrentDisplayable()
                        _weatherApiStatus.value = WeatherApiStatus.DONE

                        // Write the new data to the database

                        // Clear the old data from the database first
                        databaseDao.clearCurrentWeatherDatabase()
                        Log.d(TAG, "Clear current database called")
                        // Convert the network data to database data
                        val databaseCurrent =
                            CoroutineScope(Dispatchers.Default).async {
                                translateDatabaseFromCurrentWeather(_currentWeatherResponse!!)
                            }.await()
                        // Insert the new data
                        databaseDao.insertCurrentWeather(databaseCurrent)
                        Log.d(TAG, "Insert current weather to database called")
                    }
                }
            } else { // Use the data in the database
                fetchCurrentWeatherFromDatabase()
                if (_currentWeatherResponse != null) {
                    // Setup display data
                    setupCurrentDisplayable()
                }
                _weatherApiStatus.value = WeatherApiStatus.DONE
            }
            isNewCurrentQuery = false // Reset isNewQuery
        }
    }

    suspend fun callForecastWeather(lat: String, lon: String, onError: () -> Unit) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Weather forecast network called")
            _weatherApiStatus.value = WeatherApiStatus.LOADING

            var instead = false
            var fetchNewData = false

            if (databaseDao.getSoonestForecastDate() == null) { // Database empty?
                Log.d(TAG, "Forecast Weather database returned null!")
                fetchNewData = true
            } else if (isNewForecastQuery) { // Is this just a refresh or a new query?
                fetchNewData = true
            } else if (hasEnoughTimePassedForecast(databaseDao.getSoonestForecastDate()!!)) { // Is data recent?
                fetchNewData = true
            }
            if (fetchNewData) { // Get new data from the network
                Log.d(TAG, "Fetching new Forecast data from the network")
                try {
                    _forecastWeatherResponse =
                        OpenWeatherMapApi.retrofitService.getFiveDayForecastResponse(
                            lat = lat,
                            lon = lon,
                            units = UNITS_METRIC,
                            appid = APIKEY
                        )
                } catch (e: Exception) {
                    _weatherApiStatus.value = WeatherApiStatus.ERROR
                    Log.d(TAG, "Error in forecast weather network call!")
                    e.printStackTrace()
                    // Snackbar notifying of network error
                    onError()
                    // Fetch from database instead
                    instead = true
                    fetchForecastWeatherFromDatabase()
                }
                if (_forecastWeatherResponse != null) {
                    if (!instead) { // Don't do this twice
                        // Setup displaying the data
                        _forecastWeatherDisplayable = setupForecastDisplayable()
                        _weatherCity.value = setupCity()
                        _sortedForecastDisplayable.value =
                            separateByDate(_forecastWeatherDisplayable!!)
                        setupForecastWholeDays()
                        onCancelShowFocused() // Make sure we're not displaying focused view
                        _weatherApiStatus.value = WeatherApiStatus.DONE
                        // Write the new data to the database
                        // First, clear the old data from the Forecast database
                        databaseDao.clearForecastDatabase()
                        Log.d(TAG, "Clear forecast database called")
                        // Clear the old data from the City database
                        databaseDao.clearCityDatabase()
                        Log.d(TAG, "Clear city database called")

                        // Convert the city network data to database data
                        val databaseCity = CoroutineScope(Dispatchers.Default).async {
                            translateDatabaseFromCity(_forecastWeatherResponse!!.city!!)
                        }.await()
                        // Insert the City item into the database
                        databaseDao.insertCity(databaseCity)
                        Log.d(TAG, "Insert city database called")

                        // Convert the network forecast data to database data
                        val databaseForecast = CoroutineScope(Dispatchers.Default).async {
                            translateDatabaseFromForecast(_forecastWeatherResponse!!)
                        }.await()
                        // Insert all the database Forecast items in the list into the database
                        for (databasePeriod in databaseForecast) {
                            databaseDao.insertForecast(databasePeriod)
                        }
                        Log.d(TAG, "Insert all the forecast data called")
                    }
                }
            } else { // Use the data in the database
                fetchForecastWeatherFromDatabase()
                // Setup displaying the data
                if (_forecastWeatherResponse != null) {
                    _forecastWeatherDisplayable = setupForecastDisplayable()
                    _weatherCity.value = setupCity()
                    _sortedForecastDisplayable.value = separateByDate(_forecastWeatherDisplayable!!)
                    setupForecastWholeDays()
                    onCancelShowFocused() // Make sure we're not displaying focused view
                }
                _weatherApiStatus.value = WeatherApiStatus.DONE
            }
            isNewForecastQuery = false // Reset isNewQuery
        }
    }

    suspend fun fetchCurrentWeatherFromDatabase() {
        _weatherApiStatus.value = WeatherApiStatus.LOADING
        // Get the data from the database
        val fromDatabaseCurrent = databaseDao.getCurrentWeatherDatabaseData()
        Log.d(TAG, "Fetching current weather data from database")
        // Convert the database data to network-style data
        if (fromDatabaseCurrent != null) {
            _currentWeatherResponse = CoroutineScope(Dispatchers.Default).async {
                translateCurrentWeatherFromDatabase(fromDatabaseCurrent)
            }.await()
        }
        if (_currentWeatherResponse != null) {
            // Setup display data
            setupCurrentDisplayable()
            _weatherApiStatus.value = WeatherApiStatus.DONE
        }
    }

    suspend fun fetchForecastWeatherFromDatabase() {
        _weatherApiStatus.value = WeatherApiStatus.LOADING
        // Get the data from the database
        val databaseCity = databaseDao.getCityDatabaseData()
        val databaseForecastList = databaseDao.getForecastDatabaseData()
        Log.d(TAG, "Fetching city and forecast data from the database")

        // Convert the database city to network-style city so we can use it
        if (databaseCity != null) {
            val networkCity = CoroutineScope(Dispatchers.Default).async {
                translateCityFromDatabase(databaseCity)
            }.await()
            // Convert the database forecast to network-style forecast and set _forecastWeatherResponse
            _forecastWeatherResponse = CoroutineScope(Dispatchers.Default).async {
                translateForecastFromDatabase(networkCity, databaseForecastList)
            }.await()
        }
        if (_forecastWeatherResponse != null) {
            // Setup displaying the data
            _forecastWeatherDisplayable = setupForecastDisplayable()
            _weatherCity.value = setupCity()
            _sortedForecastDisplayable.value = separateByDate(_forecastWeatherDisplayable!!)
            setupForecastWholeDays()
            onCancelShowFocused() // Make sure we're not displaying focused view
            _weatherApiStatus.value = WeatherApiStatus.DONE
        }
    }


    private fun hasEnoughTimePassedCurrent(displayingTime: Long): Boolean {
        val now = System.currentTimeMillis() / 1000
        return ((now - displayingTime) >= 1800) // Half hour
    }

    private fun hasEnoughTimePassedForecast(displayingTime: Long): Boolean {
        val now = System.currentTimeMillis() / 1000
        return ((now - displayingTime) >= 3600) // 1 hour
    }

    // Set the _currentWeatherResponse to the database data
    private suspend fun translateCurrentWeatherFromDatabase(databaseCurrent: CurrentDatabaseEntity): OpenWeatherCurrentModel {

        return OpenWeatherCurrentModel(
            weather = listOf(
                CurrentWeather(
                    id = databaseCurrent.id,
                    main = databaseCurrent.main,
                    description = databaseCurrent.description
                )
            ),
            main = CurrentMainWeather(
                temp = databaseCurrent.temp,
                feelsLike = databaseCurrent.feelsLike,
                pressure = databaseCurrent.pressure,
                humidity = databaseCurrent.humidity,
                tempMin = databaseCurrent.tempMin,
                tempMax = databaseCurrent.tempMax
            ),
            visibility = databaseCurrent.visibility,
            wind = Wind(
                speed = databaseCurrent.speed,
                deg = databaseCurrent.deg,
                gust = databaseCurrent.gust
            ),
            clouds = Cloudiness(
                all = databaseCurrent.clouds
            ),
            rain = Precipitation(
                oneHour = databaseCurrent.rainOneHour,
                threeHours = databaseCurrent.rainThreeHours
            ),
            snow = Precipitation(
                oneHour = databaseCurrent.snowOneHour,
                threeHours = databaseCurrent.snowThreeHours
            ),
            dt = databaseCurrent.dt,
            sys = Sys(
                sunrise = databaseCurrent.sunrise,
                sunset = databaseCurrent.sunset
            ),
            timezone = databaseCurrent.timezone,
            name = databaseCurrent.name
        )
    }

    private suspend fun translateDatabaseFromCurrentWeather(
        openWeatherCurrentModel: OpenWeatherCurrentModel
    ): CurrentDatabaseEntity {
        return CurrentDatabaseEntity(
            dt = openWeatherCurrentModel.dt!!,
            id = openWeatherCurrentModel.weather!![0]!!.id,
            main = openWeatherCurrentModel.weather[0]!!.main,
            description = openWeatherCurrentModel.weather[0]!!.description,
            temp = openWeatherCurrentModel.main!!.temp,
            feelsLike = openWeatherCurrentModel.main.feelsLike,
            pressure = openWeatherCurrentModel.main.pressure,
            humidity = openWeatherCurrentModel.main.humidity,
            tempMin = openWeatherCurrentModel.main.tempMin,
            tempMax = openWeatherCurrentModel.main.tempMax,
            visibility = openWeatherCurrentModel.visibility,
            speed = openWeatherCurrentModel.wind?.speed,
            deg = openWeatherCurrentModel.wind?.deg,
            gust = openWeatherCurrentModel.wind?.gust,
            clouds = openWeatherCurrentModel.clouds?.all,
            rainOneHour = openWeatherCurrentModel.rain?.oneHour,
            rainThreeHours = openWeatherCurrentModel.rain?.threeHours,
            snowOneHour = openWeatherCurrentModel.snow?.oneHour,
            snowThreeHours = openWeatherCurrentModel.snow?.threeHours,
            sunrise = openWeatherCurrentModel.sys?.sunrise,
            sunset = openWeatherCurrentModel.sys?.sunset,
            timezone = openWeatherCurrentModel.timezone,
            name = openWeatherCurrentModel.name
        )
    }

    // Convert database City data to network data
    private suspend fun translateCityFromDatabase(cityDatabase: CityDatabaseEntity): City {
        return City(
            name = cityDatabase.name,
            coord = Coordinates(null, null),
            country = null,
            timezone = cityDatabase.timezone,
            sunrise = cityDatabase.sunrise!!,
            sunset = cityDatabase.sunset!!
        )
    }

    // Convert network City data to Database City data
    private suspend fun translateDatabaseFromCity(city: City): CityDatabaseEntity {
        return CityDatabaseEntity(
            name = city.name,
            timezone = city.timezone,
            sunrise = city.sunrise,
            sunset = city.sunset
        )
    }

    // Set _forecastWeatherResponse from the database data
    private suspend fun translateForecastFromDatabase(
        city: City,
        databaseForecastList: List<ForecastDatabaseEntity>
    ): OpenWeatherForecastModel {
        val forecastList = mutableListOf<ForecastPeriod>()
        for (period in databaseForecastList) {
            val forecastPeriod = ForecastPeriod(
                dt = period.dt,
                main = MainWeather(
                    temp = period.temp,
                    feelsLike = period.feelsLike,
                    tempMin = period.tempMin,
                    tempMax = period.tempMax,
                    humidity = period.humidity
                ),
                weather = listOf(
                    Weather(
                        id = period.id,
                        main = period.main,
                        description = period.description
                    )
                ),
                clouds = Cloudiness(
                    all = period.clouds
                ),
                wind = Wind(
                    speed = period.speed,
                    deg = period.deg,
                    gust = period.gust
                ),
                visibility = period.visibility,
                pop = period.pop,
                rain = Precipitation(
                    oneHour = period.rainOneHour,
                    threeHours = period.rainThreeHours
                ),
                snow = Precipitation(
                    oneHour = period.snowOneHour,
                    threeHours = period.snowThreeHours
                ),
                sys = PartOfDay(
                    pod = period.partOfDay
                ),
                dtTxt = null
            )
            forecastList.add(forecastPeriod)
        }
        return OpenWeatherForecastModel(
            list = forecastList,
            city = city
        )
    }

    private suspend fun translateDatabaseFromForecast(networkForecast: OpenWeatherForecastModel): List<ForecastDatabaseEntity> {
        val databaseForecasts = mutableListOf<ForecastDatabaseEntity>()
        for (period in networkForecast.list!!) {
            val forecastPeriod = ForecastDatabaseEntity(
                dt = period.dt!!,
                temp = period.main!!.temp,
                feelsLike = period.main.feelsLike,
                tempMin = period.main.tempMin,
                tempMax = period.main.tempMax,
                humidity = period.main.humidity,
                id = period.weather!![0].id,
                main = period.weather[0].main,
                description = period.weather[0].description,
                clouds = period.clouds?.all,
                speed = period.wind?.speed,
                deg = period.wind?.deg,
                gust = period.wind?.gust,
                visibility = period.visibility,
                pop = period.pop,
                rainOneHour = period.rain?.oneHour,
                rainThreeHours = period.rain?.threeHours,
                snowOneHour = period.snow?.oneHour,
                snowThreeHours = period.snow?.threeHours,
                partOfDay = period.sys?.pod
            )
            databaseForecasts.add(forecastPeriod)
        }
        return databaseForecasts
    }


    // Return the displayable data ready for the viewModel
    private fun setupForecastDisplayable(): List<ForecastDisplayableModel> {
        // Create a list of ForeCastDisplayableModel
        val displayables = mutableListOf<ForecastDisplayableModel>()
        // Map out the response data to the displayable type list
        for (item in _forecastWeatherResponse!!.list!!) {
            // Create a Date object with the time zone offset applied, so as to display the
            // time/date in the local time of the forecast location
            val itemDate = Date((item.dt!! + _forecastWeatherResponse!!.city!!.timezone!!) * 1000L)
            displayables.add(
                ForecastDisplayableModel(
                    date = setupDateFormat(DATE_SKELETON).format(itemDate),
                    time = setupDateFormat(TIME_SKELETON).format(itemDate),
                    pod = item.sys!!.pod!!,
                    code = item.weather!![0].id,
                    iconResId = selectWeatherIndividualIcon(item.weather[0].id, item.sys.pod!!),
                    description = item.weather[0].description,
                    temp = item.main!!.temp!!,
                    feelsLike = item.main.feelsLike!!,
                    humidity = item.main.humidity!!,
                    cloudiness = item.clouds!!.all!!,
                    windSpeed = item.wind!!.speed!!,
                    windGust = if (item.wind.gust != null) {
                        item.wind.gust
                    } else {
                        null
                    },
                    rain = if (item.rain != null) {
                        Precip(item.rain.oneHour, item.rain.threeHours)
                    } else {
                        null
                    },
                    snow = if (item.snow != null) {
                        Precip(item.snow.oneHour, item.snow.threeHours)
                    } else {
                        null
                    },
                    visibility = item.visibility!!,
                    pop = (item.pop!! * 100).toInt()
                )
            )
        }
        return displayables
    }

    // Return the city data
    private fun setupCity(): WeatherCity {
        return WeatherCity(
            name = _forecastWeatherResponse!!.city!!.name,
            timezone = _forecastWeatherResponse!!.city!!.timezone,
            sunrise = _forecastWeatherResponse!!.city!!.sunrise!!,
            sunset = _forecastWeatherResponse!!.city!!.sunset!!
        )
    }


    // Create an appropriate DateFormat. Use UTC timezone to enable easily compensating for local time
    private fun setupDateFormat(skel: String): SimpleDateFormat {
        val skeleton = DateFormat.getBestDateTimePattern(Locale.getDefault(), skel)
        val zoned = SimpleDateFormat(skeleton, Locale.getDefault())
        zoned.timeZone = TimeZone.getTimeZone("UTC")
        return zoned
    }

    private fun selectWeatherIndividualIcon(weatherCode: Int, pod: String): Int {
        @DrawableRes val resId =
            when (weatherCode) {
                in 200..232 -> R.drawable.tstorm
                in 300..321 -> R.drawable.drizzle
                500 -> R.drawable.rain_light
                501, 520, 521 -> R.drawable.rain
                502, 503, 504, 522, 531 -> R.drawable.rain_heavy
                511 -> R.drawable.freezing_rain
                600 -> R.drawable.flurries
                612, 615, 620 -> R.drawable.snow_light
                601, 611, 613, 616, 621 -> R.drawable.snow
                602, 622 -> R.drawable.snow_heavy
                701, 731, 761 -> R.drawable.fog_light
                711, 721, 741, 751, 762, 771 -> R.drawable.fog
                781 -> R.drawable.warning_weather
                800 -> if (pod == "n") {
                    R.drawable.clear_night
                } else {
                    R.drawable.clear_day
                }

                801 -> if (pod == "n") {
                    R.drawable.mostly_clear_night
                } else {
                    R.drawable.mostly_clear_day
                }

                802 -> if (pod == "n") {
                    R.drawable.partly_cloudy_night
                } else {
                    R.drawable.partly_cloudy_day
                }

                803 -> R.drawable.mostly_cloudy
                804 -> R.drawable.cloudy
                else -> R.drawable.baseline_cloud_off_24
            }
        return resId
    }

    private fun selectBackground(weatherCode: Int): Int {
        @DrawableRes val backResId =
            when(weatherCode) {
                in 200..232 -> R.drawable.lightning
                in 300..531 -> R.drawable.rainy
                in 600..622 -> R.drawable.snowfall_201496
                in 700..771 -> R.drawable.foggy
                781 -> R.drawable.mostcloudysky
                800 -> R.drawable.clearsky
                801 -> R.drawable.partcloudy
                802 -> R.drawable.cloudsky
                803, 804 -> R.drawable.mostcloudysky
                else -> R.drawable.partcloudy
            }
        return backResId
    }


    // Group the forecast periods into a new list of lists organized by date
    private fun separateByDate(periods: List<ForecastDisplayableModel>): List<List<ForecastDisplayableModel>> {

        val dayPeriods = periods.groupBy { it.date }
        val dayPeriodsValues = dayPeriods.values
        val sortedList = mutableListOf<List<ForecastDisplayableModel>>()
        for (thing in dayPeriodsValues) {
            sortedList.add(thing)
        }
        // Return the list of periods grouped by date
        return sortedList
    }

    // Set the whole day values
    private fun setupForecastWholeDays() {
        val wholeDayList = mutableListOf<ForecastWholeDayModel>()
        for (dayList in _sortedForecastDisplayable.value!!) {
            val dayCodes = mutableListOf<Int>()
            var dayCodePod = "d" // Track pod in case only night is present
            // Select that day's weather code (using daytime only)
            for (period in dayList) {
                if (period.pod == "d") {
                    // Add the daytime codes for that day to the list of the day's codes
                    dayCodes.add(period.code)
                    dayCodePod = "d"
                }
            }// period end
            // 'dayCodes' could be empty at this point if the day contained only nighttime periods,
            // so we would need to use the nighttime codes instead.
            if (dayCodes.isEmpty()) {
                for (period in dayList) {
                    // Add the the nighttime codes instead
                    dayCodes.add(period.code)
                    dayCodePod = "n"
                }// period end
            }

            // Get the overall weather code for the day
            val dayCode = selectOverallWeatherCode(dayCodes)

            // Calculate all the values for the day
            val date = dayList[0].date
            val icon = selectWeatherIndividualIcon(dayCode, dayCodePod)
            val descrip = dayList.first { it.code == dayCode }.description
            val high = dayList.maxBy { it.temp }.temp
            val low = dayList.minBy { it.temp }.temp
            // Set the hi and lo F and C temperatures
            val bigSmall = determineFAndCAndString(high, low)
            // Set the symbol to C or F and km/h or mph
            determineUnitSymbols()
            val wind = dayList.maxBy { it.windSpeed }.windSpeed
            // Gust might be null
            val gust = if (dayList[0].windGust != null) {
                determineWindSpeedsAndString(dayList.maxBy { it.windGust!! }.windGust!!)
            } else {
                null
            }
            // Set the wind as metric or imperial
            val windSpeed = determineWindSpeedsAndString(wind)
            // Get the highest pop for the daytime, or night if no daytime periods present
            val pop = if (dayCodePod == "d") {
                dayList.filter { it.pod == "d" }.maxBy { it.pop }.pop
            } else {
                dayList.maxBy { it.pop }.pop
            }

            // Initialize a WholeDay object with them and add it to the list
            wholeDayList.add(
                ForecastWholeDayModel(
                    date = date,
                    iconResId = icon,
                    dayDescription = descrip,
                    tempHiBig = bigSmall.bigFirst,
                    tempHiSmall = bigSmall.smallFirst,
                    tempLoBig = bigSmall.bigSecond,
                    tempLoSmall = bigSmall.smallSecond,
                    symbolBig = symbolBig!!,
                    symbolSmall = symbolSmall!!,
                    maxWind = windSpeed,
                    maxGust = gust,
                    maxPop = pop,
                    speedSymbol = speedSymbol!!
                )
            )
        }// day end
        _forecastWholeDaysDisplayable.value = wholeDayList
    }


    private fun selectOverallWeatherCode(dayCodeList: List<Int>): Int {
        /**
         * Rank condition groups from most to least significant and set 'trigger' to
         * the first one (most significant) it finds by breaking out of the loop as
         * soon as 'trigger' is not null
         */
        var trigger: Int? = null
        while (trigger == null) {
            // Tornado or Squall?
            trigger = dayCodeList.sorted().lastOrNull { it in 771..781 }
            if (trigger != null) {
                break
            }
            // Thunderstorm?
            trigger = dayCodeList.sorted().lastOrNull { it in 200..232 }
            if (trigger != null) {
                break
            }
            // Snow?
            trigger = dayCodeList.sorted().lastOrNull { it in 600..622 }
            if (trigger != null) {
                break
            }
            // Rain?
            trigger = dayCodeList.sorted().lastOrNull { it in 500..531 }
            if (trigger != null) {
                break
            }
            // Drizzle?
            trigger = dayCodeList.sorted().lastOrNull { it in 300..321 }
            if (trigger != null) {
                break
            }
            // Atmosphere?
            trigger = dayCodeList.sorted().lastOrNull { it in 701..762 }
            if (trigger != null) {
                break
            }
            // Clouds?
            trigger = dayCodeList.sorted().lastOrNull { it in 801..804 }
            if (trigger != null) {
                break
            }
            // Clear?
            trigger = dayCodeList.sorted().lastOrNull { it == 800 }
            if (trigger != null) {
                break
            }
            // Don't loop infinitely in case something went wrong
            break
        }
        return trigger ?: 800
    }


    private fun determineFAndCAndString(networkHigh: Double, networkLow: Double): BigSmall {
        val firstF = ((networkHigh * 9 / 5) + 32).toInt().toString()
        val secondF = ((networkLow * 9 / 5) + 32).toInt().toString()

        val firstC = networkHigh.toInt().toString()
        val secondC = networkLow.toInt().toString()
        return if (isMetric) {
            BigSmall(
                bigFirst = firstC,
                bigSecond = secondC,
                smallFirst = firstF,
                smallSecond = secondF
            )
        } else {
            BigSmall(
                bigFirst = firstF,
                bigSecond = secondF,
                smallFirst = firstC,
                smallSecond = secondC
            )
        }
    }

    private fun determineWindSpeedsAndString(networkWind: Double): String {
        val windMph = (networkWind / 1.609).trimToTwoDecimals()
        // val gustMph = (networkGust / 1.609).trimToTwoDecimals()

        val windKm = networkWind.trimToTwoDecimals()
        //  val gustKm = networkGust.trimToTwoDecimals()
        return if (isMetric) {
            windKm
        } else {
            windMph
        }
    }

    private fun determinePrecipAndString(networkThreeHour: Double?): String? {
        val precipInches = (networkThreeHour?.div(25.4))?.trimToTwoDecimals()
        val precipMm = networkThreeHour?.trimToTwoDecimals()

        return if (isMetric) {
            precipMm
        } else {
            precipInches
        }
    }

    private fun determineVisibilityAndString(networkVis: Int): String {
        val visMi = ((networkVis / 1000) / 1.609).toInt().toString()
        val visKm = (networkVis / 1000).toString()
        return if (isMetric) {
            visKm
        } else {
            visMi
        }
    }


    private fun determineUnitSymbols() {
        if (isMetric) {
            symbolBig = "C"
            symbolSmall = "F"
            speedSymbol = "km/h"
            distanceSymbol = "Km"
            precipSymbol = "mm"
        } else {
            symbolBig = "F"
            symbolSmall = "C"
            speedSymbol = "mph"
            distanceSymbol = "Mi"
            precipSymbol = "in"
        }
    }

    fun switchUnits(newIsMetric: Boolean) {
        isMetric = newIsMetric
        // Renew the whole List
        setupForecastWholeDays()
        setupCurrentDisplayable()
        setupPeriodDisplayable()
    }


    private fun setupPeriodDisplayable() {

        val dayPeriodsHolder = mutableListOf<ForecastPeriodModel>()
        for (period in _sortedForecastDisplayable.value!![dayIndex]) {
            val bigSmall = determineFAndCAndString(period.temp, period.feelsLike)
            val gust = if (period.windGust != null) {
                determineWindSpeedsAndString(period.windGust)
            } else {
                null
            }
            val rain: String? = if (period.rain?.threeHour != null) {
                determinePrecipAndString(period.rain.threeHour)
            } else {
                null
            }
            val snow: String? = if (period.snow?.threeHour != null) {
                determinePrecipAndString(period.snow.threeHour)
            } else {
                null
            }
            determineUnitSymbols()
            // Add the period to the day's list
            dayPeriodsHolder.add(
                ForecastPeriodModel(
                    dayIndex = dayIndex,
                    time = period.time,
                    date = period.date,
                    iconResId = period.iconResId,
                    description = period.description,
                    tempBig = bigSmall.bigFirst,
                    tempSmall = bigSmall.smallFirst,
                    feelsLikeBig = bigSmall.bigSecond,
                    feelsLikeSmall = bigSmall.smallSecond,
                    humidity = period.humidity.toString(),
                    cloudiness = period.cloudiness.toString(),
                    windSpeed = determineWindSpeedsAndString(period.windSpeed),
                    windGust = gust,
                    symbolBig = symbolBig!!,
                    symbolSmall = symbolSmall!!,
                    speedSymbol = speedSymbol!!,
                    distanceSymbol = distanceSymbol!!,
                    rain = rain,
                    snow = snow,
                    precipSymbol = precipSymbol!!,
                    visibility = determineVisibilityAndString(period.visibility),
                    pop = period.pop.toString(),
                    name = null,
                    background = null
                )
            )
        }
        _focusedForecastDay.value = dayPeriodsHolder
    }

    private fun setupCurrentDisplayable() {
        val currentWeatherDate =
            Date((_currentWeatherResponse!!.dt!! + _currentWeatherResponse!!.timezone!!) * 1000L)
        val bigSmall = determineFAndCAndString(
            _currentWeatherResponse!!.main!!.temp!!,
            _currentWeatherResponse!!.main!!.feelsLike!!
        )
        val wind = determineWindSpeedsAndString(
            _currentWeatherResponse!!.wind!!.speed!!,
        )
        val gust = if (_currentWeatherResponse!!.wind!!.gust != null) {
            determineWindSpeedsAndString(_currentWeatherResponse!!.wind!!.gust!!)
        } else {
            null
        }
        val rain: String? = if (_currentWeatherResponse!!.rain != null) {
            determinePrecipAndString(_currentWeatherResponse!!.rain!!.threeHours)
        } else {
            null
        }
        val snow: String? = if (_currentWeatherResponse!!.snow != null) {
            determinePrecipAndString(_currentWeatherResponse!!.rain!!.threeHours)
        } else {
            null
        }
        val back = selectBackground(_currentWeatherResponse!!.weather!![0]!!.id!!)
        determineUnitSymbols()

        _currentWeatherDisplayable.value = ForecastPeriodModel(
            dayIndex = 0, // Arbitrarily set to zero since it's not needed here
            time = setupDateFormat(TIME_SKELETON).format(currentWeatherDate),
            date = setupDateFormat(DATE_SKELETON).format(currentWeatherDate),
            iconResId = selectWeatherIndividualIcon(
                _currentWeatherResponse!!.weather!![0]!!.id!!, determineCurrentPod()
            ),
            description = _currentWeatherResponse!!.weather!![0]!!.description!!,
            tempBig = bigSmall.bigFirst,
            tempSmall = bigSmall.smallFirst,
            feelsLikeBig = bigSmall.bigSecond,
            feelsLikeSmall = bigSmall.smallSecond,
            humidity = _currentWeatherResponse!!.main!!.humidity.toString(),
            cloudiness = _currentWeatherResponse!!.clouds!!.all.toString(),
            windSpeed = wind,
            windGust = gust,
            symbolBig = symbolBig!!,
            symbolSmall = symbolSmall!!,
            speedSymbol = speedSymbol!!,
            distanceSymbol = distanceSymbol!!,
            rain = rain,
            snow = snow,
            precipSymbol = precipSymbol!!,
            visibility = determineVisibilityAndString(_currentWeatherResponse!!.visibility!!),
            pop = "n/a", // Arbitrary string; current weather has no POP
            name = _currentWeatherResponse!!.name,
            background = back
        )

    }

    private fun determineCurrentPod(): String {
        return if (_currentWeatherResponse!!.dt!! in _currentWeatherResponse!!.sys!!.sunrise!!.._currentWeatherResponse!!.sys!!.sunset!!) {
            "d"
        } else {
            "n"
        }
    }


    data class BigSmall(
        val bigFirst: String,
        val smallFirst: String,
        val bigSecond: String,
        val smallSecond: String
    )


}