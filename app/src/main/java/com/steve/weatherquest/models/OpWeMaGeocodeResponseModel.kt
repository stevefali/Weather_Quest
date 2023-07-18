package com.steve.weatherquest.models

import androidx.room.Entity
import androidx.room.PrimaryKey


// The response is a list of these
@Entity(tableName = "geocode_database_entity")
data class OpWeMaGeocodeResponseModel(
    @PrimaryKey
    val id: Int = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String?,
    val state: String?
)