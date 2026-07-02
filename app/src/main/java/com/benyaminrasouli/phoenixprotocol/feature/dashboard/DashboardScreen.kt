package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.feature.drawer.DrawerScreen
import com.benyaminrasouli.phoenixprotocol.feature.boss.BossCard
import com.benyaminrasouli.phoenixprotocol.feature.boss.BossViewModel
import com.benyaminrasouli.phoenixprotocol.feature.challenges.DailyChallengeCard
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    bossViewModel: BossViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activeBoss by bossViewModel.activeBoss.collectAsStateWithLifecycle(initialValue = null)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerScreen(
                    onNavigateToSettings = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToAbout = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.About.route)
                    },
                    onNavigateToSupport = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Support.route)
                    },
                    onNavigateToStatistics = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Statistics.route)
                    },
                    onNavigateToAchievements = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Achievements.route)
                    },
                    onNavigateToBossHistory = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.BossHistory.route)
                    },
                    onNavigateToDailyChallenges = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.DailyChallenges.route)
                    },
                    onNavigateToProfile = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToTemplates = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Templates.route)
                    },
                    onNavigateToCategories = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Categories.route)
                    },
                    onNavigateToFocusTimer = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.FocusTimer.route)
                    },
                    onNavigateToLeaderboard = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Leaderboard.route)
                    },
                    onNavigateToShadow = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Shadow.route)
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.menu),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundDark
                    )
                )

                // Greeting
                state.profile?.let { profile ->
                    Text(
                        text = "${stringResource(R.string.dashboard_welcome)} ${profile.fullName}",
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
                    title = stringResource(R.string.dashboard_priority_tasks),
                    tasks = state.activeTasks,
                    onTaskComplete = viewModel::completeTask
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Shadow Level indicator
                state.stats?.let { stats ->
                    if (stats.shadowLevel > 0) {
                        Text(
                            text = "${stringResource(R.string.dashboard_shadow_level)}: ${stats.shadowLevel}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Boss Card
                BossCard(
                    boss = activeBoss,
                    onClick = { navController.navigate(Screen.BossDetail.route) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Challenges Card
                DailyChallengeCard(
                    challenges = state.challenges,
                    onClick = { navController.navigate(Screen.DailyChallenges.route) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateTask.route) },
                containerColor = PhoenixOrange,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.task_create))
            }
        }
    }
}
