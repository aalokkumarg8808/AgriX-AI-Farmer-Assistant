package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.PlantDiseaseAnalysis
import com.example.localization.Localization
import com.example.ui.components.ErrorStateView
import com.example.ui.components.LoadingStateView
import com.example.ui.theme.AgriXGreenLight
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiState

@Composable
fun DiseaseDetectionScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val selectedImage by viewModel.selectedDiseaseImage.collectAsState()
    val diseaseState by viewModel.diseaseState.collectAsState()
    val colors = LocalAgriXCustomColors.current

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                // First decode bounds to calculate safe inSampleSize and avoid OutOfMemoryError
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(it)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                }
                var sampleSize = 1
                val maxDim = 1024
                if (boundsOptions.outHeight > maxDim || boundsOptions.outWidth > maxDim) {
                    val halfHeight = boundsOptions.outHeight / 2
                    val halfWidth = boundsOptions.outWidth / 2
                    while ((halfHeight / sampleSize) >= maxDim && (halfWidth / sampleSize) >= maxDim) {
                        sampleSize *= 2
                    }
                }
                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = context.contentResolver.openInputStream(it)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
                if (bitmap != null) {
                    viewModel.setSelectedDiseaseImage(bitmap)
                }
            } catch (e: Exception) {
                // Keep UI stable if image stream cannot be decoded
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setSelectedDiseaseImage(bitmap)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
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
                text = Localization.getString("disease_detection_title", currentLanguage),
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
            // Upload Leaf Image Area
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedImage != null) {
                            Image(
                                bitmap = selectedImage!!.asImageBitmap(),
                                contentDescription = "Selected Crop Leaf",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(AgriXGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = AgriXGreenPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Localization.getString("upload_leaf_image", currentLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Take or choose a clear picture of affected crop leaf for instant diagnosis",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Choose from Gallery Button
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("choose_gallery_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriXGreenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.getString("choose_gallery", currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }

                            // Take Photo Button
                            OutlinedButton(
                                onClick = {
                                    val hasCamPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasCamPerm) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("take_photo_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, AgriXGreenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = AgriXGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.getString("take_photo", currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriXGreenPrimary,
                                    maxLines = 1
                                )
                            }
                        }

                        if (selectedImage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.analyzePlantDisease() },
                                enabled = diseaseState !is UiState.Loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("analyze_plant_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Localization.getString("analyze_crop", currentLanguage),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Analysis Result Stream
            when (val state = diseaseState) {
                is UiState.Loading -> item {
                    LoadingStateView(Localization.getString("analyzing_image", currentLanguage))
                }
                is UiState.Error -> item {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.analyzePlantDisease() }
                    )
                }
                is UiState.Success -> {
                    item {
                        DiseaseAnalysisResultCard(analysis = state.data, colors = colors)
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
private fun DiseaseAnalysisResultCard(
    analysis: PlantDiseaseAnalysis,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    val isHealthy = analysis.healthStatus.contains("Healthy", ignoreCase = true) ||
            analysis.healthStatus.contains("स्वस्थ", ignoreCase = true)
    val isUncertain = analysis.isUncertain

    val statusColor = when {
        isUncertain -> Color(0xFFD97706)
        isHealthy -> AgriXGreenPrimary
        else -> Color(0xFFDC2626)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.diseaseName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Status: ${analysis.healthStatus}",
                        fontSize = 13.sp,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isUncertain) colors.cardOrangeBg else colors.cardGreenBg
                ) {
                    Text(
                        text = "${analysis.confidencePercent}% Confidence",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUncertain) colors.cardOrangeText else colors.cardGreenText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (isUncertain) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardOrangeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low confidence warning",
                            tint = colors.cardOrangeText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Low confidence diagnosis. Please take a closer, well-lit photo of the affected leaf.",
                            fontSize = 12.sp,
                            color = colors.cardOrangeText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Symptoms
            if (analysis.symptoms.isNotEmpty()) {
                Text(
                    text = "Symptoms:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                analysis.symptoms.forEach { sym ->
                    Text(
                        text = "• $sym",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Treatment & Cure
            if (analysis.treatmentSteps.isNotEmpty()) {
                Text(
                    text = "Treatment & Cure:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                analysis.treatmentSteps.forEach { step ->
                    Text(
                        text = "• $step",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Prevention Tips
            if (analysis.preventionTips.isNotEmpty()) {
                Text(
                    text = "Prevention Tips:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                analysis.preventionTips.forEach { tip ->
                    Text(
                        text = "• $tip",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Agricultural Advisory Disclaimer
            if (analysis.disclaimer.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardBlueBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
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
                            text = analysis.disclaimer,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
