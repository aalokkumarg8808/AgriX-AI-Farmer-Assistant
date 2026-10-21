package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val colors = LocalAgriXCustomColors.current

    var fullName by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var farmSizeAcres by remember { mutableStateOf("") }
    var primaryCrop by remember { mutableStateOf("") }
    var otherCrops by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("Loamy Soil") }
    var irrigationType by remember { mutableStateOf("Canal / Tube Well") }
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }

    var isLocating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var isSoilDropdownExpanded by remember { mutableStateOf(false) }
    var isIrrigationDropdownExpanded by remember { mutableStateOf(false) }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }

    val soilOptions = listOf("Loamy Soil", "Alluvial Soil", "Black Soil", "Red Soil", "Clay Soil", "Sandy Soil")
    val irrigationOptions = listOf("Canal / Tube Well", "Drip Irrigation", "Sprinkler System", "Rainfed")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            isLocating = true
            viewModel.detectLocationForSetup { resolvedLoc ->
                isLocating = false
                if (resolvedLoc.isNotBlank()) {
                    location = resolvedLoc
                }
            }
        }
    }

    fun submitSetup() {
        if (fullName.trim().isBlank()) {
            errorMessage = "Please enter your name"
            return
        }

        isSaving = true
        val acres = farmSizeAcres.toDoubleOrNull() ?: 0.0
        val expYears = experienceYears.toIntOrNull() ?: 0

        viewModel.completeSetup(
            fullName = fullName.trim(),
            farmName = farmName.trim(),
            location = location.trim(),
            phone = phone.trim(),
            farmSize = acres,
            mainCrop = primaryCrop.trim(),
            otherCrops = otherCrops.trim(),
            experience = expYears,
            irrigation = irrigationType,
            soilType = soilType,
            language = selectedLanguage
        ) {
            isSaving = false
            viewModel.navigateTo(Screen.HOME)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Brand Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_agrix_ai_logo),
                contentDescription = "AgriX AI Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = Localization.getString("setup_title", currentLanguage),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Text(
            text = Localization.getString("setup_subtitle", currentLanguage),
            fontSize = 13.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Required Fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Full Name (REQUIRED)
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    errorMessage = null
                },
                label = { Text("${Localization.getString("full_name", currentLanguage)} *") },
                placeholder = { Text("e.g. Ramesh Kumar") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_name_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Email (Read-only from Account)
            val account by viewModel.activeAccount.collectAsState()
            OutlinedTextField(
                value = account?.email ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(Localization.getString("email", currentLanguage)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = colors.borderSubtle,
                    unfocusedBorderColor = colors.borderSubtle,
                    focusedTextColor = colors.textSecondary,
                    unfocusedTextColor = colors.textSecondary
                )
            )

            // Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(Localization.getString("phone", currentLanguage)) },
                placeholder = { Text("e.g. +91 9876543210") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Farm Name
            OutlinedTextField(
                value = farmName,
                onValueChange = { farmName = it },
                label = { Text(Localization.getString("farm_name", currentLanguage)) },
                placeholder = { Text("e.g. Gupta Organic Farm") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Location (Optional with GPS button)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(Localization.getString("location", currentLanguage)) },
                placeholder = { Text("e.g. Modinagar, Uttar Pradesh") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                trailingIcon = {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AgriXGreenPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Detect location",
                                tint = AgriXGreenPrimary
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_location_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Farm Size (Acres)
            OutlinedTextField(
                value = farmSizeAcres,
                onValueChange = { farmSizeAcres = it },
                label = { Text(Localization.getString("land_size", currentLanguage)) },
                placeholder = { Text("e.g. 3.5") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Terrain,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_farm_size_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Primary Crop
            OutlinedTextField(
                value = primaryCrop,
                onValueChange = { primaryCrop = it },
                label = { Text(Localization.getString("primary_crop", currentLanguage)) },
                placeholder = { Text("e.g. Wheat") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_primary_crop_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Other Crops
            OutlinedTextField(
                value = otherCrops,
                onValueChange = { otherCrops = it },
                label = { Text(Localization.getString("other_crops", currentLanguage)) },
                placeholder = { Text("e.g. Rice, Mustard, Vegetables") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Farming Experience (Years)
            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = { Text(Localization.getString("experience", currentLanguage)) },
                placeholder = { Text("e.g. 5") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = AgriXGreenPrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_experience_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                )
            )

            // Soil Type Dropdown
            ExposedDropdownMenuBox(
                expanded = isSoilDropdownExpanded,
                onExpandedChange = { isSoilDropdownExpanded = !isSoilDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = soilType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(Localization.getString("soil_type", currentLanguage)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSoilDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = AgriXGreenPrimary,
                        unfocusedBorderColor = colors.borderSubtle
                    )
                )
                ExposedDropdownMenu(
                    expanded = isSoilDropdownExpanded,
                    onDismissRequest = { isSoilDropdownExpanded = false }
                ) {
                    soilOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                soilType = opt
                                isSoilDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Irrigation Type Dropdown
            ExposedDropdownMenuBox(
                expanded = isIrrigationDropdownExpanded,
                onExpandedChange = { isIrrigationDropdownExpanded = !isIrrigationDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = irrigationType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(Localization.getString("irrigation_type", currentLanguage)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Water,
                            contentDescription = null,
                            tint = AgriXGreenPrimary
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isIrrigationDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = AgriXGreenPrimary,
                        unfocusedBorderColor = colors.borderSubtle
                    )
                )
                ExposedDropdownMenu(
                    expanded = isIrrigationDropdownExpanded,
                    onDismissRequest = { isIrrigationDropdownExpanded = false }
                ) {
                    irrigationOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                irrigationType = opt
                                isIrrigationDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Preferred Language Selection
            Text(
                text = Localization.getString("preferred_language", currentLanguage),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppLanguage.values().forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedLanguage = lang
                                viewModel.setLanguage(lang)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) AgriXGreenPrimary else colors.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AgriXGreenPrimary else colors.borderSubtle
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = lang.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else colors.textPrimary
                            )
                            Text(
                                text = lang.nativeName,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Error message view
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage ?: "",
                fontSize = 13.sp,
                color = Color(0xFFDC2626),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Complete Setup Button
        Button(
            onClick = { submitSetup() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("setup_complete_btn"),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgriXGreenPrimary,
                contentColor = Color.White
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = Localization.getString("complete_setup", currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
