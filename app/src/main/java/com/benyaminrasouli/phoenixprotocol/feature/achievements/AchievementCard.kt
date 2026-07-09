package com.benyaminrasouli.phoenixprotocol.feature.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.AchievementWithStatus
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun AchievementCard(
    achievement: AchievementWithStatus,
    modifier: Modifier = Modifier
) {
    val alpha = if (achievement.isUnlocked) 1f else 0.4f

    Card(
        modifier = modifier.alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = achievement.achievement.title,
                tint = if (achievement.isUnlocked) PhoenixGold else TextSecondary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.achievement.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (achievement.isUnlocked) PhoenixOrange else TextSecondary
            )

            Text(
                text = achievement.achievement.rarity,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
