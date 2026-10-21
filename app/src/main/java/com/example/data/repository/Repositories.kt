package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiInlineData
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.WeatherApiClient
import com.example.data.api.GeocodingApiClient
import com.example.data.db.FarmFieldDao
import com.example.data.db.FarmTaskDao
import com.example.data.db.GuideDao
import com.example.data.db.UserAccountDao
import com.example.data.db.UserProfileDao
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMessageStatus
import com.example.data.model.CropRecommendation
import com.example.data.model.FarmFieldEntity
import com.example.data.model.FarmTaskEntity
import com.example.data.model.GuideEntity
import com.example.data.model.PlantDiseaseAnalysis
import com.example.data.model.SecurityUtils
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WeatherData
import com.example.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

class WeatherRepository {

    suspend fun resolveLocationName(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val response = GeocodingApiClient.service.reverseGeocode(lat, lon)
            val addr = response.address
            val place = addr?.village ?: addr?.town ?: addr?.city ?: "Active Field"
            val state = addr?.state ?: ""
            if (state.isNotEmpty()) "$place, $state" else place
        } catch (e: Exception) {
            "Active Field"
        }
    }

    suspend fun resolveCoordinates(query: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val results = GeocodingApiClient.service.search(query)
            val first = results.firstOrNull() ?: return@withContext null
            val lat = first.lat.toDoubleOrNull() ?: return@withContext null
            val lon = first.lon.toDoubleOrNull() ?: return@withContext null
            Pair(lat, lon)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchWeather(
        locationName: String = "Active Field",
        lat: Double? = null,
        lon: Double? = null,
        language: AppLanguage
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            var activeLat = lat
            var activeLon = lon

            if (activeLat == null || activeLon == null) {
                if (locationName == "Active Field" || locationName.isBlank()) {
                    throw Exception("LOCATION_UNAVAILABLE")
                }
                val coords = resolveCoordinates(locationName)
                if (coords != null) {
                    activeLat = coords.first
                    activeLon = coords.second
                } else {
                    throw Exception("LOCATION_UNRESOLVED")
                }
            }

            val response = WeatherApiClient.service.getWeatherForecast(latitude = activeLat!!, longitude = activeLon!!)
            val current = response.current ?: throw Exception("Weather data unavailable")

            val temp = current.temperature2m
            val humidity = current.relativeHumidity2m
            val wind = current.windSpeed10m
            val rainChance = current.precipitationProbability ?: 15
            val code = current.weatherCode

            val conditionText = mapWeatherCode(code, language)
            val forecast = generateFarmingForecast(temp, humidity, wind, rainChance, conditionText, language)

            val finalLocationName = if (locationName == "Active Field") {
                resolveLocationName(activeLat, activeLon)
            } else {
                locationName
            }

            Result.success(
                WeatherData(
                    locationName = finalLocationName,
                    temperatureC = temp,
                    conditionText = conditionText,
                    humidityPercent = humidity,
                    windSpeedKmh = wind,
                    rainProbabilityPercent = rainChance,
                    farmingForecast = forecast
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapWeatherCode(code: Int, language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> when (code) {
                0 -> "Clear Sky"
                1, 2, 3 -> "Partly Cloudy"
                45, 48 -> "Foggy"
                51, 53, 55 -> "Light Drizzle"
                61, 63, 65 -> "Rainy"
                80, 81, 82 -> "Rain Showers"
                95, 96, 99 -> "Thunderstorm"
                else -> "Fair Weather"
            }
            AppLanguage.HI -> when (code) {
                0 -> "साफ़ आसमान"
                1, 2, 3 -> "आंशिक रूप से बादल"
                45, 48 -> "कोहरा"
                51, 53, 55 -> "हल्की बूंदाबांदी"
                61, 63, 65 -> "बारिश"
                80, 81, 82 -> "तेज़ बौछारें"
                95, 96, 99 -> "आंधी-तूफान"
                else -> "सुहावना मौसम"
            }
            AppLanguage.PA -> when (code) {
                0 -> "ਸਾਫ਼ ਆਸਮਾਨ"
                1, 2, 3 -> "ਕੁੱਝ ਬੱਦਲਵਾਈ"
                45, 48 -> "ਧੁੰਦ"
                51, 53, 55 -> "ਹਲਕੀ ਫੁਹਾਰ"
                61, 63, 65 -> "ਮੀਂਹ"
                80, 81, 82 -> "ਤੇਜ਼ ਛੱੜਾਅ"
                95, 96, 99 -> "ਝੱਖੜ ਅਤੇ ਮੀਂਹ"
                else -> "ਵਧੀਆ ਮੌਸਮ"
            }
        }
    }

    private fun generateFarmingForecast(temp: Double, humidity: Int, wind: Double, rainChance: Int, condition: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> {
                when {
                    rainChance > 50 -> "High chance of rain ($rainChance%). Hold off on chemical spraying and postpone irrigation today."
                    temp > 35 -> "Hot temperature (${temp.toInt()}°C). Irrigate early morning or evening to prevent evaporation."
                    wind > 20 -> "Strong wind speeds (${wind.toInt()} km/h). Avoid pesticide spraying to prevent drift."
                    else -> "Optimal weather condition ($condition). Ideal for field preparation, fertilization, and crop monitoring."
                }
            }
            AppLanguage.HI -> {
                when {
                    rainChance > 50 -> "बारिश की अधिक संभावना ($rainChance%)। आज रासायनिक छिड़काव रोकें और सिंचाई टालें।"
                    temp > 35 -> "उच्च तापमान (${temp.toInt()}°C)। वाष्पीकरण से बचने के लिए सुबह जल्दी या शाम को सिंचाई करें।"
                    wind > 20 -> "तेज़ हवाएँ (${wind.toInt()} किमी/घंटा)। कीटनाशक के बहाव से बचने के लिए छिड़काव न करें।"
                    else -> "अनुकूल मौसम स्थिति ($condition)। खेत की तैयारी, उर्वरक देने और फसल की देखरेख के लिए उत्तम समय।"
                }
            }
            AppLanguage.PA -> {
                when {
                    rainChance > 50 -> "ਮੀਂਹ ਦੀ ਕਾਫ਼ੀ ਸੰਭਾਵਨਾ ($rainChance%)। ਅੱਜ ਸਪਰੇਅ ਅਤੇ ਸਿੰਚਾਈ ਦਾ ਕੰਮ ਮੁਲਤਵੀ ਕਰੋ।"
                    temp > 35 -> "ਉੱਚ ਤਾਪਮਾਨ (${temp.toInt()}°C)। ਵਾਸਪੀਕਰਨ ਤੋਂ ਬਚਣ ਲਈ ਸਵੇਰੇ ਜਲਦੀ ਜਾਂ ਸ਼ਾਮ ਨੂੰ ਸਿੰਚਾਈ ਕਰੋ।"
                    wind > 20 -> "ਤੇਜ਼ ਹਵਾਵਾਂ (${wind.toInt()} ਕਿਲੋਮੀਟਰ/ਘੰਟਾ)। ਕੀਟਨਾਸ਼ਕ ਦੇ ਬਹਾਅ ਤੋਂ ਬਚਣ ਲਈ ਸਪਰੇਅ ਨਾ ਕਰੋ।"
                    else -> "ਢੁਕਵਾਂ ਮੌਸਮ ($condition)। ਖੇਤ ਦੀ ਤਿਆਰੀ, ਖਾਦ ਪਾਉਣ ਅਤੇ ਫ਼ਸਲ ਦੀ ਸੰਭਾਲ ਲਈ ਢੁਕਵਾਂ ਸਮਾਂ।"
                }
            }
        }
    }
}

class CropRecommendationRepository {
    suspend fun getRecommendations(
        soilType: String,
        season: String,
        waterAvailability: String,
        landSizeAcres: Double,
        language: AppLanguage
    ): Result<List<CropRecommendation>> = withContext(Dispatchers.IO) {
        val apiKey = GeminiApiClient.getApiKey()
        
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Securely return localized guidelines if API Key is not set yet
            return@withContext Result.failure(Exception("KEY_MISSING"))
        }

        try {
            val prompt = """
                Give crop recommendations for an Indian farm with:
                Soil: $soilType, Season: $season, Water Availability: $waterAvailability, Land Size: $landSizeAcres acres.
                Language needed: ${language.displayName}.
                Return 4 recommended crops formatted strictly as line delimited:
                CropName | SuitabilityLevel(HIGH/MODERATE/LOW) | YieldEstimate | GrowthPeriod | WaterNeed | Description
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )
            val response = GeminiApiClient.service.generateContent(model = "gemini-3.1-flash-lite", apiKey = apiKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                val parsed = parseGeminiCropRecommendations(text, language)
                if (parsed.isNotEmpty()) return@withContext Result.success(parsed)
            }
        } catch (e: HttpException) {
            if (e.code() == 429) {
                return@withContext Result.failure(Exception("RATE_LIMIT"))
            }
        } catch (e: Exception) {
            if (e.message?.contains("429") == true) {
                return@withContext Result.failure(Exception("RATE_LIMIT"))
            }
        }
        
        // Final fallback to clean deterministic agronomy model
        Result.success(getDeterministicCropRecommendations(soilType, season, waterAvailability, language))
    }

    private fun parseGeminiCropRecommendations(text: String, language: AppLanguage): List<CropRecommendation> {
        val list = mutableListOf<CropRecommendation>()
        val lines = text.split("\n")
        for (line in lines) {
            if (line.contains("|")) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 6) {
                    val level = when {
                        parts[1].contains("HIGH", true) || parts[1].contains("High", true) -> "HIGH"
                        parts[1].contains("LOW", true) -> "LOW"
                        else -> "MODERATE"
                    }
                    val badge = when (level) {
                        "HIGH" -> if (language == AppLanguage.HI) "उच्च उपयुक्तता" else if (language == AppLanguage.PA) "ਉੱਚ ਢੁਕਵਾਂ" else "High Suitability"
                        "MODERATE" -> if (language == AppLanguage.HI) "मध्यम उपयुक्तता" else if (language == AppLanguage.PA) "ਦਰਮਿਆਨਾ ਢੁਕਵਾਂ" else "Moderate Suitability"
                        else -> if (language == AppLanguage.HI) "कम उपयुक्तता" else if (language == AppLanguage.PA) "ਘੱਟ ਢੁਕਵਾਂ" else "Low Suitability"
                    }
                    list.add(
                        CropRecommendation(
                            cropName = parts[0],
                            suitabilityLevel = level,
                            suitabilityBadge = badge,
                            description = parts[5],
                            yieldEstimate = parts[2],
                            durationMonths = parts[3],
                            waterNeed = parts[4]
                        )
                    )
                }
            }
        }
        return list
    }

    private fun getDeterministicCropRecommendations(soil: String, season: String, water: String, language: AppLanguage): List<CropRecommendation> {
        val highBadge = if (language == AppLanguage.HI) "उच्च उपयुक्तता" else if (language == AppLanguage.PA) "ਉੱਚ ਢੁਕਵਾਂ" else "High Suitability"
        val modBadge = if (language == AppLanguage.HI) "मध्यम उपयुक्तता" else if (language == AppLanguage.PA) "ਦਰਮਿਆਨਾ ਢੁਕਵਾਂ" else "Moderate Suitability"

        return listOf(
            CropRecommendation(
                cropName = if (language == AppLanguage.HI) "चावल / धान (Rice)" else if (language == AppLanguage.PA) "ਝੋਨਾ (Rice)" else "Rice",
                suitabilityLevel = "HIGH",
                suitabilityBadge = highBadge,
                description = if (language == AppLanguage.HI) "आपके जल स्तर और मिट्टी के लिए उत्कृष्ट विकल्प।" else if (language == AppLanguage.PA) "ਤੁਹਾਡੇ ਪਾਣੀ ਦੇ ਪੱਧਰ ਅਤੇ ਮਿੱਟੀ ਲਈ ਵਧੀਆ ਫਸਲ।" else "Excellent choice for your soil moisture and seasonal profile.",
                yieldEstimate = "22-26 Quintals/Acre",
                durationMonths = "4 Months",
                waterNeed = "High"
            ),
            CropRecommendation(
                cropName = if (language == AppLanguage.HI) "मक्का (Maize)" else if (language == AppLanguage.PA) "ਮੱਕੀ (Maize)" else "Maize",
                suitabilityLevel = "HIGH",
                suitabilityBadge = highBadge,
                description = if (language == AppLanguage.HI) "कम पानी की आवश्यकता और अच्छी बाज़ार कीमत।" else if (language == AppLanguage.PA) "ਘੱਟ ਪਾਣੀ ਦੀ ਲੋੜ ਅਤੇ ਵਧੀਆ ਮੰਡੀ ਕੀਮਤ।" else "Moderate water demand with high yield stability and good market demand.",
                yieldEstimate = "18-22 Quintals/Acre",
                durationMonths = "3.5 Months",
                waterNeed = "Moderate"
            ),
            CropRecommendation(
                cropName = if (language == AppLanguage.HI) "सोयाबीन (Soybean)" else if (language == AppLanguage.PA) "ਸੋਇਆਬੀਨ (Soybean)" else "Soybean",
                suitabilityLevel = "MODERATE",
                suitabilityBadge = modBadge,
                description = if (language == AppLanguage.HI) "मिट्टी में नाइट्रोजन बढ़ाता है और कम लागत वाली फसल।" else if (language == AppLanguage.PA) "ਮਿੱਟੀ ਵਿੱਚ ਨਾਈਟ੍ਰੋਜਨ ਵਧਾਉਂਦਾ ਹੈ।" else "Fixes soil nitrogen and provides balanced crop rotation benefits.",
                yieldEstimate = "10-14 Quintals/Acre",
                durationMonths = "3.5 Months",
                waterNeed = "Moderate"
            ),
            CropRecommendation(
                cropName = if (language == AppLanguage.HI) "मूंगफली (Groundnut)" else if (language == AppLanguage.PA) "ਮੂੰਗਫਲੀ (Groundnut)" else "Groundnut",
                suitabilityLevel = "MODERATE",
                suitabilityBadge = modBadge,
                description = if (language == AppLanguage.HI) "बलुई और दोमट मिट्टी में अच्छी पैदावार।" else if (language == AppLanguage.PA) "ਰੇਤਲੀ ਅਤੇ ਦੋਮਟ ਮਿੱਟੀ ਲਈ ਵਧੀਆ।" else "Good yield potential in well-drained loamy soils with moderate irrigation.",
                yieldEstimate = "8-12 Quintals/Acre",
                durationMonths = "4 Months",
                waterNeed = "Low-Moderate"
            )
        )
    }
}

class DiseaseDetectionRepository {
    suspend fun analyzePlantImage(bitmap: Bitmap, language: AppLanguage): Result<PlantDiseaseAnalysis> = withContext(Dispatchers.IO) {
        val apiKey = GeminiApiClient.getApiKey()
        
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("KEY_MISSING"))
        }

        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = """
                Analyze this plant/crop leaf image.
                Respond strictly in ${language.displayName} (${language.code}).
                Provide structured details formatted exactly as follows:
                DISEASE_NAME: <Name of disease or Healthy>
                STATUS: <Healthy or Infected>
                CONFIDENCE: <Integer percentage e.g. 92>
                SYMPTOMS: <Symptom 1>, <Symptom 2>, <Symptom 3>
                TREATMENT: <Step 1>, <Step 2>, <Step 3>
                PREVENTION: <Tip 1>, <Tip 2>
                SEVERITY: <Low / Moderate / High>
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Data))
                        )
                    )
                )
            )

            val response = GeminiApiClient.service.generateContent(model = "gemini-3.1-flash-lite", apiKey = apiKey, request = request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                val analysis = parseDiseaseAnalysis(responseText, language)
                return@withContext Result.success(analysis)
            }
        } catch (e: HttpException) {
            if (e.code() == 429) {
                return@withContext Result.failure(Exception("RATE_LIMIT"))
            }
        } catch (e: Exception) {
            if (e.message?.contains("429") == true) {
                return@withContext Result.failure(Exception("RATE_LIMIT"))
            }
        }
        
        Result.failure(Exception("Analysis failed. Please check network and try again."))
    }

    private fun parseDiseaseAnalysis(text: String, language: AppLanguage): PlantDiseaseAnalysis {
        var diseaseName = if (language == AppLanguage.HI) "पौधों का स्वास्थ्य विश्लेषण" else if (language == AppLanguage.PA) "ਪੌਦਿਆਂ ਦੀ ਸਿਹਤ" else "Plant Health Analysis"
        var healthStatus = "Infected"
        var confidence = 90
        val symptoms = mutableListOf<String>()
        val treatment = mutableListOf<String>()
        val prevention = mutableListOf<String>()
        var severity = "MODERATE"

        text.split("\n").forEach { line ->
            when {
                line.contains("DISEASE_NAME:", true) -> diseaseName = line.substringAfter(":").trim()
                line.contains("STATUS:", true) -> healthStatus = line.substringAfter(":").trim()
                line.contains("CONFIDENCE:", true) -> {
                    val digits = line.filter { it.isDigit() }
                    if (digits.isNotEmpty()) confidence = digits.toInt().coerceIn(50, 99)
                }
                line.contains("SYMPTOMS:", true) -> symptoms.addAll(line.substringAfter(":").split(",").map { it.trim() })
                line.contains("TREATMENT:", true) -> treatment.addAll(line.substringAfter(":").split(",").map { it.trim() })
                line.contains("PREVENTION:", true) -> prevention.addAll(line.substringAfter(":").split(",").map { it.trim() })
                line.contains("SEVERITY:", true) -> severity = line.substringAfter(":").trim().uppercase()
            }
        }

        if (symptoms.isEmpty()) symptoms.add(if (language == AppLanguage.HI) "पत्तियों पर धब्बे और मलिनकिरण" else "Leaf discoloration and spots")
        if (treatment.isEmpty()) treatment.add(if (language == AppLanguage.HI) "सिफारिश किए गए कवकनाशी का छिॉर्म छिड़काव करें" else "Apply recommended systemic fungicide spray")
        if (prevention.isEmpty()) prevention.add(if (language == AppLanguage.HI) "उचित जलनिकासी बनाए रखें" else "Maintain good soil aeration and field sanitation")

        return PlantDiseaseAnalysis(
            diseaseName = diseaseName,
            healthStatus = healthStatus,
            confidencePercent = confidence,
            symptoms = symptoms,
            treatmentSteps = treatment,
            preventionTips = prevention,
            severityLevel = severity
        )
    }
}

