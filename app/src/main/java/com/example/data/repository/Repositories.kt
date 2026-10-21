package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiInlineData
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.GeocodingApiClient
import com.example.data.api.OpenMeteoResponse
import com.example.data.api.WeatherApiClient
import com.example.data.db.FarmFieldDao
import com.example.data.db.FarmTaskDao
import com.example.data.db.GuideDao
import com.example.data.db.UserAccountDao
import com.example.data.db.UserProfileDao
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMessageStatus
import com.example.data.model.CropRecommendation
import com.example.data.model.DailyForecast
import com.example.data.model.FarmFieldEntity
import com.example.data.model.FarmTaskEntity
import com.example.data.model.GuideEntity
import com.example.data.model.PlantDiseaseAnalysis
import com.example.data.model.RecommendationSource
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
import kotlin.math.min
import kotlin.math.roundToInt

class WeatherRepository {

    suspend fun resolveLocationName(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val response = GeocodingApiClient.reverseGeocodeCached(lat, lon)
            val addr = response.address
            val place = addr?.village ?: addr?.town ?: addr?.city ?: addr?.suburb ?: addr?.county ?: "Active Field"
            val state = addr?.state ?: ""
            if (state.isNotEmpty() && state != place) "$place, $state" else place
        } catch (e: Exception) {
            "Active Field"
        }
    }

    suspend fun resolveCoordinates(query: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val results = GeocodingApiClient.searchCached(query)
            val first = results.firstOrNull() ?: return@withContext null
            val lat = first.lat.toDoubleOrNull() ?: return@withContext null
            val lon = first.lon.toDoubleOrNull() ?: return@withContext null
            if (!isValidCoordinate(lat, lon)) return@withContext null
            Pair(lat, lon)
        } catch (e: Exception) {
            null
        }
    }

    fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat.isFinite() && lon.isFinite() && lat in -90.0..90.0 && lon in -180.0..180.0
    }

    suspend fun fetchWeather(
        locationName: String = "Active Field",
        lat: Double? = null,
        lon: Double? = null,
        language: AppLanguage,
        isVerifiedGps: Boolean = false
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            var activeLat = lat
            var activeLon = lon
            var verifiedGps = isVerifiedGps

            if (activeLat == null || activeLon == null) {
                verifiedGps = false
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

            if (!isValidCoordinate(activeLat, activeLon)) {
                throw Exception("INVALID_COORDINATES")
            }

            val response = WeatherApiClient.service.getWeatherForecast(
                latitude = activeLat,
                longitude = activeLon
            )

            val finalLocationName = if (locationName == "Active Field" || locationName.isBlank()) {
                resolveLocationName(activeLat, activeLon)
            } else {
                locationName
            }

            val weatherData = parseWeatherResponse(
                response = response,
                locationName = finalLocationName,
                language = language,
                isVerifiedGps = verifiedGps
            )
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseWeatherResponse(
        response: OpenMeteoResponse,
        locationName: String,
        language: AppLanguage,
        isVerifiedGps: Boolean = false
    ): WeatherData {
        val current = response.current ?: throw IllegalArgumentException("Weather data unavailable")

        val temp = current.temperature2m
        val humidity = current.relativeHumidity2m.coerceIn(0, 100)
        val wind = current.windSpeed10m.coerceAtLeast(0.0)
        val rainChance = (current.precipitationProbability ?: 0).coerceIn(0, 100)
        val code = current.weatherCode

        val conditionText = mapWeatherCode(code, language)
        val forecast = generateFarmingForecast(temp, humidity, wind, rainChance, conditionText, language)

        val daily = response.daily
        val dailyList = mutableListOf<DailyForecast>()
        if (daily != null && daily.time.isNotEmpty()) {
            val count = minOf(
                5,
                daily.time.size,
                daily.temperature2mMax.size,
                daily.temperature2mMin.size
            )
            for (i in 0 until count) {
                val dayCode = daily.weatherCode.getOrElse(i) { code }
                val dayRain = (daily.precipitationProbabilityMax.getOrNull(i) ?: rainChance).coerceIn(0, 100)
                val dayLabel = formatDayLabel(daily.time[i], i, language)
                dailyList.add(
                    DailyForecast(
                        dateLabel = dayLabel,
                        maxTempC = daily.temperature2mMax[i],
                        minTempC = daily.temperature2mMin[i],
                        conditionText = mapWeatherCode(dayCode, language),
                        rainProbabilityPercent = dayRain,
                        weatherCode = dayCode
                    )
                )
            }
        }

        return WeatherData(
            locationName = locationName,
            temperatureC = temp,
            conditionText = conditionText,
            humidityPercent = humidity,
            windSpeedKmh = wind,
            rainProbabilityPercent = rainChance,
            farmingForecast = forecast,
            dailyForecast = dailyList,
            isVerifiedGps = isVerifiedGps
        )
    }

    private fun formatDayLabel(isoDate: String, index: Int, language: AppLanguage): String {
        if (index == 0) {
            return when (language) {
                AppLanguage.HI -> "आज ($isoDate)"
                AppLanguage.PA -> "ਅੱਜ ($isoDate)"
                AppLanguage.EN -> "Today ($isoDate)"
            }
        }
        if (index == 1) {
            return when (language) {
                AppLanguage.HI -> "कल ($isoDate)"
                AppLanguage.PA -> "ਕੱਲ੍ਹ ($isoDate)"
                AppLanguage.EN -> "Tomorrow ($isoDate)"
            }
        }
        return isoDate
    }

    fun mapWeatherCode(code: Int, language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> when (code) {
                0 -> "Clear Sky"
                1, 2, 3 -> "Partly Cloudy"
                45, 48 -> "Foggy"
                51, 53, 55 -> "Light Drizzle"
                61, 63, 65 -> "Rainy"
                71, 73, 75 -> "Snow / Frost"
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
                71, 73, 75 -> "पाला / बर्फबारी"
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
                71, 73, 75 -> "ਕੋਰਾ / ਬਰਫ਼ਬਾਰੀ"
                80, 81, 82 -> "ਤੇਜ਼ ਛੱੜਾਅ"
                95, 96, 99 -> "ਝੱਖੜ ਅਤੇ ਮੀਂਹ"
                else -> "ਵਧੀਆ ਮੌਸਮ"
            }
        }
    }

    private fun generateFarmingForecast(
        temp: Double,
        humidity: Int,
        wind: Double,
        rainChance: Int,
        condition: String,
        language: AppLanguage
    ): String {
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

    fun validateInputs(
        soilType: String,
        season: String,
        waterAvailability: String,
        landSizeAcres: Double
    ): String? {
        if (soilType.isBlank() || season.isBlank() || waterAvailability.isBlank()) {
            return "INVALID_INPUT"
        }
        if (!landSizeAcres.isFinite() || landSizeAcres <= 0.0 || landSizeAcres > 100_000.0) {
            return "INVALID_LAND_SIZE"
        }
        return null
    }

    suspend fun getRecommendations(
        soilType: String,
        season: String,
        waterAvailability: String,
        landSizeAcres: Double,
        language: AppLanguage
    ): Result<List<CropRecommendation>> = withContext(Dispatchers.IO) {
        val validationError = validateInputs(soilType, season, waterAvailability, landSizeAcres)
        if (validationError != null) {
            return@withContext Result.failure(Exception(validationError))
        }

        if (GeminiApiClient.isConfigured()) {
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
                val response = GeminiApiClient.executeGenerateContent(request = request)
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
        }

        // Deterministic rule-based agronomy fallback explicitly tagged with LOCAL_RULE_BASED
        Result.success(
            getDeterministicCropRecommendations(
                soil = soilType,
                season = season,
                water = waterAvailability,
                landSizeAcres = landSizeAcres,
                language = language
            )
        )
    }

    fun parseGeminiCropRecommendations(text: String, language: AppLanguage): List<CropRecommendation> {
        val list = mutableListOf<CropRecommendation>()
        val lines = text.split("\n")
        for (line in lines) {
            if (line.contains("|")) {
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 6 && parts[0].isNotBlank()) {
                    val level = when {
                        parts[1].contains("HIGH", true) -> "HIGH"
                        parts[1].contains("LOW", true) -> "LOW"
                        else -> "MODERATE"
                    }
                    val badge = suitabilityBadgeFor(level, language)
                    list.add(
                        CropRecommendation(
                            cropName = parts[0].trimStart('-', '*', '1', '2', '3', '4', '.', ' '),
                            suitabilityLevel = level,
                            suitabilityBadge = badge,
                            description = parts[5],
                            yieldEstimate = parts[2],
                            durationMonths = parts[3],
                            waterNeed = parts[4],
                            source = RecommendationSource.AI
                        )
                    )
                }
            }
        }
        return list
    }

    private fun suitabilityBadgeFor(level: String, language: AppLanguage): String {
        return when (level) {
            "HIGH" -> if (language == AppLanguage.HI) "उच्च उपयुक्तता" else if (language == AppLanguage.PA) "ਉੱਚ ਢੁਕਵਾਂ" else "High Suitability"
            "MODERATE" -> if (language == AppLanguage.HI) "मध्यम उपयुक्तता" else if (language == AppLanguage.PA) "ਦਰਮਿਆਨਾ ਢੁਕਵਾਂ" else "Moderate Suitability"
            else -> if (language == AppLanguage.HI) "कम उपयुक्तता" else if (language == AppLanguage.PA) "ਘੱਟ ਢੁਕਵਾਂ" else "Low Suitability"
        }
    }

    fun getDeterministicCropRecommendations(
        soil: String,
        season: String,
        water: String,
        landSizeAcres: Double = 2.5,
        language: AppLanguage
    ): List<CropRecommendation> {
        val highBadge = suitabilityBadgeFor("HIGH", language)
        val modBadge = suitabilityBadgeFor("MODERATE", language)

        val isRabi = season.contains("Rabi", ignoreCase = true) || season.contains("Winter", ignoreCase = true)
        val isZaid = season.contains("Zaid", ignoreCase = true) || season.contains("Summer", ignoreCase = true)
        val isLowWater = water.contains("Low", ignoreCase = true)
        val isHighWater = water.contains("High", ignoreCase = true)
        val isSandyOrBlack = soil.contains("Sandy", ignoreCase = true) || soil.contains("Black", ignoreCase = true)

        if (isRabi) {
            return listOf(
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "गेहूँ (Wheat)" else if (language == AppLanguage.PA) "ਕਣਕ (Wheat)" else "Wheat",
                    suitabilityLevel = if (isLowWater) "MODERATE" else "HIGH",
                    suitabilityBadge = if (isLowWater) modBadge else highBadge,
                    description = if (language == AppLanguage.HI) "रबी सीजन और $soil के लिए प्रमुख फसल (${landSizeAcres} एकड़)।" else if (language == AppLanguage.PA) "ਹਾੜੀ ਦੇ ਸੀਜ਼ਨ ਅਤੇ $soil ਲਈ ਮੁੱਖ ਫ਼ਸਲ।" else "Primary Rabi winter crop well-suited for $soil with $water water availability.",
                    yieldEstimate = "18-22 Quintals/Acre",
                    durationMonths = "4.5 Months",
                    waterNeed = "Moderate",
                    source = RecommendationSource.LOCAL_RULE_BASED
                ),
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "सरसों (Mustard)" else if (language == AppLanguage.PA) "ਸਰ੍ਹੋਂ (Mustard)" else "Mustard",
                    suitabilityLevel = "HIGH",
                    suitabilityBadge = highBadge,
                    description = if (language == AppLanguage.HI) "कम पानी में उच्च तेल उपज देने वाली रबी फसल।" else if (language == AppLanguage.PA) "ਘੱਟ ਪਾਣੀ ਵਿੱਚ ਵਧੀਆ ਝਾੜ ਦੇਣ ਵਾਲੀ ਫ਼ਸਲ।" else "High suitability winter oilseed crop requiring minimal irrigation in $soil.",
                    yieldEstimate = "7-10 Quintals/Acre",
                    durationMonths = "4 Months",
                    waterNeed = "Low-Moderate",
                    source = RecommendationSource.LOCAL_RULE_BASED
                ),
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "चना (Chickpea / Gram)" else if (language == AppLanguage.PA) "ਛੋਲੇ (Chickpea)" else "Chickpea (Gram)",
                    suitabilityLevel = if (isLowWater || isSandyOrBlack) "HIGH" else "MODERATE",
                    suitabilityBadge = if (isLowWater || isSandyOrBlack) highBadge else modBadge,
                    description = if (language == AppLanguage.HI) "मिट्टी में नाइट्रोजन स्तर सुधारने और कम सिंचाई के लिए उपयुक्त।" else if (language == AppLanguage.PA) "ਮਿੱਟੀ ਦੀ ਸਿਹਤ ਅਤੇ ਘੱਟ ਪਾਣੀ ਲਈ ਵਧੀਆ।" else "Drought-tolerant winter pulse crop that enriches soil nitrogen.",
                    yieldEstimate = "8-11 Quintals/Acre",
                    durationMonths = "4 Months",
                    waterNeed = "Low",
                    source = RecommendationSource.LOCAL_RULE_BASED
                ),
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "जौ (Barley)" else if (language == AppLanguage.PA) "ਜੌਂ (Barley)" else "Barley",
                    suitabilityLevel = "MODERATE",
                    suitabilityBadge = modBadge,
                    description = if (language == AppLanguage.HI) "हल्की और मध्यम मिट्टी में कम लागत वाली रबी फसल।" else if (language == AppLanguage.PA) "ਹਲਕੀ ਮਿੱਟੀ ਵਿੱਚ ਘੱਟ ਲਾਗਤ ਵਾਲੀ ਫ਼ਸਲ।" else "Hardy winter cereal tolerant of variable moisture and alkaline soils.",
                    yieldEstimate = "14-18 Quintals/Acre",
                    durationMonths = "4 Months",
                    waterNeed = "Low-Moderate",
                    source = RecommendationSource.LOCAL_RULE_BASED
                )
            )
        }

        if (isZaid) {
            return listOf(
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "मूंग (Green Gram / Moong)" else if (language == AppLanguage.PA) "ਮੂੰਗੀ (Moong)" else "Green Gram (Moong)",
                    suitabilityLevel = "HIGH",
                    suitabilityBadge = highBadge,
                    description = if (language == AppLanguage.HI) "जायद (गर्मी) सीजन के लिए 60-65 दिन की अल्पावधि दलहनी फसल।" else if (language == AppLanguage.PA) "ਗਰਮੀਆਂ ਦੇ ਸੀਜ਼ਨ ਲਈ 60-65 ਦਿਨਾਂ ਦੀ ਫ਼ਸਲ।" else "Short-duration summer pulse (60-65 days) ideal between Rabi and Kharif.",
                    yieldEstimate = "5-7 Quintals/Acre",
                    durationMonths = "2 Months",
                    waterNeed = "Low-Moderate",
                    source = RecommendationSource.LOCAL_RULE_BASED
                ),
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "तरबूज / ककड़ी (Cucurbits)" else if (language == AppLanguage.PA) "ਤਰਬੂਜ / ਖੀਰਾ (Cucurbits)" else "Watermelon / Cucumber",
                    suitabilityLevel = if (isLowWater) "MODERATE" else "HIGH",
                    suitabilityBadge = if (isLowWater) modBadge else highBadge,
                    description = if (language == AppLanguage.HI) "$soil में गर्मी के मौसम में उच्च बाज़ार मूल्य वाली फसल।" else if (language == AppLanguage.PA) "ਗਰਮੀਆਂ ਵਿੱਚ ਵਧੀਆ ਆਮਦਨ ਦੇਣ ਵਾਲੀ ਫ਼ਸਲ।" else "High-value summer cash crop suited for well-drained $soil.",
                    yieldEstimate = "80-120 Quintals/Acre",
                    durationMonths = "2.5 Months",
                    waterNeed = "Moderate",
                    source = RecommendationSource.LOCAL_RULE_BASED
                ),
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "उड़द (Black Gram)" else if (language == AppLanguage.PA) "ਮਾਂਹ (Black Gram)" else "Black Gram (Urad)",
                    suitabilityLevel = "MODERATE",
                    suitabilityBadge = modBadge,
                    description = if (language == AppLanguage.HI) "गर्मी के मौसम में कम पानी और मिट्टी सुधार के लिए उपयुक्त।" else if (language == AppLanguage.PA) "ਮਿੱਟੀ ਦੀ ਉਪਜਾਊ ਸ਼ਕਤੀ ਵਧਾਉਣ ਲਈ ਢੁਕਵੀਂ।" else "Summer legume crop that thrives under warm temperatures and moderate moisture.",
                    yieldEstimate = "5-6 Quintals/Acre",
                    durationMonths = "2.5 Months",
                    waterNeed = "Low-Moderate",
                    source = RecommendationSource.LOCAL_RULE_BASED
                ),
                CropRecommendation(
                    cropName = if (language == AppLanguage.HI) "चारा मक्का (Fodder Maize)" else if (language == AppLanguage.PA) "ਚਾਰਾ ਮੱਕੀ (Fodder Maize)" else "Fodder Maize",
                    suitabilityLevel = "MODERATE",
                    suitabilityBadge = modBadge,
                    description = if (language == AppLanguage.HI) "पशुपालन और गर्मी के हरे चारे के लिए तेज़ बढ़ने वाली फसल।" else if (language == AppLanguage.PA) "ਗਰਮੀਆਂ ਵਿੱਚ ਹਰੇ ਚਾਰੇ ਲਈ ਵਧੀਆ ਫ਼ਸਲ।" else "Fast-growing summer green fodder crop for mixed livestock farms.",
                    yieldEstimate = "120-150 Quintals/Acre",
                    durationMonths = "2 Months",
                    waterNeed = "Moderate",
                    source = RecommendationSource.LOCAL_RULE_BASED
                )
            )
        }

        // Default: Kharif (Monsoon) adjusted for water & soil
        return listOf(
            CropRecommendation(
                cropName = if (isLowWater) {
                    if (language == AppLanguage.HI) "बाजरा (Pearl Millet)" else if (language == AppLanguage.PA) "ਬਾਜਰਾ (Pearl Millet)" else "Pearl Millet (Bajra)"
                } else {
                    if (language == AppLanguage.HI) "चावल / धान (Rice)" else if (language == AppLanguage.PA) "ਝੋਨਾ (Rice)" else "Rice"
                },
                suitabilityLevel = "HIGH",
                suitabilityBadge = highBadge,
                description = if (isLowWater) {
                    if (language == AppLanguage.HI) "कम पानी और $soil के लिए अत्यधिक सूखा-सहिष्णु खरीफ फसल।" else if (language == AppLanguage.PA) "ਘੱਟ ਪਾਣੀ ਅਤੇ $soil ਲਈ ਵਧੀਆ ਖਰੀਫ ਫ਼ਸਲ।" else "Highly drought-resilient Kharif crop ideal for $soil and low water availability."
                } else {
                    if (language == AppLanguage.HI) "आपके जल स्तर ($water) और $soil के लिए उत्कृष्ट खरीफ विकल्प।" else if (language == AppLanguage.PA) "ਤੁਹਾਡੇ ਪਾਣੀ ਦੇ ਪੱਧਰ ਅਤੇ ਮਿੱਟੀ ਲਈ ਵਧੀਆ ਫਸਲ।" else "Excellent Kharif choice for $soil and $water moisture conditions."
                },
                yieldEstimate = if (isLowWater) "10-14 Quintals/Acre" else "22-26 Quintals/Acre",
                durationMonths = if (isLowWater) "3 Months" else "4 Months",
                waterNeed = if (isLowWater) "Low" else "High",
                source = RecommendationSource.LOCAL_RULE_BASED
            ),
            CropRecommendation(
                cropName = if (language == AppLanguage.HI) "मक्का (Maize)" else if (language == AppLanguage.PA) "ਮੱਕੀ (Maize)" else "Maize",
                suitabilityLevel = "HIGH",
                suitabilityBadge = highBadge,
                description = if (language == AppLanguage.HI) "कम पानी की आवश्यकता और अच्छी बाज़ार कीमत।" else if (language == AppLanguage.PA) "ਘੱਟ ਪਾਣੀ ਦੀ ਲੋੜ ਅਤੇ ਵਧੀਆ ਮੰਡੀ ਕੀਮਤ।" else "Moderate water demand with high yield stability and good market demand.",
                yieldEstimate = "18-22 Quintals/Acre",
                durationMonths = "3.5 Months",
                waterNeed = "Moderate",
                source = RecommendationSource.LOCAL_RULE_BASED
            ),
            CropRecommendation(
                cropName = if (language == AppLanguage.HI) "सोयाबीन (Soybean)" else if (language == AppLanguage.PA) "ਸੋਇਆਬੀਨ (Soybean)" else "Soybean",
                suitabilityLevel = if (isHighWater) "MODERATE" else "HIGH",
                suitabilityBadge = if (isHighWater) modBadge else highBadge,
                description = if (language == AppLanguage.HI) "मिट्टी में नाइट्रोजन बढ़ाता है और कम लागत वाली फसल।" else if (language == AppLanguage.PA) "ਮਿੱਟੀ ਵਿੱਚ ਨਾਈਟ੍ਰੋਜਨ ਵਧਾਉਂਦਾ ਹੈ।" else "Fixes soil nitrogen and provides balanced crop rotation benefits.",
                yieldEstimate = "10-14 Quintals/Acre",
                durationMonths = "3.5 Months",
                waterNeed = "Moderate",
                source = RecommendationSource.LOCAL_RULE_BASED
            ),
            CropRecommendation(
                cropName = if (language == AppLanguage.HI) "मूंगफली (Groundnut)" else if (language == AppLanguage.PA) "ਮੂੰਗਫਲੀ (Groundnut)" else "Groundnut",
                suitabilityLevel = "MODERATE",
                suitabilityBadge = modBadge,
                description = if (language == AppLanguage.HI) "बलुई और दोमट मिट्टी में अच्छी पैदावार।" else if (language == AppLanguage.PA) "ਰੇਤਲੀ ਅਤੇ ਦੋਮਟ ਮਿੱਟੀ ਲਈ ਵਧੀਆ।" else "Good yield potential in well-drained soils with moderate irrigation.",
                yieldEstimate = "8-12 Quintals/Acre",
                durationMonths = "4 Months",
                waterNeed = "Low-Moderate",
                source = RecommendationSource.LOCAL_RULE_BASED
            )
        )
    }
}

