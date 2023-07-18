package com.steve.weatherquest.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.steve.weatherquest.clearFocusFromHoistedState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSearchView(
    searchedLocation: String,
    onSearchChange: (String) -> Unit,
    clearTrigger: Int,
    focusTrigger: Int,
) {

    val focusRequester = remember {
        FocusRequester()
    }
    Column {
        TextField(
            value = searchedLocation,
            onValueChange = { onSearchChange(it) },
            placeholder = {
                Text(
                    text = "Search location",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            maxLines = 1,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
            },

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .clearFocusFromHoistedState(
                    clearTrigger = clearTrigger
                )
        )
        if (focusTrigger > 0) {
            LaunchedEffect(focusTrigger) {
                focusRequester.requestFocus()
            }
        }
    }
}
