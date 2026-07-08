package com.benyaminrasouli.phoenixprotocol.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.home.components.HomeFABBar
import com.benyaminrasouli.phoenixprotocol.feature.drawer.DrawerScreen
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    currentRoute: String?,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val noShellRoutes = listOf(Screen.Splash.route, Screen.Onboarding.route)
    val showShell = currentRoute !in noShellRoutes

    if (!showShell) {
        content()
        return
    }

    val onNavigate: (String) -> Unit = { route ->
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                DrawerScreen(
                    onNavigateToSettings = { onNavigate(Screen.Settings.route) },
                    onNavigateToAbout = { onNavigate(Screen.About.route) },
                    onNavigateToSupport = { onNavigate(Screen.Support.route) },
                    onNavigateToStatistics = { onNavigate(Screen.Statistics.route) },
                    onNavigateToProfile = { onNavigate(Screen.Profile.route) },
                    onNavigateToFocusTimer = { onNavigate(Screen.FocusTimer.route) },
                    onNavigateToShadow = { onNavigate(Screen.Shadow.route) }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                HomeFABBar(
                    currentRoute = navController.currentDestination?.route,
                    onNavigate = { route ->
                        when (route) {
                            "home" -> {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                            "tasklist" -> onNavigate(Screen.TaskList.route)
                            "focus" -> onNavigate(Screen.FocusTimer.route)
                            "profile" -> onNavigate(Screen.Profile.route)
                            "support" -> onNavigate(Screen.Support.route)
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content()
            }
        }
    }
}
