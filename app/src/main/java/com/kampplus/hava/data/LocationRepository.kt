package com.kampplus.hava.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.kampplus.hava.model.City
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

interface LocationRepository {
    suspend fun locate(): City?
}

class AndroidLocationRepository(private val context: Context) : LocationRepository {
    override suspend fun locate(): City? {
        val location = withTimeoutOrNull(20_000) { currentLocation(context) } ?: return null
        return locationCity(context, location)
    }
}

@Suppress("MissingPermission")
private suspend fun currentLocation(context: Context): Location? = suspendCancellableCoroutine { continuation ->
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val providers = listOfNotNull(
        LocationManager.NETWORK_PROVIDER,
        LocationManager.GPS_PROVIDER.takeIf {
            fine
        }
    ).filter { manager.isProviderEnabled(it) }
    if (providers.isEmpty()) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }
    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            manager.removeUpdates(this)
            if (continuation.isActive) continuation.resume(location)
        }
        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit

        @Deprecated("Legacy Android callback")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }
    continuation.invokeOnCancellation { manager.removeUpdates(listener) }
    try {
        providers.forEach { manager.requestLocationUpdates(it, 0L, 0f, listener, Looper.getMainLooper()) }
    } catch (error: Exception) {
        manager.removeUpdates(listener)
        if (continuation.isActive) continuation.resume(null)
    }
}