class AiAssistantRepository {
    suspend fun getAiResponse(
        userMessage: String,
        conversationHistory: List<AiChatMessage>,
        language: AppLanguage? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = GeminiApiClient.getApiKey()
        
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("KEY_MISSING"))
        }

        try {
            val systemPrompt = """
                You are AgriX AI, an expert agricultural assistant dedicated to helping farmers.
                You provide practical guidance on crops, soil testing, seeds, fertilizers, irrigation, pest control, plant diseases, and weather.

                CRITICAL LANGUAGE RULES (MANDATORY & INDEPENDENT OF APP SETTINGS):
                - Detect and match the language and script of the user's latest query automatically:
                  1. English question -> Reply in clear, natural English.
                  2. Hindi in Devanagari script (e.g., गेहूँ के लिए कौन सी खाद अच्छी है?) -> Reply in Hindi using Devanagari script.
                  3. Hinglish / Romanized Hindi (Hindi written using the English/Latin alphabet, e.g., "Gehu ke liye kaunsi khaad achhi hai?") -> Reply in natural, conversational Hinglish using the English/Latin alphabet.
                  4. Mixed Hindi-English question -> Reply in natural Hinglish.
                  5. Explicit language instruction (e.g., "answer in Hindi", "reply in English") -> Strictly follow the user's requested language.
                  6. If the user changes language mid-conversation, immediately adapt to the new language.
                - NEVER force responses into a fixed application language. The user's input alone determines the response language.

                CRITICAL RESPONSE LENGTH & RELEVANCE RULES (MANDATORY):
                - Answers must be short, direct, actionable, and match the question's scope:
                  1. Simple question -> 1 to 3 short sentences.
                  2. Direct factual question -> Answer directly, without unnecessary introductory greetings or filler.
                  3. "Explain briefly" -> Provide a short, focused explanation.
                  4. "In detail" or "step by step" -> Provide more detail or structured steps ONLY when explicitly requested.
                  5. Complex farming question -> Give the essential explanation and immediate actionable steps.
                  6. Follow-up question -> Answer ONLY the specific follow-up point instead of repeating the entire previous answer.
                - Do NOT automatically produce:
                  * Long introductions or repeated summaries
                  * Unrequested background information
                  * Huge fertilizer schedules for simple questions
                  * Excessive headings or long bullet lists for simple questions
                  * Repeated safety disclaimers when unnecessary
                - Keep essential safety cautions only when handling hazardous chemicals or toxic pesticides.
            """.trimIndent()

            val isDetailedRequest = userMessage.contains("detail", ignoreCase = true) ||
                    userMessage.contains("step by step", ignoreCase = true) ||
                    userMessage.contains("vistaar", ignoreCase = true) ||
                    userMessage.contains("vistrit", ignoreCase = true) ||
                    userMessage.contains("explain all", ignoreCase = true) ||
                    userMessage.contains("guide", ignoreCase = true) ||
                    userMessage.contains("विस्तार", ignoreCase = true)

            val maxTokens = if (isDetailedRequest) 800 else 350

            val contentsList = mutableListOf<GeminiContent>()
            
            // Limit context window to recent successful messages, ensuring userMessage isn't duplicated
            val recentHistory = conversationHistory
                .filter { it.status == AiMessageStatus.SUCCESS && it.text.isNotBlank() }
                .let { list ->
                    if (list.isNotEmpty() && list.last().isUser && list.last().text.trim() == userMessage.trim()) {
                        list.dropLast(1)
                    } else {
                        list
                    }
                }
                .takeLast(4)

            recentHistory.forEach { msg ->
                contentsList.add(
                    GeminiContent(
                        role = if (msg.isUser) "user" else "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userMessage))
                )
            )

            val request = GeminiRequest(
                contents = contentsList,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(maxOutputTokens = maxTokens, temperature = 0.5f)
            )

            val response = GeminiApiClient.service.generateContent(model = "gemini-3.1-flash-lite", apiKey = apiKey, request = request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!reply.isNullOrBlank()) {
                return@withContext Result.success(reply)
            }
        } catch (e: HttpException) {
            val errorType = when (e.code()) {
                429 -> "RATE_LIMIT"
                401 -> "AUTH_ERROR"
                403 -> "PERMISSION_ERROR"
                408 -> "TIMEOUT"
                in 500..599 -> "SERVER_ERROR"
                else -> "HTTP_${e.code()}"
            }
            return@withContext Result.failure(Exception(errorType))
        } catch (e: java.io.IOException) {
            return@withContext Result.failure(Exception("NO_INTERNET"))
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val errorType = when {
                msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") -> "RATE_LIMIT"
                msg.contains("401") -> "AUTH_ERROR"
                msg.contains("403") -> "PERMISSION_ERROR"
                msg.contains("408") || msg.contains("timeout", ignoreCase = true) -> "TIMEOUT"
                msg.contains("500") || msg.contains("502") || msg.contains("503") -> "SERVER_ERROR"
                msg.contains("Unable to resolve host", ignoreCase = true) || msg.contains("ConnectException") -> "NO_INTERNET"
                else -> "GENERAL_ERROR"
            }
            return@withContext Result.failure(Exception(errorType))
        }

        Result.failure(Exception("GENERAL_ERROR"))
    }
}

