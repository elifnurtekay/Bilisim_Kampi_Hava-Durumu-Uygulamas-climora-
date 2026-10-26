package com.kampplus.hava.data

import kotlinx.coroutines.CancellationException

interface CityWeatherTool {
    suspend fun lookup(city: String, countryCode: String?, region: String?): String
}

class OpenMeteoCityWeatherTool(
    private val cities: CityRepository = OpenMeteoCityRepository(),
    private val weather: WeatherRepository = OpenMeteoWeatherRepository()
) : CityWeatherTool {
    override suspend fun lookup(city: String, countryCode: String?, region: String?): String {
        if (city.isBlank() || city.length > 100) return "Geçerli bir şehir adı gerekli. Hava verisi yok."
        return try {
            val matches = cities.search(city).filter {
                (countryCode.isNullOrBlank() || it.countryCode.equals(countryCode, ignoreCase = true)) &&
                    (region.isNullOrBlank() || it.admin1.equals(region, ignoreCase = true))
            }.distinctBy { it.id }
            when {
                matches.isEmpty() -> "Şehir bulunamadı; kullanıcıdan şehir ve ülke bilgisini iste. Hava verisi yok."
                matches.size > 1 ->
                    "Birden fazla eşleşme var. Hava verisi YOK; kullanıcıya hangisi olduğunu sor: " +
                        matches.take(5).joinToString("; ") { "${it.name}, ${it.subtitle} (${it.countryCode})" }
                else -> matches.single().let {
                    weatherChatContext("${it.name}, ${it.subtitle}", weather.getForecast(it.latitude, it.longitude))
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            "Bu şehir için güncel hava verisi alınamadı. Tahmin uydurma; yeniden denemesini iste."
        }
    }
}
