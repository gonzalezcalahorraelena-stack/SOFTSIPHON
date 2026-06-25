package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.SiphonMetrics
import java.text.DecimalFormat

@Composable
fun TelemetryHubView(
    metrics: SiphonMetrics,
    modifier: Modifier = Modifier
) {
    val df = remember { DecimalFormat("#,##0.00") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- row 1: hero kpi card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_kpi_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SiphonDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(SiphonCyan.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(200f, 100f),
                            radius = 400f
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL DATA SIPHONED",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = SiphonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Live Sync",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = SiphonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${df.format(metrics.totalSiphonedGb)} GB",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.testTag("total_siphoned_value")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Encrypted neural siphon sockets routing securely to 3 primary cloud warehouses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // --- Row 2: Secondary KPIs ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiMiniCard(
                title = "ACTIVE PIPELINES",
                value = "${metrics.activePipelines} Streams",
                subtext = "4 GCP, 3 AWS, 1 AZURE",
                icon = Icons.Default.Speed,
                accentColor = SiphonCyan,
                modifier = Modifier.weight(1f)
            )

            KpiMiniCard(
                title = "NETWORK LATENCY",
                value = "${metrics.averageLatencyMs} ms",
                subtext = "SHA-512 Handshakes",
                icon = Icons.Default.Timer,
                accentColor = SiphonMagenta,
                modifier = Modifier.weight(1f)
            )
        }

        // --- Row 3: Custom Telemetry Line Graph ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .testTag("telemetry_graph_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REAL-TIME SIPHON THROUGHPUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = SiphonDark.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${String.format("%.2f", metrics.throughputHistory.lastOrNull() ?: 0f)} GB/s",
                            fontSize = 20.sp,
                            color = SiphonDark,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SiphonCyan, RoundedCornerShape(50))
                        )
                        Text(
                            text = "Siphon Rate",
                            style = MaterialTheme.typography.bodySmall,
                            color = SiphonDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Line graph canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("siphon_rate_chart")
                ) {
                    val width = size.width
                    val height = size.height
                    val points = metrics.throughputHistory

                    if (points.size > 1) {
                        val maxVal = 6.0f // Scale chart to max 6 GB/s
                        val xSpacing = width / (points.size - 1)

                        val path = Path()
                        val fillPath = Path()

                        // Initialize paths at starting point
                        val startX = 0f
                        val startY = height - (points[0] / maxVal) * height
                        path.moveTo(startX, startY)
                        fillPath.moveTo(startX, height)
                        fillPath.lineTo(startX, startY)

                        for (i in 1 until points.size) {
                            val nextX = i * xSpacing
                            val nextY = height - (points[i] / maxVal) * height
                            path.lineTo(nextX, nextY)
                            fillPath.lineTo(nextX, nextY)
                        }

                        // Close the fill path to draw area gradient
                        fillPath.lineTo(width, height)
                        fillPath.close()

                        // Draw background grid lines (horizontal)
                        val gridLines = 4
                        for (g in 1..gridLines) {
                            val yOffset = (height / gridLines) * g
                            drawLine(
                                color = SiphonDark.copy(alpha = 0.05f),
                                start = Offset(0f, yOffset),
                                end = Offset(width, yOffset),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw area gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(SiphonCyan.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw glowing line stroke
                        drawPath(
                            path = path,
                            color = SiphonCyan,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw final dot
                        val lastX = width
                        val lastY = height - (points.last() / maxVal) * height
                        drawCircle(
                            color = SiphonCyan,
                            radius = 6.dp.toPx(),
                            center = Offset(lastX, lastY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(lastX, lastY)
                        )
                    }
                }
            }
        }

        // --- Row 4: Cloud Infrastructure Metrics ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResourceUsageCard(
                title = "CPU Core Affinity",
                percentage = metrics.cpuUsage,
                accentColor = SiphonMagenta,
                info = "Dynamic clustering threadpool active",
                modifier = Modifier.weight(1f)
            )

            ResourceUsageCard(
                title = "Siphon Cache RAM",
                percentage = metrics.memoryUsage,
                accentColor = SiphonYellow,
                info = "Ring-buffer memory pool allocation",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KpiMiniCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("kpi_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = SiphonDark.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SiphonDark
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                fontSize = 10.sp,
                color = SiphonDark.copy(alpha = 0.6f),
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun ResourceUsageCard(
    title: String,
    percentage: Int,
    accentColor: Color,
    info: String,
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat() / 100f,
        animationSpec = tween(500),
        label = "usage_gauge"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = SiphonDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$percentage%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SiphonDark
                )

                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SiphonDark.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedPercentage)
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = info,
                fontSize = 9.sp,
                color = SiphonDark.copy(alpha = 0.5f),
                maxLines = 1
            )
        }
    }
}