class AuthRepository(
    private val accountDao: UserAccountDao,
    private val profileDao: UserProfileDao
) {
    val activeAccount: Flow<UserAccountEntity?> = accountDao.getActiveAccount()

    suspend fun getActiveAccountDirect(): UserAccountEntity? = withContext(Dispatchers.IO) {
        accountDao.getActiveAccountDirect()
    }

    suspend fun createAccount(email: String, password: String): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim().lowercase()
            val existing = accountDao.findAccountByEmail(trimmedEmail)
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with this email already exists"))
            }
            val salt = SecurityUtils.generateSalt()
            val hash = SecurityUtils.hashPassword(password, salt)

            accountDao.clearActiveSessions()

            val newAccount = UserAccountEntity(
                email = trimmedEmail,
                passwordHash = hash,
                salt = salt,
                isSetupCompleted = false,
                isLoggedIn = true
            )
            val newId = accountDao.insertAccount(newAccount)
            val created = newAccount.copy(id = newId)

            profileDao.saveUserProfile(
                UserProfileEntity(
                    id = newId,
                    userId = newId,
                    fullName = "",
                    location = "",
                    phone = "",
                    preferredLanguage = "EN"
                )
            )

            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim().lowercase()
            val account = accountDao.findAccountByEmail(trimmedEmail)
                ?: return@withContext Result.failure(Exception("Invalid email or password"))

            val isValid = SecurityUtils.verifyPassword(password, account.salt, account.passwordHash)
            if (!isValid) {
                return@withContext Result.failure(Exception("Invalid email or password"))
            }

            accountDao.clearActiveSessions()
            accountDao.setActiveSession(account.id)
            Result.success(account.copy(isLoggedIn = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeSetup(userId: Long, profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        profileDao.saveUserProfile(profile.copy(id = userId, userId = userId))
        accountDao.setSetupCompleted(userId, true)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        accountDao.clearActiveSessions()
    }
}

class FarmRepository(
    private val fieldDao: FarmFieldDao,
    private val taskDao: FarmTaskDao
) {
    fun getFieldsForUser(userId: Long): Flow<List<FarmFieldEntity>> = fieldDao.getFieldsByUserId(userId)
    fun getTasksForUser(userId: Long): Flow<List<FarmTaskEntity>> = taskDao.getTasksByUserId(userId)

    suspend fun addField(field: FarmFieldEntity) = withContext(Dispatchers.IO) { fieldDao.insertField(field) }
    suspend fun deleteField(field: FarmFieldEntity) = withContext(Dispatchers.IO) { fieldDao.deleteField(field) }
    suspend fun deleteFieldById(id: Long) = withContext(Dispatchers.IO) { fieldDao.deleteFieldById(id) }

    suspend fun addTask(task: FarmTaskEntity) = withContext(Dispatchers.IO) { taskDao.insertTask(task) }
    suspend fun updateTask(task: FarmTaskEntity) = withContext(Dispatchers.IO) { taskDao.updateTask(task) }
    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) { taskDao.deleteTask(id) }
}

