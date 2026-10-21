package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AgriXBottomBar
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.CreateAccountScreen
import com.example.ui.screens.CropRecommendationScreen
import com.example.ui.screens.DiseaseDetectionScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.FarmScreen
import com.example.ui.screens.GuidesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.WeatherDetailScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.AgriXTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val userProfile by viewModel.userProfile.collectAsState()
            val themeMode = userProfile?.themeMode ?: "System"
            AgriXTheme(themeMode = themeMode) {
                AgriXApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AgriXApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    // Handle back button for non-root screens
    when (currentScreen) {
        Screen.WELCOME, Screen.HOME -> {
            // Default system back behavior
        }
        Screen.LOGIN, Screen.CREATE_ACCOUNT, Screen.SETUP -> {
            BackHandler {
                viewModel.navigateTo(Screen.WELCOME)
            }
        }
        Screen.EDIT_PROFILE, Screen.SETTINGS -> {
            BackHandler {
                viewModel.navigateTo(Screen.PROFILE)
            }
        }
        else -> {
            BackHandler {
                viewModel.navigateTo(Screen.HOME)
            }
        }
    }

    val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    val isAuthScreen = currentScreen == Screen.WELCOME ||
            currentScreen == Screen.LOGIN ||
            currentScreen == Screen.CREATE_ACCOUNT ||
            currentScreen == Screen.SETUP

    val showBottomBar = !isImeVisible &&
            !isAuthScreen &&
            currentScreen != Screen.EDIT_PROFILE &&
            currentScreen != Screen.SETTINGS

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (showBottomBar) {
                AgriXBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    language = currentLanguage
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.WELCOME -> WelcomeScreen(viewModel = viewModel)
                Screen.LOGIN -> LoginScreen(viewModel = viewModel)
                Screen.CREATE_ACCOUNT -> CreateAccountScreen(viewModel = viewModel)
                Screen.SETUP -> SetupScreen(viewModel = viewModel)
                Screen.HOME -> HomeScreen(viewModel = viewModel)
                Screen.FARM -> FarmScreen(viewModel = viewModel)
                Screen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                Screen.GUIDES -> GuidesScreen(viewModel = viewModel)
                Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                Screen.EDIT_PROFILE -> EditProfileScreen(viewModel = viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                Screen.CROP_REC -> CropRecommendationScreen(viewModel = viewModel)
                Screen.DISEASE_DETECTION -> DiseaseDetectionScreen(viewModel = viewModel)
                Screen.WEATHER_DETAIL -> WeatherDetailScreen(viewModel = viewModel)
            }
        }
    }
}
