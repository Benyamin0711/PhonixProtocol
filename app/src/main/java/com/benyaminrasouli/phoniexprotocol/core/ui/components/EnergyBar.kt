package com.benyaminrasouli.phoniexprotocol.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.ui.theme.EnergyGreen
import com.benyaminrasouli.phoniexprotocol.ui.theme.EnergyRed
import com.benyaminrasouli.phoniexprotocol.ui.theme.EnergyYellow
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceVariantDark

@Composable
fun EnergyBar(
    energy: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        energy >= 70 -> EnergyGreen
        energy >= 40 -> EnergyYellow
        else -> EnergyRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceVariantDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (energy / 100f).coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}
