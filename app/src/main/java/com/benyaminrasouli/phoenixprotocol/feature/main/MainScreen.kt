package com.benyaminrasouli.phoenixprotocol.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.BottomNavBar
import com.benyaminrasouli.phoenixprotocol.feature.drawer.DrawerScreen
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
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
                    onNavigateToShadow = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Shadow.route)
                    },
                    onNavigateToAnalytics = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Analytics.route)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                BottomNavBar(
                    currentRoute = navController.currentDestination?.route,
                    onNavigate = { route ->
                        when (route) {
                            "dashboard" -> {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            }
                            "profile" -> navController.navigate(Screen.Profile.route)
                            "settings" -> navController.navigate(Screen.Settings.route)
                            "support" -> navController.navigate(Screen.Support.route)
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
