# Home Screen Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the home screen with a new FAB bar, Instagram-style stories, user status box, 3×3 feature grid, support footer, and a simple task screen.

**Architecture:** Replace the current Dashboard + BottomNavBar with a new HomeScreen composable. The FAB bar is rebuilt with new items (Timer, Support, Home, Tasks, Profile). The HomeScreen contains scrollable sections: Header → Stories → User Status → Feature Grid → Support Footer.

**Tech Stack:** Jetpack Compose, Material3, Hilt, Navigation Compose, Room (existing)

## Global Constraints

- RTL disabled: `supportsRtl="false"` + `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`
- Language change via SharedPreferences + LocaleHelper.setLocale() without recreate()
- Use existing theme colors: PhoenixOrange, SurfaceDark, TextSecondary
- Icons from material-icons-core only (no extended)
- All screens wrapped in MainScreen shell except Splash and Onboarding

---

## File Structure

| Action | File | Responsibility |
|--------|------|---------------|
| Create | `feature/home/HomeScreen.kt` | Main home screen composable |
| Create | `feature/home/components/StorySection.kt` | Instagram-style horizontal stories |
| Create | `feature/home/components/UserStatusCard.kt` | User status box with XP bar |
| Create | `feature/home/components/FeatureGrid.kt` | 3×3 app tools grid |
| Create | `feature/home/components/SupportFooter.kt` | Support CTA footer |
| Create | `feature/home/components/HomeFABBar.kt` | New FAB bar component |
| Create | `feature/home/HomeViewModel.kt` | ViewModel for home screen data |
| Create | `feature/tasks/TaskListScreen.kt` | Simple task list (rewrite existing) |
| Modify | `feature/main/MainScreen.kt` | Update to use new FAB bar |
| Modify | `core/navigation/NavGraph.kt` | Add Home route, update TaskList route |
| Modify | `core/navigation/Screen.kt` | Add Screen.Home route constant |
| Delete | `feature/dashboard/DashboardScreen.kt` | Replaced by HomeScreen |
| Delete | `feature/dashboard/components/BottomNavBar.kt` | Replaced by HomeFABBar |

---

### Task 1: Navigation Setup

**Covers:** [S1, S2]

**Files:**
- Modify: `core/navigation/Screen.kt`
- Modify: `core/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: existing Screen sealed class
- Produces: `Screen.Home` route constant, updated NavGraph with Home route

- [ ] **Step 1: Add Screen.Home route**

In `core/navigation/Screen.kt`, add:
```kotlin
object Home : Screen("home")
```

- [ ] **Step 2: Update NavGraph to use Home as start of main routes**

In `core/navigation/NavGraph.kt`, change the `when` block to include:
```kotlin
Screen.Home.route -> HomeScreen(navController)
```

Make `Screen.Home.route` the default/dashboard replacement.

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/
git commit -m "feat: add Home route to navigation"
```

---

### Task 2: New FAB Bar Component

**Covers:** [S1]

**Files:**
- Create: `feature/home/components/HomeFABBar.kt`
- Modify: `feature/main/MainScreen.kt`

**Interfaces:**
- Consumes: `currentRoute: String?`, `onNavigate: (String) -> Unit`
- Produces: `HomeFABBar` composable

