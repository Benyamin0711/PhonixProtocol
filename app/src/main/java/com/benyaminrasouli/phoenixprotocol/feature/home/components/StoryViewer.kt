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

private data class SlideLocation(val storyIndex: Int, val slideIndex: Int)

private fun locateSlide(
    flatIndex: Int,
    stories: List<StoryItem>
): SlideLocation {
    var remaining = flatIndex
    for (i in stories.indices) {
        val slidesInStory = stories[i].slides.size
        if (remaining < slidesInStory) {
            return SlideLocation(storyIndex = i, slideIndex = remaining)
        }
        remaining -= slidesInStory
    }
    // Fallback to last slide
    val lastStory = stories.lastIndex
    return SlideLocation(lastStory, stories[lastStory].slides.lastIndex)
}

private fun getFlatIndex(location: SlideLocation, stories: List<StoryItem>): Int {
    var index = 0
    for (i in 0 until location.storyIndex) {
        index += stories[i].slides.size
    }
    return index + location.slideIndex
}

private fun totalSlides(stories: List<StoryItem>): Int =
    stories.sumOf { it.slides.size }

@Composable
fun StoryViewer(
    stories: List<StoryItem>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val total = totalSlides(stories)
    var currentFlatIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, total - 1)) }
    var isPaused by remember { mutableStateOf(false) }
    val progressAnimatable = remember { Animatable(0f) }

    val location = locateSlide(currentFlatIndex, stories)
    val currentStory = stories[location.storyIndex]
    val currentSlide = currentStory.slides[location.slideIndex]

    // Auto-advance timer
    LaunchedEffect(currentFlatIndex, isPaused) {
        if (!isPaused) {
            progressAnimatable.snapTo(0f)
            progressAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = STORY_DURATION_MS,
                    easing = LinearEasing
                )
            )
            // Auto-advance to next slide
            if (currentFlatIndex < total - 1) {
                currentFlatIndex++
            } else {
                onDismiss()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Swipe down to dismiss
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
            // Tap to navigate, hold to pause
            .pointerInput(currentFlatIndex) {
                detectTapGestures(
                    onPress = { _ ->
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 3) {
                            // Left third -> previous slide
                            if (currentFlatIndex > 0) {
                                currentFlatIndex--
                            }
                        } else if (offset.x > screenWidth * 2 / 3) {
                            // Right third -> next slide
                            if (currentFlatIndex < total - 1) {
                                currentFlatIndex++
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
            // Progress bars - one per slide across all stories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                var flatIndex = 0
                stories.forEach { story ->
                    story.slides.forEach { slide ->
                        val thisFlatIndex = flatIndex
                        StoryProgressBar(
                            progress = when {
                                thisFlatIndex < currentFlatIndex -> 1f
                                thisFlatIndex == currentFlatIndex -> progressAnimatable.value
                                else -> 0f
                            },
                            modifier = Modifier.weight(1f)
                        )
                        flatIndex++
                    }
                }
            }

            // Username + avatar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder
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

        // Center content (image area - gradient placeholder)
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
