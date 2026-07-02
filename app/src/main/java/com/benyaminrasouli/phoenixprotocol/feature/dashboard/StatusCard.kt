package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.core.ui.components.EnergyBar
import com.benyaminrasouli.phoenixprotocol.core.ui.components.PhoenixCard
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun StatusCard(
    level: Int,
    xp: Int,
    rank: String,
    phoenixEnergy: Int,
    modifier: Modifier = Modifier
) {
    PhoenixCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = rank,
                    style = MaterialTheme.typography.titleLarge,
                    color = PhoenixGold
                )
                Text(
                    text = "Level $level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$xp XP",
                    style = MaterialTheme.typography.titleMedium,
                    color = PhoenixOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Phoenix Energy",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "$phoenixEnergy/100",
                style = MaterialTheme.typography.labelLarge,
                color = PhoenixOrange
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        EnergyBar(energy = phoenixEnergy)
    }
}
