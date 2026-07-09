package com.benyaminrasouli.phoenixprotocol.feature.focus

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.EnergyGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixRed
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceVariantDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val CustomDurations = listOf(5, 15, 25, 30, 45, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    navController: NavController,
    onFullScreenChanged: (Boolean) -> Unit = {},
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Notify MainScreen about fullscreen state
    LaunchedEffect(state.isFullScreen) {
        onFullScreenChanged(state.isFullScreen)
    }

    // True fullscreen - hide system UI
    val window = (context as? Activity)?.window
    val view = (context as? Activity)?.window?.decorView

    LaunchedEffect(state.isFullScreen) {
        if (window != null && view != null) {
            if (state.isFullScreen) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, view)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val controller = WindowInsetsControllerCompat(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Restore system UI when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            onFullScreenChanged(false)
            if (window != null && view != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val controller = WindowInsetsControllerCompat(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Sensor for strict mode
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    val accelerometer = remember {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    DisposableEffect(sensorManager, accelerometer, state.strictMode, state.isRunning) {
        if (!state.strictMode || !state.isRunning) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isWarningActive) {
            WarningOverlay(
                countdown = state.warningCountdown,
                onPhonePutDown = { viewModel.dismissWarning() }
            )
        } else if (state.isFullScreen) {
            PremiumFullScreenTimer(
                state = state,
                onStart = { viewModel.start() },
                onPause = { viewModel.pause() },
                onExitFullScreen = { viewModel.toggleFullScreen() }
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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.focus_timer_back)
                    )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Phase indicator
            PhaseIndicator(
                phase = state.phase,
                sessionsCompleted = state.sessionsCompleted
            )

            ModeSelector(
                selectedMode = state.mode,
                onModeSelected = { viewModel.setMode(it) },
                isRunning = state.isRunning
            )

            if (state.mode == TimerMode.CUSTOM && state.phase == SessionPhase.WORK) {
                DurationSelector(
                    selectedDuration = state.selectedDuration,
                    onDurationSelected = { viewModel.setDuration(it) },
                    isRunning = state.isRunning
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Premium timer display
            PremiumTimerDisplay(
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalDurationSeconds,
                phase = state.phase
            )

            // Controls
            TimerControls(
                isRunning = state.isRunning,
                onStart = { viewModel.start() },
                onPause = { viewModel.pause() },
                onStop = { viewModel.stop() },
                onToggleFullScreen = { viewModel.toggleFullScreen() },
                isFullScreen = state.isFullScreen
            )

            // Strict mode
            StrictModeToggle(
                strictMode = state.strictMode,
                onToggle = { viewModel.toggleStrictMode() },
                isRunning = state.isRunning
            )

            Spacer(modifier = Modifier.weight(1f))

            // Stats
            StatsSection(
                totalFocusSeconds = state.totalFocusSeconds,
                completedSessions = state.completedSessions,
                currentSession = state.sessionsCompleted
            )
        }
    }
}

@Composable
private fun PhaseIndicator(
    phase: SessionPhase,
    sessionsCompleted: Int
) {
    val phaseText = when (phase) {
        SessionPhase.WORK -> "WORK"
        SessionPhase.SHORT_BREAK -> "SHORT BREAK"
        SessionPhase.LONG_BREAK -> "LONG BREAK"
    }

    val phaseColor = when (phase) {
        SessionPhase.WORK -> PhoenixOrange
        SessionPhase.SHORT_BREAK -> EnergyGreen
        SessionPhase.LONG_BREAK -> PhoenixGold
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val isCompleted = index < (sessionsCompleted % 4)
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) PhoenixOrange else SurfaceVariantDark)
            )
            if (index < 3) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = phaseText,
            color = phaseColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ModeSelector(
    selectedMode: TimerMode,
    onModeSelected: (TimerMode) -> Unit,
    isRunning: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TimerMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selectedMode,
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
                ),
                enabled = !isRunning
            )
        }
    }
}

