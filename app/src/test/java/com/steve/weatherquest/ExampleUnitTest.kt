package com.steve.weatherquest

import com.steve.weatherquest.data.IsoCodes
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun countryCodeConversion_isWorking() {
        val code = IsoCodes().convertThreeToTwoLetter("BRB")
        assertSame("BB", code)
    }
}