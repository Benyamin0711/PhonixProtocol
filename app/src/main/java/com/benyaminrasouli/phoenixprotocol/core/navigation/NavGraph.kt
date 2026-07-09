package com.benyaminrasouli.phoenixprotocol.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.benyaminrasouli.phoenixprotocol.feature.splash.SplashScreen
import com.benyaminrasouli.phoenixprotocol.feature.main.MainScreen
import com.benyaminrasouli.phoenixprotocol.feature.onboarding.OnboardingScreen
import com.benyaminrasouli.phoenixprotocol.feature.home.HomeScreen
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
import com.benyaminrasouli.phoenixprotocol.feature.notifications.NotificationScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        val routes = listOf(
            Screen.Home.route, Screen.TaskList.route, Screen.CreateTask.route,
            Screen.Statistics.route, Screen.Achievements.route, Screen.BossDetail.route,
            Screen.Settings.route, Screen.About.route, Screen.DailyChallenges.route,
            Screen.BossHistory.route, Screen.Support.route, Screen.Profile.route,
            Screen.Templates.route, Screen.Categories.route, Screen.FocusTimer.route,
            Screen.Shadow.route, Screen.Analytics.route, Screen.PrivacyPolicy.route,
            Screen.Notifications.route
        )

        routes.forEach { route ->
            composable(route) {
                MainScreen(navController, currentRoute = route) { onStoryVisibilityChanged, onOpenDrawer ->
                    when (route) {
                        Screen.Home.route -> HomeScreen(
                            navController,
                            onOpenDrawer = onOpenDrawer,
                            onStoryVisibilityChanged = onStoryVisibilityChanged
                        )
                        Screen.TaskList.route -> TaskListScreen(navController)
                        Screen.CreateTask.route -> CreateTaskScreen(navController)
                        Screen.Statistics.route -> StatisticsScreen(navController)
                        Screen.Achievements.route -> AchievementScreen(navController)
                        Screen.BossDetail.route -> BossDetailScreen(navController)
                        Screen.Settings.route -> SettingsScreen(navController)
                        Screen.About.route -> AboutScreen(navController)
                        Screen.DailyChallenges.route -> DailyChallengeScreen(navController)
                        Screen.BossHistory.route -> BossHistoryScreen(navController)
                        Screen.Support.route -> SupportScreen(navController)
                        Screen.Profile.route -> ProfileScreen(navController)
                        Screen.Templates.route -> TemplatesListScreen(navController)
                        Screen.Categories.route -> CategoriesScreen(navController)
                        Screen.FocusTimer.route -> FocusTimerScreen(
                            navController = navController,
                            onFullScreenChanged = onStoryVisibilityChanged
                        )
                        Screen.Shadow.route -> ShadowScreen(navController)
                        Screen.Analytics.route -> AnalyticsScreen(navController)
                        Screen.PrivacyPolicy.route -> PrivacyPolicyScreen(navController)
                        Screen.Notifications.route -> NotificationScreen(navController)
                    }
                }
            }
        }
    }
}
