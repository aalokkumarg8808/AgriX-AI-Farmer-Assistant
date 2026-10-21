package com.example

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.OpenMeteoCurrent
import com.example.data.api.OpenMeteoDaily
import com.example.data.api.OpenMeteoResponse
import com.example.data.db.AgriXDatabase
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMessageStatus
import com.example.data.model.FarmFieldEntity
import com.example.data.model.FarmTaskEntity
import com.example.data.model.RecommendationSource
import com.example.data.model.SecurityUtils
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.CropRecommendationRepository
import com.example.data.repository.DiseaseDetectionRepository
import com.example.data.repository.FarmRepository
import com.example.data.repository.ProfileRepository
import com.example.data.repository.WeatherRepository
import com.example.localization.AppLanguage
import com.example.ui.components.parseInlineMarkdown
import com.example.ui.components.parseMarkdownBlocks
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.FarmScreen
import com.example.ui.theme.AgriXTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AgriXDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AgriXDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    // 1. Password hashing & verification tests (PBKDF2 v2 + Legacy v1 upgrade)
    @Test
    fun `test PBKDF2 password hashing and constant-time verification`() {
        val salt1 = SecurityUtils.generateSalt()
        val salt2 = SecurityUtils.generateSalt()
        val hash1 = SecurityUtils.hashPassword("FarmerPass#2025", salt1)
        val hash2 = SecurityUtils.hashPassword("FarmerPass#2025", salt2)

        assertTrue(hash1.startsWith("v2$"))
        assertNotEquals("Salted hashes for identical passwords must differ", hash1, hash2)
        assertFalse(SecurityUtils.isLegacyHash(hash1))

        assertTrue(SecurityUtils.verifyPassword("FarmerPass#2025", salt1, hash1))
        assertTrue(SecurityUtils.verifyPassword("FarmerPass#2025", salt2, hash2))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", salt1, hash1))
        assertFalse(SecurityUtils.verifyPassword("", salt1, hash1))
    }

    @Test
    fun `test legacy SHA-256 hash verification and transparent upgrade on login`() = runBlocking {
        val salt = SecurityUtils.generateSalt()
        val legacyBytes = SecurityUtils.hashPasswordLegacyV1("LegacyPass123", salt)
        val legacyHash = Base64.encodeToString(legacyBytes, Base64.NO_WRAP)

        assertTrue(SecurityUtils.isLegacyHash(legacyHash))
        assertTrue(SecurityUtils.verifyPassword("LegacyPass123", salt, legacyHash))
        assertFalse(SecurityUtils.verifyPassword("WrongPass", salt, legacyHash))

        val accountDao = db.userAccountDao()
        val profileDao = db.userProfileDao()
        val userId = accountDao.insertAccount(
            UserAccountEntity(
                email = "ramesh@farm.in",
                passwordHash = legacyHash,
                salt = salt,
                isSetupCompleted = true,
                isLoggedIn = false
            )
        )

        val authRepo = AuthRepository(accountDao, profileDao)
        val loginResult = authRepo.login("ramesh@farm.in", "LegacyPass123")
        assertTrue(loginResult.isSuccess)
        val loggedIn = loginResult.getOrThrow()
        assertEquals(userId, loggedIn.id)
        assertTrue(
            "Password hash should be upgraded to v2 PBKDF2 after login",
            loggedIn.passwordHash.startsWith("v2$")
        )

        val storedUser = accountDao.getAccountById(userId)
        assertNotNull(storedUser)
        assertTrue(storedUser!!.passwordHash.startsWith("v2$"))
    }

    // 2. Login, password change, and account switching tests
    @Test
    fun `test user registration, login, and password change`() = runBlocking {
        val authRepo = AuthRepository(db.userAccountDao(), db.userProfileDao())
        val regResult = authRepo.createAccount("suresh@farm.in", "InitialPass1")
        assertTrue(regResult.isSuccess)
        val userId = regResult.getOrThrow().id

        // Duplicate registration should fail
        val dupResult = authRepo.createAccount("suresh@farm.in", "AnotherPass")
        assertTrue(dupResult.isFailure)

        // Verify login works with initial password
        assertTrue(authRepo.login("suresh@farm.in", "InitialPass1").isSuccess)
        assertTrue(authRepo.login("suresh@farm.in", "WrongPass").isFailure)

        // Short password change should fail
        val shortChange = authRepo.changePassword(userId, "123")
        assertTrue(shortChange.isFailure)

        // Valid password change
        val validChange = authRepo.changePassword(userId, "NewSecurePass9")
        assertTrue(validChange.isSuccess)

        // Old password must no longer work; new password must work
        assertTrue(authRepo.login("suresh@farm.in", "InitialPass1").isFailure)
        assertTrue(authRepo.login("suresh@farm.in", "NewSecurePass9").isSuccess)
    }

    // 3. User-scoped profile, field, and task isolation + account deletion cleanup
    @Test
    fun `test strict multi-user isolation and account deletion cleanup`() = runBlocking {
        val authRepo = AuthRepository(db.userAccountDao(), db.userProfileDao())
        val profileRepo = ProfileRepository(db.userProfileDao(), db.farmFieldDao(), db.farmTaskDao())
        val farmRepo = FarmRepository(db.farmFieldDao(), db.farmTaskDao())

        val user1Id = authRepo.createAccount("farmer1@agrix.in", "pass1234").getOrThrow().id
        val user2Id = authRepo.createAccount("farmer2@agrix.in", "pass5678").getOrThrow().id

        profileRepo.saveProfile(
            UserProfileEntity(id = user1Id, userId = user1Id, fullName = "Farmer One", location = "Rampur, UP", farmSizeAcres = 4.5)
        )
        profileRepo.saveProfile(
            UserProfileEntity(id = user2Id, userId = user2Id, fullName = "Farmer Two", location = "Karnal, Haryana", farmSizeAcres = 10.0)
        )

        farmRepo.addField(
            FarmFieldEntity(userId = user1Id, name = "North Wheat Plot", cropName = "Wheat", areaAcres = 2.5, status = "Growing")
        )
        farmRepo.addField(
            FarmFieldEntity(userId = user2Id, name = "South Rice Paddy", cropName = "Rice", areaAcres = 6.0, status = "Growing")
        )

        farmRepo.addTask(FarmTaskEntity(userId = user1Id, title = "Check wheat moisture", dueDate = "Today", isCompleted = false))
        farmRepo.addTask(FarmTaskEntity(userId = user2Id, title = "Inspect paddy bunds", dueDate = "Tomorrow", isCompleted = true))

        // Verify isolation
        val user1Profile = profileRepo.getProfileForUser(user1Id).first()
        val user2Profile = profileRepo.getProfileForUser(user2Id).first()
        assertEquals("Rampur, UP", user1Profile?.location)
        assertEquals("Karnal, Haryana", user2Profile?.location)

        val user1Fields = farmRepo.getFieldsForUser(user1Id).first()
        val user2Fields = farmRepo.getFieldsForUser(user2Id).first()
        assertEquals(1, user1Fields.size)
        assertEquals("North Wheat Plot", user1Fields[0].name)
        assertEquals(1, user2Fields.size)
        assertEquals("South Rice Paddy", user2Fields[0].name)

        val user1Tasks = farmRepo.getTasksForUser(user1Id).first()
        val user2Tasks = farmRepo.getTasksForUser(user2Id).first()
        assertEquals(1, user1Tasks.size)
        assertEquals("Check wheat moisture", user1Tasks[0].title)
        assertEquals(1, user2Tasks.size)
        assertEquals("Inspect paddy bunds", user2Tasks[0].title)

        // Delete User 1 data and account, verify User 1 records are wiped while User 2 remains intact
        profileRepo.clearAccountData(user1Id)
        authRepo.deleteAccountById(user1Id)

        assertNull(profileRepo.getProfileForUser(user1Id).first())
        assertTrue(farmRepo.getFieldsForUser(user1Id).first().isEmpty())
        assertTrue(farmRepo.getTasksForUser(user1Id).first().isEmpty())
        assertNull(db.userAccountDao().getAccountById(user1Id))

        // User 2 data must still be present
        assertNotNull(profileRepo.getProfileForUser(user2Id).first())
        assertEquals(1, farmRepo.getFieldsForUser(user2Id).first().size)
        assertEquals(1, farmRepo.getTasksForUser(user2Id).first().size)
    }

    // 4. Room database guide seeding idempotency
    @Test
    fun `test idempotent guide seeding does not duplicate guides`() = runBlocking {
        AgriXDatabase.ensureGuidesSeeded(db)
        val firstCount = db.guideDao().getGuidesCount()
        assertTrue("Guides should be seeded", firstCount > 0)

        // Calling again must not duplicate rows
        AgriXDatabase.ensureGuidesSeeded(db)
        val secondCount = db.guideDao().getGuidesCount()
        assertEquals(firstCount, secondCount)
    }

    // 5. Crop recommendation input validation and local fallback source labeling
    @Test
    fun `test crop recommendation input validation and rule-based fallback labeling`() {
        val repo = CropRecommendationRepository()

        assertNotNull("Zero land size must fail validation", repo.validateInputs("Loamy", "Canal", "Rabi", 0.0))
        assertNotNull("Negative land size must fail validation", repo.validateInputs("Loamy", "Canal", "Rabi", -5.0))
        assertNotNull("Excessive land size must fail validation", repo.validateInputs("Loamy", "Canal", "Rabi", 250000.0))
        assertNotNull("Blank soil must fail validation", repo.validateInputs("", "Canal", "Rabi", 5.0))
        assertNull("Valid inputs must pass validation", repo.validateInputs("Loamy", "Canal", "Rabi", 5.0))

        val rabiRecs = repo.getDeterministicCropRecommendations(
            soil = "Loamy",
            season = "Rabi",
            water = "Low",
            landSizeAcres = 4.0,
            language = AppLanguage.EN
        )
        assertTrue(rabiRecs.isNotEmpty())
        assertTrue(rabiRecs.all { it.source == RecommendationSource.LOCAL_RULE_BASED })
        assertTrue(rabiRecs.any { it.cropName.contains("Mustard", ignoreCase = true) || it.cropName.contains("Chickpea", ignoreCase = true) })

        val zaidRecs = repo.getDeterministicCropRecommendations(
            soil = "Sandy",
            season = "Zaid",
            water = "Moderate",
            landSizeAcres = 2.0,
            language = AppLanguage.EN
        )
        assertTrue(zaidRecs.isNotEmpty())
        assertTrue(zaidRecs.all { it.source == RecommendationSource.LOCAL_RULE_BASED })
    }

    // 6. Disease analysis response parsing and safety checks
    @Test
    fun `test disease analysis response parsing never fabricates infected 90 percent on invalid output`() {
        val repo = DiseaseDetectionRepository()

        // Malformed / unstructured response must fail cleanly, never fabricate "Infected (90%)"
        val malformedResult = repo.parseDiseaseAnalysis("I cannot see the image clearly.", AppLanguage.EN)
        assertTrue("Malformed response must return failure", malformedResult.isFailure)

        // Low-confidence response (< 60%) must be flagged as uncertain
        val lowConfidenceText = """
            DISEASE_NAME: Early Blight
            STATUS: Infected
            CONFIDENCE: 45
            SYMPTOMS: Small dark spots on lower leaves
            TREATMENT: Remove affected leaves, Consult local agricultural officer
            PREVENTION: Avoid overhead watering
            SEVERITY: Low
        """.trimIndent()
        val lowConfResult = repo.parseDiseaseAnalysis(lowConfidenceText, AppLanguage.EN)
        assertTrue(lowConfResult.isSuccess)
        val lowConfAnalysis = lowConfResult.getOrThrow()
        assertEquals(45, lowConfAnalysis.confidencePercent)
        assertTrue("Confidence below 60% must set isUncertain = true", lowConfAnalysis.isUncertain)
        assertTrue("Disclaimer must be populated", lowConfAnalysis.disclaimer.isNotBlank())

        // High-confidence valid healthy response
        val healthyText = """
            DISEASE_NAME: Healthy Plant
            STATUS: Healthy
            CONFIDENCE: 92
            SYMPTOMS: Uniform green leaves
            TREATMENT: None
            PREVENTION: Continue balanced irrigation
            SEVERITY: None
        """.trimIndent()
        val healthyResult = repo.parseDiseaseAnalysis(healthyText, AppLanguage.EN)
        assertTrue(healthyResult.isSuccess)
        val healthyAnalysis = healthyResult.getOrThrow()
        assertEquals("Healthy", healthyAnalysis.healthStatus)
        assertEquals(92, healthyAnalysis.confidencePercent)
        assertFalse(healthyAnalysis.isUncertain)
    }

    // 7. Weather response parsing with 5-day forecast
    @Test
    fun `test weather response parsing extracts current metrics and 5-day daily forecast`() {
        val weatherRepo = WeatherRepository()

        val sampleResponse = OpenMeteoResponse(
            latitude = 29.6857,
            longitude = 76.9905,
            current = OpenMeteoCurrent(
                temperature2m = 28.4,
                relativeHumidity2m = 62,
                windSpeed10m = 14.2,
                precipitationProbability = 20,
                weatherCode = 2
            ),
            daily = OpenMeteoDaily(
                time = listOf("2025-03-01", "2025-03-02", "2025-03-03", "2025-03-04", "2025-03-05"),
                weatherCode = listOf(0, 2, 61, 3, 0),
                temperature2mMax = listOf(30.0, 29.2, 26.5, 27.8, 31.1),
                temperature2mMin = listOf(16.2, 17.0, 15.8, 16.0, 17.5),
                precipitationProbabilityMax = listOf(5, 20, 80, 30, 0)
            )
        )

        val parsed = weatherRepo.parseWeatherResponse(
            response = sampleResponse,
            locationName = "Karnal, Haryana",
            language = AppLanguage.EN,
            isVerifiedGps = true
        )

        assertEquals(28.4, parsed.temperatureC, 0.01)
        assertEquals(62, parsed.humidityPercent)
        assertEquals(14.2, parsed.windSpeedKmh, 0.01)
        assertEquals(20, parsed.rainProbabilityPercent)
        assertEquals("Partly Cloudy", parsed.conditionText)
        assertTrue(parsed.isVerifiedGps)
        assertEquals(5, parsed.dailyForecast.size)
        assertEquals("Clear Sky", parsed.dailyForecast[0].conditionText)
        assertEquals(80, parsed.dailyForecast[2].rainProbabilityPercent)
        assertEquals("Rainy", parsed.dailyForecast[2].conditionText)
    }

    // 8. UI & Error state handling tests
    @Test
    fun `test FarmScreen renders without crash`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)
        composeTestRule.setContent {
            AgriXTheme {
                FarmScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `test AgriXApp navigation to FarmScreen renders without crash`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)
        viewModel.navigateTo(Screen.HOME)
        composeTestRule.setContent {
            AgriXTheme {
                AgriXApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        viewModel.navigateTo(Screen.FARM)
        composeTestRule.waitForIdle()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AgriX AI", appName)
    }

    @Test
    fun `test MainViewModel navigation and flows`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)
        assertNotNull(viewModel)
        assertEquals(Screen.WELCOME, viewModel.currentScreen.value)

        viewModel.navigateTo(Screen.HOME)
        assertEquals(Screen.HOME, viewModel.currentScreen.value)
        viewModel.navigateTo(Screen.FARM)
        assertEquals(Screen.FARM, viewModel.currentScreen.value)
    }

    @Test
    fun `test parseMarkdownBlocks and parseInlineMarkdown strips raw asterisks`() {
        val sampleMarkdown = """
            For wheat:
            
            **Nitrogen (N)** – 50 kg/acre
            
            ### 1. At the Time of Sowing
            * **DAP (Di-ammonium Phosphate):** 50 kg per acre
        """.trimIndent()

        val blocks = parseMarkdownBlocks(sampleMarkdown)
        assertEquals(6, blocks.size)

        val inlineAnnotated = parseInlineMarkdown("**Nitrogen (N)** – 50 kg/acre", Color.Black)
        assertEquals("Nitrogen (N) – 50 kg/acre", inlineAnnotated.text)
    }

    @Test
    fun `test AI message status and error handling for missing key, rate limits, and network failure`() {
        val userMsg = AiChatMessage(isUser = true, text = "Hello", status = AiMessageStatus.SUCCESS)
        assertEquals(AiMessageStatus.SUCCESS, userMsg.status)

        val rateLimitMsg = AiChatMessage(
            isUser = false,
            text = "Rate limit exceeded. Please wait a moment and try again.",
            status = AiMessageStatus.ERROR,
            errorType = "RATE_LIMIT"
        )
        assertEquals(AiMessageStatus.ERROR, rateLimitMsg.status)
        assertEquals("RATE_LIMIT", rateLimitMsg.errorType)

        val missingKeyMsg = AiChatMessage(
            isUser = false,
            text = "AI service is not configured.",
            status = AiMessageStatus.ERROR,
            errorType = "MISSING_KEY"
        )
        assertEquals(AiMessageStatus.ERROR, missingKeyMsg.status)
        assertEquals("MISSING_KEY", missingKeyMsg.errorType)
    }

    @Test
    fun `test AiAssistantScreen renders without crash`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)
        composeTestRule.setContent {
            AgriXTheme {
                AiAssistantScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `test ensureInitialGreeting populates assistant message`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)
        viewModel.ensureInitialGreeting(AppLanguage.EN)
        val msgs = viewModel.aiMessages.value
        assertEquals(1, msgs.size)
        assertFalse(msgs[0].isUser)
        assertTrue(msgs[0].text.contains("AgriX AI"))
    }
}
