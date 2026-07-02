package com.benyaminrasouli.phoniexprotocol.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.benyaminrasouli.phoniexprotocol.feature.splash.SplashScreen
import com.benyaminrasouli.phoniexprotocol.feature.onboarding.OnboardingScreen
import com.benyaminrasouli.phoniexprotocol.feature.dashboard.DashboardScreen
import com.benyaminrasouli.phoniexprotocol.feature.tasks.TaskListScreen
import com.benyaminrasouli.phoniexprotocol.feature.tasks.CreateTaskScreen
import com.benyaminrasouli.phoniexprotocol.feature.statistics.StatisticsScreen
import com.benyaminrasouli.phoniexprotocol.feature.achievements.AchievementScreen
import com.benyaminrasouli.phoniexprotocol.feature.boss.BossDetailScreen
import com.benyaminrasouli.phoniexprotocol.feature.boss.BossHistoryScreen
import com.benyaminrasouli.phoniexprotocol.feature.settings.SettingsScreen
import com.benyaminrasouli.phoniexprotocol.feature.about.AboutScreen
import com.benyaminrasouli.phoniexprotocol.feature.challenges.DailyChallengeScreen
import com.benyaminrasouli.phoniexprotocol.feature.profile.ProfileScreen
import com.benyaminrasouli.phoniexprotocol.feature.support.SupportScreen
import com.benyaminrasouli.phoniexprotocol.feature.templates.TemplatesListScreen
import com.benyaminrasouli.phoniexprotocol.feature.categories.CategoriesScreen
import com.benyaminrasouli.phoniexprotocol.feature.focus.FocusTimerScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(
            Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SplashScreen(navController = navController)
        }
        composable(
            Screen.Onboarding.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            OnboardingScreen(navController = navController)
        }
        composable(
            Screen.Dashboard.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            DashboardScreen(navController = navController)
        }
        composable(
            Screen.TaskList.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            TaskListScreen(navController = navController)
        }
        composable(
            Screen.CreateTask.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            CreateTaskScreen(navController = navController)
        }
        composable(
            Screen.Statistics.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            StatisticsScreen(navController = navController)
        }
        composable(
            Screen.Achievements.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            AchievementScreen(navController = navController)
        }
        composable(
            Screen.BossDetail.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            BossDetailScreen(navController = navController)
        }
        composable(
            Screen.Settings.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            SettingsScreen(navController = navController)
        }
        composable(
            Screen.About.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            AboutScreen(navController = navController)
        }
        composable(
            Screen.DailyChallenges.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            DailyChallengeScreen(navController = navController)
        }
        composable(
            Screen.BossHistory.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            BossHistoryScreen(navController = navController)
        }
        composable(
            Screen.Support.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            SupportScreen(navController = navController)
        }
        composable(
            Screen.Profile.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            ProfileScreen(navController = navController)
        }
        composable(
            Screen.Templates.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            TemplatesListScreen(navController = navController)
        }
        composable(
            Screen.Categories.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            CategoriesScreen(navController = navController)
        }
        composable(
            Screen.FocusTimer.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            FocusTimerScreen(navController = navController)
        }
    }
}
