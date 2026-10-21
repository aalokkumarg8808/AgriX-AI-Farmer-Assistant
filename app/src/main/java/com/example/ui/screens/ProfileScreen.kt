package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.localization.Localization
import com.example.ui.theme.AgriXGreenLight
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ProfileScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val fields by viewModel.farmFields.collectAsState()
    val colors = LocalAgriXCustomColors.current

    val profile = userProfile ?: UserProfileEntity()

    val fieldCount = fields.size
    val totalAcres = fields.sumOf { it.areaAcres }
    val formattedAcres = if (totalAcres > 0) String.format("%.1f", totalAcres) else "0"
    
    val avgSoilHealth = if (fields.isNotEmpty()) {
        val parsed = fields.mapNotNull { it.soilHealth.replace("%", "").trim().toIntOrNull() }
        if (parsed.isNotEmpty()) "${parsed.average().toInt()}%" else "--"
    } else {
        "--"
    }

    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.getString("profile_title", currentLanguage),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            IconButton(
                onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = Localization.getString("settings_title", currentLanguage),
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(AgriXGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(3.dp, Color.White, CircleShape)
                                    .clickable { viewModel.navigateTo(Screen.EDIT_PROFILE) },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(74.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = AgriXGreenPrimary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(AgriXGreenPrimary)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val nameText = if (profile.fullName.isNotBlank()) profile.fullName else Localization.getString("unnamed_farmer", currentLanguage)
                            Text(
                                text = nameText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val locText = if (profile.location.isNotBlank()) profile.location else Localization.getString("not_set", currentLanguage)
                                Text(
                                    text = locText,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Farming Summary Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Eco,
                        iconTint = AgriXGreenPrimary,
                        bgColor = colors.cardGreenBg,
                        value = "$fieldCount",
                        label = "Fields",
                        textColor = colors.textPrimary,
                        subTextColor = colors.textSecondary,
                        surfaceColor = colors.surface,
                        borderColor = colors.borderSubtle
                    )

                    SummaryStatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Agriculture,
                        iconTint = colors.cardBlueText,
                        bgColor = colors.cardBlueBg,
                        value = formattedAcres,
                        label = "Acres",
                        textColor = colors.textPrimary,
                        subTextColor = colors.textSecondary,
                        surfaceColor = colors.surface,
                        borderColor = colors.borderSubtle
                    )

                    SummaryStatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BarChart,
                        iconTint = colors.cardOrangeText,
                        bgColor = colors.cardOrangeBg,
                        value = avgSoilHealth,
                        label = "Soil Health",
                        textColor = colors.textPrimary,
                        subTextColor = colors.textSecondary,
                        surfaceColor = colors.surface,
                        borderColor = colors.borderSubtle
                    )
                }
            }

            // Options List Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Localization.getString("farm_details", currentLanguage),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        InfoRow(label = Localization.getString("farm_name", currentLanguage), value = profile.farmName, colors = colors)
                        InfoRow(label = Localization.getString("main_crop", currentLanguage), value = profile.primaryCrop, colors = colors)
                        InfoRow(label = Localization.getString("other_crops", currentLanguage), value = profile.otherCrops, colors = colors)
                        InfoRow(label = Localization.getString("soil_type", currentLanguage), value = profile.soilType, colors = colors)
                        InfoRow(label = Localization.getString("irrigation_type", currentLanguage), value = profile.irrigationType, colors = colors)
                        InfoRow(label = Localization.getString("experience_years", currentLanguage), value = if (profile.experienceYears > 0) "${profile.experienceYears} ${Localization.getString("years", currentLanguage)}" else "", colors = colors)
                        InfoRow(label = Localization.getString("phone", currentLanguage), value = profile.phone, colors = colors)
                    }
                }
            }

            // Options List Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        ProfileOptionRow(
                            icon = Icons.Default.Eco,
                            iconBg = colors.cardGreenBg,
                            iconTint = colors.cardGreenText,
                            title = Localization.getString("my_farm", currentLanguage),
                            onClick = { viewModel.navigateTo(Screen.FARM) },
                            textColor = colors.textPrimary,
                            arrowColor = colors.textSecondary
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.History,
                            iconBg = colors.cardPurpleBg,
                            iconTint = colors.cardPurpleText,
                            title = Localization.getString("crop_history", currentLanguage),
                            onClick = {
                                dialogTitle = Localization.getString("crop_history", currentLanguage)
                                dialogMessage = if (fields.isNotEmpty()) "History recorded for ${fields.size} fields." else "No crop history recorded yet."
                            },
                            textColor = colors.textPrimary,
                            arrowColor = colors.textSecondary
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.Bookmark,
                            iconBg = colors.cardOrangeBg,
                            iconTint = colors.cardOrangeText,
                            title = Localization.getString("saved_recommendations", currentLanguage),
                            onClick = {
                                dialogTitle = Localization.getString("saved_recommendations", currentLanguage)
                                dialogMessage = "Check Crop Recommendation tab to view personalized soil and crop advice."
                            },
                            textColor = colors.textPrimary,
                            arrowColor = colors.textSecondary
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.Notifications,
                            iconBg = colors.cardBlueBg,
                            iconTint = colors.cardBlueText,
                            title = Localization.getString("notifications", currentLanguage),
                            onClick = {
                                dialogTitle = Localization.getString("notifications", currentLanguage)
                                dialogMessage = "Weather alerts & farming reminders are active."
                            },
                            textColor = colors.textPrimary,
                            arrowColor = colors.textSecondary
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.Help,
                            iconBg = colors.cardPurpleBg,
                            iconTint = colors.cardPurpleText,
                            title = Localization.getString("help_support", currentLanguage),
                            onClick = {
                                dialogTitle = Localization.getString("help_support", currentLanguage)
                                dialogMessage = "AgriX AI is your smart farming assistant. Use AI Assistant tab or Guides anytime for instant farming support."
                            },
                            textColor = colors.textPrimary,
                            arrowColor = colors.textSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (dialogTitle != null && dialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                dialogTitle = null
                dialogMessage = null
            },
            title = { Text(text = dialogTitle!!, fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = { Text(text = dialogMessage!!, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    dialogTitle = null
                    dialogMessage = null
                }) {
                    Text("OK", color = AgriXGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = colors.textSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
    }
}

@Composable
private fun SummaryStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    value: String,
    label: String,
    textColor: Color,
    subTextColor: Color,
    surfaceColor: Color,
    borderColor: Color
) {
    Surface(
        modifier = modifier.height(95.dp),
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = subTextColor
                )
            }
        }
    }
}

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    textColor: Color,
    arrowColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open",
            tint = arrowColor,
            modifier = Modifier.size(16.dp)
        )
    }
}
