package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiAssistantView
import com.example.ui.components.NetworkMap
import com.example.ui.components.PipelineTerminalView
import com.example.ui.components.TelemetryHubView
import com.example.ui.theme.*
import com.example.viewmodel.SiphonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SiphonViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()

    var isSidebarExpanded by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // --- 1. COLLAPSIBLE LEFT SIDEBAR NAV ---
        AnimatedVisibility(
            visible = isSidebarExpanded,
            enter = expandHorizontally(expandFrom = Alignment.Start, animationSpec = tween(250)),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start, animationSpec = tween(250))
        ) {
            SidebarNav(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) },
                onCollapseTrigger = { isSidebarExpanded = false }
            )
        }

        // --- 2. MAIN CONTENT WRAPPER ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Top Global Search & Profile Area
            TopBarArea(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSidebarExpanded = isSidebarExpanded,
                onExpandTrigger = { isSidebarExpanded = true },
                metrics = metrics
            )

            // Dynamic Main Workspace
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = currentTab,
                    animationSpec = tween(300),
                    label = "tab_crossfade"
                ) { tab ->
                    when (tab) {
                        "network_canvas" -> NetworkMap(viewModel = viewModel)
                        "telemetry_hub" -> TelemetryHubView(metrics = metrics)
                        "siphon_pipelines" -> PipelineTerminalView(logs = terminalLogs)
                        "ai_architect" -> AiAssistantView(viewModel = viewModel)
                        "settings" -> SettingsView()
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarNav(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onCollapseTrigger: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(SiphonDark)
            .border(1.dp, SiphonBorderDark.copy(alpha = 0.5f))
            .padding(vertical = 16.dp, horizontal = 12.dp)
            .testTag("sidebar_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Brand Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Miniature dynamic Canvas replicating the node logo from Imagen1.jpg
                Canvas(modifier = Modifier.size(28.dp)) {
                    val w = size.width
                    val h = size.height

                    // Draw interconnecting thin paths of the brand logo
                    drawLine(SiphonCyan, Offset(w * 0.2f, h * 0.3f), Offset(w * 0.8f, h * 0.2f), strokeWidth = 1.dp.toPx())
                    drawLine(SiphonMagenta, Offset(w * 0.8f, h * 0.2f), Offset(w * 0.5f, h * 0.8f), strokeWidth = 1.dp.toPx())
                    drawLine(SiphonYellow, Offset(w * 0.5f, h * 0.8f), Offset(w * 0.2f, h * 0.3f), strokeWidth = 1.dp.toPx())
                    drawLine(Color.White, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.8f, h * 0.2f), strokeWidth = 1.5.dp.toPx())

                    // Draw glowing color nodes
                    drawCircle(SiphonCyan, radius = 3.dp.toPx(), center = Offset(w * 0.2f, h * 0.3f))
                    drawCircle(SiphonMagenta, radius = 3.5.dp.toPx(), center = Offset(w * 0.8f, h * 0.2f))
                    drawCircle(SiphonYellow, radius = 3.dp.toPx(), center = Offset(w * 0.5f, h * 0.8f))
                    drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.5f))
                }

                // Replicate geometric "SoftSiphon" typography exactly
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = SiphonCyan, fontWeight = FontWeight.Bold)) {
                            append("Soft")
                        }
                        withStyle(style = SpanStyle(color = SiphonMagenta, fontWeight = FontWeight.SemiBold)) {
                            append("Si")
                        }
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Normal)) {
                            append("phon")
                        }
                    },
                    fontSize = 17.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
            }

            // Collapse icon
            IconButton(
                onClick = onCollapseTrigger,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Collapse Menu",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Nav List Items
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SidebarItem(
                label = "Network Canvas",
                icon = Icons.Default.Hub,
                isSelected = currentTab == "network_canvas",
                onClick = { onTabSelected("network_canvas") }
            )

            SidebarItem(
                label = "Telemetry Hub",
                icon = Icons.Default.Analytics,
                isSelected = currentTab == "telemetry_hub",
                onClick = { onTabSelected("telemetry_hub") }
            )

            SidebarItem(
                label = "Siphon Pipelines",
                icon = Icons.Default.Terminal,
                isSelected = currentTab == "siphon_pipelines",
                onClick = { onTabSelected("siphon_pipelines") }
            )

            SidebarItem(
                label = "AI Architect",
                icon = Icons.Default.AutoAwesome,
                isSelected = currentTab == "ai_architect",
                onClick = { onTabSelected("ai_architect") },
                badge = "Gemini"
            )

            Spacer(modifier = Modifier.height(24.dp))

            SidebarItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                isSelected = currentTab == "settings",
                onClick = { onTabSelected("settings") }
            )
        }

        // Corporate Footprint block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            Text(
                text = "SOFTSIPHON CORPORATE",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Version 3.8.4 - Live Sync",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.2f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SidebarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: String? = null
) {
    val bg = if (isSelected) SiphonSurfaceVariantDark else Color.Transparent
    val contentColor = if (isSelected) SiphonCyan else Color.White.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )

        badge?.let {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(listOf(SiphonCyan, SiphonMagenta)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = it,
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarArea(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSidebarExpanded: Boolean,
    onExpandTrigger: () -> Unit,
    metrics: com.example.viewmodel.SiphonMetrics
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trigger sidebar expansion if collapsed
        if (!isSidebarExpanded) {
            IconButton(
                onClick = onExpandTrigger,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Expand Sidebar", tint = SiphonDark)
            }
        }

        // Global responsive Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Global system search...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .widthIn(max = 300.dp)
                .height(42.dp)
                .testTag("global_search_input"),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SiphonCyan,
                unfocusedBorderColor = SiphonDark.copy(alpha = 0.1f),
                focusedContainerColor = SiphonDark.copy(alpha = 0.02f),
                unfocusedContainerColor = SiphonDark.copy(alpha = 0.02f)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Right hand stats / Profile block
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live nodes online stat badge
            Row(
                modifier = Modifier
                    .background(SiphonCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, SiphonCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(Color.Green, RoundedCornerShape(50)))
                Text(
                    text = "7/7 NODES ONLINE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = SiphonDark
                )
            }

            // Siphon rate ticker
            Row(
                modifier = Modifier
                    .background(SiphonMagenta.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, SiphonMagenta.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = SiphonMagenta, modifier = Modifier.size(12.dp))
                Text(
                    text = "${String.format("%.1f", metrics.throughputHistory.lastOrNull() ?: 0f)} GB/S",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = SiphonDark
                )
            }

            // User profile avatar representation
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SiphonDark),
                contentAlignment = Alignment.Center
            ) {
                Text("OP", color = SiphonYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SYSTEM SETTINGS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SiphonDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Siphon API Credentials", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SiphonDark)
                Text(
                    "You can configure a custom Gemini API Key below. This key will be used to generate rich architectural pipeline codes.",
                    fontSize = 11.sp,
                    color = SiphonDark.copy(alpha = 0.6f)
                )

                OutlinedTextField(
                    value = "***************************",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Gemini API Secrets Key") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = SiphonDark.copy(alpha = 0.15f),
                        disabledTextColor = SiphonDark.copy(alpha = 0.5f)
                    )
                )

                Text(
                    "Note: API keys are injected at compile time via the Secrets panel in Google AI Studio to maintain absolute corporate integrity.",
                    fontSize = 10.sp,
                    color = SiphonMagenta,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Global Visual Palette", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SiphonDark)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Replicate Original Contrast", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SiphonDark)
                        Text("Utilizes the exact light workspace and branding colors from logo.", fontSize = 10.sp, color = SiphonDark.copy(alpha = 0.5f))
                    }

                    Switch(checked = true, onCheckedChange = {})
                }
            }
        }
    }
}
