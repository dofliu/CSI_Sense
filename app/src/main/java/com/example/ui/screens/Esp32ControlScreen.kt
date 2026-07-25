package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Esp32ControlScreen(
    viewModel: CsiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isZh = currentLang == AppLanguage.ZH

    val config by viewModel.esp32Config.collectAsState()
    val lastPingStatus by viewModel.lastPingStatus.collectAsState()

    var ipInput by remember(config.esp32Ip) { mutableStateOf(config.esp32Ip) }
    var portInput by remember(config.esp32Port) { mutableStateOf(config.esp32Port.toString()) }

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
                text = getString("esp32_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = getString("esp32_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        // ESP32 IP & UDP Port Network Card
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
                    Icon(imageVector = Icons.Default.Router, contentDescription = null, tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "UDP 本機廣播連線設定" else "ESP32 UDP Socket Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text(getString("esp32_ip_label")) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("esp32_ip_input")
                    )

                    OutlinedTextField(
                        value = portInput,
                        onValueChange = { portInput = it },
                        label = { Text(getString("esp32_port_label")) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("esp32_port_input")
                    )
                }

                Button(
                    onClick = {
                        val portInt = portInput.toIntOrNull() ?: 8888
                        viewModel.updateEsp32Network(ipInput, portInt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_esp32_network_btn")
                ) {
                    Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isZh) "儲存 UDP 網絡配置" else "Save UDP Connection Settings")
                }

                Button(
                    onClick = { viewModel.sendEsp32TriggerPing() },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ping_esp32_btn")
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = PurpleOnContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(getString("esp32_ping_btn"), color = PurpleOnContainer, fontWeight = FontWeight.Bold)
                }

                lastPingStatus?.let { status ->
                    Text(text = status, style = MaterialTheme.typography.bodySmall, color = EmeraldSecondary)
                }
            }
        }

        // Sampling Frequency Slider Card
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
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "CSI 採樣頻率 (Sampling Frequency)" else "CSI Sampling Frequency (Hz)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${config.samplingRateHz} Hz",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = PurplePrimary
                    )
                }

                Text(
                    text = if (isZh) "較高採樣頻率 (如 50Hz) 可精確擷取快速跌倒過程，但會增加 CPU 負擔。" else "Higher sampling rate (50Hz) captures fast fall dynamics but increases CPU usage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Slider(
                    value = config.samplingRateHz.toFloat(),
                    onValueChange = { viewModel.updateSamplingRate(it.toInt()) },
                    valueRange = 5f..100f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = PurplePrimary,
                        activeTrackColor = PurplePrimary
                    ),
                    modifier = Modifier.testTag("sampling_rate_slider")
                )
            }
        }

        // Subcarrier Count Selection (32, 64, 128)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CellTower, contentDescription = null, tint = AmberWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "OFDM 子載波數量 (Subcarrier Count)" else "OFDM Subcarriers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(32, 64, 128).forEachIndexed { index, count ->
                        SegmentedButton(
                            selected = config.subcarrierCount == count,
                            onClick = { viewModel.updateSubcarrierCount(count) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            modifier = Modifier.testTag("subcarrier_btn_$count")
                        ) {
                            Text("$count Carriers")
                        }
                    }
                }
            }
        }

        // Filter Mode Options
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = EmeraldSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "相位與降噪濾波算法" else "CSI Phase & Denoising Algorithm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Raw" to if (isZh) "原始數據" else "Raw",
                        "Butterworth" to if (isZh) "巴特沃斯低通" else "Butterworth",
                        "Hampel Filter" to if (isZh) "Hampel 離群濾波" else "Hampel Filter",
                        "PCA" to if (isZh) "PCA 主成分分析" else "PCA Method"
                    ).forEach { (modeKey, label) ->
                        FilterChip(
                            selected = config.noiseFilterLevel == modeKey,
                            onClick = { viewModel.updateFilterMode(modeKey) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleContainer,
                                selectedLabelColor = PurpleOnContainer
                            ),
                            modifier = Modifier.testTag("filter_chip_$modeKey")
                        )
                    }
                }
            }
        }
    }
}
