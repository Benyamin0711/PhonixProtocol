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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.core.domain.model.IdentityPath
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

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
            text = stringResource(R.string.onboarding_choose_path),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_who_are_you),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IdentityPath.entries.forEach { path ->
                IdentityCard(
                    path = path,
                    isSelected = selectedPath == path.name,
                    onClick = { onPathSelected(path.name) }
                )
            }
        }
    }
}

@Composable
private fun getIdentityName(path: IdentityPath): String {
    return when (path) {
        IdentityPath.WARRIOR -> stringResource(R.string.identity_warrior)
        IdentityPath.SCHOLAR -> stringResource(R.string.identity_scholar)
        IdentityPath.BUILDER -> stringResource(R.string.identity_builder)
        IdentityPath.MONK -> stringResource(R.string.identity_monk)
        IdentityPath.COMMANDER -> stringResource(R.string.identity_commander)
        IdentityPath.CREATOR -> stringResource(R.string.identity_creator)
        IdentityPath.STRATEGIST -> stringResource(R.string.identity_strategist)
        IdentityPath.SENTINEL -> stringResource(R.string.identity_sentinel)
    }
}

@Composable
private fun getIdentityDescription(path: IdentityPath): String {
    return when (path) {
        IdentityPath.WARRIOR -> stringResource(R.string.identity_warrior_desc)
        IdentityPath.SCHOLAR -> stringResource(R.string.identity_scholar_desc)
        IdentityPath.BUILDER -> stringResource(R.string.identity_builder_desc)
        IdentityPath.MONK -> stringResource(R.string.identity_monk_desc)
        IdentityPath.COMMANDER -> stringResource(R.string.identity_commander_desc)
        IdentityPath.CREATOR -> stringResource(R.string.identity_creator_desc)
        IdentityPath.STRATEGIST -> stringResource(R.string.identity_strategist_desc)
        IdentityPath.SENTINEL -> stringResource(R.string.identity_sentinel_desc)
    }
}

@Composable
private fun IdentityCard(
    path: IdentityPath,
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
                text = getIdentityName(path),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) PhoenixOrange else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = getIdentityDescription(path),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
