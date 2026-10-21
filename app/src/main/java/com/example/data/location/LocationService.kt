package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.api.GeocodingApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class DeviceCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
)

sealed interface LocationStatus {
    object Initial : LocationStatus
    object RequestingPermission : LocationStatus
    object Locating : LocationStatus
    data class Success(val coordinates: DeviceCoordinates, val locationName: String) : LocationStatus
    data class PermissionDenied(val message: String = "Location permission denied") : LocationStatus
    data class Unavailable(val message: String = "Location services unavailable") : LocationStatus
}

class LocationService(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    suspend fun getRealDeviceLocation(): DeviceCoordinates? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission() || locationManager == null) {
            return@withContext null
        }

        try {
            // 1. Try to get the best last known location
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: Location? = null
            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLocation == null || loc.accuracy < bestLocation.accuracy || loc.time > bestLocation.time) {
                                bestLocation = loc
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    // Ignore and try next
                } catch (e: Exception) {
                    // Ignore
                }
            }

            // If a recent location (within 10 minutes) with reasonable accuracy is found, use it
            if (bestLocation != null && (System.currentTimeMillis() - bestLocation.time < 600_000)) {
                return@withContext DeviceCoordinates(
                    latitude = bestLocation.latitude,
                    longitude = bestLocation.longitude,
                    accuracy = bestLocation.accuracy
                )
            }

            // 2. Request a live single-shot location update with a 6-second timeout
            val liveLoc = withTimeoutOrNull(6000L) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            try {
                                locationManager.removeUpdates(this)
                            } catch (e: Exception) {}
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }

                    try {
                        val enabledProvider = when {
                            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                            else -> null
                        }

                        if (enabledProvider != null) {
                            locationManager.requestSingleUpdate(
                                enabledProvider,
                                listener,
                                Looper.getMainLooper()
                            )
                        } else {
                            continuation.resume(bestLocation)
                        }
                    } catch (e: SecurityException) {
                        continuation.resume(bestLocation)
                    } catch (e: Exception) {
                        continuation.resume(bestLocation)
                    }

                    continuation.invokeOnCancellation {
                        try {
                            locationManager.removeUpdates(listener)
                        } catch (e: Exception) {}
                    }
                }
            }

            val finalLocation = liveLoc ?: bestLocation
            if (finalLocation != null) {
                DeviceCoordinates(
                    latitude = finalLocation.latitude,
                    longitude = finalLocation.longitude,
                    accuracy = finalLocation.accuracy
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        // 1. Try Android Geocoder first
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val address = suspendCancellableCoroutine<String?> { cont ->
                        geocoder.getFromLocation(lat, lon, 1) { addresses ->
                            val first = addresses.firstOrNull()
                            val locality = first?.locality ?: first?.subLocality ?: first?.subAdminArea ?: first?.adminArea
                            val state = first?.adminArea ?: ""
                            val res = if (!locality.isNullOrBlank()) {
                                if (state.isNotBlank() && state != locality) "$locality, $state" else locality
                            } else null
                            cont.resume(res)
                        }
                    }
                    if (!address.isNullOrBlank()) return@withContext address
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val first = addresses?.firstOrNull()
                    val locality = first?.locality ?: first?.subLocality ?: first?.subAdminArea ?: first?.adminArea
                    val state = first?.adminArea ?: ""
                    if (!locality.isNullOrBlank()) {
                        return@withContext if (state.isNotBlank() && state != locality) "$locality, $state" else locality
                    }
                }
            }
        } catch (e: Exception) {
            // Fall through to Nominatim
        }

        // 2. Try Nominatim Geocoding API
        try {
            val response = GeocodingApiClient.service.reverseGeocode(lat, lon)
            val addr = response.address
            val place = addr?.village ?: addr?.town ?: addr?.city ?: addr?.suburb ?: addr?.county ?: addr?.stateDistrict
            val state = addr?.state ?: ""
            if (!place.isNullOrBlank()) {
                if (state.isNotBlank() && state != place) "$place, $state" else place
            } else if (response.displayName.isNotBlank()) {
                response.displayName.split(",").take(2).joinToString(",").trim()
            } else {
                "Active Field"
            }
        } catch (e: Exception) {
            "Active Field"
        }
    }
}
