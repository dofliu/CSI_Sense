package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CsiFrame
import com.example.ui.AppLanguage
import com.example.ui.LanguageManager
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.PurplePrimary

@Composable
fun CsiSubcarrierSpectrumChart(
    frame: CsiFrame,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isZh = currentLang == AppLanguage.ZH

    val amplitudes = frame.subcarrierAmplitudes
    val subCount = amplitudes.size

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isZh) "CSI 子載波能量振幅頻譜 ($subCount 載波)" else "CSI Subcarrier Spectrum ($subCount Carriers)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "SNR: ${String.format("%.1f", frame.snr)} dB",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (subCount > 0) {
                    val barWidth = width / subCount
                    val maxVal = 40.0f

                    for (i in amplitudes.indices) {
                        val amp = amplitudes[i].coerceIn(0f, maxVal)
                        val barHeight = (amp / maxVal) * height
                        val x = i * barWidth
                        val y = height - barHeight

                        val color = when {
                            amp > 30f -> AlertRed
                            amp > 20f -> AmberWarning
                            else -> PurplePrimary
                        }

                        drawRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(barWidth * 0.8f, barHeight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CsiWaveformChart(
    recentFrames: List<CsiFrame>,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isZh = currentLang == AppLanguage.ZH

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isZh) "即時多普勒波動與特徵方差" else "Doppler Motion Waveform & Variance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            val currentVar = recentFrames.lastOrNull()?.csiVariance ?: 0f
            Text(
                text = "Var: ${String.format("%.2f", currentVar)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (currentVar > 20f) AlertRed else PurplePrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (recentFrames.size > 1) {
                    val path = Path()
                    val maxVar = 35.0f
                    val stepX = width / (recentFrames.size - 1)

                    recentFrames.forEachIndexed { i, f ->
                        val norm = (f.csiVariance / maxVar).coerceIn(0f, 1f)
                        val x = i * stepX
                        val y = height - (norm * height)

                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = PurplePrimary,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyActivityBarChart(
    hourlyData: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val maxCount = (hourlyData.values.maxOrNull() ?: 1).toFloat()
            val barWidth = width / 24f

            for (hour in 0 until 24) {
                val count = hourlyData[hour] ?: 0
                val barHeight = (count / maxCount) * height
                val x = hour * barWidth
                val y = height - barHeight

                drawRect(
                    color = if (count > 0) PurplePrimary else BorderOutline,
                    topLeft = Offset(x, y),
                    size = Size(barWidth * 0.7f, barHeight.coerceAtLeast(2f))
                )
            }
        }
    }
}
