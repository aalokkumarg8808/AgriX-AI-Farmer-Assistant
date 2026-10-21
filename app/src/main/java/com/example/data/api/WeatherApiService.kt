package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @Json(name = "precipitation_probability") val precipitationProbability: Int? = 0,
    @Json(name = "weather_code") val weatherCode: Int = 0
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int?> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current") val current: OpenMeteoCurrent?,
    @Json(name = "daily") val daily: OpenMeteoDaily? = null
)

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentFields: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation_probability,weather_code",
        @Query("daily") dailyFields: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 5
    ): OpenMeteoResponse
}

object WeatherApiClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
