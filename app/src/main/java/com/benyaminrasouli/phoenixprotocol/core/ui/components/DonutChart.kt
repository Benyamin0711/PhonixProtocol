package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DonutChart(
    segments: List<Triple<String, Int, Color>>,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty()) return
    val total = segments.sumOf { it.second }.coerceAtLeast(1)
    Canvas(modifier = modifier.size(150.dp)) {
        val strokeWidth = 30.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f
        segments.forEach { (_, count, color) ->
            val sweepAngle = (count.toFloat() / total) * 360f
            drawArc(color = color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth))
            startAngle += sweepAngle
        }
    }
}