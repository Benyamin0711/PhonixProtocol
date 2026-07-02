package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold

@Composable
fun LineChart(
    data: List<Pair<String, Int>>,
    lineColor: Color = PhoenixGold,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)
    Canvas(
        modifier = modifier.fillMaxWidth().height(150.dp)
    ) {
        val chartHeight = size.height - 30.dp.toPx()
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val linePath = Path()
        val fillPath = Path()
        data.forEachIndexed { index, (_, value) ->
            val x = index * stepX
            val y = chartHeight - (value.toFloat() / maxValue) * chartHeight
            if (index == 0) { linePath.moveTo(x, y); fillPath.moveTo(x, chartHeight); fillPath.lineTo(x, y) }
            else { linePath.lineTo(x, y); fillPath.lineTo(x, y) }
        }
        fillPath.lineTo(size.width, chartHeight)
        fillPath.close()
        drawPath(path = fillPath, brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)))
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 3.dp.toPx()))
    }
}