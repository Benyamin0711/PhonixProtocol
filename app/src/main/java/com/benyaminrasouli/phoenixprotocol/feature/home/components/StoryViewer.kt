package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private const val STORY_DURATION_MS = 5000

@Composable
fun StoryViewer(
    stories: List<StoryItem>,
    storyIndex: Int,
    initialSlideIndex: Int,
    onDismiss: () -> Unit,
    onOpen: () -> Unit = {}
) {
    val story = stories[storyIndex.coerceIn(0, stories.lastIndex)]
    val totalSlidesInStory = story.slides.size
    var currentSlideIndex by remember {
        mutableIntStateOf(initialSlideIndex.coerceIn(0, totalSlidesInStory - 1))
    }
    var isPaused by remember { mutableStateOf(false) }
    val progressAnimatable = remember { Animatable(0f) }

    // Sync index when story changes
    LaunchedEffect(storyIndex, initialSlideIndex) {
        currentSlideIndex = initialSlideIndex.coerceIn(0, totalSlidesInStory - 1)
    }

    val currentSlide = story.slides[currentSlideIndex]

    // Auto-advance timer
    LaunchedEffect(currentSlideIndex, isPaused) {
        if (!isPaused) {
            progressAnimatable.snapTo(0f)
            progressAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = STORY_DURATION_MS,
                    easing = LinearEasing
                )
            )
            if (currentSlideIndex < totalSlidesInStory - 1) {
                currentSlideIndex++
            } else {
                onDismiss()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {},
                    onDragCancel = {},
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 40f) {
                            onDismiss()
                        }
                    }
                )
            }
            .pointerInput(currentSlideIndex) {
                detectTapGestures(
                    onPress = { _ ->
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 3) {
                            if (currentSlideIndex > 0) {
                                currentSlideIndex--
                            }
                        } else if (offset.x > screenWidth * 2 / 3) {
                            if (currentSlideIndex < totalSlidesInStory - 1) {
                                currentSlideIndex++
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
                    Brush.verticalGradient(colors = currentSlide.gradientColors)
                )
        )

        // Top section: Progress bars + Username
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Progress bars - one per slide in this story only
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(totalSlidesInStory) { index ->
                    StoryProgressBar(
                        progress = when {
                            index < currentSlideIndex -> 1f
                            index == currentSlideIndex -> progressAnimatable.value
                            else -> 0f
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Username + avatar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentSlide.title.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSlide.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Center content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentSlide.title,
                color = Color.White.copy(alpha = 0.15f),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Bottom text overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = currentSlide.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentSlide.description,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
            if (currentSlideIndex == totalSlidesInStory - 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Open",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White)
        )
    }
}
