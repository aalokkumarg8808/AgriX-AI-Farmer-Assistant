package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @Json(name = "precipitation_probability") val precipitationProbability: Int? = 0,
    @Json(name = "weather_code") val weatherCode: Int = 0
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current") val current: OpenMeteoCurrent?
)

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double = 28.8318, // Default: Modinagar / UP
        @Query("longitude") longitude: Double = 77.5838,
        @Query("current") currentFields: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation_probability,weather_code"
    ): OpenMeteoResponse
}

object WeatherApiClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