- [ ] **Step 1: Create HomeFABBar.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.home.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun HomeFABBar(
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
            // Left: Timer, Support
            FABNavItem(
                icon = Icons.Filled.AccessTime,
                label = "Timer",
                route = "focus",
                currentRoute = currentRoute,
                onClick = { onNavigate("focus") }
            )
            FABNavItem(
                icon = Icons.Filled.Info,
                label = "Support",
                route = "support",
                currentRoute = currentRoute,
                onClick = { onNavigate("support") }
            )

            Spacer(modifier = Modifier.width(56.dp))

            // Right: Tasks, Profile
            FABNavItem(
                icon = Icons.Filled.List,
                label = "Tasks",
                route = "tasklist",
                currentRoute = currentRoute,
                onClick = { onNavigate("tasklist") }
            )
            FABNavItem(
                icon = Icons.Filled.Person,
                label = "Profile",
                route = "profile",
                currentRoute = currentRoute,
                onClick = { onNavigate("profile") }
            )
        }

        // Center FAB (Home - App Logo)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(64.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(PhoenixOrange)
                .clickable { onNavigate("home") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Home",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun FABNavItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    onClick: () -> Unit
) {
    val isSelected = currentRoute == route
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PhoenixOrange else TextSecondary,
        label = "fabNavItemColor"
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

- [ ] **Step 2: Update MainScreen.kt to use HomeFABBar**

Replace the `bottomBar` content in `MainScreen.kt`:
```kotlin
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
}
```

Update imports to use `HomeFABBar` instead of `BottomNavBar`.

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/components/HomeFABBar.kt
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/main/MainScreen.kt
git commit -m "feat: add new FAB bar with Timer, Support, Home, Tasks, Profile"
```

---

### Task 3: Home Screen Header

**Covers:** [S2]

**Files:**
- Create: `feature/home/HomeScreen.kt`

**Interfaces:**
- Consumes: `navController: NavController`
- Produces: `HomeScreen` composable

- [ ] **Step 1: Create HomeScreen.kt with header**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "PHOENIX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = TextSecondary
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: notifications */ }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/HomeScreen.kt
git commit -m "feat: create HomeScreen with header (menu, title, notifications)"
```

---

### Task 4: Story Section

**Covers:** [S3]

**Files:**
- Create: `feature/home/components/StorySection.kt`
- Modify: `feature/home/HomeScreen.kt`

**Interfaces:**
- Consumes: `navController: NavController`
- Produces: `StorySection` composable with horizontal scrollable cards

- [ ] **Step 1: Create StorySection.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

data class StoryItem(
    val title: String,
    val description: String,
    val gradientColors: List<Color>,
    val onLearnMore: () -> Unit
)

@Composable
fun StorySection(
    onNavigateToFeature: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stories = listOf(
        StoryItem(
            title = "Pomodoro Timer",
            description = "Boost your focus with timed work sessions",
            gradientColors = listOf(PhoenixOrange, Color(0xFFFF6B35)),
            onLearnMore = { onNavigateToFeature("focus_timer") }
        ),
        StoryItem(
            title = "Meditation",
            description = "Find peace with guided meditation",
            gradientColors = listOf(Color(0xFF6B73FF), Color(0xFF000DFE)),
            onLearnMore = { onNavigateToFeature("meditation") }
        ),
        StoryItem(
            title = "Breathing Exercises",
            description = "Calm your mind with breathing techniques",
            gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
            onLearnMore = { onNavigateToFeature("breathing") }
        ),
        StoryItem(
            title = "Frequency Listening",
            description = "Train your ears with audio frequencies",
            gradientColors = listOf(Color(0xFFEE0979), Color(0xFFFF6A00)),
            onLearnMore = { onNavigateToFeature("frequency") }
        )
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stories.forEach { story ->
                StoryCard(story = story)
            }
        }
    }
}

@Composable
private fun StoryCard(story: StoryItem) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(240.dp)
            .clickable(onClick = story.onLearnMore),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = story.gradientColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = story.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = story.onLearnMore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Learn More",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Add StorySection to HomeScreen**

In `HomeScreen.kt`, add after the TopAppBar:
```kotlin
StorySection(
    onNavigateToFeature = { feature ->
        when (feature) {
            "focus_timer" -> navController.navigate("focus_timer")
            // other features will be added later
        }
    }
)
```

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/components/StorySection.kt
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/HomeScreen.kt
git commit -m "feat: add Instagram-style story section with feature cards"
```

---

### Task 5: User Status Card

**Covers:** [S4]

**Files:**
- Create: `feature/home/components/UserStatusCard.kt`
- Modify: `feature/home/HomeScreen.kt`

**Interfaces:**
- Consumes: user data (username, level, status, rank, xp, quote)
- Produces: `UserStatusCard` composable

- [ ] **Step 1: Create UserStatusCard.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun UserStatusCard(
    username: String = "Phoenix User",
    level: Int = 5,
    status: String = "Active",
    rank: Int = 3,
    currentXp: Int = 750,
    maxXp: Int = 1000,
    quote: String = "Stay consistent, stay strong",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PhoenixOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(1).uppercase(),
                        color = PhoenixOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.titleSmall,
                        color = PhoenixOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rank #$rank",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // XP Progress
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { currentXp.toFloat() / maxXp.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PhoenixOrange,
                    trackColor = PhoenixOrange.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$currentXp/$maxXp XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quote
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
```

- [ ] **Step 2: Add UserStatusCard to HomeScreen**

