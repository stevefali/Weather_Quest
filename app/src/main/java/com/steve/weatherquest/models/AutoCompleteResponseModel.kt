package com.steve.weatherquest.models


// Main response object is an array of Item objects
data class AutoCompleteResponseModel(
    val items: List<Item>
)


// Items object
data class Item(
    val title: String,
    val localityType: String?,
    val address: HereAddress,
    val highlights: Highlights
)


data class HereAddress(
    val label: String,
    val countryCode: String,
    val countryName: String,
    val stateCode: String?,
    val state: String?,
    val city: String?,
)

// The highlights object consists of lists of highlight vectors
data class Highlights(
    val title: List<HighlightVector>,
    val address: AddressHighlight
)

data class HighlightVector(
    val start: Int,
    val end: Int
)

data class AddressHighlight(
    val label: List<HighlightVector>
)