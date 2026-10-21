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
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig(maxOutputTokens = 1000, temperature = 0.7f)
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
 * Gemini API Client supporting two execution modes:
 *
 * 1. **Production Backend Proxy Mode (Recommended)**:
 *    When `BuildConfig.AI_BACKEND_BASE_URL` is configured (e.g. `https://your-backend.example.com/`),
 *    requests are routed to the backend proxy without sending any client-side API key (`apiKey = null`).
 *    The backend holds `GEMINI_API_KEY` exclusively in server-side environment secrets.
 *
 * 2. **Direct Client Mode (Local Prototype / AI Studio Preview Fallback)**:
 *    When `AI_BACKEND_BASE_URL` is blank, requests are sent directly to
 *    `https://generativelanguage.googleapis.com/` using `BuildConfig.GEMINI_API_KEY`.
 *    WARNING: Any value compiled into Android `BuildConfig` or an APK is extractable by decompiling
 *    the binary. Direct mode should only be used for local development/testing with restricted keys.
 */
object GeminiApiClient {
    private const val DIRECT_BASE_URL = "https://generativelanguage.googleapis.com/"
    const val DEFAULT_MODEL = "gemini-3.1-flash-lite"
    const val MAX_USER_PROMPT_CHARS = 2_000

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getBackendBaseUrl(): String {
        return try {
            val raw = BuildConfig::class.java.getDeclaredField("AI_BACKEND_BASE_URL").get(null) as? String ?: ""
            val trimmed = raw.trim()
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                if (trimmed.endsWith("/")) trimmed else "$trimmed/"
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun isUsingBackendProxy(): Boolean = getBackendBaseUrl().isNotEmpty()

    val service: GeminiApiService by lazy {
        val baseUrl = getBackendBaseUrl().ifEmpty { DIRECT_BASE_URL }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Returns true if either a backend proxy URL is configured OR a non-placeholder local API key is set.
     */
    fun isConfigured(): Boolean {
        if (isUsingBackendProxy()) return true
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && key != "YOUR_API_KEY" && key != "NONE"
    }

    /**
     * Executes a Gemini generation request via the configured backend proxy (if set) or direct API.
     * Never exposes the API key in thrown exceptions or logs.
     */
    suspend fun executeGenerateContent(
        request: GeminiRequest,
        model: String = DEFAULT_MODEL
    ): GeminiResponse {
        val keyParam = if (isUsingBackendProxy()) null else getApiKey()
        return service.generateContent(
            model = model,
            apiKey = keyParam,
            request = request
        )
    }
}
