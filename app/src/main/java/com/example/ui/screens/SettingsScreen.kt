package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
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
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val colors = LocalAgriXCustomColors.current

    val profile = userProfile ?: UserProfileEntity()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmationDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header matching reference image: Back arrow, "Settings" title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
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

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = Localization.getString("settings_title", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // APP PREFERENCES Section
            item {
                SettingsSectionHeader(title = Localization.getString("app_preferences", currentLanguage), textColor = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsValueRow(
                            icon = Icons.Default.Language,
                            iconBg = colors.cardBlueBg,
                            iconTint = colors.cardBlueText,
                            title = Localization.getString("preferred_language", currentLanguage),
                            value = currentLanguage.displayName,
                            onClick = { showLanguageDialog = true },
                            textColor = colors.textPrimary,
                            subTextColor = colors.textSecondary
                        )

                        HorizontalDivider(color = colors.borderSubtle, thickness = 0.8.dp)

                        SettingsValueRow(
                            icon = Icons.Default.Palette,
                            iconBg = colors.cardGreenBg,
                            iconTint = colors.cardGreenText,
                            title = Localization.getString("theme", currentLanguage),
                            value = profile.themeMode,
                            onClick = { showThemeDialog = true },
                            textColor = colors.textPrimary,
                            subTextColor = colors.textSecondary
                        )
                    }
                }
            }

            // NOTIFICATIONS Section
            item {
                SettingsSectionHeader(title = Localization.getString("notifications", currentLanguage), textColor = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsToggleRow(
                            icon = Icons.Default.Notifications,
                            iconBg = colors.cardBlueBg,
                            iconTint = colors.cardBlueText,
                            title = Localization.getString("weather_alerts", currentLanguage),
                            isChecked = profile.weatherAlertsEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(weatherAlerts = checked)
                            },
                            textColor = colors.textPrimary
                        )

                        HorizontalDivider(color = colors.borderSubtle, thickness = 0.8.dp)

                        SettingsToggleRow(
                            icon = Icons.Default.Eco,
                            iconBg = colors.cardGreenBg,
                            iconTint = colors.cardGreenText,
                            title = Localization.getString("farming_reminders", currentLanguage),
                            isChecked = profile.farmingRemindersEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(farmingReminders = checked)
                            },
                            textColor = colors.textPrimary
                        )

                        HorizontalDivider(color = colors.borderSubtle, thickness = 0.8.dp)

                        SettingsToggleRow(
                            icon = Icons.Default.BarChart,
                            iconBg = colors.cardOrangeBg,
                            iconTint = colors.cardOrangeText,
                            title = Localization.getString("crop_updates", currentLanguage),
                            isChecked = profile.cropUpdatesEnabled,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(cropUpdates = checked)
                            },
                            textColor = colors.textPrimary
                        )
                    }
                }
            }

            // ACCOUNT Section
            item {
                SettingsSectionHeader(title = Localization.getString("account", currentLanguage), textColor = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsValueRow(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            iconBg = colors.cardOrangeBg,
                            iconTint = colors.cardOrangeText,
                            title = Localization.getString("logout", currentLanguage),
                            value = "",
                            onClick = { showLogoutConfirmationDialog = true },
                            textColor = colors.textPrimary,
                            subTextColor = colors.textSecondary
                        )

                        HorizontalDivider(color = colors.borderSubtle, thickness = 0.8.dp)

                        SettingsValueRow(
                            icon = Icons.Default.Lock,
                            iconBg = colors.cardBlueBg,
                            iconTint = colors.cardBlueText,
                            title = Localization.getString("change_password", currentLanguage),
                            value = "",
                            onClick = { showPasswordDialog = true },
                            textColor = colors.textPrimary,
                            subTextColor = colors.textSecondary
                        )

                        HorizontalDivider(color = colors.borderSubtle, thickness = 0.8.dp)

                        SettingsValueRow(
                            icon = Icons.Default.Delete,
                            iconBg = Color(0xFFFEE2E2),
                            iconTint = Color(0xFFDC2626),
                            title = Localization.getString("delete_account", currentLanguage),
                            titleColor = Color(0xFFDC2626),
                            value = "",
                            onClick = { showDeleteConfirmationDialog = true },
                            textColor = Color(0xFFDC2626),
                            subTextColor = colors.textSecondary
                        )
                    }
                }
            }

            // ABOUT Section
            item {
                SettingsSectionHeader(title = Localization.getString("about", currentLanguage), textColor = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    SettingsValueRow(
                        icon = Icons.Default.Info,
                        iconBg = colors.cardBlueBg,
                        iconTint = colors.cardBlueText,
                        title = Localization.getString("app_version", currentLanguage),
                        value = "v1.0.0",
                        onClick = {
                            Toast.makeText(context, "AgriX AI v1.0.0 (Local SQLite Build)", Toast.LENGTH_SHORT).show()
                        },
                        textColor = colors.textPrimary,
                        subTextColor = colors.textSecondary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(Localization.getString("preferred_language", currentLanguage), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguage == lang,
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AgriXGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${lang.displayName} (${lang.nativeName})", fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage))
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        val themes = listOf("System", "Light", "Dark")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(Localization.getString("theme", currentLanguage), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSettings(themeMode = theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = profile.themeMode.equals(theme, ignoreCase = true),
                                onClick = {
                                    viewModel.updateSettings(themeMode = theme)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AgriXGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = theme, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage))
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmationDialog = false },
            title = { Text(Localization.getString("logout", currentLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(Localization.getString("logout_confirm", currentLanguage)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmationDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriXGreenPrimary)
                ) {
                    Text(Localization.getString("logout", currentLanguage), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmationDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage))
                }
            }
        )
    }

    // Change Password Dialog
    if (showPasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        var passError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text(Localization.getString("change_password", currentLanguage), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter new local account password (minimum 6 characters):", fontSize = 13.sp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = {
                            newPass = it
                            passError = null
                        },
                        singleLine = true,
                        placeholder = { Text("New Password") }
                    )
                    if (passError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = passError!!, fontSize = 12.sp, color = Color(0xFFDC2626))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPass.length < 6) {
                        passError = "Password must be at least 6 characters"
                    } else {
                        viewModel.changePassword(newPass) { success, err ->
                            if (success) {
                                Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                                showPasswordDialog = false
                            } else {
                                passError = err ?: "Failed to update password"
                            }
                        }
                    }
                }) {
                    Text("Update", color = AgriXGreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage))
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Delete Local Account Data?", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text("This will permanently remove all your locally stored field data, tasks, and profile settings for this account on this device.", fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccountData()
                    showDeleteConfirmationDialog = false
                    Toast.makeText(context, "Account data reset", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete Everything", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, textColor: Color) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsValueRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    value: String,
    onClick: () -> Unit,
    titleColor: Color? = null,
    textColor: Color = Color.Black,
    subTextColor: Color = Color.Gray
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                color = titleColor ?: textColor
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = subTextColor
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = subTextColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AgriXGreenPrimary
            )
        )
    }
}
