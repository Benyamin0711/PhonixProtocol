package com.benyaminrasouli.phoenixprotocol.feature.focus

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.util.toFocusTimeText
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.EnergyGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixRed
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceVariantDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary
import kotlin.math.sqrt

private val CustomDurations = listOf(5, 15, 25, 30, 45, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    navController: NavController,
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Sensor setup for strict mode
    LaunchedEffect(state.strictMode, state.isRunning, state.isFullScreen) {
        if (!state.strictMode || !state.isRunning) return@LaunchedEffect
    }

    // Sensor listener for phone pickup detection
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val accelerometer = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    DisposableEffect(sensorManager, accelerometer, state.strictMode, state.isRunning) {
        if (!state.strictMode || !state.isRunning || state.isFullScreen) {
            onDispose {}
        } else {
            val gravity = FloatArray(3) { 0f }
            val alpha = 0.8f
            val movementThreshold = 2.5f

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    if (!state.strictMode || !state.isRunning || state.isWarningActive) return

                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                    val lx = event.values[0] - gravity[0]
                    val ly = event.values[1] - gravity[1]
                    val lz = event.values[2] - gravity[2]

                    val linearAccel = sqrt(lx * lx + ly * ly + lz * lz)

                    if (linearAccel > movementThreshold) {
                        viewModel.onPhonePickupDetected()
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        }
    }

    // Brightness management for fullscreen
    LaunchedEffect(state.isFullScreen) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val params = activity.window.attributes
        if (state.isFullScreen) {
            params.screenBrightness = 0.15f
            activity.window.attributes = params
        } else {
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.window.attributes = params
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isWarningActive) {
            WarningScreen(
                countdown = state.warningCountdown,
                onPhonePutDown = { viewModel.dismissWarning() }
            )
        } else if (state.isFullScreen) {
            FullScreenTimer(
                timerValue = state.remainingSeconds,
                isRunning = state.isRunning,
                onExitFullScreen = { viewModel.toggleFullScreen() },
                onStart = { viewModel.start() },
                onPause = { viewModel.pause() }
            )
        } else {
            NormalTimerContent(
                state = state,
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTimerContent(
    state: FocusTimerState,
    viewModel: FocusTimerViewModel,
    navController: NavController
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            Spacer(modifier = Modifier.height(4.dp))

            TimerDisplay(
                remainingSeconds = state.remainingSeconds,
                progress = state.progress
            )

            TimerControls(
                isRunning = state.isRunning,
                onStart = { viewModel.start() },
                onPause = { viewModel.pause() },
                onStop = { viewModel.stop() },
                onToggleFullScreen = { viewModel.toggleFullScreen() },
                isFullScreen = state.isFullScreen
            )

            // Strict Mode Toggle
            StrictModeToggle(
                strictMode = state.strictMode,
                onToggle = { viewModel.toggleStrictMode() }
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
private fun StrictModeToggle(
    strictMode: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Halat Sakht Gir",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Phone pickup triggers warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = strictMode,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PhoenixOrange,
                    checkedTrackColor = PhoenixOrange.copy(alpha = 0.3f)
                )
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
                            TimerMode.POMODORO -> "Pomodoro"
                            TimerMode.CUSTOM -> "Custom"
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
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
    onStop: () -> Unit,
    onToggleFullScreen: () -> Unit,
    isFullScreen: Boolean
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
                Icons.Default.Close,
                contentDescription = "Stop",
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
                imageVector = Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Start",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = onToggleFullScreen,
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceVariantDark, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = if (isFullScreen) "Exit Fullscreen" else "Fullscreen",
                tint = PhoenixGold,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun StatsSection(
    totalFocusSeconds: Int,
    completedSessions: Int
) {
    val focusTimeText = totalFocusSeconds.toFocusTimeText()

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
                label = "Total Focus",
                value = focusTimeText
            )
            StatItem(
                label = "Completed",
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

// --- Full Screen Timer ---

@Composable
private fun FullScreenTimer(
    timerValue: Int,
    isRunning: Boolean,
    onExitFullScreen: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.98f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(320, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, animationSpec = tween(320, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alphaAnim.value * 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scaleAnim.value)
        ) {
            FullScreenTimerCircle(timerValue = timerValue, size = 340.dp)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (isRunning) onPause() else onStart() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) PhoenixRed else PhoenixOrange
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause" else "Start",
                        color = Color.White
                    )
                }

                Button(
                    onClick = onExitFullScreen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exit",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenTimerCircle(timerValue: Int, size: androidx.compose.ui.unit.Dp = 300.dp) {
    val minutes = timerValue / 60
    val seconds = timerValue % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val total = 25 * 60f
    val targetProgress = 1f - (timerValue.toFloat() / total)
    val progressAnim = remember { Animatable(targetProgress) }

    LaunchedEffect(targetProgress) {
        progressAnim.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    val textPulse = remember { Animatable(1f) }
    LaunchedEffect(timerValue) {
        textPulse.snapTo(1f)
        textPulse.animateTo(1.06f, animationSpec = tween(110))
        textPulse.animateTo(1f, animationSpec = tween(220))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val outerRadius = size.toPx() / 2

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF06203A), Color(0xFF06203A).copy(alpha = 0.85f))
                ),
                radius = outerRadius,
                center = center
            )

            val innerRadius = outerRadius - 12f
            for (i in 0 until 60) {
                val angle = Math.toRadians(i * 6.0 - 90.0).toFloat()
                val sx = center.x + outerRadius * kotlin.math.cos(angle)
                val sy = center.y + outerRadius * kotlin.math.sin(angle)
                val ex = center.x + innerRadius * kotlin.math.cos(angle)
                val ey = center.y + innerRadius * kotlin.math.sin(angle)
                drawLine(
                    color = if (i % 5 == 0) Color(0xFFfca311) else Color(0x55ffffff),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = if (i % 5 == 0) 3f else 1f
                )
            }

            drawArc(
                brush = Brush.sweepGradient(listOf(Color(0xFFfca311), Color(0xFFef476f))),
                startAngle = -90f,
                sweepAngle = 360f * progressAnim.value,
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round),
                topLeft = Offset(0f, 0f),
                size = Size(size.toPx(), size.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.scale(textPulse.value)
            )
            Text(
                text = "Focus Session",
                color = Color(0xFFbcd9ff),
                fontSize = 16.sp
            )
        }
    }
}

// --- Warning Screen ---

@Composable
private fun WarningScreen(countdown: Int, onPhonePutDown: () -> Unit) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.94f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(260, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, animationSpec = tween(360, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB0000010))
            .alpha(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(PhoenixRed, Color(0xFF9b0000))))
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Warning!",
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Put your phone down on a table or pocket to continue the timer.",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = countdown.toString(),
                fontSize = 72.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "If not followed, timer will restart from the beginning",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onPhonePutDown,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "I put the phone down",
                    color = PhoenixRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
