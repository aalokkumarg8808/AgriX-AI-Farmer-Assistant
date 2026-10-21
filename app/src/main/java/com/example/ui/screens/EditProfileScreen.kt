package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun EditProfileScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val colors = LocalAgriXCustomColors.current

    val profile = userProfile ?: UserProfileEntity()

    var nameInput by remember(profile) { mutableStateOf(profile.fullName) }
    var farmNameInput by remember(profile) { mutableStateOf(profile.farmName) }
    var phoneInput by remember(profile) { mutableStateOf(profile.phone) }
    var locationInput by remember(profile) { mutableStateOf(profile.location) }
    var farmSizeInput by remember(profile) { mutableStateOf(profile.farmSizeAcres.toString()) }
    var primaryCropInput by remember(profile) { mutableStateOf(profile.primaryCrop) }
    var otherCropsInput by remember(profile) { mutableStateOf(profile.otherCrops) }
    var experienceInput by remember(profile) { mutableStateOf(profile.experienceYears.toString()) }
    var soilTypeInput by remember(profile) { mutableStateOf(profile.soilType) }
    var irrigationInput by remember(profile) { mutableStateOf(profile.irrigationType) }
    var selectedLang by remember(profile) { mutableStateOf(currentLanguage) }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSoilDialog by remember { mutableStateOf(false) }
    var showIrrigationDialog by remember { mutableStateOf(false) }

    val soilOptions = listOf("Loamy Soil", "Alluvial Soil", "Black Soil", "Red Soil", "Clay Soil", "Sandy Soil")
    val irrigationOptions = listOf("Canal / Tube Well", "Drip Irrigation", "Sprinkler System", "Rainfed")

    val performSave = {
        val updated = profile.copy(
            fullName = nameInput.trim(),
            farmName = farmNameInput.trim(),
            phone = phoneInput.trim(),
            location = locationInput.trim(),
            farmSizeAcres = farmSizeInput.toDoubleOrNull() ?: 0.0,
            primaryCrop = primaryCropInput.trim(),
            otherCrops = otherCropsInput.trim(),
            experienceYears = experienceInput.toIntOrNull() ?: 0,
            soilType = soilTypeInput,
            irrigationType = irrigationInput,
            preferredLanguage = selectedLang.name
        )
        viewModel.saveProfile(updated)
        viewModel.setLanguage(selectedLang)

        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
        viewModel.navigateTo(Screen.PROFILE)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.PROFILE) },
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }

            Text(
                text = Localization.getString("edit_profile", currentLanguage),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            IconButton(
                onClick = { performSave() },
                modifier = Modifier.testTag("save_check_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = AgriXGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Picture
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Photo",
                                tint = AgriXGreenPrimary,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(AgriXGreenPrimary)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable {
                                    Toast.makeText(context, "Avatar options ready", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Name Field Card
            item {
                EditInputFieldCard(
                    icon = Icons.Default.Person,
                    label = Localization.getString("full_name", currentLanguage),
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    tag = "input_edit_name",
                    colors = colors
                )
            }

            // Phone Number Field Card
            item {
                EditInputFieldCard(
                    icon = Icons.Default.Phone,
                    label = Localization.getString("phone", currentLanguage),
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    tag = "input_edit_phone",
                    colors = colors
                )
            }

            // Location Field Card with Clear X
            item {
                EditInputFieldCard(
                    icon = Icons.Default.LocationOn,
                    label = Localization.getString("location", currentLanguage),
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    tag = "input_edit_location",
                    colors = colors,
                    trailingIcon = {
                        if (locationInput.isNotEmpty()) {
                            IconButton(onClick = { locationInput = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )
            }

            // Farm Name
            item {
                EditInputFieldCard(
                    icon = Icons.Default.Agriculture,
                    label = Localization.getString("farm_name", currentLanguage),
                    value = farmNameInput,
                    onValueChange = { farmNameInput = it },
                    tag = "input_edit_farm_name",
                    colors = colors
                )
            }

            // Farm Size
            item {
                EditInputFieldCard(
                    icon = Icons.Default.Terrain,
                    label = Localization.getString("farm_size", currentLanguage),
                    value = farmSizeInput,
                    onValueChange = { farmSizeInput = it },
                    tag = "input_edit_farm_size",
                    colors = colors,
                    keyboardType = KeyboardType.Decimal
                )
            }

            // Primary Crop
            item {
                EditInputFieldCard(
                    icon = Icons.Default.Grass,
                    label = Localization.getString("main_crop", currentLanguage),
                    value = primaryCropInput,
                    onValueChange = { primaryCropInput = it },
                    tag = "input_edit_primary_crop",
                    colors = colors
                )
            }

            // Other Crops
            item {
                EditInputFieldCard(
                    icon = Icons.Default.Agriculture,
                    label = Localization.getString("other_crops", currentLanguage),
                    value = otherCropsInput,
                    onValueChange = { otherCropsInput = it },
                    tag = "input_edit_other_crops",
                    colors = colors
                )
            }

            // Soil Type Select
            item {
                EditSelectFieldCard(
                    icon = Icons.Default.Terrain,
                    label = Localization.getString("soil_type", currentLanguage),
                    value = soilTypeInput,
                    onClick = { showSoilDialog = true },
                    colors = colors
                )
            }

            // Irrigation Select
            item {
                EditSelectFieldCard(
                    icon = Icons.Default.Water,
                    label = Localization.getString("irrigation_type", currentLanguage),
                    value = irrigationInput,
                    onClick = { showIrrigationDialog = true },
                    colors = colors
                )
            }

            // Experience
            item {
                EditInputFieldCard(
                    icon = Icons.Default.History,
                    label = Localization.getString("experience_years", currentLanguage),
                    value = experienceInput,
                    onValueChange = { experienceInput = it },
                    tag = "input_edit_experience",
                    colors = colors,
                    keyboardType = KeyboardType.Number
                )
            }

            // Language Selection Card
            item {
                EditSelectFieldCard(
                    icon = Icons.Default.Language,
                    label = Localization.getString("preferred_language", currentLanguage),
                    value = selectedLang.displayName,
                    onClick = { showLanguageDialog = true },
                    colors = colors
                )
            }

            // Save Changes Bottom Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { performSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_changes_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriXGreenPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = Localization.getString("save_changes", currentLanguage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(Localization.getString("preferred_language", currentLanguage), fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Column {
                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLang = lang
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLang == lang,
                                onClick = {
                                    selectedLang = lang
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AgriXGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${lang.displayName} (${lang.nativeName})",
                                fontSize = 15.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage), color = colors.textSecondary)
                }
            }
        )
    }

    if (showSoilDialog) {
        AlertDialog(
            onDismissRequest = { showSoilDialog = false },
            title = { Text(Localization.getString("soil_type", currentLanguage), fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Column {
                    soilOptions.forEach { soil ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    soilTypeInput = soil
                                    showSoilDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = soilTypeInput == soil,
                                onClick = {
                                    soilTypeInput = soil
                                    showSoilDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AgriXGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = soil, fontSize = 15.sp, color = colors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoilDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage), color = colors.textSecondary)
                }
            }
        )
    }

    if (showIrrigationDialog) {
        AlertDialog(
            onDismissRequest = { showIrrigationDialog = false },
            title = { Text(Localization.getString("irrigation_type", currentLanguage), fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Column {
                    irrigationOptions.forEach { irr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    irrigationInput = irr
                                    showIrrigationDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = irrigationInput == irr,
                                onClick = {
                                    irrigationInput = irr
                                    showIrrigationDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AgriXGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = irr, fontSize = 15.sp, color = colors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIrrigationDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage), color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun EditSelectFieldCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.cardGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = AgriXGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun EditInputFieldCard(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String,
    colors: com.example.ui.theme.AgriXCustomColors,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.cardGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AgriXGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(tag),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType)
                )
            }

            if (trailingIcon != null) {
                trailingIcon()
            }
        }
    }
}
