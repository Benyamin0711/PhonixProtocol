package com.benyaminrasouli.phoniexprotocol.feature.boss

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.ActiveBoss
import com.benyaminrasouli.phoniexprotocol.core.ui.components.AnimatedProgressBar
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.CompletedGreen
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BossDetailScreen(
    navController: NavController,
    viewModel: BossViewModel = hiltViewModel()
) {
    val activeBoss by viewModel.activeBoss.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updateProgress()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.boss_mission)) },
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

        if (activeBoss == null) {
            Text(
                text = stringResource(R.string.boss_no_active),
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            activeBoss?.let { boss ->
                BossDetailContent(
                    activeBoss = boss,
                    onComplete = { viewModel.completeBoss(boss.boss.id) }
                )
            }
        }
    }
}

@Composable
private fun BossDetailContent(
    activeBoss: ActiveBoss,
    onComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BossHeader(activeBoss = activeBoss)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.boss_goals),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(activeBoss.goals) { goal ->
            GoalItem(goal = goal)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }

        if (activeBoss.isCompleted) {
            item {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.boss_claim_rewards))
                }
            }
        }
    }
}

@Composable
private fun BossHeader(activeBoss: ActiveBoss) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = "Boss",
                tint = PhoenixOrange,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = activeBoss.boss.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${stringResource(R.string.boss_level, activeBoss.boss.level)} \u2022 ${stringResource(R.string.boss_xp_reward, activeBoss.boss.rewardXp)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        AnimatedProgressBar(
            progress = activeBoss.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        )

        Text(
            text = stringResource(R.string.boss_complete, (activeBoss.progress * 100).toInt()),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun GoalItem(goal: BossGoal) {
    val goalColor by animateColorAsState(
        targetValue = if (goal.isCompleted) CompletedGreen else PhoenixOrange,
        label = "goalColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${goal.current}/${goal.target}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        AnimatedVisibility(
            visible = goal.isCompleted,
            enter = fadeIn() + scaleIn()
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = goalColor,
                modifier = Modifier.size(24.dp)
            )
        }

        if (!goal.isCompleted) {
            AnimatedProgressBar(
                progress = goal.progress,
                modifier = Modifier
                    .width(100.dp)
                    .height(8.dp),
                progressColor = goalColor
            )
        }
    }
}
