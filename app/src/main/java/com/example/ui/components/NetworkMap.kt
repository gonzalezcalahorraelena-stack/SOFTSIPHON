package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NetworkNode
import com.example.viewmodel.SiphonViewModel
import kotlin.math.sqrt

@Composable
fun NetworkMap(
    viewModel: SiphonViewModel,
    modifier: Modifier = Modifier
) {
    val nodes by viewModel.nodes.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val isSiphoningActive by viewModel.isSiphoningAnimationActive.collectAsState()

    var showAddNodeDialog by remember { mutableState modelOf(false) }
    var newNodeName by remember { mutableState modelOf("") }
    var newNodeColor by remember { mutableState modelOf(SiphonCyan) }

    // Pulse animation for flowing data particles along lines
    val infiniteTransition = rememberInfiniteTransition(label = "data_flow_particles")
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_progress"
    )

    // Pulsing glow for selected node
    val selectedGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = SineInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "selected_glow"
    )

    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Canvas for drawing connection links, nodes, and particles
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("network_canvas")
                .pointerInput(nodes) {
                    detectTapGestures { offset ->
                        // Detect node click based on Euclidean distance
                        var clickedNode: NetworkNode? = null
                        for (node in nodes) {
                            val nodeX = (node.x / 100f) * widthPx
                            val nodeY = (node.y / 100f) * heightPx
                            val dist = sqrt((offset.x - nodeX) * (offset.x - nodeX) + (offset.y - nodeY) * (offset.y - nodeY))
                            if (dist < 45f) { // 45px tap radius (about 18dp)
                                clickedNode = node
                                break
                            }
                        }
                        viewModel.selectNode(clickedNode)
                    }
                }
        ) {
            // 1. Draw connecting lines between nodes
            val drawnConnections = mutableSetOf<String>()
            nodes.forEach { node ->
                val nodeX = (node.x / 100f) * widthPx
                val nodeY = (node.y / 100f) * heightPx

                node.connectedTo.forEach { targetId ->
                    val target = nodes.find { it.id == targetId }
                    if (target != null) {
                        // Avoid drawing duplicate paths
                        val connectionKey = if (node.id < target.id) "${node.id}-${target.id}" else "${target.id}-${node.id}"
                        if (connectionKey !in drawnConnections) {
                            drawnConnections.add(connectionKey)
                            val targetX = (target.x / 100f) * widthPx
                            val targetY = (target.y / 100f) * heightPx

                            // Solid connection line
                            drawLine(
                                color = SiphonDark.copy(alpha = 0.15f),
                                start = Offset(nodeX, nodeY),
                                end = Offset(targetX, targetY),
                                strokeWidth = 2.dp.toPx()
                            )

                            // 2. Draw flowing data particles if siphoning is active
                            if (isSiphoningActive) {
                                // Flow from node -> target
                                val pX = nodeX + (targetX - nodeX) * particleProgress
                                val pY = nodeY + (targetY - nodeY) * particleProgress

                                // Multiple offset particles for complex feel
                                drawCircle(
                                    color = if (node.color == SiphonDark) SiphonMagenta else node.color,
                                    radius = 5.dp.toPx(),
                                    center = Offset(pX, pY)
                                )

                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = Offset(pX, pY)
                                )

                                // Reverse flow (target -> node) to double density
                                val revProgress = (particleProgress + 0.5f) % 1.0f
                                val rpX = targetX + (nodeX - targetX) * revProgress
                                val rpY = targetY + (nodeY - targetY) * revProgress

                                drawCircle(
                                    color = if (target.color == SiphonDark) SiphonCyan else target.color,
                                    radius = 4.dp.toPx(),
                                    center = Offset(rpX, rpY)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Draw Nodes themselves
            nodes.forEach { node ->
                val nodeX = (node.x / 100f) * widthPx
                val nodeY = (node.y / 100f) * heightPx
                val isSelected = selectedNode?.id == node.id

                // Draw external glowing outer circle if selected
                if (isSelected) {
                    drawCircle(
                        color = node.color,
                        radius = 24.dp.toPx(),
                        center = Offset(nodeX, nodeY),
                        alpha = selectedGlowAlpha
                    )
                }

                // Node Base Outer Border (Primary Cyan or Magenta etc)
                drawCircle(
                    color = node.color,
                    radius = 14.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )

                // White core overlay
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )

                // Dark Inner node dot
                drawCircle(
                    color = SiphonDark,
                    radius = 5.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )

                // Draw micro label text for the node
                val titleLayout = textMeasurer.measure(
                    text = AnnotatedString(node.name),
                    style = TextStyle(
                        color = SiphonDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                )

                drawText(
                    textLayoutResult = titleLayout,
                    topLeft = Offset(nodeX - titleLayout.size.width / 2f, nodeY + 18.dp.toPx())
                )

                val subtextLayout = textMeasurer.measure(
                    text = AnnotatedString(node.ipAddress),
                    style = TextStyle(
                        color = SiphonDark.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                )

                drawText(
                    textLayoutResult = subtextLayout,
                    topLeft = Offset(nodeX - subtextLayout.size.width / 2f, nodeY + 32.dp.toPx())
                )
            }
        }

        // Overlay 1: Interactive quick status / controls panel
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp)
                .width(220.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "Neural Hub",
                    tint = SiphonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "SIPHON NEURAL NET",
                    style = MaterialTheme.typography.titleSmall,
                    color = SiphonDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Interactive visual mapping of real-time pipeline ingestion nodes. Click a node to view properties.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = SiphonDark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Siphon State:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SiphonDark
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isSiphoningActive) SiphonCyan else SiphonMagenta,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Text(
                        text = if (isSiphoningActive) "PULSING" else "STANDBY",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = if (isSiphoningActive) SiphonCyan else SiphonMagenta
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.toggleSiphoning() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("toggle_siphon_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSiphoningActive) SiphonMagenta else SiphonCyan,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isSiphoningActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Trigger Toggle",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isSiphoningActive) "Pause Siphon" else "Resume Siphon",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Overlay 2: Top-Right node action button
        FloatingActionButton(
            onClick = { showAddNodeDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
                .testTag("add_node_fab"),
            containerColor = SiphonDark,
            contentColor = SiphonYellow,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Custom Siphon Node", modifier = Modifier.size(22.dp))
        }

        // Overlay 3: Bottom Sheet detail view (rendered inside Box for clean responsive alignment)
        selectedNode?.let { node ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("node_details_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SiphonDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(node.color, RoundedCornerShape(50))
                            )
                            Text(
                                text = node.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.selectNode(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("×", color = Color.White.copy(alpha = 0.6f), fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("IP ADDRESS", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(node.ipAddress, color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("THROUGHPUT", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${node.bandwidthGbps} Gbps", color = SiphonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("LATENCY", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${node.latencyMs} ms", color = SiphonMagenta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("NODE STATUS", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(node.status.uppercase(), color = SiphonYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Node dialog
    if (showAddNodeDialog) {
        AlertDialog(
            onDismissRequest = { showAddNodeDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = SiphonCyan)
                    Text("Provision Siphon Node", color = SiphonDark, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Configure a new corporate ingest node. This will bind directly into the neural network pathway.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SiphonDark.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = newNodeName,
                        onValueChange = { newNodeName = it },
                        label = { Text("Node Name") },
                        placeholder = { Text("e.g. AWS-Ingress-Chicago") },
                        modifier = Modifier.fillMaxWidth().testTag("new_node_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SiphonCyan,
                            unfocusedBorderColor = SiphonDark.copy(alpha = 0.2f),
                            focusedLabelColor = SiphonCyan
                        )
                    )

                    Text("Network Pathway Color", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SiphonDark)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(SiphonCyan, SiphonMagenta, SiphonYellow).forEach { color ->
                            val colorLabel = when (color) {
                                SiphonCyan -> "Cyan"
                                SiphonMagenta -> "Magenta"
                                else -> "Yellow"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .pointerInput(color) {
                                        detectTapGestures { newNodeColor = color }
                                    }
                            ) {
                                RadioButton(
                                    selected = newNodeColor == color,
                                    onClick = { newNodeColor = color },
                                    colors = RadioButtonDefaults.colors(selectedColor = color)
                                )
                                Text(colorLabel, style = MaterialTheme.typography.bodySmall, color = SiphonDark)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNodeName.isNotBlank()) {
                            viewModel.addNode(newNodeName, newNodeColor)
                            newNodeName = ""
                            showAddNodeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiphonCyan, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Provision")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNodeDialog = false }) {
                    Text("Cancel", color = SiphonDark.copy(alpha = 0.6f))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Utility to create a Custom Easing function similar to Sine
val SineInOutEasing = Easing { fraction ->
    ((1f - kotlin.math.cos(fraction * Math.PI.toFloat())) / 2f)
}
