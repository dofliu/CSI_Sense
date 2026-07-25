package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.CsiViewModel
import com.example.ui.LanguageManager
import com.example.ui.components.CsiSubcarrierSpectrumChart
import com.example.ui.components.CsiWaveformChart
import com.example.ui.getString
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CsiLiveScreen(
    viewModel: CsiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isZh = currentLang == AppLanguage.ZH

    val currentFrame by viewModel.currentFrame.collectAsState()
    val recentFrames by viewModel.recentFrames.collectAsState()
    val latestInference by viewModel.latestInference.collectAsState()
    val esp32Config by viewModel.esp32Config.collectAsState()
    val pyTorchConfig by viewModel.pyTorchConfig.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val lastPingStatus by viewModel.lastPingStatus.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header & Streaming Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isZh) "Wi-Fi CSI 即時姿態與頻譜" else "Wi-Fi CSI Real-time Spectrum",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isZh) "ESP32 熱點脈衝與 64 子載波相位分析" else "ESP32 Hotspot Pulse & 64 Subcarrier Phase Analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { viewModel.toggleStreaming() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isStreaming) EmeraldSecondary.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f))
                    .testTag("stream_toggle_button")
            ) {
                Icon(
                    imageVector = if (isStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Toggle Stream",
                    tint = if (isStreaming) EmeraldSecondary else AlertRed
                )
            }
        }

        // Emergency Fall Alert Banner
        AnimatedVisibility(
            visible = latestInference.isAlert,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = AlertRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isZh) "緊急警報：偵測到跌倒姿態！" else "CRITICAL ALERT: FALL DETECTED!",
                            fontWeight = FontWeight.Black,
                            color = AlertRed,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isZh) "CSI 頻譜出現多普勒突波。置信度：${String.format("%.1f", latestInference.confidence * 100)}%" else "High CSI Doppler fluctuation. Confidence: ${String.format("%.1f", latestInference.confidence * 100)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Live Real-Time Inference Card (Bold Theme Purple Accent)
        Card(
            colors = CardDefaults.cardColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "AI Model",
                        tint = PurpleContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "PyTorch AI 姿態推斷" else "PyTorch AI Action Inference",
                        style = MaterialTheme.typography.labelLarge,
                        color = PurpleContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (pyTorchConfig.isConnected) EmeraldSecondary else AlertRed)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (pyTorchConfig.isConnected) (if (isZh) "AI 伺服器連線" else "AI Server Online") else (if (isZh) "離線" else "Offline"),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = when (latestInference.actionName) {
                                "Standing", "Standing Up" -> if (isZh) "站立 / 靜止" else "Standing"
                                "Walking" -> if (isZh) "走動中" else "Walking"
                                "Sitting / Calm" -> if (isZh) "坐下 / 靜止" else "Sitting"
                                "Fall Detected" -> if (isZh) "警報：跌倒！" else "FALL DETECTED!"
                                else -> latestInference.actionName
                            },
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (isZh) "推斷延遲: ${latestInference.latencyMs} ms" else "Inference Latency: ${latestInference.latencyMs} ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = PurpleContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format("%.1f", latestInference.confidence * 100)}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = PurpleContainer
                        )
                        Text(
                            text = if (isZh) "模型置信度" else "Confidence",
                            style = MaterialTheme.typography.labelSmall,
                            color = PurpleContainer
                        )
                    }
                }
            }
        }

        // Hardware Status Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = "Hotspot",
                        tint = EmeraldSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isZh) "熱點連線" else "Hotspot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("IP: ${esp32Config.esp32Ip}", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Trigger",
                        tint = if (esp32Config.isTriggerEnabled) PurplePrimary else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isZh) "ESP32 觸發" else "ESP32 Trigger", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(if (esp32Config.isTriggerEnabled) "ENABLED (${esp32Config.samplingRateHz}Hz)" else "DISABLED", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "Subcarriers",
                        tint = AmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${esp32Config.subcarrierCount} ${if (isZh) "載波" else "Carriers"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("Ch. ${esp32Config.activeChannel} (2.4G)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        // Subcarrier Spectrum Visualization
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CsiSubcarrierSpectrumChart(
                frame = currentFrame,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Motion Waveform Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CsiWaveformChart(
                recentFrames = recentFrames,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Trigger Control & Quick Test Shortcut Actions
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Trigger",
                        tint = AmberWarning
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "ESP32 硬體脈衝觸發與測試" else "ESP32 Hardware Trigger & Controls",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isZh) "開啟 ESP32 UDP 觸發發送" else "ESP32 Trigger Pulse Stream",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = esp32Config.isTriggerEnabled,
                        onCheckedChange = { viewModel.setEsp32TriggerEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = PurplePrimary),
                        modifier = Modifier.testTag("trigger_switch")
                    )
                }

                Button(
                    onClick = { viewModel.sendEsp32TriggerPing() },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_trigger_ping_btn")
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isZh) "發送 UDP Ping 脈衝至 ESP32 (${esp32Config.esp32Ip})" else "Send UDP Trigger Pulse to ESP32 (${esp32Config.esp32Ip})")
                }

                lastPingStatus?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    text = if (isZh) "姿態推斷模擬測試：" else "Quick Motion Simulation:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 8.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "Sitting / Calm" to if (isZh) "坐下 / 靜止" else "Sitting",
                        "Walking" to if (isZh) "人體走動" else "Walking",
                        "Fall Detected" to if (isZh) "跌倒警報" else "Fall Alert",
                        "Standing Up" to if (isZh) "站立" else "Standing"
                    ).forEach { (actionKey, label) ->
                        OutlinedButton(
                            onClick = { viewModel.simulateBehavior(actionKey) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (actionKey == "Fall Detected") AlertRed else PurplePrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (actionKey == "Fall Detected") AlertRed.copy(alpha = 0.5f) else PurplePrimary.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
