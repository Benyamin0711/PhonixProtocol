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
    }
}
