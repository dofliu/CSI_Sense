package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.CsiActionLog
import com.example.ui.AppLanguage
import com.example.ui.CsiViewModel
import com.example.ui.LanguageManager
import com.example.ui.components.HourlyActivityBarChart
import com.example.ui.getString
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CsiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isZh = currentLang == AppLanguage.ZH

    val allLogs by viewModel.allActionLogs.collectAsState(initial = emptyList())
    var filterOnlyAlerts by remember { mutableStateOf(false) }

    val filteredLogs = if (filterOnlyAlerts) allLogs.filter { it.isAlert } else allLogs
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val fallCount = allLogs.count { it.isAlert }
    val avgConfidence = if (allLogs.isNotEmpty()) allLogs.map { it.confidence }.average() else 0.0

    val hourlyData = remember(allLogs) {
        val map = mutableMapOf<Int, Int>()
        allLogs.forEach { log ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = log.timestamp
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            map[hour] = (map[hour] ?: 0) + 1
        }
        map
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
                    text = getString("history_title"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isZh) "姿態推斷紀錄、跌倒警報與 CSI 雜訊歷史審查" else "Historical records of detected behaviors, falls, and CSI variances",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            if (allLogs.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier.testTag("clear_logs_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Logs",
                        tint = AlertRed
                    )
                }
            }
        }

        // Summary Statistics Row
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
                    Text(getString("history_total"), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("${allLogs.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = PurplePrimary)
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
                    Text(getString("history_fall_count"), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$fallCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = if (fallCount > 0) AlertRed else EmeraldSecondary)
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
                    Text(getString("history_avg_conf"), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("${String.format("%.0f", avgConfidence * 100)}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = EmeraldSecondary)
                }
            }
        }

        // Hourly Activity Distribution Chart
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
                    Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "每小時人體動態分布" else "Hourly Activity Distribution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                HourlyActivityBarChart(hourlyData = hourlyData)
            }
        }

        // Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isZh) "事件推斷日誌" else "Inference Event Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))

            FilterChip(
                selected = filterOnlyAlerts,
                onClick = { filterOnlyAlerts = !filterOnlyAlerts },
                label = { Text(if (filterOnlyAlerts) getString("history_filter_alerts") else getString("history_show_all"), fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AlertRed.copy(alpha = 0.2f),
                    selectedLabelColor = AlertRed
                ),
                modifier = Modifier.testTag("filter_alerts_chip")
            )
        }

        // Log List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (filterOnlyAlerts) (if (isZh) "尚無跌倒警報紀錄" else "No fall alerts logged yet") else (if (isZh) "尚無動作日誌紀錄" else "No action events recorded yet"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    ActionLogItemCard(log = log, dateFormat = dateFormat, isZh = isZh)
                }
            }
        }
    }
}

@Composable
fun ActionLogItemCard(
    log: CsiActionLog,
    dateFormat: SimpleDateFormat,
    isZh: Boolean
) {
    val dateStr = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (log.isAlert) AlertRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (log.isAlert) AlertRed.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (log.isAlert) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = if (log.isAlert) AlertRed else PurplePrimary
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (log.actionName) {
                            "Standing", "Standing Up" -> if (isZh) "站立 / 靜止" else "Standing"
                            "Walking" -> if (isZh) "走動中" else "Walking"
                            "Sitting / Calm" -> if (isZh) "坐下" else "Sitting"
                            "Fall Detected" -> if (isZh) "警報：跌倒！" else "FALL DETECTED!"
                            else -> log.actionName
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (log.isAlert) AlertRed else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.0f", log.confidence * 100)}% Conf.",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "RSSI: ${log.signalRssi} dBm | CSI Var: ${String.format("%.1f", log.csiVariance)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
