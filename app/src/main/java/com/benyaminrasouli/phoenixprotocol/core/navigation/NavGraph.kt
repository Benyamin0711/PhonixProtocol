package com.benyaminrasouli.phoenixprotocol.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.benyaminrasouli.phoenixprotocol.feature.splash.SplashScreen
import com.benyaminrasouli.phoenixprotocol.feature.main.MainScreen
import com.benyaminrasouli.phoenixprotocol.feature.onboarding.OnboardingScreen
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.DashboardScreen
import com.benyaminrasouli.phoenixprotocol.feature.tasks.TaskListScreen
import com.benyaminrasouli.phoenixprotocol.feature.tasks.CreateTaskScreen
import com.benyaminrasouli.phoenixprotocol.feature.statistics.StatisticsScreen
import com.benyaminrasouli.phoenixprotocol.feature.achievements.AchievementScreen
import com.benyaminrasouli.phoenixprotocol.feature.boss.BossDetailScreen
import com.benyaminrasouli.phoenixprotocol.feature.boss.BossHistoryScreen
import com.benyaminrasouli.phoenixprotocol.feature.settings.SettingsScreen
import com.benyaminrasouli.phoenixprotocol.feature.about.AboutScreen
import com.benyaminrasouli.phoenixprotocol.feature.challenges.DailyChallengeScreen
import com.benyaminrasouli.phoenixprotocol.feature.profile.ProfileScreen
import com.benyaminrasouli.phoenixprotocol.feature.support.SupportScreen
import com.benyaminrasouli.phoenixprotocol.feature.templates.TemplatesListScreen
import com.benyaminrasouli.phoenixprotocol.feature.categories.CategoriesScreen
import com.benyaminrasouli.phoenixprotocol.feature.focus.FocusTimerScreen
import com.benyaminrasouli.phoenixprotocol.feature.shadow.ShadowScreen
import com.benyaminrasouli.phoenixprotocol.feature.analytics.AnalyticsScreen
import com.benyaminrasouli.phoenixprotocol.feature.privacy.PrivacyPolicyScreen

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
            MainScreen(navController = navController) {
                DashboardScreen(navController = navController)
            }
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
            MainScreen(navController = navController) {
                SettingsScreen(navController = navController)
            }
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
            MainScreen(navController = navController) {
                SupportScreen(navController = navController)
            }
        }
        composable(
            Screen.Profile.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            MainScreen(navController = navController) {
                ProfileScreen(navController = navController)
            }
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
        composable(
            Screen.Shadow.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            ShadowScreen(navController = navController)
        }
        composable(
            Screen.Analytics.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            AnalyticsScreen(navController = navController)
        }
        composable(
            Screen.PrivacyPolicy.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            PrivacyPolicyScreen(navController = navController)
        }
    }
}
