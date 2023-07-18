package com.steve.weatherquest.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val SETTINGS_DATASTORE_NAME = "weatherSettings"

private val TAG = "SettingsDataStoreRepository"

private val Context.dataStore by preferencesDataStore(SETTINGS_DATASTORE_NAME)

@Singleton
class SettingsDataStoreRepository @Inject constructor(@ApplicationContext appContext: Context) {

    private val settingsDataStore = appContext.dataStore

    // Keys
    private val IS_METRIC_KEY = booleanPreferencesKey("is_metric_key")

    // Writing data
    suspend fun saveIsMetric(desiredIsMetric: Boolean) {
        settingsDataStore.edit { storedIsMetric ->
            storedIsMetric[IS_METRIC_KEY] = desiredIsMetric
        }
    }


    // Reading data
    val weatherSettingsFlow: Flow<WeatherSettings> = settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d(TAG, "Error reading from datastore!", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { settings ->
            mapTheSettings(settings)
        }

    private fun mapTheSettings(preferences: Preferences): WeatherSettings {
        return WeatherSettings(
            isMetric = preferences[IS_METRIC_KEY] ?: true
        )
    }

}

data class WeatherSettings(
    val isMetric: Boolean
)




