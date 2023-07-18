package com.steve.weatherquest.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.steve.weatherquest.R
import com.steve.weatherquest.clearFocusFromHoistedState


@Composable
fun ExpandableSearchBar(
    suggestions: List<AnnotatedString>,
    onSuggestionClick: (Int) -> Unit,
    searchedLocation: String,
    onSearchChange: (String) -> Unit,
    onGpsClick: () -> Unit,
    clearTrigger: Int,
    focusTrigger: Int,
) {

    val focusRequester = remember {
        FocusRequester()
    }
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            // .fillMaxWidth()
            .animateContentSize(),
        tonalElevation = 9.dp
    ) {
        Column {
            var isDroppedDown by remember { mutableStateOf(false) }
            isDroppedDown = suggestions.isNotEmpty()
            TextField(
                value = searchedLocation,
                onValueChange = { onSearchChange(it) },
                placeholder = {
                    Text(
                        text = "Search location",
                        maxLines = 1,
                    )
                },
                maxLines = 1,

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 32.dp, end = 8.dp)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(visible = !isDroppedDown) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_my_location_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { onGpsClick() }
                        )
                    }
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
            AnimatedVisibility(isDroppedDown) {
                Divider(
                    //startIndent = 16.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(suggestions) { suggestion ->
                        SearchSuggestionCard(
                            location = suggestion,
                            onClick = { onSuggestionClick(suggestions.indexOf(suggestion)) }
                        )
                    }
                }
            }

        }
    }
    if (focusTrigger > 0) {
        LaunchedEffect(focusTrigger) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun SearchSuggestionCard(
    location: AnnotatedString,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.outline_add_location_24),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


/**
 * State holder class for the [ExpandableSearchBar]
 */
//class SearchBarUserInputState(private val hint: String, initialText: String) {
//
//    var text by mutableStateOf(initialText)
//
//    val isHint: Boolean
//        get() = text == hint
//
//    /**
//     * Custom saver since rememberSaveable can't save [SearchBarUserInputState] in a bundle
//     */
//    companion object {
//        val Saver: Saver<SearchBarUserInputState, *> = listSaver(
//            save = { listOf(it.hint, it.text) },
//            restore = {
//                SearchBarUserInputState(
//                    hint = it[0],
//                    initialText = it[1]
//                )
//            }
//        )
//    }
//
//}
//
///**
// * Custom rememberer for the custom saver for [SearchBarUserInputState]
// */
//@Composable
//fun rememberSearchBarUserInputState(hint: String): SearchBarUserInputState =
//    rememberSaveable(hint, saver = SearchBarUserInputState.Saver) {
//        SearchBarUserInputState(hint, hint)
//    }


