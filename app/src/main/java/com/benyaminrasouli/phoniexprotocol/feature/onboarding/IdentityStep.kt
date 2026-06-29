package com.benyaminrasouli.phoniexprotocol.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.core.domain.model.IdentityPath
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

data class IdentityOption(
    val path: IdentityPath,
    val label: String,
    val description: String
)

private val identityOptions = listOf(
    IdentityOption(IdentityPath.WARRIOR, "Warrior", "Strength through battle"),
    IdentityOption(IdentityPath.SCHOLAR, "Scholar", "Knowledge is power"),
    IdentityOption(IdentityPath.BUILDER, "Builder", "Create what matters"),
    IdentityOption(IdentityPath.MONK, "Monk", "Discipline through focus"),
    IdentityOption(IdentityPath.COMMANDER, "Commander", "Lead with authority"),
    IdentityOption(IdentityPath.CREATOR, "Creator", "Express through creation"),
    IdentityOption(IdentityPath.STRATEGIST, "Strategist", "Plan every move"),
    IdentityOption(IdentityPath.SENTINEL, "Sentinel", "Protect what matters")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdentityStep(
    selectedPath: String,
    onPathSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Path",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Who are you becoming?",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            identityOptions.forEach { option ->
                IdentityCard(
                    option = option,
                    isSelected = selectedPath == option.path.name,
                    onClick = { onPathSelected(option.path.name) }
                )
            }
        }
    }
}

@Composable
private fun IdentityCard(
    option: IdentityOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = SurfaceDark,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PhoenixOrange else TextSecondary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) PhoenixOrange else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
