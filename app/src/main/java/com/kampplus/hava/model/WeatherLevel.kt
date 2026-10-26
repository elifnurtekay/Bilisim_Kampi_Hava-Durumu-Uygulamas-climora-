package com.kampplus.hava.model

data class WeatherLevel(val label: String, val fraction: Float, val explanation: String)

fun uvLevel(value: Double?): WeatherLevel? = value?.takeIf { it.isFinite() && it >= 0 }?.let {
    WeatherLevel(
        when {
            it < 3 -> "Düşük"
            it < 6 -> "Orta"
            it < 8 -> "Yüksek"
            it < 11 -> "Çok yüksek"
            else -> "Aşırı"
        },
        (it / 12).toFloat().coerceIn(0f, 1f),
        if (it < 3) {
            "3’ün altı düşük · Günün en yüksek UV tahmini."
        } else {
            "3 ve üzeri: gölge, şapka ve güneş koruması önerilir."
        }
    )
}

fun pressureLevel(value: Double?): WeatherLevel? = value?.takeIf { it.isFinite() && it > 0 }?.let {
    WeatherLevel(
        when {
            it < 1000 -> "Düşük"
            it <= 1020 -> "Normal aralık"
            else -> "Yüksek"
        },
        ((it - 980) / 60).toFloat().coerceIn(0f, 1f),
        "Deniz seviyesine göre. Yaklaşık 1000–1020 hPa normal aralık; tek başına yağış veya tehlike göstergesi değildir."
    )
}
