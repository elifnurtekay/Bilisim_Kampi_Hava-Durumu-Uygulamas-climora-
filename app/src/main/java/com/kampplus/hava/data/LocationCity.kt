package com.kampplus.hava.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.kampplus.hava.model.City
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

suspend fun locationCity(context: Context, location: Location): City {
    val address = try {
        withTimeoutOrNull(8_000) {
            if (!Geocoder.isPresent()) return@withTimeoutOrNull null
            val geocoder = Geocoder(context, Locale.forLanguageTag("tr"))
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine<Address?> { continuation ->
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                            }
                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                }
            }
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }
    val district = address?.subAdminArea?.takeIf { it.isNotBlank() }
        ?: address?.locality?.takeIf { it.isNotBlank() }
    val province = address?.adminArea?.takeIf { it.isNotBlank() }
    val name = listOfNotNull(district, province).distinct().joinToString(" / ").ifBlank { "Bulunduğum konum" }
    val accuracy = if (location.hasAccuracy()) "Yaklaşık ±${location.accuracy.toInt()} m" else "Cihaz konumu"
    val detail = if (address == null) "Şehir/ilçe adı bulunamadı • $accuracy" else accuracy
    return City(
        -1,
        name,
        location.latitude,
        location.longitude,
        address?.countryCode,
        listOfNotNull(address?.countryName, detail).joinToString(" • "),
        null
    )
}
