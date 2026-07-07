package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.CampaignItem
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.CompletedGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.EnergyYellow
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextPrimary

@Composable
fun SpiritSection(
    prayers: List<CampaignItem>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BackgroundDark)
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_spirit),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PhoenixOrange
        )

        Spacer(modifier = Modifier.height(18.dp))

        prayers.forEachIndexed { index, prayer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prayer.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (prayer.isCompleted) CompletedGreen else TextPrimary
                    )
                    Text(
                        text = "+${prayer.xp} XP",
                        fontSize = 12.sp,
                        color = EnergyYellow
                    )
                }

                Button(
                    onClick = { onToggle(index) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (prayer.isCompleted) CompletedGreen else SurfaceDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (prayer.isCompleted) stringResource(R.string.dashboard_undo) else stringResource(R.string.dashboard_start),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (index < prayers.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
