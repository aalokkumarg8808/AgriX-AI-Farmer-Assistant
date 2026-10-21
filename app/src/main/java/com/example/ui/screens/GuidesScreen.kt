package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.GuideEntity
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AgriXGreenLight
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun GuidesScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val selectedCategory by viewModel.selectedGuideCategory.collectAsState()
    val guides by viewModel.guides.collectAsState()
    val colors = LocalAgriXCustomColors.current

    val categories = listOf(
        "All" to Localization.getString("category_all", currentLanguage),
        "Crops" to Localization.getString("category_crops", currentLanguage),
        "Fertilizer" to Localization.getString("category_fertilizer", currentLanguage),
        "Irrigation" to Localization.getString("category_irrigation", currentLanguage),
        "Pests" to Localization.getString("category_pests", currentLanguage)
    )

    val filteredGuides = if (selectedCategory.equals("All", true)) {
        guides
    } else {
        guides.filter { it.category.equals(selectedCategory, true) }
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
                text = Localization.getString("guides_title", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (catKey, catLabel) ->
                val isSelected = selectedCategory.equals(catKey, true)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) AgriXGreenPrimary else colors.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AgriXGreenPrimary else colors.borderSubtle
                    ),
                    modifier = Modifier.clickable { viewModel.setGuideCategory(catKey) }
                ) {
                    Text(
                        text = catLabel,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredGuides.isEmpty()) {
                item {
                    EmptyStateView(
                        message = "No guides available in this category.",
                        actionText = "Show All Guides",
                        onAction = { viewModel.setGuideCategory("All") }
                    )
                }
            } else {
                items(filteredGuides, key = { it.id }) { guide ->
                    ExpandableGuideCard(
                        guide = guide,
                        language = currentLanguage,
                        colors = colors
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun ExpandableGuideCard(
    guide: GuideEntity,
    language: AppLanguage,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    var expanded by remember { mutableStateOf(false) }

    val title = when (language) {
        AppLanguage.HI -> guide.titleHi
        AppLanguage.PA -> guide.titlePa
        AppLanguage.EN -> guide.titleEn
    }

    val content = when (language) {
        AppLanguage.HI -> guide.contentHi
        AppLanguage.PA -> guide.contentPa
        AppLanguage.EN -> guide.contentEn
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AgriXGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Guide",
                        tint = AgriXGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.cardGreenBg
                    ) {
                        Text(
                            text = guide.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.cardGreenText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand guide",
                        tint = colors.textSecondary
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = content,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Estimated read: ${guide.readTimeMinutes} min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgriXGreenPrimary
                    )
                }
            }
        }
    }
}
