package com.kampplus.hava.data

import com.kampplus.hava.model.City
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.Forecast
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CityWeatherToolTest {
    private val france = City(1, "Paris", 48.85, 2.35, "FR", "France", "Île-de-France")
    private val texas = City(2, "Paris", 33.66, -95.55, "US", "United States", "Texas")
    private var requested: Pair<Double, Double>? = null
    private val cities = object : CityRepository {
        override suspend fun search(query: String) = listOf(france, texas)
    }
    private val weather = object : WeatherRepository {
        override suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
            requested = latitude to longitude
            return Forecast(CurrentWeather(12.0, 0, LocalDateTime.of(2026, 10, 25, 12, 0)), emptyList(), emptyList())
        }
    }

    @Test fun `ulke filtresi dogru koordinatin havasini getirir`() = runTest {
        val answer = OpenMeteoCityWeatherTool(cities, weather).lookup("Paris", "FR", null)
        assertEquals(48.85 to 2.35, requested)
        assertTrue(answer.contains("12.0"))
    }

    @Test fun `belirsiz sehirde hava uydurulmaz secim istenir`() = runTest {
        val answer = OpenMeteoCityWeatherTool(cities, weather).lookup("Paris", null, null)
        assertEquals(null, requested)
        assertTrue(answer.contains("Birden fazla"))
        assertTrue(answer.contains("Texas"))
    }
}
