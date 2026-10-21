package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyForecast
import com.example.data.model.WeatherData
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.ui.components.ErrorStateView
import com.example.ui.components.LoadingStateView
import com.example.ui.components.WeatherCard
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiState

@Composable
fun WeatherDetailScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val colors = LocalAgriXCustomColors.current

    var customLocation by remember { mutableStateOf("") }
    var showLocationInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.getString("weather_title", currentLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            IconButton(onClick = { viewModel.refreshCurrentWeather() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = Localization.getString("refresh", currentLanguage),
                    tint = AgriXGreenPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Location Search / Override Bar
            item {
                if (showLocationInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customLocation,
                            onValueChange = { customLocation = it },
                            placeholder = { Text("Enter town / district") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("weather_location_search_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface,
                                focusedBorderColor = AgriXGreenPrimary,
                                unfocusedBorderColor = colors.borderSubtle
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AgriXGreenPrimary,
                            modifier = Modifier
                                .clickable {
                                    if (customLocation.isNotBlank()) {
                                        viewModel.fetchWeatherForNamedLocation(customLocation.trim())
                                        showLocationInput = false
                                    }
                                }
                                .testTag("weather_location_search_button")
                        ) {
                            Text(
                                text = "Search",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.getString("current_weather", currentLanguage),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = Localization.getString("change_location", currentLanguage),
                            fontSize = 12.sp,
                            color = AgriXGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showLocationInput = true }
                                .testTag("change_location_button")
                        )
                    }
                }
            }

            // Current Weather Main Card
            when (val state = weatherState) {
                is UiState.Loading -> item {
                    LoadingStateView(Localization.getString("loading", currentLanguage))
                }
                is UiState.Error -> item {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.loadDeviceLocationAndWeather() }
                    )
                }
                is UiState.Success -> {
                    item {
                        val isGps = state.data.isVerifiedGps
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isGps) colors.cardGreenBg else colors.cardBlueBg,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isGps) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isGps) colors.cardGreenText else colors.cardBlueText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isGps) {
                                        Localization.getString("gps_verified", currentLanguage)
                                    } else {
                                        Localization.getString("manual_location", currentLanguage)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGps) colors.cardGreenText else colors.cardBlueText
                                )
                            }
                        }
                    }

                    item {
                        WeatherCard(
                            weather = state.data,
                            onClick = {}
                        )
                    }

                    // 5-Day Extended Weather Forecast Card
                    item {
                        FiveDayForecastCard(
                            current = state.data,
                            language = currentLanguage,
                            colors = colors
                        )
                    }
                }
                else -> {}
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun FiveDayForecastCard(
    current: WeatherData,
    language: AppLanguage,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = Localization.getString("weather_forecast_5days", language),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val dailyItems: List<DailyForecast> = if (current.dailyForecast.isNotEmpty()) {
                current.dailyForecast
            } else {
                listOf(
                    DailyForecast(
                        dateLabel = "Today",
                        maxTempC = current.temperatureC,
                        minTempC = current.temperatureC,
                        conditionText = current.conditionText,
                        rainProbabilityPercent = current.rainProbabilityPercent
                    )
                )
            }

            dailyItems.forEach { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day.dateLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1.1f)
                    )

                    Row(
                        modifier = Modifier.weight(1.2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isRainy = day.rainProbabilityPercent > 40
                        Icon(
                            imageVector = if (isRainy) Icons.Default.WbCloudy else Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (isRainy) Color(0xFF0284C7) else Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = day.conditionText,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }

                    Text(
                        text = "${day.maxTempC.toInt()}° / ${day.minTempC.toInt()}°C",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriXGreenPrimary
                    )
                }
            }
        }
    }
}
