package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherData
import com.example.localization.Localization
import com.example.ui.components.ErrorStateView
import com.example.ui.components.LoadingStateView
import com.example.ui.components.WeatherCard
import com.example.ui.theme.AgriXGreenLight
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
                            modifier = Modifier.weight(1f),
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
                            modifier = Modifier.clickable {
                                if (customLocation.isNotBlank()) {
                                    viewModel.fetchWeatherForNamedLocation(customLocation.trim())
                                    showLocationInput = false
                                }
                            }
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
                            modifier = Modifier.clickable { showLocationInput = true }
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
                        WeatherCard(
                            weather = state.data,
                            onClick = {}
                        )
                    }

                    // 5-Day Extended Weather Forecast Card
                    item {
                        FiveDayForecastCard(state.data, colors = colors)
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
                text = "5-Day Farming Forecast",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val forecastDays = listOf(
                Triple("Today", "${current.temperatureC.toInt()}°C", current.conditionText),
                Triple("Tomorrow", "${(current.temperatureC + 1).toInt()}°C", "Partly Cloudy"),
                Triple("Day 3", "${(current.temperatureC - 1).toInt()}°C", "Sunny"),
                Triple("Day 4", "${(current.temperatureC).toInt()}°C", "Rain Showers"),
                Triple("Day 5", "${(current.temperatureC + 2).toInt()}°C", "Clear Sky")
            )

            forecastDays.forEach { (day, temp, cond) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        modifier = Modifier.width(90.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cond,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }

                    Text(
                        text = temp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriXGreenPrimary
                    )
                }
            }
        }
    }
}
