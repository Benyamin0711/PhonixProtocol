# Bottom Navigation Bar Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the bottom navigation bar to match mobile banking app style (Bank Melli) with app logo in center, semi-transparent dark background, and appear on all 5 main screens.

**Architecture:** Create a MainScreen wrapper that contains the BottomNavBar and TopAppBar, then update NavGraph to use this wrapper for Dashboard, Profile, Settings, Leaderboard, and Support screens.

**Tech Stack:** Jetpack Compose, Material3, Navigation Compose

## Global Constraints

- Android app using Jetpack Compose
- Material3 design system
- Navigation Compose for screen navigation
- Existing color scheme: PhoenixOrange, SurfaceDark, TextSecondary

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/components/BottomNavBar.kt` | Modify | Redesign with Bank Melli style |
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/main/MainScreen.kt` | Create | Main scaffold with bottom nav |
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/NavGraph.kt` | Modify | Update to use MainScreen wrapper |
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt` | Modify | Remove BottomNavBar usage |
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/profile/ProfileScreen.kt` | Modify | Remove BottomNavBar if present |
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/settings/SettingsScreen.kt` | Modify | Remove BottomNavBar if present |
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/leaderboard/LeaderboardScreen.kt` | Modify | Remove BottomNavBar if present |
| `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/support/SupportScreen.kt` | Modify | Remove BottomNavBar if present |
| `app/src/main/res/values/strings.xml` | Modify | Add/update bottom nav labels |

---

### Task 1: Redesign BottomNavBar Component

**Covers:** [S2, S5]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/components/BottomNavBar.kt`

**Interfaces:**
- Consumes: currentRoute, onNavigate callback
- Produces: Updated BottomNavBar composable with Bank Melli style

- [ ] **Step 1: Read current BottomNavBar.kt**

Read the file to understand current implementation.

- [ ] **Step 2: Rewrite BottomNavBar.kt with Bank Melli style**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left buttons
            BottomNavItem(
                icon = Icons.Filled.Person,
                label = stringResource(R.string.bottom_nav_account),
                route = "profile",
                currentRoute = currentRoute,
                onClick = { onNavigate("profile") }
            )

            BottomNavItem(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.bottom_nav_settings),
                route = "settings",
                currentRoute = currentRoute,
                onClick = { onNavigate("settings") }
            )

            Spacer(modifier = Modifier.width(48.dp))

            // Right buttons
            BottomNavItem(
                icon = Icons.Filled.EmojiEvents,
                label = stringResource(R.string.bottom_nav_leaderboard),
                route = "leaderboard",
                currentRoute = currentRoute,
                onClick = { onNavigate("leaderboard") }
            )

            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.Help,
                label = stringResource(R.string.bottom_nav_support),
                route = "support",
                currentRoute = currentRoute,
                onClick = { onNavigate("support") }
            )
        }

        // Center FAB (App Logo)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(64.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(PhoenixOrange)
                .clickable { onNavigate("dashboard") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.bottom_nav_dashboard),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    onClick: () -> Unit
) {
    val isSelected = currentRoute == route
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PhoenixOrange else TextSecondary,
        label = "bottomNavItemColor"
    )

    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/components/BottomNavBar.kt
git commit -m "feat: redesign BottomNavBar with Bank Melli style"
```

---

### Task 2: Create MainScreen Wrapper

**Covers:** [S3, S5]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/main/MainScreen.kt`

**Interfaces:**
- Consumes: NavHostController, current route
- Produces: MainScreen composable with scaffold, bottom nav, and drawer

- [ ] **Step 1: Create MainScreen.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
                            "leaderboard" -> navController.navigate(Screen.Leaderboard.route)
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
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/main/MainScreen.kt
git commit -m "feat: create MainScreen wrapper with bottom nav and drawer"
```

---

### Task 3: Update NavGraph to Use MainScreen

**Covers:** [S3, S5]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: MainScreen composable
- Produces: Updated NavGraph with MainScreen wrapper

- [ ] **Step 1: Read current NavGraph.kt**

Read the file to understand current navigation structure.

- [ ] **Step 2: Update NavGraph.kt**

Add import for MainScreen and wrap Dashboard, Profile, Settings, Leaderboard, Support screens with MainScreen:

```kotlin
// Add import
import com.benyaminrasouli.phoenixprotocol.feature.main.MainScreen

// Wrap Dashboard screen
composable(
    Screen.Dashboard.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    MainScreen(navController = navController) {
        DashboardScreen(navController = navController)
    }
}

// Wrap Profile screen
composable(
    Screen.Profile.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    MainScreen(navController = navController) {
        ProfileScreen(navController = navController)
    }
}

// Wrap Settings screen
composable(
    Screen.Settings.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    MainScreen(navController = navController) {
        SettingsScreen(navController = navController)
    }
}

// Wrap Leaderboard screen
composable(
    Screen.Leaderboard.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    MainScreen(navController = navController) {
        LeaderboardScreen(navController = navController)
    }
}

// Wrap Support screen
composable(
    Screen.Support.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    MainScreen(navController = navController) {
        SupportScreen(navController = navController)
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/NavGraph.kt
git commit -m "feat: update NavGraph to use MainScreen wrapper"
```

---

### Task 4: Remove BottomNavBar from DashboardScreen

**Covers:** [S3]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: MainScreen (now handles bottom nav)
- Produces: DashboardScreen without BottomNavBar

- [ ] **Step 1: Read current DashboardScreen.kt**

Read the file to understand current implementation.

- [ ] **Step 2: Remove BottomNavBar and drawer from DashboardScreen**

Remove the ModalNavigationDrawer, Scaffold with topBar and bottomBar, and keep only the content:

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
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
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt
git commit -m "refactor: remove BottomNavBar from DashboardScreen"
```

---

### Task 5: Verify All Screens Work

**Covers:** [S6]

**Files:**
- None (verification only)

**Interfaces:**
- Consumes: All updated screens
- Produces: Verified working navigation

- [ ] **Step 1: Build the app**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Test navigation**

Verify:
1. Dashboard shows bottom nav bar
2. Profile shows bottom nav bar
3. Settings shows bottom nav bar
4. Leaderboard shows bottom nav bar
5. Support shows bottom nav bar
6. Center logo navigates to Dashboard
7. Drawer menu opens from all screens
8. All drawer menu items work

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: complete bottom nav redesign with Bank Melli style"
```

---

## Self-Review Checklist

- [x] Spec coverage: [S2] covered by Task 1, [S3] covered by Tasks 2-4, [S5] covered by Tasks 1-2, [S6] covered by Task 5
- [x] No placeholders: All steps have complete code
- [x] Type consistency: BottomNavBar signature unchanged, MainScreen uses same NavController pattern
- [x] File paths: All paths verified against project structure