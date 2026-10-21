package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FarmFieldEntity
import com.example.data.model.FarmTaskEntity
import com.example.data.model.WeatherData
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.ui.theme.AgriXBackground
import com.example.ui.theme.AgriXGreenLight
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.theme.BadgeGrowingBg
import com.example.ui.theme.BadgeGrowingText
import com.example.ui.theme.BadgePreparingBg
import com.example.ui.theme.BadgePreparingText
import com.example.ui.theme.BadgeSownBg
import com.example.ui.theme.BadgeSownText
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardBlueBg
import com.example.ui.theme.CardGreenBg
import com.example.ui.theme.CardOrangeBg
import com.example.ui.theme.CardPurpleBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen

@Composable
fun AgriXTopBar(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    val colors = LocalAgriXCustomColors.current
    var showLangMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AgriX Brand
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White), // White background for the custom rounded logo
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_agrix_ai_logo),
                    contentDescription = "AgriX AI Logo",
                    tint = Color.Unspecified, // Keep original vector colors
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "AgriX AI",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        // Language & Notification Action
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cardGreenBg,
                    modifier = Modifier.clickable { showLangMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = colors.cardGreenText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLanguage.code.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.cardGreenText
                        )
                    }
                }

                DropdownMenu(
                    expanded = showLangMenu,
                    onDismissRequest = { showLangMenu = false }
                ) {
                    AppLanguage.values().forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${lang.nativeName} (${lang.displayName})",
                                    fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                    color = colors.textPrimary
                                )
                            },
                            onClick = {
                                onLanguageSelected(lang)
                                showLangMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingHeader(
    greetingText: String
) {
    val colors = LocalAgriXCustomColors.current
    Text(
        text = greetingText,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = colors.textPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp)
    )
}

@Composable
fun WeatherCard(
    weather: WeatherData,
    onClick: () -> Unit
) {
    val colors = LocalAgriXCustomColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("weather_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBlueBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = weather.locationName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Temp & Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${weather.temperatureC.toInt()}°C",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = weather.conditionText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White) // Keep white for weather icon circle
                        .border(1.dp, colors.borderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (weather.rainProbabilityPercent > 40) Icons.Default.WbCloudy else Icons.Default.WbSunny,
                        contentDescription = "Weather condition",
                        tint = if (weather.rainProbabilityPercent > 40) Color(0xFF0284C7) else Color(0xFFF59E0B),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    icon = Icons.Default.WaterDrop,
                    iconTint = if (colors.textPrimary == Color(0xFF111827)) Color(0xFF0284C7) else Color(0xFF60A5FA),
                    label = "Humidity",
                    value = "${weather.humidityPercent}%"
                )
                MetricItem(
                    icon = Icons.Default.Air,
                    iconTint = colors.textMuted,
                    label = "Wind",
                    value = "${weather.windSpeedKmh.toInt()} km/h"
                )
                MetricItem(
                    icon = Icons.Default.WbCloudy,
                    iconTint = if (colors.textPrimary == Color(0xFF111827)) Color(0xFF2563EB) else Color(0xFF93C5FD),
                    label = "Rain Chance",
                    value = "${weather.rainProbabilityPercent}%"
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    val colors = LocalAgriXCustomColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun SummaryStatsRow(
    cropCount: Int,
    acresCount: Double,
    soilHealth: String
) {
    val colors = LocalAgriXCustomColors.current
    val acresFormatted = if (acresCount > 0) String.format("%.1f", acresCount) else "0"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatItemCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Eco,
            iconBg = colors.cardGreenBg,
            iconTint = colors.cardGreenText,
            value = "$cropCount",
            label = "Crops"
        )
        StatItemCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Home,
            iconBg = colors.cardBlueBg,
            iconTint = colors.cardBlueText,
            value = acresFormatted,
            label = "Acres"
        )
        StatItemCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CheckCircle,
            iconBg = colors.cardOrangeBg,
            iconTint = colors.cardOrangeText,
            value = soilHealth,
            label = "Soil Health"
        )
    }
}

