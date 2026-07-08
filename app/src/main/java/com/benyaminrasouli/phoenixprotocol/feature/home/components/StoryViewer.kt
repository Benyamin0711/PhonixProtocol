package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val STORY_DURATION_MS = 5000L

@Composable
fun StoryViewer(
    stories: List<StoryItem>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = ((1f - progress) * STORY_DURATION_MS).toInt(),
            easing = LinearEasing
        ),
        label = "storyProgress"
    )

    LaunchedEffect(currentIndex, isPaused) {
        if (!isPaused) {
            progress = 0f
            val stepMs = 50L
            val steps = (STORY_DURATION_MS / stepMs).toInt()
            for (i in 1..steps) {
                delay(stepMs)
                progress = i.toFloat() / steps
            }
            // Auto-advance to next story
            if (currentIndex < stories.lastIndex) {
                currentIndex++
                progress = 0f
            } else {
                onDismiss()
            }
        }
    }

    val story = stories[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 2) {
                            // Left half → previous story
                            if (currentIndex > 0) {
                                currentIndex--
                                progress = 0f
                            }
                        } else {
                            // Right half → next story
                            if (currentIndex < stories.lastIndex) {
                                currentIndex++
                                progress = 0f
                            } else {
                                onDismiss()
                            }
                        }
                    }
                )
            }
    ) {
        // Story content background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = story.gradientColors)
                )
        )

        // Progress bars at top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Back button + progress indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { index, _ ->
                        LinearProgressIndicator(
                            progress = {
                                when {
                                    index < currentIndex -> 1f
                                    index == currentIndex -> animatedProgress
                                    else -> 0f
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        // Center content (placeholder image area)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = story.title,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Bottom text overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(24.dp)
        ) {
            Text(
                text = story.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = story.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}
