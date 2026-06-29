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
}
