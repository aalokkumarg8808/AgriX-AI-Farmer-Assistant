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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMessageStatus
import com.example.localization.AppLanguage
import com.example.localization.Localization
import com.example.ui.components.MarkdownText
import com.example.ui.theme.AgriXGreenPrimary
import com.example.ui.theme.LocalAgriXCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val messages by viewModel.aiMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val colors = LocalAgriXCustomColors.current

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val chips = listOf(
        Localization.getString("chip_1", currentLanguage),
        Localization.getString("chip_2", currentLanguage),
        Localization.getString("chip_3", currentLanguage)
    )

    // Initial greeting if empty
    LaunchedEffect(messages.isEmpty(), currentLanguage) {
        if (messages.isEmpty()) {
            viewModel.ensureInitialGreeting(currentLanguage)
        }
    }

    // Auto-scroll to bottom on new messages
    val lastMessageCount = remember { mutableStateOf(messages.size) }
    LaunchedEffect(messages.size, isAiLoading) {
        if (messages.size > lastMessageCount.value || isAiLoading) {
            val totalItems = messages.size + (if (isAiLoading) 1 else 0)
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
        lastMessageCount.value = messages.size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
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
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AgriXGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = Localization.getString("ai_assistant_title", currentLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "English • हिन्दी • Hinglish",
                    fontSize = 12.sp,
                    color = AgriXGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = Localization.getString("ai_subtitle", currentLanguage),
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(messages) { msg ->
                if (msg.status == AiMessageStatus.ERROR) {
                    ErrorMessageBubble(
                        message = msg,
                        onRetry = { viewModel.retryLastQuestion() },
                        language = currentLanguage
                    )
                } else {
                    ChatBubble(message = msg, colors = colors)
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AgriXGreenPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AgriX AI is thinking...",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { chipText ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriXGreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable(enabled = !isAiLoading) {
                        if (!isAiLoading) {
                            viewModel.sendAiQuestion(chipText)
                        }
                    }
                ) {
                    Text(
                        text = chipText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (!isAiLoading) AgriXGreenPrimary else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Input Field + Send Button
        val isSendEnabled = inputText.isNotBlank() && !isAiLoading
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                enabled = !isAiLoading,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = colors.textPrimary
                ),
                placeholder = {
                    Text(
                        text = Localization.getString("ask_placeholder", currentLanguage),
                        fontSize = 15.sp,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = AgriXGreenPrimary,
                    unfocusedBorderColor = colors.borderSubtle
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = isSendEnabled) {
                        if (isSendEnabled) {
                            viewModel.sendAiQuestion(inputText)
                            inputText = ""
                        }
                    }
                    .testTag("send_ai_button"),
                shape = CircleShape,
                color = if (isSendEnabled) AgriXGreenPrimary else Color.Gray.copy(alpha = 0.35f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = Localization.getString("send", currentLanguage),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: AiChatMessage,
    colors: com.example.ui.theme.AgriXCustomColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) AgriXGreenPrimary else colors.surface,
            border = if (!message.isUser) androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle) else null,
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            MarkdownText(
                markdown = message.text,
                fontSize = 16.sp,
                color = if (message.isUser) Color.White else colors.textPrimary,
                lineHeight = 24.sp,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun ErrorMessageBubble(
    message: AiChatMessage,
    onRetry: () -> Unit,
    language: AppLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFEF2F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = Color(0xFF991B1B),
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.getString("retry", language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
