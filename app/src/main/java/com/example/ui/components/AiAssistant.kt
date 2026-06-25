package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.SiphonViewModel

@Composable
fun AiAssistantView(
    viewModel: SiphonViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    var inputPrompt by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // Quick Prompt Suggestions
    val suggestions = listOf(
        "Optimize telemetry bottlenecks",
        "Perform node safety audit",
        "Generate JSON extraction pipeline"
    )

    // Auto-scroll to latest message
    LaunchedEffect(chatHistory.size, isAiLoading) {
        if (chatHistory.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Header Block ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SiphonCyan, SiphonMagenta)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini AI",
                        tint = SiphonYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "GEMINI AI ARCHITECT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Connected & reading live pipeline metrics",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- Chat Bubble Feed ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("ai_chat_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatHistory) { msg ->
                        ChatBubble(msg = msg)
                    }

                    if (isAiLoading) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        // --- Suggestion Chips ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            suggestions.forEach { chipText ->
                Box(
                    modifier = Modifier
                        .background(SiphonDark.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .border(1.dp, SiphonDark.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAiLoading) {
                            viewModel.askAi(chipText)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chipText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SiphonDark.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- Input Controls ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Ask about node performance, schemas...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("ai_prompt_input"),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SiphonCyan,
                    unfocusedBorderColor = SiphonDark.copy(alpha = 0.15f),
                    focusedLabelColor = SiphonCyan
                )
            )

            FloatingActionButton(
                onClick = {
                    if (inputPrompt.isNotBlank() && !isAiLoading) {
                        viewModel.askAi(inputPrompt)
                        inputPrompt = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("ai_send_button"),
                containerColor = SiphonCyan,
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send AI Request",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val bubbleShape = if (msg.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    val bubbleBg = if (msg.isUser) {
        SiphonDark
    } else {
        SiphonDark.copy(alpha = 0.04f)
    }

    val textColor = if (msg.isUser) {
        Color.White
    } else {
        SiphonDark
    }

    val alignment = if (msg.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (msg.isUser) "user_message_bubble" else "ai_message_bubble"),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(bubbleBg, bubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 290.dp)
        ) {
            val formattedText = formatChatMessageText(msg.text, msg.isUser)
            Text(
                text = formattedText,
                fontSize = 12.sp,
                color = textColor,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = msg.timestamp,
            fontSize = 9.sp,
            color = SiphonDark.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(SiphonDark.copy(alpha = 0.04f), RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "AI Architect is analyzing systems...",
                fontSize = 11.sp,
                color = SiphonDark.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Formats chat messages dynamically, supporting inline bold tags `**` and syntax highlighters.
 */
fun formatChatMessageText(text: String, isUserMessage: Boolean): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            if (boldStart != -1) {
                // Append preceding text
                append(text.substring(cursor, boldStart))

                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    // Append styled bold block
                    pushStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = if (isUserMessage) Color.White else SiphonCyan))
                    append(text.substring(boldStart + 2, boldEnd))
                    pop()
                    cursor = boldEnd + 2
                } else {
                    append("**")
                    cursor = boldStart + 2
                }
            } else {
                append(text.substring(cursor))
                break
            }
        }
    }
}
