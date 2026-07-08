package com.benyaminrasouli.phoenixprotocol.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.benyaminrasouli.phoenixprotocol.feature.home.components.StoryViewer
import com.benyaminrasouli.phoenixprotocol.feature.home.components.SupportFooter
import com.benyaminrasouli.phoenixprotocol.feature.home.components.UserStatusCard
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

private val defaultStories = listOf(
    StoryItem(
        title = "Pomodoro Timer",
        description = "Boost your focus with timed work sessions",
        gradientColors = listOf(PhoenixOrange, Color(0xFFFF6B35))
    ),
    StoryItem(
        title = "Meditation",
        description = "Find peace with guided meditation",
        gradientColors = listOf(Color(0xFF6B73FF), Color(0xFF000DFE))
    ),
    StoryItem(
        title = "Breathing Exercises",
        description = "Calm your mind with breathing techniques",
        gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
    ),
    StoryItem(
        title = "Frequency Listening",
        description = "Train your ears with audio frequencies",
        gradientColors = listOf(Color(0xFFEE0979), Color(0xFFFF6A00))
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle(initialValue = null)
    val stats by viewModel.userStats.collectAsStateWithLifecycle(initialValue = null)

    var showStoryViewer by remember { mutableStateOf(false) }
    var selectedStoryIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "PHOENIX",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                    IconButton(onClick = { /* TODO: notifications */ }) {
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

            StorySection(
                stories = defaultStories,
                onStoryClick = { index ->
                    selectedStoryIndex = index
                    showStoryViewer = true
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

        if (showStoryViewer) {
            StoryViewer(
                stories = defaultStories,
                initialIndex = selectedStoryIndex,
                onDismiss = { showStoryViewer = false }
            )
        }
    }
}