In `HomeScreen.kt`, add after StorySection:
```kotlin
Spacer(modifier = Modifier.height(16.dp))
UserStatusCard()
```

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/components/UserStatusCard.kt
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/HomeScreen.kt
git commit -m "feat: add user status card with XP progress bar"
```

---

### Task 6: Feature Grid (3×3)

**Covers:** [S5]

**Files:**
- Create: `feature/home/components/FeatureGrid.kt`
- Modify: `feature/home/HomeScreen.kt`

**Interfaces:**
- Consumes: `navController: NavController`
- Produces: `FeatureGrid` composable

- [ ] **Step 1: Create FeatureGrid.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val route: String?,
    val isAvailable: Boolean = true
)

@Composable
fun FeatureGrid(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val features = listOf(
        FeatureItem("Pomodoro", androidx.compose.material.icons.Icons.Filled.AccessTime, "focus_timer"),
        FeatureItem("Meditation", androidx.compose.material.icons.Icons.Filled.SelfImprovement, null, false),
        FeatureItem("Breathing", androidx.compose.material.icons.Icons.Filled.Air, null, false),
        FeatureItem("Frequency", androidx.compose.material.icons.Icons.Filled.GraphicEq, null, false),
        FeatureItem("Tasks", androidx.compose.material.icons.Icons.Filled.List, "tasklist"),
        FeatureItem("Boss", androidx.compose.material.icons.Icons.Filled.EmojiEvents, "boss"),
        FeatureItem("Challenges", androidx.compose.material.icons.Icons.Filled.EmojiEvents, "daily_challenges"),
        FeatureItem("Templates", androidx.compose.material.icons.Icons.Filled.Folder, "templates"),
        FeatureItem("Shadow", androidx.compose.material.icons.Icons.Filled.Visibility, "shadow")
    )

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "App Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        for (rowIndex in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (colIndex in 0..2) {
                    val index = rowIndex * 3 + colIndex
                    if (index < features.size) {
                        FeatureGridItem(
                            feature = features[index],
                            onClick = {
                                if (features[index].isAvailable && features[index].route != null) {
                                    onNavigate(features[index].route)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (rowIndex < 2) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FeatureGridItem(
    feature: FeatureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(enabled = feature.isAvailable, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (feature.isAvailable)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = if (feature.isAvailable) PhoenixOrange else TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = if (feature.isAvailable) TextSecondary else TextSecondary.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
                if (!feature.isAvailable) {
                    Text(
                        text = "Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = PhoenixOrange.copy(alpha = 0.7f),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Add FeatureGrid to HomeScreen**

In `HomeScreen.kt`, add after UserStatusCard:
```kotlin
Spacer(modifier = Modifier.height(16.dp))
FeatureGrid(
    onNavigate = { route ->
        navController.navigate(route)
    }
)
```

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/components/FeatureGrid.kt
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/HomeScreen.kt
git commit -m "feat: add 3x3 feature grid with available and coming soon items"
```

---

### Task 7: Support Footer

**Covers:** [S6]

**Files:**
- Create: `feature/home/components/SupportFooter.kt`
- Modify: `feature/home/HomeScreen.kt`

**Interfaces:**
- Consumes: `navController: NavController`
- Produces: `SupportFooter` composable

- [ ] **Step 1: Create SupportFooter.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun SupportFooter(
    onNavigateToSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Need help?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "We're here for you. Reach out anytime.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigateToSupport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhoenixOrange
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Support Us",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
```

- [ ] **Step 2: Add SupportFooter to HomeScreen**

In `HomeScreen.kt`, add after FeatureGrid:
```kotlin
Spacer(modifier = Modifier.height(16.dp))
SupportFooter(
    onNavigateToSupport = { navController.navigate("support") }
)
Spacer(modifier = Modifier.height(16.dp))
```

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/components/SupportFooter.kt
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/home/HomeScreen.kt
git commit -m "feat: add support footer with CTA button"
```

---

### Task 8: Simple Task List Screen

**Covers:** [S7]

**Files:**
- Create: `feature/tasks/TaskListScreen.kt` (rewrite existing)
- Create: `feature/tasks/TaskItem.kt`

**Interfaces:**
- Consumes: `navController: NavController`
- Produces: `TaskListScreen` composable

- [ ] **Step 1: Create TaskItem.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.tasks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun TaskItem(
    title: String,
    priority: String = "Medium",
    modifier: Modifier = Modifier
) {
    var checked by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = CheckboxDefaults.colors(
                checkedColor = PhoenixOrange
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = priority,
            style = MaterialTheme.typography.bodySmall,
            color = when (priority) {
                "High" -> MaterialTheme.colorScheme.error
                "Low" -> TextSecondary
                else -> PhoenixOrange
            }
        )
    }
}
```

- [ ] **Step 2: Rewrite TaskListScreen.kt**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController
) {
    val sampleTasks = listOf(
        "Complete morning routine" to "High",
        "Review project tasks" to "Medium",
        "Exercise for 30 minutes" to "High",
        "Read for 20 minutes" to "Low",
        "Plan tomorrow's tasks" to "Medium"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tasks",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: create task */ },
                containerColor = PhoenixOrange
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            items(sampleTasks) { (title, priority) ->
                TaskItem(title = title, priority = priority)
            }
        }
    }
}
```

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/tasks/
git commit -m "feat: create simple task list screen with sample data"
```

---

### Task 9: Wire Everything Together

**Covers:** [S1-S7]

**Files:**
- Modify: `feature/main/MainScreen.kt`
- Modify: `core/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: all created components
- Produces: fully wired home screen with navigation

- [ ] **Step 1: Update MainScreen to pass drawer open callback to HomeScreen**

In `MainScreen.kt`, update the HomeScreen call:
```kotlin
Screen.Home.route -> HomeScreen(
    navController = navController,
    onOpenDrawer = { scope.launch { drawerState.open() } }
)
```

- [ ] **Step 2: Verify all routes work**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: complete home screen redesign with FAB bar, stories, status card, feature grid, and task screen"
```
