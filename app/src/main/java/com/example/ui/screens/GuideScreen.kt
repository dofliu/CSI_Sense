package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.LanguageManager
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted

data class GuideSection(
    val id: String,
    val titleEn: String,
    val titleZh: String,
    val icon: ImageVector,
    val steps: List<GuideStep>
)

data class GuideStep(
    val headerEn: String,
    val headerZh: String,
    val bodyEn: String,
    val bodyZh: String,
    val codeSnippet: String? = null
)

@Composable
fun GuideScreen(
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isZh = currentLang == AppLanguage.ZH

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val sections = remember {
        listOf(
            GuideSection(
                id = "overview",
                titleEn = "System Architecture",
                titleZh = "1. 系統架構與原理",
                icon = Icons.Default.Sensors,
                steps = listOf(
                    GuideStep(
                        headerEn = "Wi-Fi Channel State Information (CSI)",
                        headerZh = "Wi-Fi 頻道狀態資訊 (CSI) 概念",
                        bodyEn = "CSI captures fine-grained physical layer wireless signal reflections across 64 subcarriers. When a person moves, stands, or falls in an indoor space, body reflections alter the subcarrier phase and amplitude signatures.",
                        bodyZh = "CSI 可擷取 Wi-Fi 物理層 64 個子載波的高解析度振幅與相位變動。當有人體在室內走動、坐下或跌倒時，干擾反射會改變無線電頻譜特徵，進而實現無穿戴式的姿態與跌倒偵測。"
                    ),
                    GuideStep(
                        headerEn = "Data Stream Flow",
                        headerZh = "系統整體數據串流架構",
                        bodyEn = "ESP32 (CSI Collector) ──[UDP:8888]──> Android App (CSI Filter & Spectrum) ──[UDP/Socket:9999]──> PyTorch AI Engine (CNN/LSTM Inference) ──> Fall Alert.",
                        bodyZh = "ESP32 數據收集節點 ──[UDP:8888 廣播]──> Android 手機 App (即時頻譜與降噪) ──[UDP/Socket:9999]──> PyTorch AI 邊緣模型 (神經網路推斷) ──> 跌倒警報通報。"
                    )
                )
            ),
            GuideSection(
                id = "esp32",
                titleEn = "ESP32 Hotspot Setup",
                titleZh = "2. ESP32 與熱點配置",
                icon = Icons.Default.Router,
                steps = listOf(
                    GuideStep(
                        headerEn = "Step 1: Mobile Hotspot Configuration",
                        headerZh = "步驟一：開啟手機個人熱點",
                        bodyEn = "Turn on Android Personal Hotspot with SSID 'ESP32_CSI_NET' and WPA2 Password '12345678'. Connect your ESP32 board to this hotspot.",
                        bodyZh = "請開啟 Android 手機熱點，名稱設為『ESP32_CSI_NET』，密碼為『12345678』。將 ESP32 開發板連至此熱點。"
                    ),
                    GuideStep(
                        headerEn = "Step 2: ESP32 Firmware UDP Broadcast",
                        headerZh = "步驟二：ESP32 韌體與 UDP 廣播設置",
                        bodyEn = "Flash your ESP32 with ESP-IDF or Arduino Wi-Fi CSI example. Configure the target UDP socket to send binary CSI packets to Port 8888.",
                        bodyZh = "使用 ESP-IDF 或 Arduino 燒錄 CSI 採樣韌體，將 UDP 數據套接字發送至本機手機 IP 的 8888 端口。",
                        codeSnippet = """// Arduino / ESP-IDF Snippet
#include <WiFi.h>
#include <WiFiUdp.h>
#include "esp_wifi.h"

WiFiUDP udp;
const char* ssid = "ESP32_CSI_NET";
const char* password = "12345678";

void setup() {
  WiFi.begin(ssid, password);
  // Enable CSI collection
  wifi_csi_config_t csi_config = {
    .lltf_en = true, .htltf_en = true, .stbc_en = true, .ltf_merge_en = true
  };
  esp_wifi_set_csi_config(&csi_config);
  esp_wifi_set_csi_rx_cb(csi_cb);
  esp_wifi_set_csi(true);
}"""
                    )
                )
            ),
            GuideSection(
                id = "pytorch",
                titleEn = "PyTorch AI Engine",
                titleZh = "3. PyTorch AI 伺服器配置",
                icon = Icons.Default.Memory,
                steps = listOf(
                    GuideStep(
                        headerEn = "Step 1: Python Socket Listener",
                        headerZh = "步驟一：啟動 Python Socket 監聽器",
                        bodyEn = "Run a lightweight Python script on your local PC or GPU edge device listening on UDP port 9999. Receive raw subcarrier array and run PyTorch 1D-CNN or LSTM model inference.",
                        bodyZh = "在 PC 或 邊緣 GPU 上執行 Python 腳本，監聽 UDP 9999 端口。接收 64 維度 CSI 子載波陣列，並傳入 1D-CNN / LSTM 深度學習模型。",
                        codeSnippet = """# Python PyTorch Server Snippet
import socket, json, torch

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(('0.0.0.0', 9999))
model = torch.load('csi_fall_model.pth').eval()

while True:
    data, addr = sock.recvfrom(2048)
    csi_matrix = parse_csi(data) # [1, 64]
    with torch.no_grad():
        out = model(csi_matrix)
        action_idx = torch.argmax(out).item()
    # Send result back to Android
    res = json.dumps({"action": action_names[action_idx], "conf": 0.98})
    sock.sendto(res.encode(), addr)"""
                    ),
                    GuideStep(
                        headerEn = "Step 2: Test Sync in App",
                        headerZh = "步驟二：在 App 中進行連線測試",
                        bodyEn = "Go to 'PyTorch AI' tab in app, enter Python server IP, and press 'Test Connection'. Verify ping latency < 35ms.",
                        bodyZh = "進入 App 內的『PyTorch AI』分頁，輸入 Python 伺服器 IP 並點擊『測試 AI 伺服器連線』，確認延遲在 35ms 以內。"
                    )
                )
            ),
            GuideSection(
                id = "spectrum",
                titleEn = "CSI Spectrum & Motion",
                titleZh = "4. CSI 頻譜與多普勒變動解析",
                icon = Icons.Default.Wifi,
                steps = listOf(
                    GuideStep(
                        headerEn = "Spectrum Chart Interpretation",
                        headerZh = "頻譜圖與波動解析",
                        bodyEn = "The top bar graph displays amplitude across 64 OFDM subcarriers. Stable indoor conditions yield flat, low-variance amplitude curves.",
                        bodyZh = "上方條狀圖代表 64 個 OFDM 子載波的能量振幅。無人狀態下曲線呈現平緩低方差；人體運動時會產生強烈多普勒干擾。"
                    ),
                    GuideStep(
                        headerEn = "Action Variance Profiles",
                        headerZh = "常見動作方差臨界參考",
                        bodyEn = "• Standing / Quiet: Variance < 2.5 dBm\n• Sitting: Short spike ~ 5.0 dBm\n• Walking: Periodic oscillations ~ 12.0 - 18.0 dBm\n• FALL ALERT: Sharp high-amplitude spike > 25.0 dBm within 300ms.",
                        bodyZh = "• 靜止 / 站立：方差 < 2.5 dBm\n• 坐下 / 微動：短期突波 ~ 5.0 dBm\n• 人體走動：週期性波幅 ~ 12.0 - 18.0 dBm\n• 跌倒警報：300ms 內出現超高振幅強烈突波 > 25.0 dBm！"
                    )
                )
            ),
            GuideSection(
                id = "tuning",
                titleEn = "Fall Alert Tuning",
                titleZh = "5. 跌倒警報與動態門檻設定",
                icon = Icons.Default.Warning,
                steps = listOf(
                    GuideStep(
                        headerEn = "Threshold Fine-Tuning",
                        headerZh = "動態靈敏度微調指南",
                        bodyEn = "If false alarms occur due to pets or doors, adjust the Trigger Threshold in Live CSI screen to -65 dBm or raise Fall Alarm Threshold to 85%.",
                        bodyZh = "若因寵物移動或關門誤報，可調降即時頻譜中的觸發門檻（如 -65 dBm）或將 PyTorch 跌倒判定置信度提高至 85% 以上。"
                    ),
                    GuideStep(
                        headerEn = "Vibration & Pop-up Dialog",
                        headerZh = "震動與懸浮視窗提醒",
                        bodyEn = "Enable 'Alert Vibration' and 'Pop-up Alert' under PyTorch tab to get high-priority warnings during sudden falls.",
                        bodyZh = "在 PyTorch 設定頁中開啟『跌倒警報震動提示』與『懸浮視窗通知』，可在突發跌倒時立即觸發手機高優先級震動警報。"
                    )
                )
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isZh) "系統使用說明與操作手冊" else "User Guide & System Manual",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isZh) "ESP32 姿態感測、PyTorch AI 與跌倒偵測完整教學" else "Step-by-step documentation for ESP32, PyTorch AI & Fall Detection",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // Horizontal Category Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PurplePrimary,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PurplePrimary
                )
            }
        ) {
            sections.forEachIndexed { index, sec ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.testTag("guide_tab_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = sec.icon,
                            contentDescription = null,
                            tint = if (selectedTabIndex == index) PurplePrimary else TextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isZh) sec.titleZh else sec.titleEn,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) PurplePrimary else TextMuted
                        )
                    }
                }
            }
        }

        // Active Section Content
        val currentSection = sections[selectedTabIndex]

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentSection.steps) { step ->
                GuideStepCard(step = step, isZh = isZh)
            }
        }
    }
}

@Composable
fun GuideStepCard(
    step: GuideStep,
    isZh: Boolean
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PurplePrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isZh) step.headerZh else step.headerEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isZh) step.bodyZh else step.bodyEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    if (step.codeSnippet != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = PurplePrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sample Code / 範例程式碼",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = step.codeSnippet,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
