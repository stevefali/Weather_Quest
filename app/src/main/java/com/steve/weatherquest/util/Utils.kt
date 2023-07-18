package com.steve.weatherquest.util

import java.util.*


fun String.capitalizeEachWord(): String {
    return this.trim().split("\\s+".toRegex())
        .joinToString(" ") { it -> it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
}



// Remove trailing zeros
fun Double.trimToTwoDecimals(): String {
    return String.format("%.2f", this)
}