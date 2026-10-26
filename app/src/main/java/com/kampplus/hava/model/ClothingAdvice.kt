package com.kampplus.hava.model

/** Hissedilen sıcaklık ve gelecek birkaç saatin yağışına dayalı yerel kurallar. */
fun Forecast.clothingAdvice(): List<String> {
    val temperature = current.apparentTemperatureC ?: current.temperatureC
    val tips = mutableListOf(
        when {
            temperature < 5 -> "Kalın mont, atkı ve kapalı ayakkabı iyi bir seçim."
            temperature < 13 -> "Bir mont veya kalın ceketle çık; içine katman ekle."
            temperature < 20 -> "İnce bir ceket ya da hırka gün içinde işine yarar."
            temperature < 27 -> "Hafif bir üst ve rahat, nefes alan kıyafetler seç."
            else -> "Açık renkli, ince ve nefes alan kıyafetlerle daha rahat edersin."
        }
    )
    val upcoming = hourly.filter { !it.time.isBefore(current.observedAt.withMinute(0).withSecond(0)) }.take(6)
    val wet = current.weatherCode in 51..67 || current.weatherCode in 80..82 || current.weatherCode >= 95
    if (wet || upcoming.any { (it.precipitationProbability ?: 0) >= 40 }) tips += "Şemsiyeni al; suya dayanıklı bir dış katman da iyi olur."
    if (current.weatherCode in 71..77 || current.weatherCode in 85..86) tips += "Kar için kaymayan tabanlı, suya dayanıklı bot tercih et."
    if ((current.windSpeedKmh ?: 0.0) >= 25) tips += "Rüzgâr belirgin; rüzgâr kesen bir ceket tercih et."
    if (current.isDay && (daily.firstOrNull()?.uvIndex ?: 0.0) >= 3) tips += "Güneş gözlüğü ve şapka da yanına al."
    return tips
}
