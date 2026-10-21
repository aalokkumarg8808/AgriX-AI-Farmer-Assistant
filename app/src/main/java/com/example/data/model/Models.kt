package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val isSetupCompleted: Boolean = false,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val userId: Long = 1,
    val fullName: String = "",
    val farmName: String = "",
    val location: String = "",
    val phone: String = "",
    val farmSizeAcres: Double = 0.0,
    val primaryCrop: String = "",
    val otherCrops: String = "",
    val soilType: String = "",
    val irrigationType: String = "",
    val experienceYears: Int = 0,
    val preferredLanguage: String = "EN",
    val profileImageUri: String? = null,
    val themeMode: String = "System",
    val weatherAlertsEnabled: Boolean = true,
    val farmingRemindersEnabled: Boolean = true,
    val cropUpdatesEnabled: Boolean = true
)

@Entity(tableName = "farm_fields")
data class FarmFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val name: String,
    val cropName: String,
    val areaAcres: Double,
    val status: String, // "Growing", "Sown", "Preparing"
    val soilHealth: String = "78%"
)

@Entity(tableName = "farm_tasks")
data class FarmTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val fieldId: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: String = "Today"
)

@Entity(tableName = "guides")
data class GuideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Crops", "Fertilizer", "Irrigation", "Pests", "Soil"
    val titleEn: String,
    val titleHi: String,
    val titlePa: String,
    val contentEn: String,
    val contentHi: String,
    val contentPa: String,
    val readTimeMinutes: Int = 3
)

data class DailyForecast(
    val dateLabel: String,
    val maxTempC: Double,
    val minTempC: Double,
    val conditionText: String,
    val rainProbabilityPercent: Int,
    val weatherCode: Int = 0
)

data class WeatherData(
    val locationName: String,
    val temperatureC: Double,
    val conditionText: String,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val rainProbabilityPercent: Int,
    val farmingForecast: String,
    val dailyForecast: List<DailyForecast> = emptyList(),
    val isVerifiedGps: Boolean = false
)

enum class RecommendationSource {
    AI,
    LOCAL_RULE_BASED
}

data class CropRecommendation(
    val cropName: String,
    val suitabilityLevel: String, // "HIGH", "MODERATE", "LOW"
    val suitabilityBadge: String,
    val description: String,
    val yieldEstimate: String,
    val durationMonths: String,
    val waterNeed: String,
    val source: RecommendationSource = RecommendationSource.AI
)

data class PlantDiseaseAnalysis(
    val diseaseName: String,
    val healthStatus: String,
    val confidencePercent: Int,
    val symptoms: List<String>,
    val treatmentSteps: List<String>,
    val preventionTips: List<String>,
    val severityLevel: String,
    val isUncertain: Boolean = false,
    val disclaimer: String = ""
)

enum class AiMessageStatus {
    SUCCESS,
    LOADING,
    ERROR
}

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val status: AiMessageStatus = AiMessageStatus.SUCCESS,
    val errorType: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
