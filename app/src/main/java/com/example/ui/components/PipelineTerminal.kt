package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun PipelineTerminalView(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    // Auto-scroll to bottom of logs when a new one is siphoned
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            lazyListState.animateScrollToItem(logs.size - 1)
        }
    }

    // High fidelity pre-compiled pipeline code schema
    val pipelineSchemaJson = remember {
        """{
  "softsiphon_kernel": "v3.8.4",
  "pipeline_cluster": "PROD-NEURAL-EAST",
  "active_sockets": [
    {
      "socket_id": "SOFTSIPH-902",
      "source_provider": "AWS-US-EAST",
      "target_node": "NEURAL-CORE",
      "throughput_limit_gbps": 15.0,
      "secure_layer": "AES-256-GCM"
    },
    {
      "socket_id": "SOFTSIPH-104",
      "source_provider": "GCP-EUROPE",
      "target_node": "NEURAL-CORE",
      "compression_ratio": 4.2
    }
  ],
  "auto_cluster_routing": true,
  "telemetry_colorway": {
    "primary_cyan": "#00A4E4",
    "primary_magenta": "#E5007D",
    "secondary_yellow": "#FFD200"
  }
}"""
    }

    // Render beautiful syntax highlighted JSON
    val highlightedJson = remember(pipelineSchemaJson) {
        highlightJson(pipelineSchemaJson)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Split 1: Pipeline JSON Config Editor ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .testTag("pipeline_code_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SiphonDark),
            border = BorderStroke(1.dp, SiphonBorderDark)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Editor header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SiphonSurfaceVariantDark)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = SiphonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "softsiphon-pipeline-schema.json",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Simulated status badge
                    Box(
                        modifier = Modifier
                            .background(SiphonCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DEPLOYED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = SiphonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Code contents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(14.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = highlightedJson,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Split 2: Running Logs Terminal ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("terminal_logs_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F12)), // Pitch black terminal background
            border = BorderStroke(1.dp, Color(0xFF23232C))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Terminal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF16161D))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = SiphonMagenta,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LIVE DATA-STREAM INGESTION LOGS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Glowing pulse dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Green, RoundedCornerShape(50))
                    )
                }

                // Terminal lines
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { log ->
                        val coloredLog = parseLogLine(log)
                        Text(
                            text = coloredLog,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Parses and colors JSON schemas dynamically into beautiful annotated tokens.
 */
fun highlightJson(rawJson: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        while (index < rawJson.length) {
            val char = rawJson[index]
            when {
                char == '"' -> {
                    // String literal parsing
                    val start = index
                    index++
                    while (index < rawJson.length && rawJson[index] != '"') {
                        if (rawJson[index] == '\\' && index + 1 < rawJson.length) {
                            index += 2
                        } else {
                            index++
                        }
                    }
                    if (index < rawJson.length) index++ // Consume trailing quote
                    val str = rawJson.substring(start, index)
                    // Color keys in Cyan, color values in White or Light Gray
                    if (index < rawJson.length && rawJson.getOrNull(index) == ':') {
                        pushStyle(SpanStyle(color = SiphonCyan, fontWeight = FontWeight.Bold))
                        append(str)
                        pop()
                    } else {
                        pushStyle(SpanStyle(color = Color.White.copy(alpha = 0.9f)))
                        append(str)
                        pop()
                    }
                }
                char.isDigit() -> {
                    // Number parsing
                    val start = index
                    while (index < rawJson.length && (rawJson[index].isDigit() || rawJson[index] == '.')) {
                        index++
                    }
                    val num = rawJson.substring(start, index)
                    pushStyle(SpanStyle(color = SiphonMagenta, fontWeight = FontWeight.Bold))
                    append(num)
                    pop()
                }
                char == '{' || char == '}' || char == '[' || char == ']' -> {
                    pushStyle(SpanStyle(color = SiphonYellow, fontWeight = FontWeight.Bold))
                    append(char.toString())
                    pop()
                    index++
                }
                char == ':' || char == ',' -> {
                    pushStyle(SpanStyle(color = Color.White.copy(alpha = 0.5f)))
                    append(char.toString())
                    pop()
                    index++
                }
                else -> {
                    append(char.toString())
                    index++
                }
            }
        }
    }
}

/**
 * Formats live terminal logger entries with professional developer syntax coloring.
 */
fun parseLogLine(line: String): AnnotatedString {
    return buildAnnotatedString {
        val timestampEnd = line.indexOf(']')
        if (timestampEnd != -1) {
            // Timestamp
            pushStyle(SpanStyle(color = Color.White.copy(alpha = 0.4f)))
            append(line.substring(0, timestampEnd + 1))
            pop()

            val categoryEnd = line.indexOf(']', timestampEnd + 1)
            if (categoryEnd != -1) {
                val category = line.substring(timestampEnd + 1, categoryEnd + 1).trim()
                val color = when {
                    category.contains("SIPHON") -> SiphonCyan
                    category.contains("SYSTEM") -> SiphonYellow
                    category.contains("CONNECT") -> Color.Green
                    else -> SiphonMagenta
                }
                append(" ")
                pushStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold))
                append(category)
                pop()
                append(" ")
                append(line.substring(categoryEnd + 1))
            } else {
                append(line.substring(timestampEnd + 1))
            }
        } else {
            append(line)
        }
    }
}
