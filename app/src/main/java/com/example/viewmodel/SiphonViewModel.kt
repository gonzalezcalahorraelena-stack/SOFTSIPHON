package com.example.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiClient
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// --- Data Structures ---

data class NetworkNode(
    val id: Int,
    val name: String,
    var x: Float, // Relative 0f..100f
    var y: Float, // Relative 0f..100f
    var vx: Float = 0f,
    var vy: Float = 0f,
    val color: Color,
    val ipAddress: String,
    val bandwidthGbps: Double,
    val latencyMs: Int,
    val status: String, // "Active", "Syncing", "Siphoning", "Standby"
    val connectedTo: List<Int>
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

data class SiphonMetrics(
    val totalSiphonedGb: Double,
    val activePipelines: Int,
    val averageLatencyMs: Int,
    val cpuUsage: Int,
    val memoryUsage: Int,
    val throughputHistory: List<Float> // Store last 20 throughput entries
)

class SiphonViewModel : ViewModel() {

    // Selected Module
    private val _currentTab = MutableStateFlow("network_canvas")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Interactive Nodes
    private val _nodes = MutableStateFlow<List<NetworkNode>>(emptyList())
    val nodes: StateFlow<List<NetworkNode>> = _nodes.asStateFlow()

    // Selected Node for Detail Inspection
    private val _selectedNode = MutableStateFlow<NetworkNode?>(null)
    val selectedNode: StateFlow<NetworkNode?> = _selectedNode.asStateFlow()

    // Real-Time Siphon Metrics
    private val _metrics = MutableStateFlow(
        SiphonMetrics(
            totalSiphonedGb = 42984.2,
            activePipelines = 8,
            averageLatencyMs = 42,
            cpuUsage = 34,
            memoryUsage = 58,
            throughputHistory = List(20) { 1.5f + Random.nextFloat() * 2f }
        )
    )
    val metrics: StateFlow<SiphonMetrics> = _metrics.asStateFlow()

    // Live Simulated Terminal Logs
    private val _terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    // Gemini AI Chat History
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _isSiphoningAnimationActive = MutableStateFlow(true)
    val isSiphoningAnimationActive: StateFlow<Boolean> = _isSiphoningAnimationActive.asStateFlow()

    init {
        setupDefaultNodes()
        startTelemetrySimulation()
        startNodePhysicsSimulation()
        addInitialLogs()
        setupInitialAiWelcome()
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun selectNode(node: NetworkNode?) {
        _selectedNode.value = node
    }

    fun toggleSiphoning() {
        _isSiphoningAnimationActive.value = !_isSiphoningAnimationActive.value
        addLog("SYSTEM: Data Siphon Pulsing state set to ${_isSiphoningAnimationActive.value}")
    }

    private fun setupDefaultNodes() {
        _nodes.value = listOf(
            NetworkNode(
                id = 1, name = "AWS-US-East Ingress",
                x = 20f, y = 30f, color = SiphonCyan,
                ipAddress = "10.0.1.42", bandwidthGbps = 4.2, latencyMs = 12,
                status = "Siphoning", connectedTo = listOf(2, 4)
            ),
            NetworkNode(
                id = 2, name = "Neural Processing Core",
                x = 50f, y = 50f, color = SiphonDark,
                ipAddress = "10.100.8.101", bandwidthGbps = 12.8, latencyMs = 2,
                status = "Active", connectedTo = listOf(1, 3, 5, 6)
            ),
            NetworkNode(
                id = 3, name = "GCP-Europe Ingest",
                x = 80f, y = 25f, color = SiphonMagenta,
                ipAddress = "35.240.12.98", bandwidthGbps = 6.4, latencyMs = 28,
                status = "Siphoning", connectedTo = listOf(2, 7)
            ),
            NetworkNode(
                id = 4, name = "On-Prem Siphon Socket",
                x = 15f, y = 70f, color = SiphonYellow,
                ipAddress = "192.168.1.5", bandwidthGbps = 1.2, latencyMs = 8,
                status = "Syncing", connectedTo = listOf(1, 2)
            ),
            NetworkNode(
                id = 5, name = "Snowflake Databank",
                x = 85f, y = 75f, color = SiphonCyan,
                ipAddress = "104.22.4.15", bandwidthGbps = 9.0, latencyMs = 45,
                status = "Active", connectedTo = listOf(2, 6)
            ),
            NetworkNode(
                id = 6, name = "Azure-Asia Pipeline",
                x = 45f, y = 80f, color = SiphonMagenta,
                ipAddress = "40.112.5.210", bandwidthGbps = 3.5, latencyMs = 95,
                status = "Standby", connectedTo = listOf(2, 5)
            ),
            NetworkNode(
                id = 7, name = "Kafka Streaming Ingestion",
                x = 70f, y = 40f, color = SiphonYellow,
                ipAddress = "10.10.2.14", bandwidthGbps = 5.6, latencyMs = 15,
                status = "Siphoning", connectedTo = listOf(3)
            )
        )
    }

    private fun startTelemetrySimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            val logFormater = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            while (true) {
                delay(1000)
                val isSiphoning = _isSiphoningAnimationActive.value

                // Tick up metrics
                val deltaGb = if (isSiphoning) (1.2 + Random.nextDouble() * 1.5) else 0.1
                val newCpu = if (isSiphoning) (40 + Random.nextInt(25)) else (15 + Random.nextInt(10))
                val newMemory = if (isSiphoning) (60 + Random.nextInt(5)) else (45 + Random.nextInt(3))
                val newThroughput = if (isSiphoning) (2.5f + Random.nextFloat() * 2.5f) else 0.2f

                _metrics.value = _metrics.value.let { m ->
                    val updatedHistory = m.throughputHistory.drop(1) + newThroughput
                    m.copy(
                        totalSiphonedGb = m.totalSiphonedGb + deltaGb,
                        cpuUsage = newCpu,
                        memoryUsage = newMemory,
                        throughputHistory = updatedHistory,
                        averageLatencyMs = if (isSiphoning) (35 + Random.nextInt(15)) else 8
                    )
                }

                // Add random logs to simulate activity
                if (isSiphoning && Random.nextFloat() > 0.4f) {
                    val time = logFormater.format(Date())
                    val logType = Random.nextInt(4)
                    val newLog = when (logType) {
                        0 -> "[$time] [SIPHON] Siphoned 102.4 MB from GCP-Europe -> Neural Core (Success)"
                        1 -> "[$time] [PROCESS] Clustering incoming packets via neural classification..."
                        2 -> "[$time] [METRIC] Latency stabilized at ${_metrics.value.averageLatencyMs}ms (99.98% SLA)"
                        else -> "[$time] [PIPELINE] Siphon socket push completed for Snowflake databank [104.22.4.15]"
                    }
                    addLog(newLog)
                }
            }
        }
    }

    private fun startNodePhysicsSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(30) // ~33 FPS physics update for silky smoothness
                _nodes.value = _nodes.value.map { node ->
                    // Exclude currently dragged node if implemented
                    // Add micro-velocities to float smoothly
                    var vx = node.vx + (Random.nextFloat() - 0.5f) * 0.15f
                    var vy = node.vy + (Random.nextFloat() - 0.5f) * 0.15f

                    // Damping friction
                    vx *= 0.92f
                    vy *= 0.92f

                    // Limit speed
                    val speed = kotlin.math.sqrt(vx * vx + vy * vy)
                    val maxSpeed = 0.5f
                    if (speed > maxSpeed) {
                        vx = (vx / speed) * maxSpeed
                        vy = (vy / speed) * maxSpeed
                    }

                    // Apply movement
                    var newX = node.x + vx
                    var newY = node.y + vy

                    // Boundary collisions
                    val margin = 5f
                    if (newX < margin) { newX = margin; vx = -vx }
                    if (newX > 100f - margin) { newX = 100f - margin; vx = -vx }
                    if (newY < margin) { newY = margin; vy = -vy }
                    if (newY > 100f - margin) { newY = 100f - margin; vy = -vy }

                    node.copy(x = newX, y = newY, vx = vx, vy = vy)
                }
            }
        }
    }

    private fun addInitialLogs() {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val now = Date()
        _terminalLogs.value = listOf(
            "[${dateFormat.format(Date(now.time - 5000))}] [SYSTEM] SoftSiphon pipeline kernel initialized successfully.",
            "[${dateFormat.format(Date(now.time - 4000))}] [CONNECT] Connected to 7 active corporate siphoning nodes.",
            "[${dateFormat.format(Date(now.time - 3000))}] [SECURITY] Advanced encryption sockets activated (#00A4E4 -> #E5007D).",
            "[${dateFormat.format(Date(now.time - 2000))}] [PIPELINE] Dynamic data compression set to 4.2x ratio.",
            "[${dateFormat.format(Date(now.time - 1000))}] [SYSTEM] Siphon engine humming at 42.9 TB active dataset size."
        )
    }

    private fun addLog(log: String) {
        val current = _terminalLogs.value.toMutableList()
        current.add(log)
        if (current.size > 50) {
            current.removeAt(0)
        }
        _terminalLogs.value = current
    }

    private fun setupInitialAiWelcome() {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        _chatHistory.value = listOf(
            ChatMessage(
                text = "Welcome to SoftSiphon AI Architect. I am your generative data engineer. " +
                        "I can help you monitor this telemetry workspace, generate custom siphoning pipelines, " +
                        "or analyze node routes. Ask me anything!",
                isUser = false,
                timestamp = dateFormat.format(Date())
            )
        )
    }

    fun addNode(name: String, color: Color) {
        val id = (_nodes.value.maxOfOrNull { it.id } ?: 0) + 1
        val ip = "10.${Random.nextInt(255)}.${Random.nextInt(255)}.${Random.nextInt(254) + 1}"
        val connections = if (_nodes.value.isNotEmpty()) {
            listOf(_nodes.value.random().id)
        } else {
            emptyList()
        }

        val newNode = NetworkNode(
            id = id,
            name = name,
            x = 30f + Random.nextFloat() * 40f,
            y = 30f + Random.nextFloat() * 40f,
            color = color,
            ipAddress = ip,
            bandwidthGbps = 2.0 + Random.nextDouble() * 5.0,
            latencyMs = Random.nextInt(10, 80),
            status = "Syncing",
            connectedTo = connections
        )

        val updatedList = _nodes.value.toMutableList()
        // Establish bidirectional connection in default setup
        if (connections.isNotEmpty()) {
            val targetId = connections[0]
            val index = updatedList.indexOfFirst { it.id == targetId }
            if (index != -0) {
                val targetNode = updatedList[index]
                updatedList[index] = targetNode.copy(connectedTo = targetNode.connectedTo + id)
            }
        }

        updatedList.add(newNode)
        _nodes.value = updatedList
        addLog("SYSTEM: Created new neural node '$name' [$ip] with dynamic connection pathways.")
    }

    fun askAi(prompt: String) {
        if (prompt.isBlank()) return
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val userMsg = ChatMessage(prompt, true, dateFormat.format(Date()))
        _chatHistory.value = _chatHistory.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val contextString = buildContextForGemini()
            val fullPrompt = "You are the SoftSiphon AI Architect. Here is the current system state:\n" +
                    "$contextString\n\n" +
                    "The corporate user asks: \"$prompt\"\n\n" +
                    "Provide an elite, professional, corporate-grade technical answer as SoftSiphon's chief architect. " +
                    "Keep your answer clean, extremely technical yet readable, and focus on data siphoning, telemetry flow, and network safety."

            val rawAnswer = GeminiClient.generateAnswer(fullPrompt)

            val finalAnswer = if (rawAnswer == "API_KEY_MISSING") {
                simulateAiAnswer(prompt)
            } else if (rawAnswer.startsWith("API_ERROR:")) {
                "**System Pipeline Error**: Could not complete AI request due to network configuration. \n\n" +
                        "**Pre-compiled Architectural analysis for: \"$prompt\"**\n\n" +
                        simulateAiAnswer(prompt)
            } else {
                rawAnswer
            }

            _chatHistory.value = _chatHistory.value + ChatMessage(
                text = finalAnswer,
                isUser = false,
                timestamp = dateFormat.format(Date())
            )
            _isAiLoading.value = false
        }
    }

    private fun buildContextForGemini(): String {
        val totalNodes = _nodes.value.size
        val nodeNames = _nodes.value.joinToString { "${it.name} (${it.status}, IP:${it.ipAddress})" }
        val cpu = _metrics.value.cpuUsage
        val mem = _metrics.value.memoryUsage
        val siphoned = _metrics.value.totalSiphonedGb
        val throughput = _metrics.value.throughputHistory.lastOrNull() ?: 0f

        return """
            - Active Nodes Count: $totalNodes
            - Node List: $nodeNames
            - Workspace Metrics: CPU: $cpu%, MEMORY: $mem%, Siphoned Data: $siphoned GB, Current Throughput: $throughput GB/s
            - Siphoning Sockets Status: ${_isSiphoningAnimationActive.value}
        """.trimIndent()
    }

    private fun simulateAiAnswer(prompt: String): String {
        // High-fidelity fallback answers if the key is not in settings
        val p = prompt.lowercase()
        return when {
            p.contains("optimize") || p.contains("bottleneck") -> {
                "**SoftSiphon Pipeline Optimization Report**\n\n" +
                        "1. **Azure-Asia Pipeline [40.112.5.210]** currently shows high latency (95ms). We recommend allocating 4 extra siphoning channels to absorb buffer spikes.\n" +
                        "2. **Kafka Streaming Ingestion** is pushing 5.6 Gbps. Clustering is running at 99.98% CPU affinity.\n" +
                        "3. **Recommendation**: Initiate a cyan-flow load balancer to siphon GCP-Europe traffic evenly through AWS-US-East routing pathways."
            }
            p.contains("safety") || p.contains("security") || p.contains("secure") -> {
                "**Neural Node Cryptographic Audit**\n\n" +
                        "- All 7 active connections are secured with SHA-512 network handshakes, colored in **Primary Magenta (#E5007D)** on your telemetry dashboard to indicate encrypted state.\n" +
                        "- **Neural Processing Core** [10.100.8.101] operates as the cryptographic master socket. No unauthorized packet extraction detected.\n" +
                        "- **Vulnerability Alert**: Azure-Asia connection path has an unshielded node link. Apply RSA-4096 shielding via: `siphon.pipeline.shield(\"AZ-ASIA\", keys.RSA_4096)`."
            }
            p.contains("code") || p.contains("pipeline") || p.contains("generate") -> {
                "**SoftSiphon JSON-Schema Data Extraction Pipeline**\n\n" +
                        "```json\n" +
                        "{\n" +
                        "  \"siphon\": {\n" +
                        "    \"pipelineId\": \"SS-HYBRID-988\",\n" +
                        "    \"source\": \"aws.us-east-1.s3.telemetry\",\n" +
                        "    \"target\": \"snowflake.databank.neural_core\",\n" +
                        "    \"siphon_colors\": [\"#00A4E4\", \"#E5007D\"],\n" +
                        "    \"encryption\": \"AES-256-GCM\",\n" +
                        "    \"filtering_schema\": {\n" +
                        "      \"include\": [\"telemetry.*\", \"analytics.kpi\"],\n" +
                        "      \"exclude\": [\"pii.social_security\"]\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n" +
                        "```\n" +
                        "Apply this directly on the **Siphon Pipelines** tab to trigger automatic deployment."
            }
            else -> {
                "**SoftSiphon Intelligent Siphon Feedback**\n\n" +
                        "Your telemetry workspace is running at peak capacity. Total siphoned volume is ticking up at ~2.4 GB/s. " +
                        "No operational alerts. You can safely add nodes using the **(+) Action** on the bottom of the map panel, or toggle the **Siphon Pulsar**."
            }
        }
    }
}
