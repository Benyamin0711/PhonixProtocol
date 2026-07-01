package com.benyaminrasouli.phoniexprotocol.feature.challenges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@Composable
fun DailyChallengeCard(
    challenges: List<DailyChallenge>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (challenges.isEmpty()) return

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = PhoenixOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Challenges",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${challenges.count { it.completed }}/${challenges.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                challenges.take(3).forEach { challenge ->
                    ChallengeItem(challenge = challenge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (challenges.size > 3) {
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodySmall,
                        color = PhoenixOrange,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeItem(challenge: DailyChallenge) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${challenge.current}/${challenge.target}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (challenge.completed) {
            Text(
                text = if (challenge.claimed) "Claimed" else "Claim",
                style = MaterialTheme.typography.labelSmall,
                color = PhoenixOrange
            )
        } else {
            LinearProgressIndicator(
                progress = { challenge.current.toFloat() / challenge.target },
                modifier = Modifier
                    .width(80.dp)
                    .height(6.dp),
                color = PhoenixOrange,
                trackColor = PhoenixOrange.copy(alpha = 0.2f)
            )
        }
    }
}
