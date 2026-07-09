package com.benyaminrasouli.phoenixprotocol.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    content: @Composable (onStoryVisibilityChanged: (Boolean) -> Unit, onOpenDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showFABBar by remember { mutableStateOf(true) }
    var isStoryViewerOpen by remember { mutableStateOf(false) }

    val noShellRoutes = listOf(Screen.Splash.route, Screen.Onboarding.route)
    val showShell = currentRoute !in noShellRoutes

    if (!showShell) {
        content({ }, { })
        return
    }

    val onNavigate: (String) -> Unit = { route ->
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    val onOpenDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    val drawerContent: @Composable () -> Unit = {
        ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
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

    val mainContent: @Composable () -> Unit = {
        Scaffold(
            bottomBar = {
                if (showFABBar) {
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
                                "statistics" -> onNavigate(Screen.Statistics.route)
                            }
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content(
                    { isVisible ->
                        showFABBar = !isVisible
                        isStoryViewerOpen = isVisible
                    },
                    onOpenDrawer
                )
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { drawerContent() },
        gesturesEnabled = !isStoryViewerOpen
    ) {
        mainContent()
    }
}
