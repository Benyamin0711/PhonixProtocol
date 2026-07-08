package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

data class StoryItem(
    val title: String,
    val description: String,
    val gradientColors: List<Color>,
    val onLearnMore: () -> Unit
)

@Composable
fun StorySection(
    onNavigateToFeature: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stories = listOf(
        StoryItem(
            title = "Pomodoro Timer",
            description = "Boost your focus with timed work sessions",
            gradientColors = listOf(PhoenixOrange, Color(0xFFFF6B35)),
            onLearnMore = { onNavigateToFeature("focus_timer") }
        ),
        StoryItem(
            title = "Meditation",
            description = "Find peace with guided meditation",
            gradientColors = listOf(Color(0xFF6B73FF), Color(0xFF000DFE)),
            onLearnMore = { onNavigateToFeature("meditation") }
        ),
        StoryItem(
            title = "Breathing Exercises",
            description = "Calm your mind with breathing techniques",
            gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
            onLearnMore = { onNavigateToFeature("breathing") }
        ),
        StoryItem(
            title = "Frequency Listening",
            description = "Train your ears with audio frequencies",
            gradientColors = listOf(Color(0xFFEE0979), Color(0xFFFF6A00)),
            onLearnMore = { onNavigateToFeature("frequency") }
        )
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stories.forEach { story ->
                StoryCard(story = story)
            }
        }
    }
}

@Composable
private fun StoryCard(story: StoryItem) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(240.dp)
            .clickable(onClick = story.onLearnMore),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = story.gradientColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = story.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = story.onLearnMore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Learn More",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
