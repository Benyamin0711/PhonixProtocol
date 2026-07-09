    package com.benyaminrasouli.phoenixprotocol.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.home.components.FeatureGrid
import com.benyaminrasouli.phoenixprotocol.feature.home.components.StoryItem
import com.benyaminrasouli.phoenixprotocol.feature.home.components.StorySection
import com.benyaminrasouli.phoenixprotocol.feature.home.components.StorySlide
import com.benyaminrasouli.phoenixprotocol.feature.home.components.StoryViewer
import com.benyaminrasouli.phoenixprotocol.feature.home.components.SupportFooter
import com.benyaminrasouli.phoenixprotocol.feature.home.components.UserStatusCard
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

private val defaultStories = listOf(
    StoryItem(
        slides = listOf(
            StorySlide(
                title = "Pomodoro Technique",
                description = "Work 25 minutes, break 5 minutes",
                gradientColors = listOf(PhoenixOrange, Color(0xFFFF6B35))
            ),
            StorySlide(
                title = "Stay Focused",
                description = "Eliminate distractions during work sessions",
                gradientColors = listOf(PhoenixOrange, Color(0xFFE85D2C))
            ),
            StorySlide(
                title = "Track Progress",
                description = "Monitor your daily productivity",
                gradientColors = listOf(PhoenixOrange, Color(0xFFFF8C5A))
            )
        )
    ),
    StoryItem(
        slides = listOf(
            StorySlide(
                title = "Guided Meditation",
                description = "Find peace with guided meditation sessions",
                gradientColors = listOf(Color(0xFF6B73FF), Color(0xFF000DFE))
            ),
            StorySlide(
                title = "Mindfulness",
                description = "Stay present and aware of each moment",
                gradientColors = listOf(Color(0xFF5A63E8), Color(0xFF3D3BF5))
            ),
            StorySlide(
                title = "Daily Practice",
                description = "Build a consistent meditation habit",
                gradientColors = listOf(Color(0xFF7B83FF), Color(0xFF1A1DFF))
            )
        )
    ),
    StoryItem(
        slides = listOf(
            StorySlide(
                title = "Deep Breathing",
                description = "Calm your mind with breathing techniques",
                gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            ),
            StorySlide(
                title = "Box Breathing",
                description = "4-4-4-4 pattern for stress relief",
                gradientColors = listOf(Color(0xFF0D8C7E), Color(0xFF2BD86E))
            ),
            StorySlide(
                title = "Recovery Breath",
                description = "Restore energy between tasks",
                gradientColors = listOf(Color(0xFF15A697), Color(0xFF45F88E))
            )
        )
    ),
    StoryItem(
        slides = listOf(
            StorySlide(
                title = "Binaural Beats",
                description = "Train your ears with audio frequencies",
                gradientColors = listOf(Color(0xFFEE0979), Color(0xFFFF6A00))
            ),
            StorySlide(
                title = "Alpha Waves",
                description = "Relaxation and light focus state",
                gradientColors = listOf(Color(0xFFD90869), Color(0xFFE85A00))
            ),
            StorySlide(
                title = "Theta Waves",
                description = "Deep meditation and creativity",
                gradientColors = listOf(Color(0xFFF01989), Color(0xFFFF7A10))
            )
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    onStoryVisibilityChanged: (Boolean) -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle(initialValue = null)
    val stats by viewModel.userStats.collectAsStateWithLifecycle(initialValue = null)

    var showStoryViewer by remember { mutableStateOf(false) }
    var selectedStoryIndex by remember { mutableIntStateOf(0) }
    var selectedSlideIndex by remember { mutableIntStateOf(0) }

    BackHandler(enabled = showStoryViewer) {
        showStoryViewer = false
        onStoryVisibilityChanged(false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PHOENIX",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Notifications.route)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                StorySection(
                    stories = defaultStories,
                    onStoryClick = { index ->
                        selectedStoryIndex = index
                        selectedSlideIndex = 0
                        showStoryViewer = true
                        onStoryVisibilityChanged(true)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                UserStatusCard(
                    username = profile?.fullName ?: "Phoenix User",
                    level = stats?.level ?: 1,
                    rank = stats?.rank?.let { "#$it" } ?: "#1",
                    currentXp = stats?.xp ?: 0,
                    maxXp = 1000
                )

                Spacer(modifier = Modifier.height(16.dp))
                FeatureGrid(
                    onNavigate = { route -> navController.navigate(route) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                SupportFooter(
                    onNavigateToSupport = { navController.navigate(Screen.Support.route) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showStoryViewer) {
            val storyRoutes = listOf(
                Screen.FocusTimer.route,
                Screen.FocusTimer.route,
                Screen.FocusTimer.route,
                Screen.FocusTimer.route
            )
            StoryViewer(
                stories = defaultStories,
                storyIndex = selectedStoryIndex,
                initialSlideIndex = selectedSlideIndex,
                onDismiss = {
                    showStoryViewer = false
                    onStoryVisibilityChanged(false)
                },
                onOpen = {
                    showStoryViewer = false
                    onStoryVisibilityChanged(false)
                    navController.navigate(storyRoutes[selectedStoryIndex])
                }
            )
        }
    }
}
