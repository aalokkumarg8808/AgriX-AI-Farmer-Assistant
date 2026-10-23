package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 1000,
    @Json(name = "temperature") val temperature: Float? = 0.7f
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? =
        GeminiGenerationConfig(maxOutputTokens = 1000, temperature = 0.7f)
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = GeminiApiClient.DEFAULT_MODEL,
        @Query("key") apiKey: String? = null,
        @Body request: GeminiRequest
    ): GeminiResponse
}

/**
 * Gemini API client for a public Android application.
 *
 * Production builds MUST be configured with an HTTPS backend proxy. Direct API-key
 * authentication is deliberately disabled in this client because values packaged in
 * an APK can be extracted. The proxy must keep its Gemini key server-side and enforce
 * authentication, request limits, and abuse protection.
 */
object GeminiApiClient {
    const val DEFAULT_MODEL = "gemini-3.1-flash-lite"
    const val MAX_USER_PROMPT_CHARS = 2_000

    private const val CONFIGURED_BACKEND_URL = BuildConfig.AI_BACKEND_BASE_URL
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun validatedBackendUrl(): String {
        val value = CONFIGURED_BACKEND_URL.trim()
        require(value.startsWith("https://", ignoreCase = true)) {
            "AI backend is not configured. Set AI_BACKEND_BASE_URL to an HTTPS endpoint."
        }
        require(value.endsWith("/")) {
            "AI backend URL must end with '/'."
        }
        return value
    }

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(validatedBackendUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    /** Never read a packaged Gemini key; public clients must use the configured proxy. */
    fun getApiKey(): String = ""

    fun getBackendBaseUrl(): String = try {
        validatedBackendUrl()
    } catch (_: IllegalArgumentException) {
        ""
    }

    fun isUsingBackendProxy(): Boolean = getBackendBaseUrl().isNotBlank()

    fun isConfigured(): Boolean = isUsingBackendProxy()

    suspend fun executeGenerateContent(
        request: GeminiRequest,
        model: String = DEFAULT_MODEL
    ): GeminiResponse {
        check(isConfigured()) {
            "AI backend is not configured. Set AI_BACKEND_BASE_URL before building."
        }
        return service.generateContent(model = model, apiKey = null, request = request)
    }
}
