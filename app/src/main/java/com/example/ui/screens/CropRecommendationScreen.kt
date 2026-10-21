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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropRecommendation
import com.example.data.model.RecommendationSource
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.ui.components.ErrorStateView
import com.example.ui.components.LoadingStateView
import com.example.ui.theme.AgriXGreenLight
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.BadgeGrowingBg
import com.example.ui.theme.BadgeGrowingText
import com.example.ui.theme.BadgePreparingBg
import com.example.ui.theme.BadgePreparingText
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiState

@Composable
fun CropRecommendationScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val cropRecState by viewModel.cropRecState.collectAsState()
    val colors = LocalAgriXCustomColors.current

    val soilType by viewModel.soilTypeInput.collectAsState()
    val season by viewModel.seasonInput.collectAsState()
    val waterAvailability by viewModel.waterAvailabilityInput.collectAsState()
    val landSize by viewModel.landSizeInput.collectAsState()

    val soilOptions = listOf("Loamy Soil", "Clay Soil", "Sandy Soil", "Black Soil", "Silt Soil")
    val seasonOptions = listOf("Kharif (Monsoon)", "Rabi (Winter)", "Zaid (Summer)")
    val waterOptions = listOf("High", "Moderate", "Low")

    LaunchedEffect(Unit) {
        if (cropRecState is UiState.Idle) {
            viewModel.loadCropRecommendations()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = Localization.getString("crop_rec_title", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Input Controls Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Farm Conditions & Inputs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        // Soil Selector
                        SelectorRow(
                            label = Localization.getString("soil_type", currentLanguage),
                            selectedValue = soilType,
                            options = soilOptions,
                            icon = Icons.Default.Info,
                            onSelected = { viewModel.soilTypeInput.value = it },
                            textColor = colors.textPrimary,
                            subTextColor = colors.textSecondary,
                            surfaceColor = colors.surface
                        )

                        // Season Selector
                        SelectorRow(
                            label = Localization.getString("season", currentLanguage),
                            selectedValue = season,
                            options = seasonOptions,
                            icon = Icons.Default.WbSunny,
                            onSelected = { viewModel.seasonInput.value = it },
                            textColor = colors.textPrimary,
                            subTextColor = colors.textSecondary,
                            surfaceColor = colors.surface
                        )

                        // Water Selector
                        SelectorRow(
                            label = Localization.getString("water_availability", currentLanguage),
                            selectedValue = waterAvailability,
                            options = waterOptions,
                            icon = Icons.Default.WaterDrop,
                            onSelected = { viewModel.waterAvailabilityInput.value = it },
                            textColor = colors.textPrimary,
                            subTextColor = colors.textSecondary,
                            surfaceColor = colors.surface
                        )

                        // Land Size (Acres) Input
                        OutlinedTextField(
                            value = landSize,
                            onValueChange = { viewModel.landSizeInput.value = it },
                            label = { Text(Localization.getString("land_size", currentLanguage)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Terrain,
                                    contentDescription = null,
                                    tint = AgriXGreenPrimary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("crop_rec_land_size_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface,
                                focusedBorderColor = AgriXGreenPrimary,
                                unfocusedBorderColor = colors.borderSubtle
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { viewModel.loadCropRecommendations() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("get_crop_recommendations_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriXGreenPrimary)
                        ) {
                            Text(
                                text = Localization.getString("get_recommendations", currentLanguage),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Recommended Crops Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Localization.getString("recommended_crops", currentLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            // Recommendations List
            when (val state = cropRecState) {
                is UiState.Loading -> item { LoadingStateView("Finding best crops for your farm...") }
                is UiState.Error -> item {
                    val friendlyMessage = when (state.message) {
                        "KEY_MISSING" -> Localization.getString("error_key_missing", currentLanguage)
                        "RATE_LIMIT" -> Localization.getString("error_rate_limit", currentLanguage)
                        "INVALID_LAND_SIZE" -> Localization.getString("error_invalid_land_size", currentLanguage)
                        else -> state.message
                    }
                    ErrorStateView(
                        message = friendlyMessage,
                        onRetry = { viewModel.loadCropRecommendations() }
                    )
                }
                is UiState.Success -> {
                    val isFallback = state.data.firstOrNull()?.source == RecommendationSource.LOCAL_RULE_BASED
                    item {
                        SourceIndicatorBanner(
                            isFallback = isFallback,
                            language = currentLanguage,
                            colors = colors
                        )
                    }
                    items(state.data) { crop ->
                        CropRecommendationCard(crop = crop, colors = colors)
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.cardBlueBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Disclaimer",
                                    tint = colors.cardBlueText,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Localization.getString("crop_disclaimer", currentLanguage),
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
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
private fun SourceIndicatorBanner(
    isFallback: Boolean,
    language: AppLanguage,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    val label = if (isFallback) {
        Localization.getString("source_local_fallback", language)
    } else {
        Localization.getString("source_ai", language)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFallback) colors.cardOrangeBg else colors.cardGreenBg,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crop_rec_source_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isFallback) Icons.Default.Info else Icons.Default.AutoAwesome,
                contentDescription = label,
                tint = if (isFallback) colors.cardOrangeText else colors.cardGreenText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFallback) colors.cardOrangeText else colors.cardGreenText
            )
        }
    }
}

@Composable
private fun SelectorRow(
    label: String,
    selectedValue: String,
    options: List<String>,
    icon: ImageVector,
    onSelected: (String) -> Unit,
    textColor: Color,
    subTextColor: Color,
    surfaceColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AgriXGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AgriXGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }

        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue,
                    fontSize = 14.sp,
                    color = subTextColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select",
                    tint = subTextColor
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(surfaceColor)
            ) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, color = textColor) },
                        onClick = {
                            onSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CropRecommendationCard(
    crop: CropRecommendation,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AgriXGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = crop.cropName,
                    tint = AgriXGreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = crop.cropName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    val isHigh = crop.suitabilityLevel == "HIGH"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHigh) BadgeGrowingBg else BadgePreparingBg
                    ) {
                        Text(
                            text = crop.suitabilityBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHigh) BadgeGrowingText else BadgePreparingText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = crop.description,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 16.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Yield: ${crop.yieldEstimate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgriXGreenPrimary
                    )
                    Text(
                        text = "Duration: ${crop.durationMonths}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
