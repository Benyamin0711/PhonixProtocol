package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.CompletedGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextPrimary
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun CampaignGrid(
    currentDay: Int,
    dayActivityMap: Map<Int, Boolean>,
    onDayClick: (Int) -> Unit,
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
            text = stringResource(R.string.dashboard_campaign),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PhoenixOrange
        )

        Spacer(modifier = Modifier.height(18.dp))

        val rows = (0 until 12).toList() // 60 days / 5 columns = 12 rows
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEach { rowIndex ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(5) { colIndex ->
                        val dayNumber = rowIndex * 5 + colIndex + 1
                        if (dayNumber <= 60) {
                            val hasActivity = dayActivityMap[dayNumber] ?: false
                            val isActive = dayNumber == currentDay

                            DayCell(
                                dayNumber = dayNumber,
                                hasActivity = hasActivity,
                                isActive = isActive,
                                onClick = { onDayClick(dayNumber) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayNumber: Int,
    hasActivity: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isActive -> Color(0xFF3D1A00)
                    hasActivity -> Color(0xFF0A1A10)
                    else -> SurfaceDark
                }
            )
            .then(
                if (isActive) Modifier.border(2.dp, PhoenixOrange, RoundedCornerShape(12.dp))
                else Modifier.border(1.dp, TextPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.dashboard_day, dayNumber),
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                color = when {
                    isActive -> TextPrimary
                    hasActivity -> CompletedGreen
                    else -> TextSecondary
                },
                textAlign = TextAlign.Center
            )

            if (hasActivity) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CompletedGreen)
                )
            }
        }
    }
}
