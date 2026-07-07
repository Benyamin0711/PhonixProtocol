package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.CompletedGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceVariantDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextPrimary
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun StatsGrid(
    rank: String,
    level: Int,
    totalXp: Int,
    discipline: Int,
    xpProgress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = stringResource(R.string.dashboard_rank),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = rank,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PhoenixOrange
                )
            }

            StatCard(
                title = stringResource(R.string.dashboard_level_label),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$level",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = PhoenixOrange,
                    trackColor = SurfaceVariantDark
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = stringResource(R.string.dashboard_total_xp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$totalXp",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            StatCard(
                title = stringResource(R.string.dashboard_discipline),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$discipline%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = CompletedGreen
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
