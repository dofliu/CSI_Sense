package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage {
    EN, ZH
}

object LanguageManager {
    private val _currentLanguage = MutableStateFlow(AppLanguage.ZH) // Default to Chinese as requested
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.EN) AppLanguage.ZH else AppLanguage.EN
    }
}

@Composable
fun getString(key: String): String {
    val lang by LanguageManager.currentLanguage.collectAsState()
    return getLocalizedString(key, lang)
}

fun getLocalizedString(key: String, lang: AppLanguage): String {
    val isZh = lang == AppLanguage.ZH
    return when (key) {
        // App Headers
        "app_title" -> if (isZh) "CSI 訊號分析與跌倒偵測" else "CSI Analyzer & Fall Detector"
        "app_subtitle" -> if (isZh) "ESP32 熱點與 PyTorch AI 推理系統" else "ESP32 Hotspot & PyTorch AI Engine"
        
        // Nav Items
        "nav_live" -> if (isZh) "即時 CSI 訊號" else "Live CSI"
        "nav_esp32" -> if (isZh) "ESP32 熱點" else "ESP32 Control"
        "nav_pytorch" -> if (isZh) "PyTorch AI" else "PyTorch AI"
        "nav_history" -> if (isZh) "歷史日誌" else "Event Logs"
        "nav_guide" -> if (isZh) "使用說明" else "User Guide"

        // Live CSI
        "live_spectrum_title" -> if (isZh) "即時 CSI 頻譜分析儀" else "Live Spectrum Analyzer"
        "live_subcarriers_label" -> if (isZh) "個子載波" else "Subcarriers"
        "live_inference_result" -> if (isZh) "模型即時推斷結果" else "Model Inference Result"
        "live_confidence_score" -> if (isZh) "置信度 / 可信度" else "Confidence Score"
        "live_sampling_rate" -> if (isZh) "採樣頻率" else "Sampling Rate"
        "live_trigger_threshold" -> if (isZh) "觸發門檻" else "Trigger Threshold"
        "live_esp32_trigger" -> if (isZh) "ESP32 訊號觸發" else "ESP32 Signal Trigger"
        "live_denoising_filter" -> if (isZh) "降噪濾波器" else "Denoising Filter"
        "live_simulate_title" -> if (isZh) "模擬姿態與跌倒推斷" else "Simulate Pose & Motion"
        "live_start_monitor" -> if (isZh) "開始監測" else "Start Monitoring"
        "live_stop_monitor" -> if (isZh) "停止監測" else "Stop Monitoring"
        "action_standing" -> if (isZh) "站立" else "Standing"
        "action_walking" -> if (isZh) "行走" else "Walking"
        "action_sitting" -> if (isZh) "坐下" else "Sitting"
        "action_fall" -> if (isZh) "警報: 跌倒!" else "ALERT: FALL DETECTED!"

        // ESP32
        "esp32_title" -> if (isZh) "ESP32 移動熱點與硬體控制" else "ESP32 Hotspot & Hardware Setup"
        "esp32_desc" -> if (isZh) "設定 ESP32 UDP 發送端 IP 與本機接收數據連線" else "Configure ESP32 UDP transmission & local socket streaming"
        "esp32_ip_label" -> if (isZh) "ESP32 裝置 IP" else "ESP32 Device IP"
        "esp32_port_label" -> if (isZh) "UDP 本機接收端口" else "UDP Local Listening Port"
        "esp32_ping_btn" -> if (isZh) "發送 Ping 測試" else "Send Ping Test"
        "esp32_signal_strength" -> if (isZh) "WiFi 訊號強度 (RSSI)" else "WiFi Signal Strength (RSSI)"

        // PyTorch
        "pytorch_title" -> if (isZh) "PyTorch AI 推理伺服器" else "PyTorch AI Inference Server"
        "pytorch_desc" -> if (isZh) "與邊緣深度學習模型同步進行即時姿態推斷" else "Synchronize realtime posture inference with remote AI backend"
        "pytorch_ip_label" -> if (isZh) "伺服器 IP" else "Server IP Address"
        "pytorch_port_label" -> if (isZh) "通信端口" else "Communication Port"
        "pytorch_test_btn" -> if (isZh) "測試 AI 伺服器連線" else "Test AI Server Connection"
        "pytorch_fall_threshold" -> if (isZh) "跌倒觸發判定門檻" else "Fall Alarm Threshold"
        "pytorch_alert_vibration" -> if (isZh) "跌倒警報震動提示" else "Alert Vibration Feedback"
        "pytorch_alert_popup" -> if (isZh) "懸浮視窗警報通知" else "Pop-up Alert Dialog"

        // History
        "history_title" -> if (isZh) "歷史日誌與事件記錄" else "Action History & Event Logs"
        "history_total" -> if (isZh) "總事件紀錄" else "Total Events"
        "history_fall_count" -> if (isZh) "跌倒警報數" else "Fall Alerts"
        "history_avg_conf" -> if (isZh) "平均置信度" else "Avg Confidence"
        "history_clear_btn" -> if (isZh) "清除歷史紀錄" else "Clear Logs"
        "history_filter_alerts" -> if (isZh) "僅顯示跌倒警報" else "Show Alerts Only"
        "history_show_all" -> if (isZh) "顯示所有事件" else "Show All Events"

        // Guide
        "guide_title" -> if (isZh) "系統使用說明與操作手冊" else "User Guide & Manual"
        "guide_subtitle" -> if (isZh) "詳細說明 ESP32 CSI 採樣、PyTorch 推理與跌倒偵測配置" else "Comprehensive setup & troubleshooting guide for CSI tracking"

        else -> key
    }
}
