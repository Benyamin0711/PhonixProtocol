package com.benyaminrasouli.phoniexprotocol.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp)
        ) {
            // Greeting
            state.profile?.let { profile ->
                Text(
                    text = "Welcome, ${profile.fullName}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.rank.displayName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = PhoenixOrange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Slogan
            SloganBar(slogan = state.slogan)

            Spacer(modifier = Modifier.height(16.dp))

            // Status Card
            StatusCard(
                level = state.stats?.level ?: 1,
                xp = state.stats?.xp ?: 0,
                rank = state.rank.displayName,
                phoenixEnergy = state.stats?.phoenixEnergy ?: 50,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Priority Tasks
            TaskListSection(
                title = "TODAY'S PRIORITY",
                tasks = state.activeTasks,
                onTaskComplete = viewModel::completeTask
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Shadow Level indicator
            state.stats?.let { stats ->
                if (stats.shadowLevel > 0) {
                    Text(
                        text = "SHADOW LEVEL: ${stats.shadowLevel}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
