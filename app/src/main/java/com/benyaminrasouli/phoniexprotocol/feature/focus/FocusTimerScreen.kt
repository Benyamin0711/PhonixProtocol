package com.benyaminrasouli.phoniexprotocol.feature.focus

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.EnergyGreen
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixRed
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceVariantDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

private val PomodoroDurations = listOf(25, 45)
private val CustomDurations = listOf(15, 25, 30, 45, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    navController: NavController,
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.focus_timer_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.focus_timer_back))
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ModeSelector(
                selectedMode = state.mode,
                onModeSelected = { viewModel.setMode(it) }
            )

            if (state.mode == TimerMode.CUSTOM) {
                DurationSelector(
                    selectedDuration = state.selectedDuration,
                    onDurationSelected = { viewModel.setDuration(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TimerDisplay(
                remainingSeconds = state.remainingSeconds,
                progress = state.progress
            )

            TimerControls(
                isRunning = state.isRunning,
                onStart = { viewModel.start() },
                onPause = { viewModel.pause() },
                onStop = { viewModel.stop() }
            )

            Spacer(modifier = Modifier.weight(1f))

            StatsSection(
                totalFocusSeconds = state.totalFocusSeconds,
                completedSessions = state.completedSessions
            )
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: TimerMode,
    onModeSelected: (TimerMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TimerMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            FilterChip(
                selected = isSelected,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        text = when (mode) {
                            TimerMode.POMODORO -> stringResource(R.string.focus_timer_pomodoro)
                            TimerMode.CUSTOM -> stringResource(R.string.focus_timer_custom)
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                    selectedLabelColor = PhoenixOrange
                )
            )
        }
    }
}

@Composable
private fun DurationSelector(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CustomDurations.forEach { minutes ->
            val isSelected = minutes == selectedDuration
            FilterChip(
                selected = isSelected,
                onClick = { onDurationSelected(minutes) },
                label = { Text("${minutes}m") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                    selectedLabelColor = PhoenixOrange
                )
            )
        }
    }
}

@Composable
private fun TimerDisplay(
    remainingSeconds: Int,
    progress: Float
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    val trackColor = SurfaceVariantDark
    val progressColor by animateColorAsState(
        targetValue = when {
            progress >= 1f -> EnergyGreen
            progress >= 0.75f -> PhoenixGold
            else -> PhoenixOrange
        },
        label = "timerColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = timeText,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TimerControls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onStop,
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceVariantDark, CircleShape)
        ) {
            Icon(
                Icons.Default.Stop,
                contentDescription = stringResource(R.string.focus_timer_stop),
                tint = PhoenixRed,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = { if (isRunning) onPause() else onStart() },
            modifier = Modifier
                .size(72.dp)
                .background(PhoenixOrange, CircleShape)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) stringResource(R.string.focus_timer_pause) else stringResource(R.string.focus_timer_start),
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceVariantDark, CircleShape)
        )
    }
}

@Composable
private fun StatsSection(
    totalFocusSeconds: Int,
    completedSessions: Int
) {
    val hours = totalFocusSeconds / 3600
    val minutes = (totalFocusSeconds % 3600) / 60
    val focusTimeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = stringResource(R.string.focus_timer_total_focus),
                value = focusTimeText
            )
            StatItem(
                label = stringResource(R.string.focus_timer_completed),
                value = completedSessions.toString()
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PhoenixGold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