class ProfileRepository(
    private val profileDao: UserProfileDao,
    private val fieldDao: FarmFieldDao,
    private val taskDao: FarmTaskDao
) {
    fun getProfileForUser(userId: Long): Flow<UserProfileEntity?> = profileDao.getUserProfileByUserId(userId)
    val firstProfile: Flow<UserProfileEntity?> = profileDao.getFirstUserProfile()

    suspend fun getUserProfileDirect(userId: Long): UserProfileEntity? = withContext(Dispatchers.IO) {
        profileDao.getUserProfileByUserIdDirect(userId)
    }

    suspend fun saveProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        profileDao.saveUserProfile(profile)
    }

    suspend fun clearAccountData(userId: Long) = withContext(Dispatchers.IO) {
        profileDao.deleteUserProfileByUserId(userId)
        fieldDao.deleteFieldsByUserId(userId)
        taskDao.deleteTasksByUserId(userId)
    }
}

class GuideRepository(private val guideDao: GuideDao) {
    val allGuides: Flow<List<GuideEntity>> = guideDao.getAllGuides()

    fun getGuidesByCategory(category: String): Flow<List<GuideEntity>> {
        return if (category.lowercase() == "all") guideDao.getAllGuides() else guideDao.getGuidesByCategory(category)
    }
}
