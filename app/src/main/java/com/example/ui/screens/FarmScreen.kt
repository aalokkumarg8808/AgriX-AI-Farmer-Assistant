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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Localization
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FieldCardItem
import com.example.ui.components.SummaryStatsRow
import com.example.ui.components.TaskRowItem
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun FarmScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val fields by viewModel.farmFields.collectAsState()
    val tasks by viewModel.farmTasks.collectAsState()
    val colors = LocalAgriXCustomColors.current

    var showAddFieldDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val totalAcres = fields.sumOf { it.areaAcres }
    val cropCount = fields.mapNotNull { it.cropName }.distinct().filter { it.isNotBlank() }.size

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
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    modifier = Modifier.testTag("farm_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.getString("my_farm", currentLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AgriXGreenPrimary,
                modifier = Modifier
                    .clickable { showAddFieldDialog = true }
                    .testTag("add_field_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Field",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Localization.getString("add_field", currentLanguage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (fields.isNotEmpty()) {
                // Stat Summary Cards - Only shown when user has real farm data
                item {
                    SummaryStatsRow(
                        cropCount = cropCount,
                        acresCount = totalAcres,
                        soilHealth = "Active"
                    )
                }
            }

            // My Fields Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Localization.getString("my_fields", currentLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            if (fields.isEmpty()) {
                item {
                    EmptyStateView(
                        message = Localization.getString("no_farm_added_yet", currentLanguage),
                        actionText = Localization.getString("add_your_farm", currentLanguage),
                        onAction = { showAddFieldDialog = true }
                    )
                }
            } else {
                items(fields, key = { it.id }) { field ->
                    FieldCardItem(
                        field = field,
                        onClick = {}
                    )
                }
            }

            // Today's Tasks Section
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.getString("todays_tasks", currentLanguage),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Text(
                        text = Localization.getString("add_task", currentLanguage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriXGreenPrimary,
                        modifier = Modifier
                            .clickable { showAddTaskDialog = true }
                            .testTag("add_task_trigger")
                    )
                }
            }

            if (tasks.isEmpty()) {
                item {
                    Text(
                        text = "No tasks added for today.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskRowItem(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Add Field Dialog
    if (showAddFieldDialog) {
        var fieldName by remember { mutableStateOf("") }
        var cropName by remember { mutableStateOf("") }
        var areaText by remember { mutableStateOf("1.0") }
        var status by remember { mutableStateOf("Growing") }

        AlertDialog(
            onDismissRequest = { showAddFieldDialog = false },
            title = { Text(Localization.getString("add_field", currentLanguage), color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fieldName,
                        onValueChange = { fieldName = it },
                        label = { Text(Localization.getString("field_name", currentLanguage)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = AgriXGreenPrimary,
                            unfocusedBorderColor = colors.borderSubtle
                        )
                    )
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text(Localization.getString("crop_name", currentLanguage)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = AgriXGreenPrimary,
                            unfocusedBorderColor = colors.borderSubtle
                        )
                    )
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        label = { Text(Localization.getString("acres", currentLanguage)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = AgriXGreenPrimary,
                            unfocusedBorderColor = colors.borderSubtle
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val area = areaText.toDoubleOrNull() ?: 1.0
                        val name = if (fieldName.isNotBlank()) fieldName.trim() else "Field ${fields.size + 1}"
                        val crop = if (cropName.isNotBlank()) cropName.trim() else "Crop"
                        viewModel.addField(name, crop, area, status)
                        showAddFieldDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriXGreenPrimary)
                ) {
                    Text(Localization.getString("add_field", currentLanguage), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFieldDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage), color = colors.textSecondary)
                }
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text(Localization.getString("add_task", currentLanguage), color = colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Description") },
                    placeholder = { Text("e.g. Apply urea fertilizer") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = AgriXGreenPrimary,
                        unfocusedBorderColor = colors.borderSubtle
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(taskTitle.trim())
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriXGreenPrimary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage), color = colors.textSecondary)
                }
            }
        )
    }
}
