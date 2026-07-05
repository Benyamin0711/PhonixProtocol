package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.AcademicSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.CampaignGrid
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.DailyNoteSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.DayNavigation
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.DopamineControl
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.HeroSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.MissionsSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.SpiritSection
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.StatsGrid
import com.benyaminrasouli.phoenixprotocol.feature.drawer.DrawerScreen
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                    },
                    onNavigateToAnalytics = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Analytics.route)
                    },
                    onNavigateToHabits = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Habits.route)
                    },
                    onNavigateToMoodTracker = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MoodTracker.route)
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            HeroSection()

            DayNavigation(
                currentDay = state.currentDay,
                onPrevDay = { viewModel.prevDay() },
                onNextDay = { viewModel.nextDay() }
            )

            StatsGrid(
                rank = state.rank,
                level = state.level,
                totalXp = state.totalXp,
                discipline = state.discipline,
                xpProgress = state.xpProgress
            )

            MissionsSection(
                missions = state.missions,
                onToggle = { viewModel.toggleMission(it) }
            )

            SpiritSection(
                prayers = state.prayers,
                onToggle = { viewModel.togglePrayer(it) }
            )

            DailyNoteSection(
                note = state.note,
                onNoteChange = { viewModel.saveNote(it) }
            )

            AcademicSection(subjects = state.subjects)

            CampaignGrid(
                currentDay = state.currentDay,
                allDays = state.allDays,
                onDayClick = { viewModel.goToDay(it) }
            )

            DopamineControl(
                isAsh = state.isAsh,
                onRelapse = { viewModel.recordRelapse() },
                onRecover = { viewModel.recover() },
                onReset = { viewModel.resetDay() }
            )
        }
    }
}
