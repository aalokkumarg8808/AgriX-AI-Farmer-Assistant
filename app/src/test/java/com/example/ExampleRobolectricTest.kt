package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.graphics.Color
import com.example.ui.components.parseInlineMarkdown
import com.example.ui.components.parseMarkdownBlocks
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.FarmScreen
import com.example.ui.theme.AgriXTheme
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

  @org.junit.Test
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

  @org.junit.Test
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

  @org.junit.Test
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
    
    // Check navigation state flow is initial WELCOME for onboarding
    assertEquals(Screen.WELCOME, viewModel.currentScreen.value)
    
    // Navigate to HOME then FARM
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
  fun `test AI message status and error handling`() {
    val userMsg = com.example.data.model.AiChatMessage(isUser = true, text = "Hello", status = com.example.data.model.AiMessageStatus.SUCCESS)
    assertEquals(com.example.data.model.AiMessageStatus.SUCCESS, userMsg.status)

    val errorMsg = com.example.data.model.AiChatMessage(
        isUser = false,
        text = "AI service is temporarily unavailable. Please try again shortly.",
        status = com.example.data.model.AiMessageStatus.ERROR,
        errorType = "RATE_LIMIT"
    )
    assertEquals(com.example.data.model.AiMessageStatus.ERROR, errorMsg.status)
    assertEquals("RATE_LIMIT", errorMsg.errorType)
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
    viewModel.ensureInitialGreeting(com.example.localization.AppLanguage.EN)
    val msgs = viewModel.aiMessages.value
    assertEquals(1, msgs.size)
    assertEquals(false, msgs[0].isUser)
    assertEquals(true, msgs[0].text.contains("AgriX AI"))
  }
}
