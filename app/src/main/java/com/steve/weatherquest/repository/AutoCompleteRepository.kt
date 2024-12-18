package com.steve.weatherquest.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.steve.weatherquest.BuildConfig
import com.steve.weatherquest.models.AutoCompleteResponseModel
import com.steve.weatherquest.network.HereAutoCompleteApi
import com.steve.weatherquest.ui.theme.TextOnHighlightDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

private const val APIKEY = BuildConfig.HERE_API_KEY

@Singleton
class AutoCompleteRepository {

    private val TAG = "AutoCompleteRepository"

    private val highlightStyle = SpanStyle(
        fontStyle = FontStyle.Normal,
        color = TextOnHighlightDarkTheme,
        background = Color.Yellow
    )
    private val type = "city"

    private var _autoCompleteResponse: AutoCompleteResponseModel? = null
    val autoCompleteResponse get() = _autoCompleteResponse


    suspend fun callAutoComplete(qry: String, onError: () -> Unit): List<AnnotatedString> {
        withContext(Dispatchers.IO) {
            try {
                _autoCompleteResponse =
                    HereAutoCompleteApi.retrofitService.getHereAutoCompleteResponse(
                        q = qry,
                        types = type,
                        apiKey = APIKEY
                    )
            } catch (e: Exception) {
                Log.d(TAG, "Error in autoComplete network call!")
                e.printStackTrace()
                onError()
            }
        }
        return giveAutoCompleteResponseList()
    }


    // Return the autocomplete response suggestions as a list of AnnotatedStrings with appropriate highlights
    fun giveAutoCompleteResponseList(): List<AnnotatedString> {
        if (autoCompleteResponse != null && autoCompleteResponse!!.items.isNotEmpty()) {
            val annotatedTitles = mutableListOf<AnnotatedString>()
            // Iterate through the list of response items to create a list of AnnotatedStrings with highlights
            for (item in autoCompleteResponse!!.items) {
                annotatedTitles.add(
                    buildAnnotatedString {
                        append(item.address.label)
                        // The highlights are actually a list of highlights, so iterate through all of it
                        for (high in item.highlights.address.label) {
                            addStyle(
                                style = highlightStyle,
                                start = high.start,
                                end = high.end
                            )
                        }
                    }
                )
            }
            return annotatedTitles
        } else {
            return listOf()
        }
    }

    fun clearResponse() {
        _autoCompleteResponse = AutoCompleteResponseModel(listOf())
    }

}