@Composable
private fun DurationSelector(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    isRunning: Boolean
) {
    var customInput by remember { mutableStateOf(selectedDuration.toString()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CustomDurations.forEach { minutes ->
                FilterChip(
                    selected = minutes == selectedDuration,
                    onClick = {
                        if (!isRunning) {
                            onDurationSelected(minutes)
                            customInput = minutes.toString()
                        }
                    },
                    label = { Text("${minutes}m") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                        selectedLabelColor = PhoenixOrange
                    ),
                    enabled = !isRunning
                )
            }
        }

        OutlinedTextField(
            value = customInput,
            onValueChange = { text ->
                if (text.isEmpty() || (text.length <= 3 && text.all { it.isDigit() })) {
                    customInput = text
                    text.toIntOrNull()?.let { minutes ->
                        if (minutes in 1..180 && !isRunning) {
                            onDurationSelected(minutes)
                        }
                    }
                }
            },
            label = { Text("Minutes (1-180)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedIndicatorColor = PhoenixOrange,
                unfocusedIndicatorColor = SurfaceVariantDark,
                cursorColor = PhoenixOrange
            ),
            modifier = Modifier.width(160.dp),
            enabled = !isRunning
        )
    }
}

@Composable
private fun PremiumTimerDisplay(
    remainingSeconds: Int,
    totalSeconds: Int,
    phase: SessionPhase
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    val progress = if (totalSeconds > 0) {
        1f - (remainingSeconds.toFloat() / totalSeconds)
    } else 0f

    val phaseColor = when (phase) {
        SessionPhase.WORK -> PhoenixOrange
        SessionPhase.SHORT_BREAK -> EnergyGreen
        SessionPhase.LONG_BREAK -> PhoenixGold
    }

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Pulse animation for text
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0) {
            pulseAnim.snapTo(1f)
            pulseAnim.animateTo(1.02f, animationSpec = tween(100))
            pulseAnim.animateTo(1f, animationSpec = tween(150))
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val center = Offset(size.width / 2, size.height / 2)
            val radius = diameter / 2

            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = glowAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius + 40.dp.toPx()
                ),
                radius = radius + 40.dp.toPx(),
                center = center
            )

            // Background track
            drawCircle(
                color = SurfaceVariantDark.copy(alpha = 0.5f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        phaseColor,
                        phaseColor.copy(alpha = 0.7f),
                        phaseColor
                    ),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Tick marks
            val innerRadius = radius - strokeWidth / 2 - 8.dp.toPx()
            val outerRadius = radius - strokeWidth / 2 + 4.dp.toPx()
            for (i in 0 until 60) {
                val angle = Math.toRadians((i * 6 - 90).toDouble()).toFloat()
                val isMainTick = i % 5 == 0
                val sx = center.x + outerRadius * cos(angle)
                val sy = center.y + outerRadius * sin(angle)
                val ex = center.x + innerRadius * cos(angle)
                val ey = center.y + innerRadius * sin(angle)
                drawLine(
                    color = if (isMainTick) phaseColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = if (isMainTick) 2.dp.toPx() else 1.dp.toPx()
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.scale(pulseAnim.value)
            )
            Text(
                text = when (phase) {
                    SessionPhase.WORK -> "Focus Time"
                    SessionPhase.SHORT_BREAK -> "Short Break"
                    SessionPhase.LONG_BREAK -> "Long Break"
                },
                color = phaseColor.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stop button
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

        // Play/Pause button - with color change
        val buttonColor by animateColorAsState(
            targetValue = if (isRunning) PhoenixRed else PhoenixOrange,
            label = "buttonColor"
        )

        IconButton(
            onClick = { if (isRunning) onPause() else onStart() },
            modifier = Modifier
                .size(80.dp)
                .background(buttonColor, CircleShape)
        ) {
            Icon(
                imageVector = if (isRunning) PauseIcon else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Start",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        // Fullscreen button
        IconButton(
            onClick = onToggleFullScreen,
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceVariantDark, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Fullscreen",
                tint = PhoenixGold,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun StrictModeToggle(
    strictMode: Boolean,
    onToggle: () -> Unit,
    isRunning: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Strict Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Phone pickup triggers warning & restart",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = strictMode,
                onCheckedChange = { onToggle() },
                enabled = !isRunning,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PhoenixOrange,
                    checkedTrackColor = PhoenixOrange.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun StatsSection(
    totalFocusSeconds: Int,
    completedSessions: Int,
    currentSession: Int
) {
    val hours = totalFocusSeconds / 3600
    val minutes = (totalFocusSeconds % 3600) / 60
    val focusText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

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
            StatItem(label = "Total Focus", value = focusText)
            StatItem(label = "Sessions", value = completedSessions.toString())
            StatItem(label = "Today", value = "$currentSession/4")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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

// --- Premium Fullscreen Timer ---

@Composable
private fun PremiumFullScreenTimer(
    state: FocusTimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onExitFullScreen: () -> Unit
) {
    val phaseColor = when (state.phase) {
        SessionPhase.WORK -> PhoenixOrange
        SessionPhase.SHORT_BREAK -> EnergyGreen
        SessionPhase.LONG_BREAK -> PhoenixGold
    }

    // Entrance animation
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.95f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }

    // Background glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "bgGlow")
    val bgGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Animated background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = bgGlowAlpha),
                        Color.Black
                    ),
                    center = Offset(size.width / 2, size.height * 0.4f),
                    radius = size.width * 0.8f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phase text
            Text(
                text = when (state.phase) {
                    SessionPhase.WORK -> "FOCUS"
                    SessionPhase.SHORT_BREAK -> "BREAK"
                    SessionPhase.LONG_BREAK -> "LONG BREAK"
                },
                color = phaseColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large timer ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(320.dp)
                    .scale(scaleAnim.value)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 24.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = diameter / 2

                    // Outer glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                phaseColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius + 80.dp.toPx()
                        ),
                        radius = radius + 80.dp.toPx(),
                        center = center
                    )

                    // Track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress
                    val progress = if (state.totalDurationSeconds > 0) {
                        1f - (state.remainingSeconds.toFloat() / state.totalDurationSeconds)
                    } else 0f

                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                phaseColor,
                                phaseColor.copy(alpha = 0.6f),
                                phaseColor
                            ),
                            center = center
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Inner ring decoration
                    drawCircle(
                        color = phaseColor.copy(alpha = 0.1f),
                        radius = radius - 40.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%02d:%02d".format(state.displayMinutes, state.displaySeconds),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = when (state.phase) {
                            SessionPhase.WORK -> "Focus Time"
                            SessionPhase.SHORT_BREAK -> "Short Break"
                            SessionPhase.LONG_BREAK -> "Long Break"
                        },
                        color = phaseColor.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause button with color animation
                val buttonColor by animateColorAsState(
                    targetValue = if (state.isRunning) PhoenixRed else phaseColor,
                    label = "fsButtonColor"
                )

                Button(
                    onClick = { if (state.isRunning) onPause() else onStart() },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = if (state.isRunning) PauseIcon else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Exit fullscreen
                Button(
                    onClick = onExitFullScreen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXIT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Session info
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isCompleted = index < (state.sessionsCompleted % 4)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) phaseColor else Color.White.copy(alpha = 0.3f))
                    )
                    if (index < 3) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Session ${state.sessionsCompleted + 1}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

// --- Warning Overlay ---

@Composable
private fun WarningOverlay(
    countdown: Int,
    onPhonePutDown: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(300))
    }

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Countdown scale animation
    val countdownScale = remember { Animatable(1f) }
    LaunchedEffect(countdown) {
        countdownScale.snapTo(1.1f)
        countdownScale.animateTo(1f, animationSpec = tween(200))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alphaAnim.value * 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PhoenixRed.copy(alpha = pulseAlpha),
                            PhoenixRed.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning icon
            Text(
                text = "\u26A0\uFE0F",
                fontSize = 48.sp
            )

            Text(
                text = "PUT DOWN YOUR PHONE",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Place your phone on a table or in your pocket to continue the timer",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            // Countdown with animation
            Text(
                text = countdown.toString(),
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.scale(countdownScale.value)
            )

            Text(
                text = "Timer will restart if not followed",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Button(
                onClick = onPhonePutDown,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "PHONE IS DOWN",
                    color = PhoenixRed,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
