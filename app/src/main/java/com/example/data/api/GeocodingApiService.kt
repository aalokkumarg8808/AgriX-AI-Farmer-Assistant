package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

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
    @Headers("User-Agent: AgriXSmartFarmingApp/1.0 (aalokkumarg8808@gmail.com)")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): NominatimResponse

    @GET("search")
    @Headers("User-Agent: AgriXSmartFarmingApp/1.0 (aalokkumarg8808@gmail.com)")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): List<NominatimSearchResult>
}

object GeocodingApiClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    private val client = OkHttpClient.Builder().build()

    val service: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }
}
