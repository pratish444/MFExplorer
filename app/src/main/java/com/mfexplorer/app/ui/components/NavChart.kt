package com.mfexplorer.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.mfexplorer.app.domain.model.NavEntry
import com.mfexplorer.app.ui.theme.GreenUp
import com.mfexplorer.app.ui.theme.RedDown
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class ChartPeriod(val label: String, val months: Int) {
    ONE_MONTH("1M", 1),
    SIX_MONTHS("6M", 6),
    ONE_YEAR("1Y", 12),
    THREE_YEARS("3Y", 36),
    ALL("ALL", Int.MAX_VALUE)
}

@Composable
fun NavChart(
    navHistory: List<NavEntry>,
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredData = remember(navHistory, selectedPeriod) {
        filterDataByPeriod(navHistory, selectedPeriod)
    }

    val sampledData = remember(filteredData) {
        sampleData(filteredData, maxPoints = 200)
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(sampledData) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    val isPositive = remember(sampledData) {
        if (sampledData.size >= 2) {
            sampledData.last().nav >= sampledData.first().nav
        } else true
    }

    val lineColor = if (isPositive) GreenUp else RedDown
    var touchedPoint by remember { mutableStateOf<NavEntry?>(null) }
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier) {
        // Period selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChartPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = {
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chart canvas
        if (sampledData.size >= 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .pointerInput(sampledData) {
                            detectTapGestures { offset ->
                                val chartWidth = size.width.toFloat()
                                val index = ((offset.x / chartWidth) * (sampledData.size - 1))
                                    .toInt()
                                    .coerceIn(0, sampledData.size - 1)
                                touchedPoint = sampledData[index]
                            }
                        }
                ) {
                    val chartPadding = 16f
                    val chartWidth = size.width - chartPadding * 2
                    val chartHeight = size.height - chartPadding * 2

                    val minNav = sampledData.minOf { it.nav }
                    val maxNav = sampledData.maxOf { it.nav }
                    val navRange = if (maxNav - minNav > 0) maxNav - minNav else 1.0

                    val animatedCount = (sampledData.size * animProgress.value).toInt()
                        .coerceAtLeast(2)
                    val visibleData = sampledData.take(animatedCount)

                    // Draw gradient fill
                    val fillPath = Path().apply {
                        visibleData.forEachIndexed { index, entry ->
                            val x = chartPadding + (index.toFloat() / (sampledData.size - 1)) * chartWidth
                            val y = chartPadding + ((maxNav - entry.nav) / navRange).toFloat() * chartHeight
                            if (index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        val lastX = chartPadding + ((visibleData.size - 1).toFloat() / (sampledData.size - 1)) * chartWidth
                        lineTo(lastX, size.height)
                        lineTo(chartPadding, size.height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.3f),
                                lineColor.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Draw line
                    val linePath = Path().apply {
                        visibleData.forEachIndexed { index, entry ->
                            val x = chartPadding + (index.toFloat() / (sampledData.size - 1)) * chartWidth
                            val y = chartPadding + ((maxNav - entry.nav) / navRange).toFloat() * chartHeight
                            if (index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(
                            width = 2.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw touch indicator
                    touchedPoint?.let { point ->
                        val index = sampledData.indexOf(point)
                        if (index >= 0) {
                            val x = chartPadding + (index.toFloat() / (sampledData.size - 1)) * chartWidth
                            val y = chartPadding + ((maxNav - point.nav) / navRange).toFloat() * chartHeight

                            // Vertical line
                            drawLine(
                                color = lineColor.copy(alpha = 0.5f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f
                            )

                            // Dot
                            drawCircle(
                                color = lineColor,
                                radius = 6f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // Touched value display
                touchedPoint?.let { point ->
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "₹${"%.2f".format(point.nav)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = lineColor
                        )
                        Text(
                            text = point.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun filterDataByPeriod(data: List<NavEntry>, period: ChartPeriod): List<NavEntry> {
    if (period == ChartPeriod.ALL || data.isEmpty()) return data
    val latestDate = data.maxOf { it.date }
    val cutoffDate = latestDate.minus(period.months.toLong(), ChronoUnit.MONTHS)
    return data.filter { it.date.isAfter(cutoffDate) || it.date.isEqual(cutoffDate) }
}

private fun sampleData(data: List<NavEntry>, maxPoints: Int): List<NavEntry> {
    if (data.size <= maxPoints) return data
    val step = data.size.toFloat() / maxPoints
    return (0 until maxPoints).map { i ->
        data[(i * step).toInt().coerceAtMost(data.size - 1)]
    }
}
