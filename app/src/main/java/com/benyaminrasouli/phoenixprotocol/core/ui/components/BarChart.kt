package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun BarChart(
    data: List<Pair<String, Int>>,
    barColor: Color = PhoenixOrange,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)
    Canvas(
        modifier = modifier.fillMaxWidth().height(150.dp)
    ) {
        val barWidth = size.width / (data.size * 2f)
        val chartHeight = size.height - 30.dp.toPx()
        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value.toFloat() / maxValue) * chartHeight
            val x = (index * 2 + 1) * barWidth
            drawRect(
                color = barColor,
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidth, barHeight)
            )
            drawContext.canvas.nativeCanvas.apply {
                drawText(value.toString(), x + barWidth / 2, chartHeight - barHeight - 5.dp.toPx(),
                    android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
                drawText(label, x + barWidth / 2, size.height - 5.dp.toPx(),
                    android.graphics.Paint().apply { color = TextSecondary.hashCode(); textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
            }
        }
    }
}