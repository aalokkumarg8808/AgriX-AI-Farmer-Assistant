package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AgriXDatabase
import com.example.data.location.DeviceCoordinates
import com.example.data.location.LocationService
import com.example.data.location.LocationStatus
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMessageStatus
import com.example.data.model.CropRecommendation
import com.example.data.model.FarmFieldEntity
import com.example.data.model.FarmTaskEntity
import com.example.data.model.GuideEntity
import com.example.data.model.PlantDiseaseAnalysis
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WeatherData
import com.example.data.repository.AiAssistantRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CropRecommendationRepository
import com.example.data.repository.DiseaseDetectionRepository
import com.example.data.repository.FarmRepository
import com.example.data.repository.GuideRepository
import com.example.data.repository.ProfileRepository
import com.example.data.repository.WeatherRepository
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.localization.TimeOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class Screen {
    WELCOME,
    LOGIN,
    CREATE_ACCOUNT,
    SETUP,
    HOME,
    FARM,
    AI_ASSISTANT,
    GUIDES,
    PROFILE,
    EDIT_PROFILE,
    SETTINGS,
    CROP_REC,
    DISEASE_DETECTION,
    WEATHER_DETAIL
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AgriXDatabase.getDatabase(application)

    private val authRepo = AuthRepository(db.userAccountDao(), db.userProfileDao())
    private val profileRepo = ProfileRepository(db.userProfileDao(), db.farmFieldDao(), db.farmTaskDao())
    private val farmRepo = FarmRepository(db.farmFieldDao(), db.farmTaskDao())
    private val guideRepo = GuideRepository(db.guideDao())
    private val weatherRepo = WeatherRepository()
    private val locationService = LocationService(application)
    private val cropRecRepo = CropRecommendationRepository()
    private val diseaseRepo = DiseaseDetectionRepository()
    private val aiRepo = AiAssistantRepository()

    // Screen State
    private val _currentScreen = MutableStateFlow(Screen.WELCOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Language State
    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Active Logged-in Account
    val activeAccount: StateFlow<UserAccountEntity?> = authRepo.activeAccount
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // User Profile - Scoped to Active User
    val userProfile: StateFlow<UserProfileEntity?> = activeAccount.flatMapLatest { account ->
        if (account != null) {
            profileRepo.getProfileForUser(account.id)
        } else {
            profileRepo.firstProfile
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Location State
    private val _locationStatus = MutableStateFlow<LocationStatus>(LocationStatus.Initial)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    // Fields & Tasks - Scoped to Active User
    val farmFields: StateFlow<List<FarmFieldEntity>> = activeAccount.flatMapLatest { account ->
        val userId = account?.id ?: 1
        farmRepo.getFieldsForUser(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmTasks: StateFlow<List<FarmTaskEntity>> = activeAccount.flatMapLatest { account ->
        val userId = account?.id ?: 1
        farmRepo.getTasksForUser(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Guides
    private val _selectedGuideCategory = MutableStateFlow("All")
    val selectedGuideCategory: StateFlow<String> = _selectedGuideCategory.asStateFlow()

    val guides: StateFlow<List<GuideEntity>> = guideRepo.allGuides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weather State
    private val _weatherState = MutableStateFlow<UiState<WeatherData>>(UiState.Loading)
    val weatherState: StateFlow<UiState<WeatherData>> = _weatherState.asStateFlow()

    // Active Coordinates for Live Weather
    private var lastKnownCoords: DeviceCoordinates? = null

    // Crop Recommendation Inputs & State
    var soilTypeInput = MutableStateFlow("Loamy Soil")
    var seasonInput = MutableStateFlow("Kharif (Monsoon)")
    var waterAvailabilityInput = MutableStateFlow("Moderate")
    var landSizeInput = MutableStateFlow("2.5")

    private val _cropRecState = MutableStateFlow<UiState<List<CropRecommendation>>>(UiState.Idle)
    val cropRecState: StateFlow<UiState<List<CropRecommendation>>> = _cropRecState.asStateFlow()

    // Disease Detection State
    private val _selectedDiseaseImage = MutableStateFlow<Bitmap?>(null)
    val selectedDiseaseImage: StateFlow<Bitmap?> = _selectedDiseaseImage.asStateFlow()

    private val _diseaseState = MutableStateFlow<UiState<PlantDiseaseAnalysis>>(UiState.Idle)
    val diseaseState: StateFlow<UiState<PlantDiseaseAnalysis>> = _diseaseState.asStateFlow()

    // AI Chat Messages
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private var hasHandledInitialNavigation = false

    init {
        // Monitor active account for session & startup navigation
        viewModelScope.launch {
            activeAccount.collect { account ->
                if (!hasHandledInitialNavigation) {
                    hasHandledInitialNavigation = true
                    if (account == null) {
                        _currentScreen.value = Screen.WELCOME
                    } else if (!account.isSetupCompleted) {
                        _currentScreen.value = Screen.SETUP
                    } else {
                        _currentScreen.value = Screen.HOME
                    }
                }
            }
        }

        // Sync language and recommendation defaults with saved SQLite profile
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile != null) {
                    // Sync Language
                    if (profile.preferredLanguage.isNotBlank()) {
                        val lang = when (profile.preferredLanguage.uppercase()) {
                            "HI" -> AppLanguage.HI
                            "PA" -> AppLanguage.PA
                            else -> AppLanguage.EN
                        }
                        _currentLanguage.value = lang
                    }
                    // Sync Recommendation Defaults
                    if (profile.soilType.isNotBlank()) soilTypeInput.value = profile.soilType
                    if (profile.irrigationType.isNotBlank()) waterAvailabilityInput.value = profile.irrigationType
                    if (profile.farmSizeAcres > 0) landSizeInput.value = profile.farmSizeAcres.toString()
                }
            }
        }

        // Initialize real location and weather
        loadDeviceLocationAndWeather()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Authentication Methods
    fun createAccount(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepo.createAccount(email, pass)
            result.onSuccess {
                onResult(true, null)
            }.onFailure {
                onResult(false, it.message ?: "Failed to create account")
            }
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean, Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepo.login(email, pass)
            result.onSuccess { account ->
                onResult(true, account.isSetupCompleted, null)
            }.onFailure {
                onResult(false, false, it.message ?: "Invalid email or password")
            }
        }
    }

    fun completeSetup(
        fullName: String,
        farmName: String,
        location: String,
        phone: String,
        farmSize: Double,
        mainCrop: String,
        otherCrops: String,
        experience: Int,
        irrigation: String,
        soilType: String,
        language: AppLanguage,
        onComplete: () -> Unit
    ) {
        val currentUserId = activeAccount.value?.id ?: 1
        val newProfile = UserProfileEntity(
            id = currentUserId,
            userId = currentUserId,
            fullName = fullName,
            farmName = farmName,
            location = location,
            phone = phone,
            farmSizeAcres = farmSize,
            primaryCrop = mainCrop,
            otherCrops = otherCrops,
            experienceYears = experience,
            irrigationType = irrigation,
            soilType = soilType,
            preferredLanguage = language.name
        )

        _currentLanguage.value = language

        viewModelScope.launch {
            authRepo.completeSetup(currentUserId, newProfile)
            onComplete()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _currentScreen.value = Screen.WELCOME
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        val currentUserId = activeAccount.value?.id ?: 1
        viewModelScope.launch(Dispatchers.IO) {
            val current = profileRepo.getUserProfileDirect(currentUserId) ?: UserProfileEntity(id = currentUserId, userId = currentUserId)
            profileRepo.saveProfile(current.copy(preferredLanguage = lang.name, id = currentUserId, userId = currentUserId))
        }
        refreshCurrentWeather()
    }

    fun getTimeOfDay(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11 -> TimeOfDay.MORNING
            hour in 12..16 -> TimeOfDay.AFTERNOON
            else -> TimeOfDay.EVENING
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            loadDeviceLocationAndWeather()
        } else {
            _locationStatus.value = LocationStatus.PermissionDenied()
            val profileLoc = userProfile.value?.location
            if (!profileLoc.isNullOrBlank()) {
                fetchWeatherForNamedLocation(profileLoc)
            } else {
                _weatherState.value = UiState.Error("Location permission denied. Enable location or set your farm location in profile.")
            }
        }
    }

    fun detectLocationForSetup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val coords = locationService.getRealDeviceLocation()
            if (coords != null) {
                val resolved = locationService.reverseGeocode(coords.latitude, coords.longitude)
                onResult(resolved)
            } else {
                onResult("")
            }
        }
    }

    fun loadDeviceLocationAndWeather() {
        viewModelScope.launch {
            if (!locationService.hasLocationPermission()) {
                _locationStatus.value = LocationStatus.RequestingPermission
                val profileLoc = userProfile.value?.location
                if (!profileLoc.isNullOrBlank()) {
                    fetchWeatherForNamedLocation(profileLoc)
                } else {
                    _weatherState.value = UiState.Error("Please allow location access to load real-time local weather.")
                }
                return@launch
            }

            _locationStatus.value = LocationStatus.Locating
            _weatherState.value = UiState.Loading

            val coords = locationService.getRealDeviceLocation()
            if (coords != null) {
                lastKnownCoords = coords
                val resolvedName = locationService.reverseGeocode(coords.latitude, coords.longitude)
                _locationStatus.value = LocationStatus.Success(coords, resolvedName)

                val result = weatherRepo.fetchWeather(
                    locationName = resolvedName,
                    lat = coords.latitude,
                    lon = coords.longitude,
                    language = _currentLanguage.value
                )
                result.onSuccess {
                    _weatherState.value = UiState.Success(it)
                }.onFailure {
                    _weatherState.value = UiState.Error(it.message ?: "Failed to load weather.")
                }
            } else {
                _locationStatus.value = LocationStatus.Unavailable()
                val profileLoc = userProfile.value?.location
                if (!profileLoc.isNullOrBlank()) {
                    fetchWeatherForNamedLocation(profileLoc)
                } else {
                    _weatherState.value = UiState.Error("Device GPS position unavailable. Tap retry or set farm location in profile.")
                }
            }
        }
    }

    fun refreshCurrentWeather() {
        val coords = lastKnownCoords
        if (coords != null) {
            viewModelScope.launch {
                val result = weatherRepo.fetchWeather(
                    lat = coords.latitude,
                    lon = coords.longitude,
                    language = _currentLanguage.value
                )
                result.onSuccess {
                    _weatherState.value = UiState.Success(it)
                }.onFailure {
                    _weatherState.value = UiState.Error(it.message ?: "Failed to fetch weather")
                }
            }
        } else {
            loadDeviceLocationAndWeather()
        }
    }

    fun fetchWeatherForNamedLocation(locationName: String) {
        _weatherState.value = UiState.Loading
        viewModelScope.launch {
            val result = weatherRepo.fetchWeather(
                locationName = locationName,
                language = _currentLanguage.value
            )
            result.onSuccess {
                _weatherState.value = UiState.Success(it)
            }.onFailure { e ->
                val msg = when (e.message) {
                    "LOCATION_UNRESOLVED" -> "Unable to find coordinates for '$locationName'. Please check spelling."
                    else -> e.message ?: "Failed to fetch weather"
                }
                _weatherState.value = UiState.Error(msg)
            }
        }
    }

    // Crop Recommendations
    fun loadCropRecommendations() {
        _cropRecState.value = UiState.Loading
        viewModelScope.launch {
            val land = landSizeInput.value.toDoubleOrNull() ?: 2.5
            val result = cropRecRepo.getRecommendations(
                soilType = soilTypeInput.value,
                season = seasonInput.value,
                waterAvailability = waterAvailabilityInput.value,
                landSizeAcres = land,
                language = _currentLanguage.value
            )
            result.onSuccess {
                _cropRecState.value = UiState.Success(it)
            }.onFailure {
                _cropRecState.value = UiState.Error(it.message ?: "Unable to get recommendations")
            }
        }
    }

    // Disease Detection
    fun setSelectedDiseaseImage(bitmap: Bitmap?) {
        _selectedDiseaseImage.value = bitmap
        _diseaseState.value = UiState.Idle
    }

    fun analyzePlantDisease() {
        val bitmap = _selectedDiseaseImage.value ?: run {
            _diseaseState.value = UiState.Error("Please select or take an image first.")
            return
        }
        _diseaseState.value = UiState.Loading
        viewModelScope.launch {
            val result = diseaseRepo.analyzePlantImage(bitmap, _currentLanguage.value)
            result.onSuccess {
                _diseaseState.value = UiState.Success(it)
            }.onFailure {
                _diseaseState.value = UiState.Error(it.message ?: "Analysis failed")
            }
        }
    }

    // AI Assistant
    fun ensureInitialGreeting(language: AppLanguage) {
        if (_aiMessages.value.isEmpty()) {
            val greeting = when (language) {
                AppLanguage.HI -> "नमस्ते! मैं आपका AgriX AI कृषि सहायक हूँ। फसलों, मौसम, खाद या खेती से जुड़े कोई भी सवाल पूछें—हिंदी, English या Hinglish में!"
                AppLanguage.PA -> "ਸਤ ਸ੍ਰੀ ਅਕਾਲ! ਮੈਂ ਤੁਹਾਡਾ AgriX AI ਖੇਤੀ ਸਹਾਇਕ ਹਾਂ। ਫ਼ਸਲਾਂ, ਮੌਸਮ, ਖਾਦਾਂ ਜਾਂ ਖੇਤੀ ਬਾਰੇ ਕੋਈ ਵੀ ਸਵਾਲ ਪੁੱਛੋ—English, ਪੰਜਾਬੀ ਜਾਂ Hinglish ਵਿੱਚ!"
                AppLanguage.EN -> "Hello! I am your AgriX AI Farming Assistant. Ask me anything about crops, soil, fertilizers, pests, or farming in English, हिंदी, or Hinglish!"
            }
            _aiMessages.value = listOf(
                AiChatMessage(
                    isUser = false,
                    text = greeting,
                    status = AiMessageStatus.SUCCESS
                )
            )
        }
    }

    fun sendAiQuestion(userText: String) {
        if (userText.isBlank() || _isAiLoading.value) return

        val userMsg = AiChatMessage(isUser = true, text = userText, status = AiMessageStatus.SUCCESS)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val result = aiRepo.getAiResponse(userText, _aiMessages.value)
            _isAiLoading.value = false
            result.onSuccess { replyText ->
                val aiMsg = AiChatMessage(
                    isUser = false,
                    text = replyText,
                    status = AiMessageStatus.SUCCESS
                )
                _aiMessages.value = _aiMessages.value + aiMsg
            }.onFailure { e ->
                val errorKey = when (e.message) {
                    "KEY_MISSING" -> "error_key_missing"
                    "RATE_LIMIT" -> "error_rate_limit"
                    "AUTH_ERROR" -> "error_auth"
                    "PERMISSION_ERROR" -> "error_permission"
                    "TIMEOUT" -> "error_timeout"
                    "SERVER_ERROR" -> "error_server"
                    "NO_INTERNET" -> "error_no_internet"
                    else -> "error_server"
                }
                val errorText = Localization.getString(errorKey, _currentLanguage.value)
                val aiMsg = AiChatMessage(
                    isUser = false,
                    text = errorText,
                    status = AiMessageStatus.ERROR,
                    errorType = e.message
                )
                _aiMessages.value = _aiMessages.value + aiMsg
            }
        }
    }

    fun retryLastQuestion() {
        if (_isAiLoading.value) return

        val lastUserMsg = _aiMessages.value.lastOrNull { it.isUser } ?: return

        val lastMsg = _aiMessages.value.lastOrNull()
        if (lastMsg != null && !lastMsg.isUser && lastMsg.status == AiMessageStatus.ERROR) {
            _aiMessages.value = _aiMessages.value.dropLast(1)
        }

        _isAiLoading.value = true
        viewModelScope.launch {
            val result = aiRepo.getAiResponse(lastUserMsg.text, _aiMessages.value)
            _isAiLoading.value = false
            result.onSuccess { replyText ->
                val aiMsg = AiChatMessage(
                    isUser = false,
                    text = replyText,
                    status = AiMessageStatus.SUCCESS
                )
                _aiMessages.value = _aiMessages.value + aiMsg
            }.onFailure { e ->
                val errorKey = when (e.message) {
                    "KEY_MISSING" -> "error_key_missing"
                    "RATE_LIMIT" -> "error_rate_limit"
                    "AUTH_ERROR" -> "error_auth"
                    "PERMISSION_ERROR" -> "error_permission"
                    "TIMEOUT" -> "error_timeout"
                    "SERVER_ERROR" -> "error_server"
                    "NO_INTERNET" -> "error_no_internet"
                    else -> "error_server"
                }
                val errorText = Localization.getString(errorKey, _currentLanguage.value)
                val aiMsg = AiChatMessage(
                    isUser = false,
                    text = errorText,
                    status = AiMessageStatus.ERROR,
                    errorType = e.message
                )
                _aiMessages.value = _aiMessages.value + aiMsg
            }
        }
    }

    // Farm Management
    fun addField(name: String, crop: String, area: Double, status: String) {
        val currentUserId = activeAccount.value?.id ?: 1
        viewModelScope.launch {
            farmRepo.addField(
                FarmFieldEntity(
                    userId = currentUserId,
                    name = name,
                    cropName = crop,
                    areaAcres = area,
                    status = status
                )
            )
        }
    }

    fun deleteField(field: FarmFieldEntity) {
        viewModelScope.launch {
            farmRepo.deleteField(field)
        }
    }

    fun addTask(title: String) {
        val currentUserId = activeAccount.value?.id ?: 1
        viewModelScope.launch {
            farmRepo.addTask(
                FarmTaskEntity(
                    userId = currentUserId,
                    title = title
                )
            )
        }
    }

    fun toggleTask(task: FarmTaskEntity) {
        viewModelScope.launch {
            farmRepo.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            farmRepo.deleteTask(taskId)
        }
    }

    // Profile & Settings Management
    fun saveProfile(profile: UserProfileEntity) {
        val currentUserId = activeAccount.value?.id ?: 1
        viewModelScope.launch {
            profileRepo.saveProfile(profile.copy(id = currentUserId, userId = currentUserId))
        }
    }

    fun deleteAccountData() {
        val currentUserId = activeAccount.value?.id ?: 1
        viewModelScope.launch {
            profileRepo.clearAccountData(currentUserId)
            authRepo.logout()
            navigateTo(Screen.WELCOME)
        }
    }

    fun updateSettings(
        themeMode: String? = null,
        weatherAlerts: Boolean? = null,
        farmingReminders: Boolean? = null,
        cropUpdates: Boolean? = null
    ) {
        val currentUserId = activeAccount.value?.id ?: 1
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity(id = currentUserId, userId = currentUserId)
            profileRepo.saveProfile(
                current.copy(
                    id = currentUserId,
                    userId = currentUserId,
                    themeMode = themeMode ?: current.themeMode,
                    weatherAlertsEnabled = weatherAlerts ?: current.weatherAlertsEnabled,
                    farmingRemindersEnabled = farmingReminders ?: current.farmingRemindersEnabled,
                    cropUpdatesEnabled = cropUpdates ?: current.cropUpdatesEnabled
                )
            )
        }
    }

    fun setGuideCategory(category: String) {
        _selectedGuideCategory.value = category
    }
}