@Composable
private fun StatItemCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String
) {
    val colors = LocalAgriXCustomColors.current
    Surface(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onCropRecClick: () -> Unit,
    onDiseaseClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onGuidesClick: () -> Unit,
    language: AppLanguage
) {
    val colors = LocalAgriXCustomColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionTile(
                modifier = Modifier.weight(1f),
                title = Localization.getString("action_crop_rec", language),
                icon = Icons.Default.Eco,
                bgColor = colors.cardGreenBg,
                iconColor = colors.cardGreenText,
                onClick = onCropRecClick
            )
            QuickActionTile(
                modifier = Modifier.weight(1f),
                title = Localization.getString("action_fertilizer_guide", language),
                icon = Icons.Default.MenuBook,
                bgColor = colors.cardOrangeBg,
                iconColor = colors.cardOrangeText,
                onClick = onGuidesClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionTile(
                modifier = Modifier.weight(1f),
                title = Localization.getString("action_weather_update", language),
                icon = Icons.Default.WbSunny,
                bgColor = colors.cardBlueBg,
                iconColor = colors.cardBlueText,
                onClick = onWeatherClick
            )
            QuickActionTile(
                modifier = Modifier.weight(1f),
                title = Localization.getString("action_disease_detection", language),
                icon = Icons.Default.AutoAwesome,
                bgColor = colors.cardPurpleBg,
                iconColor = colors.cardPurpleText,
                onClick = onDiseaseClick
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalAgriXCustomColors.current
    Surface(
        modifier = modifier
            .height(115.dp) // Adjusted height slightly for better spacing and text wrap safety
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.25f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go",
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalFieldCardItem(
    field: FarmFieldEntity,
    onClick: () -> Unit = {}
) {
    val colors = LocalAgriXCustomColors.current
    Surface(
        modifier = Modifier
            .width(165.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Thumbnail image with chevron arrow overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.cardGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = field.cropName ?: "Crop",
                    tint = colors.cardGreenText,
                    modifier = Modifier.size(36.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Field Name
            Text(
                text = field.name ?: "Field",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Crop Name & Status Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = field.cropName ?: "Crop",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                val statusStr = field.status ?: "Growing"
                val badgeColors = when (statusStr.lowercase()) {
                    "growing" -> colors.badgeGrowingBg to colors.badgeGrowingText
                    "sown" -> colors.badgeSownBg to colors.badgeSownText
                    else -> colors.badgePreparingBg to colors.badgePreparingText
                }
                val badgeBg = badgeColors.first
                val badgeText = badgeColors.second

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = statusStr,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FieldCardItem(
    field: FarmFieldEntity,
    onClick: () -> Unit = {}
) {
    val colors = LocalAgriXCustomColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cardGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = field.cropName,
                    tint = colors.cardGreenText,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = field.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${field.cropName} • ${field.areaAcres} Acres",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }

            // Status Badge
            val (badgeBg, badgeText) = when (field.status.lowercase()) {
                "growing" -> colors.badgeGrowingBg to colors.badgeGrowingText
                "sown" -> colors.badgeSownBg to colors.badgeSownText
                else -> colors.badgePreparingBg to colors.badgePreparingText
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = badgeBg
            ) {
                Text(
                    text = field.status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Details",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TaskRowItem(
    task: FarmTaskEntity,
    onToggle: () -> Unit
) {
    val colors = LocalAgriXCustomColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) AgriXGreenPrimary else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (task.isCompleted) AgriXGreenPrimary else colors.borderSubtle,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (task.isCompleted) colors.textSecondary else colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Task details",
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AgriXBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    language: AppLanguage
) {
    val colors = LocalAgriXCustomColors.current
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    if (imeBottomPadding > 0.dp) {
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                FloatingNavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Home,
                    label = Localization.getString("nav_home", language),
                    isSelected = currentScreen == Screen.HOME,
                    onClick = { onNavigate(Screen.HOME) },
                    tag = "nav_home"
                )

                // 2. Farm
                FloatingNavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Eco,
                    label = Localization.getString("nav_farm", language),
                    isSelected = currentScreen == Screen.FARM,
                    onClick = { onNavigate(Screen.FARM) },
                    tag = "nav_farm"
                )

                // 3. AI Assistant (CENTER ITEM)
                FloatingNavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Psychology,
                    label = Localization.getString("nav_ai_assistant", language),
                    isSelected = currentScreen == Screen.AI_ASSISTANT,
                    onClick = { onNavigate(Screen.AI_ASSISTANT) },
                    tag = "nav_ai_assistant"
                )

                // 4. Guides
                FloatingNavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = Localization.getString("nav_guides", language),
                    isSelected = currentScreen == Screen.GUIDES,
                    onClick = { onNavigate(Screen.GUIDES) },
                    tag = "nav_guides"
                )

                // 5. Profile
                FloatingNavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Person,
                    label = Localization.getString("nav_profile", language),
                    isSelected = currentScreen == Screen.PROFILE || currentScreen == Screen.EDIT_PROFILE || currentScreen == Screen.SETTINGS,
                    onClick = { onNavigate(Screen.PROFILE) },
                    tag = "nav_profile"
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val colors = LocalAgriXCustomColors.current
    val activeGreen = AgriXGreenPrimary
    val inactiveGray = colors.textSecondary
    val activeBg = colors.cardGreenBg

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(28.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) activeBg else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeGreen else inactiveGray,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .height(3.dp)
                .width(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) activeGreen else Color.Transparent)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeGreen else inactiveGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LoadingStateView(message: String) {
    val colors = LocalAgriXCustomColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = AgriXGreenPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFFDC2626),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AgriXGreenPrimary,
            modifier = Modifier.clickable { onRetry() }
        ) {
            Text(
                text = "Retry",
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalAgriXCustomColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colors.cardGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = AgriXGreenPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AgriXGreenPrimary,
                modifier = Modifier.clickable { onAction() }
            ) {
                Text(
                    text = actionText,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}
