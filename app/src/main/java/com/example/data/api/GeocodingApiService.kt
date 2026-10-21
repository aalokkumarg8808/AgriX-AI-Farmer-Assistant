package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class NominatimAddress(
    @Json(name = "village") val village: String? = null,
    @Json(name = "town") val town: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "suburb") val suburb: String? = null,
    @Json(name = "county") val county: String? = null,
    @Json(name = "state_district") val stateDistrict: String? = null,
    @Json(name = "state") val state: String? = null,
    @Json(name = "country") val country: String? = null
)

@JsonClass(generateAdapter = true)
data class NominatimResponse(
    @Json(name = "display_name") val displayName: String = "",
    @Json(name = "address") val address: NominatimAddress? = null
)

@JsonClass(generateAdapter = true)
data class NominatimSearchResult(
    @Json(name = "lat") val lat: String,
    @Json(name = "lon") val lon: String,
    @Json(name = "display_name") val displayName: String = ""
)

interface GeocodingApiService {
    @GET("reverse")
    @Headers(
        "User-Agent: AgriXSmartFarmingApp/1.0 (https://github.com/aalokkumarg8808/AgriX-AI-Farmer-Assistant)",
        "Accept-Language: en"
    )
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): NominatimResponse

    @GET("search")
    @Headers(
        "User-Agent: AgriXSmartFarmingApp/1.0 (https://github.com/aalokkumarg8808/AgriX-AI-Farmer-Assistant)",
        "Accept-Language: en"
    )
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): List<NominatimSearchResult>
}

/**
 * Nominatim Geocoding Client with built-in caching and 1-request-per-second rate limiting
 * to comply with the OpenStreetMap Nominatim Usage Policy.
 */
object GeocodingApiClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val MIN_REQUEST_INTERVAL_MS = 1_050L

    private val rateLimitMutex = Mutex()
    private var lastRequestTimeMs: Long = 0L

    private val reverseCache = ConcurrentHashMap<String, NominatimResponse>()
    private val searchCache = ConcurrentHashMap<String, List<NominatimSearchResult>>()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }

    suspend fun reverseGeocodeCached(lat: Double, lon: Double): NominatimResponse {
        val cacheKey = String.format(java.util.Locale.US, "%.3f,%.3f", lat, lon)
        reverseCache[cacheKey]?.let { return it }

        val response = executeRateLimited {
            service.reverseGeocode(lat = lat, lon = lon)
        }
        reverseCache[cacheKey] = response
        return response
    }

    suspend fun searchCached(query: String): List<NominatimSearchResult> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()
        searchCache[normalized]?.let { return it }

        val results = executeRateLimited {
            service.search(query = query.trim())
        }
        if (results.isNotEmpty()) {
            searchCache[normalized] = results
        }
        return results
    }

    private suspend fun <T> executeRateLimited(block: suspend () -> T): T {
        return rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestTimeMs
            if (elapsed in 0 until MIN_REQUEST_INTERVAL_MS) {
                delay(MIN_REQUEST_INTERVAL_MS - elapsed)
            }
            try {
                block()
            } finally {
                lastRequestTimeMs = System.currentTimeMillis()
            }
        }
    }
}
