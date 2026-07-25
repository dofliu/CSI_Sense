package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.CsiViewModel
import com.example.ui.LanguageManager
import com.example.ui.getString
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyTorchBackendScreen(
    viewModel: CsiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isZh = currentLang == AppLanguage.ZH

    val config by viewModel.pyTorchConfig.collectAsState()

    var serverIpInput by remember(config.serverIp) { mutableStateOf(config.serverIp) }
    var serverPortInput by remember(config.serverPort) { mutableStateOf(config.serverPort.toString()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = getString("pytorch_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = getString("pytorch_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        // Server Connection Status Card
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
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Server",
                        tint = if (config.isConnected) EmeraldSecondary else AlertRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "PyTorch 模型伺服器狀態" else "PyTorch AI Server Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (config.isConnected) EmeraldSecondary.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (config.isConnected) "ONLINE" else "OFFLINE",
                            color = if (config.isConnected) EmeraldSecondary else AlertRed,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = serverIpInput,
                        onValueChange = { serverIpInput = it },
                        label = { Text(getString("pytorch_ip_label")) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("pytorch_ip_input")
                    )

                    OutlinedTextField(
                        value = serverPortInput,
                        onValueChange = { serverPortInput = it },
                        label = { Text(getString("pytorch_port_label")) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pytorch_port_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val portInt = serverPortInput.toIntOrNull() ?: 9999
                            viewModel.updatePyTorchBackend(serverIpInput, portInt, config.protocol)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_pytorch_btn")
                    ) {
                        Text(if (isZh) "更新伺服器 IP" else "Update Server")
                    }

                    Button(
                        onClick = { viewModel.testServerConnection() },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_pytorch_conn_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PurpleOnContainer)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(getString("pytorch_test_btn"), color = PurpleOnContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Model Information Summary Card
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
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "載入的邊緣模型架構" else "Loaded Deep Learning Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(if (isZh) "模型名稱" else "Model Architecture", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(config.modelName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(if (isZh) "傳輸延遲" else "Ping Latency", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${config.avgLatencyMs} ms", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
                        }
                    }
                }
            }
        }

        // Fall Threshold Fine-Tuning Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = AlertRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getString("pytorch_fall_threshold"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${String.format("%.0f", config.inferenceThreshold * 100)}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = AlertRed
                    )
                }

                Text(
                    text = if (isZh) "設定姿態模型判斷跌倒時的最低可信度門檻。" else "Minimum confidence percentage required to trigger critical fall warning.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Slider(
                    value = config.inferenceThreshold,
                    onValueChange = { viewModel.updateInferenceThreshold(it) },
                    valueRange = 0.5f..0.98f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = AlertRed,
                        activeTrackColor = AlertRed
                    ),
                    modifier = Modifier.testTag("inference_threshold_slider")
                )
            }
        }

        // Notification & Vibration Options
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(getString("pytorch_alert_vibration"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = config.enableVibrationAlert,
                        onCheckedChange = { viewModel.setAlertVibration(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = PurplePrimary),
                        modifier = Modifier.testTag("vibration_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = AlertRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(getString("pytorch_alert_popup"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = config.enablePopUpAlert,
                        onCheckedChange = { viewModel.setPopUpAlert(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AlertRed),
                        modifier = Modifier.testTag("popup_alert_switch")
                    )
                }
            }
        }
    }
}
