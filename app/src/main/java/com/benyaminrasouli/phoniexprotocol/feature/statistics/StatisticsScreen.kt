package com.benyaminrasouli.phoniexprotocol.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.core.ui.components.EnergyBar
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.drawer_statistics)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview card
            StatCard(title = stringResource(R.string.stats_overview)) {
                StatRow(label = stringResource(R.string.stats_total_xp), value = "${state.stats?.xp ?: 0}")
                StatRow(label = stringResource(R.string.stats_level), value = "${state.stats?.level ?: 1}")
                StatRow(label = stringResource(R.string.stats_rank), value = state.stats?.rank ?: "INITIATE")
                StatRow(label = stringResource(R.string.stats_tasks_completed), value = "${state.completedTasks}")
                StatRow(label = stringResource(R.string.stats_completion_rate), value = "${(state.completionRate * 100).toInt()}%")
                StatRow(label = stringResource(R.string.stats_achievements), value = "${state.achievementsUnlocked}/15")
            }

            // Streak card
            StatCard(title = stringResource(R.string.stats_streaks)) {
                StatRow(label = stringResource(R.string.stats_current_streak), value = "${state.stats?.currentStreak ?: 0} days")
                StatRow(label = stringResource(R.string.stats_longest_streak), value = "${state.stats?.longestStreak ?: 0} days")
            }

            // Energy card
            StatCard(title = stringResource(R.string.stats_energy)) {
                Text(
                    text = "${stringResource(R.string.stats_phoenix_energy)}: ${state.stats?.phoenixEnergy ?: 50}/100",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                EnergyBar(energy = state.stats?.phoenixEnergy ?: 50)
                Spacer(modifier = Modifier.height(12.dp))
                StatRow(
                    label = stringResource(R.string.stats_shadow_level),
                    value = "${state.stats?.shadowLevel ?: 0}",
                    valueColor = if ((state.stats?.shadowLevel ?: 0) > 0) MaterialTheme.colorScheme.error else PhoenixOrange
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = PhoenixGold
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary)
        Text(text = value, color = valueColor)
    }
}