class DiseaseDetectionRepository {

    fun downscaleBitmap(bitmap: Bitmap, maxDimension: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return bitmap
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val scale = min(maxDimension.toFloat() / width.toFloat(), maxDimension.toFloat() / height.toFloat())
        val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    suspend fun analyzePlantImage(bitmap: Bitmap, language: AppLanguage): Result<PlantDiseaseAnalysis> = withContext(Dispatchers.IO) {
        if (!GeminiApiClient.isConfigured()) {
            return@withContext Result.failure(Exception("KEY_MISSING"))
        }

        try {
            val scaledBitmap = downscaleBitmap(bitmap, maxDimension = 1024)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = """
                Analyze this plant/crop leaf image carefully.
                If the image is blurry, not a plant/leaf, or lacks clear visual evidence, set STATUS: Uncertain and CONFIDENCE below 50.
                Respond strictly in ${language.displayName} (${language.code}).
                Provide structured details formatted exactly as follows:
                DISEASE_NAME: <Name of disease, Healthy Plant, or Uncertain>
                STATUS: <Healthy, Infected, or Uncertain>
                CONFIDENCE: <Integer percentage 0 to 100>
                SYMPTOMS: <Comma-separated observed symptoms, or None>
                TREATMENT: <Comma-separated actionable steps, or None required>
                PREVENTION: <Comma-separated prevention tips>
                SEVERITY: <None, Low, Moderate, or High>
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

            val response = GeminiApiClient.executeGenerateContent(request = request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                return@withContext parseDiseaseAnalysis(responseText, language)
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

    /**
     * Strictly parses the structured AI disease diagnosis response.
     * Never fabricates an "Infected (90%)" diagnosis or synthetic fungicide treatments when
     * required fields are missing or malformed.
     */
    fun parseDiseaseAnalysis(text: String, language: AppLanguage): Result<PlantDiseaseAnalysis> {
        var diseaseName: String? = null
        var healthStatus: String? = null
        var confidence: Int? = null
        val symptoms = mutableListOf<String>()
        val treatment = mutableListOf<String>()
        val prevention = mutableListOf<String>()
        var severity = "UNKNOWN"

        val numberRegex = Regex("""\b(\d{1,3})\b""")

        text.split("\n").forEach { rawLine ->
            val line = rawLine.trim().trimStart('*', '-', '#').trim()
            when {
                line.startsWith("DISEASE_NAME:", ignoreCase = true) -> {
                    val v = line.substringAfter(":").trim().trim('*')
                    if (v.isNotBlank()) diseaseName = v
                }
                line.startsWith("STATUS:", ignoreCase = true) -> {
                    val v = line.substringAfter(":").trim().trim('*')
                    if (v.isNotBlank()) healthStatus = v
                }
                line.startsWith("CONFIDENCE:", ignoreCase = true) -> {
                    val rawConf = line.substringAfter(":").trim()
                    val match = numberRegex.find(rawConf)
                    val parsed = match?.groupValues?.getOrNull(1)?.toIntOrNull()
                    if (parsed != null && parsed in 0..100) {
                        confidence = parsed
                    }
                }
                line.startsWith("SYMPTOMS:", ignoreCase = true) -> {
                    symptoms.addAll(
                        line.substringAfter(":")
                            .split(",")
                            .map { it.trim().trim('*') }
                            .filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) && !it.equals("N/A", ignoreCase = true) }
                    )
                }
                line.startsWith("TREATMENT:", ignoreCase = true) -> {
                    treatment.addAll(
                        line.substringAfter(":")
                            .split(",")
                            .map { it.trim().trim('*') }
                            .filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) && !it.equals("N/A", ignoreCase = true) }
                    )
                }
                line.startsWith("PREVENTION:", ignoreCase = true) -> {
                    prevention.addAll(
                        line.substringAfter(":")
                            .split(",")
                            .map { it.trim().trim('*') }
                            .filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) && !it.equals("N/A", ignoreCase = true) }
                    )
                }
                line.startsWith("SEVERITY:", ignoreCase = true) -> {
                    val v = line.substringAfter(":").trim().trim('*').uppercase()
                    if (v.isNotBlank()) severity = v
                }
            }
        }

        // Reject completely unstructured or malformed responses instead of fabricating a diagnosis
        if (diseaseName.isNullOrBlank() || healthStatus.isNullOrBlank() || confidence == null) {
            return Result.failure(
                Exception(
                    when (language) {
                        AppLanguage.HI -> "अस्पष्ट निदान परिणाम। कृपया पत्ते की स्पष्ट फोटो के साथ पुनः प्रयास करें।"
                        AppLanguage.PA -> "ਅਸਪਸ਼ਟ ਨਿਦਾਨ ਨਤੀਜਾ। ਕਿਰਪਾ ਕਰਕੇ ਪੱਤੇ ਦੀ ਸਾਫ਼ ਫੋਟੋ ਨਾਲ ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ।"
                        AppLanguage.EN -> "Incomplete or uncertain AI diagnosis. Please upload a clearer leaf photo and try again."
                    }
                )
            )
        }

        val validConfidence = confidence!!
        val isUncertain = validConfidence < 60 ||
                healthStatus!!.contains("Uncertain", ignoreCase = true) ||
                diseaseName!!.contains("Uncertain", ignoreCase = true)

        val normalizedStatus = when {
            isUncertain -> when (language) {
                AppLanguage.HI -> "अनिश्चित (स्पष्ट फोटो लें)"
                AppLanguage.PA -> "ਅਨਿਸ਼ਚਿਤ (ਸਾਫ਼ ਫੋਟੋ ਲਓ)"
                AppLanguage.EN -> "Uncertain — Retake Clear Photo"
            }
            healthStatus!!.contains("Healthy", ignoreCase = true) ||
                    healthStatus!!.contains("स्वस्थ", ignoreCase = true) ||
                    healthStatus!!.contains("ਤੰਦਰੁਸਤ", ignoreCase = true) -> healthStatus!!
            else -> healthStatus!!
        }

        val disclaimerText = when (language) {
            AppLanguage.HI -> "अस्वीकरण: यह AI-आधारित प्रारंभिक अनुमान है। किसी भी रासायनिक दवा या कवकनाशी का छिड़काव करने से पहले स्थानीय कृषि विशेषज्ञ या कृषि विज्ञान केंद्र (KVK) से परामर्श लें।"
            AppLanguage.PA -> "ਬੇਦਾਅਵਾ: ਇਹ AI-ਅਧਾਰਿਤ ਸ਼ੁਰੂਆਤੀ ਅੰਦਾਜ਼ਾ ਹੈ। ਕਿਸੇ ਵੀ ਰਸਾਇਣਕ ਸਪਰੇਅ ਤੋਂ ਪਹਿਲਾਂ ਸਥਾਨਕ ਖੇਤੀਬਾੜੀ ਮਾਹਿਰ ਜਾਂ KVK ਨਾਲ ਸਲਾਹ ਕਰੋ।"
            AppLanguage.EN -> "Advisory Disclaimer: AI visual analysis is an indicative estimate only. Verify with a local agronomist or Krishi Vigyan Kendra (KVK) before applying chemical sprays."
        }

        return Result.success(
            PlantDiseaseAnalysis(
                diseaseName = diseaseName!!,
                healthStatus = normalizedStatus,
                confidencePercent = validConfidence,
                symptoms = symptoms,
                treatmentSteps = treatment,
                preventionTips = prevention,
                severityLevel = severity,
                isUncertain = isUncertain,
                disclaimer = disclaimerText
            )
        )
    }
}

class AiAssistantRepository {
    suspend fun getAiResponse(
        userMessage: String,
        conversationHistory: List<AiChatMessage>,
        language: AppLanguage? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedMessage = userMessage.trim()
        if (trimmedMessage.isEmpty()) {
            return@withContext Result.failure(Exception("EMPTY_MESSAGE"))
        }
        val boundedMessage = trimmedMessage.take(GeminiApiClient.MAX_USER_PROMPT_CHARS)

        if (!GeminiApiClient.isConfigured()) {
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

            val isDetailedRequest = boundedMessage.contains("detail", ignoreCase = true) ||
                    boundedMessage.contains("step by step", ignoreCase = true) ||
                    boundedMessage.contains("vistaar", ignoreCase = true) ||
                    boundedMessage.contains("vistrit", ignoreCase = true) ||
                    boundedMessage.contains("explain all", ignoreCase = true) ||
                    boundedMessage.contains("guide", ignoreCase = true) ||
                    boundedMessage.contains("विस्तार", ignoreCase = true)

            val maxTokens = if (isDetailedRequest) 800 else 350

            val contentsList = mutableListOf<GeminiContent>()

            // Limit context window to recent successful messages, ensuring userMessage isn't duplicated
            val recentHistory = conversationHistory
                .filter { it.status == AiMessageStatus.SUCCESS && it.text.isNotBlank() }
                .let { list ->
                    if (list.isNotEmpty() && list.last().isUser && list.last().text.trim() == boundedMessage) {
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
                        parts = listOf(GeminiPart(text = msg.text.take(GeminiApiClient.MAX_USER_PROMPT_CHARS)))
                    )
                )
            }
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = boundedMessage))
                )
            )

            val request = GeminiRequest(
                contents = contentsList,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(maxOutputTokens = maxTokens, temperature = 0.5f)
            )

            val response = GeminiApiClient.executeGenerateContent(request = request)
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
            if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
                return@withContext Result.failure(Exception("Please enter a valid email address"))
            }
            if (password.length < 6) {
                return@withContext Result.failure(Exception("Password must be at least 6 characters"))
            }
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

            // Transparently upgrade legacy SHA-256 hashes to PBKDF2-HMAC-SHA256 on successful login
            var updatedAccount = account
            if (SecurityUtils.isLegacyHash(account.passwordHash)) {
                val newSalt = SecurityUtils.generateSalt()
                val newHash = SecurityUtils.hashPassword(password, newSalt)
                updatedAccount = account.copy(salt = newSalt, passwordHash = newHash, isLoggedIn = true)
                accountDao.updateAccount(updatedAccount)
            }

            accountDao.clearActiveSessions()
            accountDao.setActiveSession(updatedAccount.id)
            Result.success(updatedAccount.copy(isLoggedIn = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(userId: Long, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (newPassword.length < 6) {
                return@withContext Result.failure(Exception("Password must be at least 6 characters"))
            }
            val account = accountDao.getAccountById(userId)
                ?: return@withContext Result.failure(Exception("No active account found"))
            val newSalt = SecurityUtils.generateSalt()
            val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
            accountDao.updateAccount(account.copy(salt = newSalt, passwordHash = newHash))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccountById(userId: Long) = withContext(Dispatchers.IO) {
        accountDao.deleteAccountById(userId)
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
