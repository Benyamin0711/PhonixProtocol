package com.benyaminrasouli.phoniexprotocol.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object TaskList : Screen("task_list")
    data object CreateTask : Screen("create_task")
    data object Drawer : Screen("drawer")
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object Support : Screen("support")
    data object Statistics : Screen("statistics")
    data object Achievements : Screen("achievements")
    data object BossDetail : Screen("boss_detail")
    data object BossHistory : Screen("boss_history")
    data object DailyChallenges : Screen("daily_challenges")
}
