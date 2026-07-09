package com.benyaminrasouli.phoenixprotocol.feature.focus

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PauseIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Left bar
            moveTo(6f, 4f)
            lineTo(10f, 4f)
            lineTo(10f, 20f)
            lineTo(6f, 20f)
            close()
            // Right bar
            moveTo(14f, 4f)
            lineTo(18f, 4f)
            lineTo(18f, 20f)
            lineTo(14f, 20f)
            close()
        }
    }.build()
}

val FullScreenIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FullScreen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Top-left corner arrow
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 3f)
            lineTo(3f, 8f)
            lineTo(5f, 8f)
            lineTo(5f, 5f)
            lineTo(8f, 5f)
            lineTo(8f, 3f)
            close()
        }
        // Top-right corner arrow
        path(fill = SolidColor(Color.Black)) {
            moveTo(21f, 3f)
            lineTo(21f, 8f)
            lineTo(19f, 8f)
            lineTo(19f, 5f)
            lineTo(16f, 5f)
            lineTo(16f, 3f)
            close()
        }
        // Bottom-left corner arrow
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 21f)
            lineTo(3f, 16f)
            lineTo(5f, 16f)
            lineTo(5f, 19f)
            lineTo(8f, 19f)
            lineTo(8f, 21f)
            close()
        }
        // Bottom-right corner arrow
        path(fill = SolidColor(Color.Black)) {
            moveTo(21f, 21f)
            lineTo(21f, 16f)
            lineTo(19f, 16f)
            lineTo(19f, 19f)
            lineTo(16f, 19f)
            lineTo(16f, 21f)
            close()
        }
    }.build()
}
