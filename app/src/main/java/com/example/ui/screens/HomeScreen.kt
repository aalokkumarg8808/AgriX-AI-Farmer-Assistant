package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Localization
import com.example.ui.components.AgriXTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.GreetingHeader
import com.example.ui.components.HorizontalFieldCardItem
import com.example.ui.components.LoadingStateView
import com.example.ui.components.QuickActionsGrid
import com.example.ui.components.SummaryStatsRow
import com.example.ui.components.WeatherCard
import com.example.ui.theme.AgriXGreenLight
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val fields by viewModel.farmFields.collectAsState()
    val colors = LocalAgriXCustomColors.current

    val locationStatus by viewModel.locationStatus.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        viewModel.onLocationPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (weatherState !is UiState.Success &&
            (locationStatus is com.example.data.location.LocationStatus.Initial ||
                    locationStatus is com.example.data.location.LocationStatus.RequestingPermission)
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val rawName = userProfile?.fullName ?: ""
    val defaultFarmer = "Farmer"
    val firstName = if (rawName.isNotBlank()) {
        rawName.trim().split("\\s+".toRegex()).firstOrNull { it.isNotBlank() } ?: defaultFarmer
    } else {
        defaultFarmer
    }
    val timeOfDay = viewModel.getTimeOfDay()
    val greeting = Localization.getGreeting(currentLanguage, timeOfDay, firstName)

    val totalAcres = fields.sumOf { it.areaAcres }
    val cropCount = fields.map { it.cropName }.distinct().filter { it.isNotBlank() }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AgriXTopBar(
            currentLanguage = currentLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dynamic Time-Based Greeting with First Name
            item {
                GreetingHeader(greetingText = greeting)
            }

            // Real-Time Weather Card
            item {
                when (val state = weatherState) {
                    is UiState.Loading -> LoadingStateView(Localization.getString("loading", currentLanguage))
                    is UiState.Success -> WeatherCard(
                        weather = state.data,
                        onClick = { viewModel.navigateTo(Screen.WEATHER_DETAIL) }
                    )
                    is UiState.Error -> ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.loadDeviceLocationAndWeather() }
                    )
                    else -> {}
                }
            }

            // Stat Summary Cards - Shown only when user has farm data
            if (fields.isNotEmpty()) {
                item {
                    SummaryStatsRow(
                        cropCount = cropCount,
                        acresCount = totalAcres,
                        soilHealth = "Active"
                    )
                }
            }

            // Quick Actions Grid
            item {
                QuickActionsGrid(
                    onCropRecClick = { viewModel.navigateTo(Screen.CROP_REC) },
                    onDiseaseClick = { viewModel.navigateTo(Screen.DISEASE_DETECTION) },
                    onWeatherClick = { viewModel.navigateTo(Screen.WEATHER_DETAIL) },
                    onGuidesClick = { viewModel.navigateTo(Screen.GUIDES) },
                    language = currentLanguage
                )
            }

            // Today's Farming Forecast
            item {
                if (weatherState is UiState.Success) {
                    val weather = (weatherState as UiState.Success).data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(AgriXGreenLight, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Forecast",
                                        tint = AgriXGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = Localization.getString("todays_forecast", currentLanguage),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = weather.farmingForecast,
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // My Fields Section with View All
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.getString("my_fields", currentLanguage),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = Localization.getString("view_all", currentLanguage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriXGreenPrimary,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.FARM) }
                    )
                }
            }

            // Fields Horizontal Scroll List
            if (fields.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        EmptyStateView(
                            message = Localization.getString("no_farm_added_yet", currentLanguage),
                            actionText = Localization.getString("add_your_farm", currentLanguage),
                            onAction = { viewModel.navigateTo(Screen.FARM) }
                        )
                    }
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(fields, key = { it.id }) { field ->
                            HorizontalFieldCardItem(
                                field = field,
                                onClick = { viewModel.navigateTo(Screen.FARM) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
