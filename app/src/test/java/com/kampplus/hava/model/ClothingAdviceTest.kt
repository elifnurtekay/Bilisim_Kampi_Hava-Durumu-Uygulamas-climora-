package com.kampplus.hava.model

import java.time.LocalDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClothingAdviceTest {
    private val now = LocalDateTime.of(2026, 10, 25, 14, 30)
    private fun forecast(feels: Double, code: Int = 0, hours: List<HourlyForecast> = emptyList()) = Forecast(
        CurrentWeather(25.0, code, now, feels),
        hours,
        emptyList()
    )

    @Test fun `hissedilen sicaklik kiyafet secimini belirler`() {
        assertTrue(forecast(3.0).clothingAdvice().first().contains("Kalın mont"))
        assertTrue(forecast(30.0).clothingAdvice().first().contains("Açık renkli"))
    }

    @Test fun `gecmis yagis semsiye onerisine neden olmaz`() {
        val hours = listOf(HourlyForecast(now.minusHours(3), 15.0, 61, 100))
        assertFalse(forecast(18.0, hours = hours).clothingAdvice().any { it.contains("Şemsiye") })
    }

    @Test fun `gelecek yagis ve mevcut yagis semsiye onerir`() {
        val hours = listOf(HourlyForecast(now.plusHours(2), 15.0, 61, 60))
        assertTrue(forecast(18.0, hours = hours).clothingAdvice().any { it.contains("Şemsiye") })
        assertTrue(forecast(18.0, code = 61).clothingAdvice().any { it.contains("Şemsiye") })
    }

    @Test fun `kar icin bot onerilir`() {
        assertTrue(forecast(-2.0, code = 75).clothingAdvice().any { it.contains("bot") })
    }
}
