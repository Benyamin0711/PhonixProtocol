package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val route: String?,
    val isAvailable: Boolean = true
)

@Composable
fun FeatureGrid(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val features = listOf(
        FeatureItem("Pomodoro", Icons.Filled.PlayArrow, "focus_timer"),
        FeatureItem("Meditation", Icons.Filled.Star, null, false),
        FeatureItem("Breathing", Icons.Filled.Favorite, null, false),
        FeatureItem("Frequency", Icons.Filled.Star, null, false),
        FeatureItem("Tasks", Icons.AutoMirrored.Filled.List, "task_list"),
        FeatureItem("Boss", Icons.Filled.Star, "boss_detail"),
        FeatureItem("Challenges", Icons.Filled.Star, "daily_challenges"),
        FeatureItem("Templates", Icons.Filled.Star, "templates"),
        FeatureItem("Shadow", Icons.Filled.Star, "shadow")
    )

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "App Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        for (rowIndex in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (colIndex in 0..2) {
                    val index = rowIndex * 3 + colIndex
                    if (index < features.size) {
                        FeatureGridItem(
                            feature = features[index],
                            onClick = {
                                if (features[index].isAvailable) {
                                    features[index].route?.let { onNavigate(it) }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (rowIndex < 2) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FeatureGridItem(
    feature: FeatureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(enabled = feature.isAvailable, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (feature.isAvailable)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = if (feature.isAvailable) PhoenixOrange else TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = if (feature.isAvailable) TextSecondary else TextSecondary.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
                if (!feature.isAvailable) {
                    Text(
                        text = "Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = PhoenixOrange.copy(alpha = 0.7f),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
