# Social Leaderboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a social leaderboard screen showing simulated players ranked by XP, with all-time and weekly views, ready for future backend integration.

**Architecture:** Repository pattern with mock implementation. LeaderboardRepository interface allows swapping MockLeaderboardRepository for a real API later. Mock data generates ~15 simulated players with randomized stats.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Material 3

## Global Constraints
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- All UI text via `stringResource()` — never hardcode
- Bilingual: English + Persian (RTL)
- Feature-based Clean Architecture with Hilt DI
- Build verified: `./gradlew assembleDebug` after each task
- All new strings must have both English and Persian locales
- Database at version 9 (no migration needed — leaderboard is mock-only)

---

## STEP 1 — Data Layer

### Task 1: LeaderboardEntry + Repository Interface + Mock Implementation

**Covers:** [S3]

**Files:**
- Create: `app/src/main/java/.../core/domain/model/LeaderboardEntry.kt`
- Create: `app/src/main/java/.../core/domain/repository/LeaderboardRepository.kt`
- Create: `app/src/main/java/.../core/data/repository/MockLeaderboardRepository.kt`
- Modify: `app/src/main/java/.../di/AppModule.kt`

**Interfaces:**
- Consumes: UserStatsDao (to get current user's XP/level/rank)
- Produces: LeaderboardEntry data class, LeaderboardRepository interface, MockLeaderboardRepository

- [ ] **Step 1: Create LeaderboardEntry data class**

Create `app/src/main/java/.../core/domain/model/LeaderboardEntry.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.model

data class LeaderboardEntry(
    val id: Long,
    val name: String,
    val avatarColor: String,
    val xp: Int,
    val level: Int,
    val rank: Rank,
    val isCurrentUser: Boolean = false
)
```

- [ ] **Step 2: Create LeaderboardRepository interface**

Create `app/src/main/java/.../core/domain/repository/LeaderboardRepository.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.domain.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getAllTimeLeaderboard(): Flow<List<LeaderboardEntry>>
    fun getWeeklyLeaderboard(): Flow<List<LeaderboardEntry>>
}
```

- [ ] **Step 3: Create MockLeaderboardRepository**

Create `app/src/main/java/.../core/data/repository/MockLeaderboardRepository.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.domain.model.LeaderboardEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.model.Rank
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.LeaderboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MockLeaderboardRepository @Inject constructor(
    private val userStatsDao: UserStatsDao
) : LeaderboardRepository {

    private val mockPlayers = listOf(
        MockPlayer("ShadowHunter", "#9C27B0", 48200, 76),
        MockPlayer("PhoenixRider", "#FF6B35", 45100, 72),
        MockPlayer("VoidWalker", "#2196F3", 42800, 68),
        MockPlayer("NightFlame", "#E91E63", 39500, 64),
        MockPlayer("StormBreaker", "#4CAF50", 36200, 59),
        MockPlayer("IronPulse", "#FFC107", 33100, 54),
        MockPlayer("GhostStrike", "#00BCD4", 29800, 49),
        MockPlayer("BlazeMaster", "#FF5722", 26400, 44),
        MockPlayer("FrostEdge", "#607D8B", 23100, 39),
        MockPlayer("ThunderVolt", "#795548", 19800, 33),
        MockPlayer("DarkNova", "#673AB7", 16500, 28),
        MockPlayer("SteelFang", "#8BC34A", 13200, 22),
        MockPlayer("CrimsonAsh", "#F44336", 9900, 17),
        MockPlayer("SilverMist", "#9E9E9E", 6600, 12),
        MockPlayer("EmberSoul", "#FF9800", 3300, 6)
    )

    override fun getAllTimeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val stats = userStatsDao.getStats()
        val userEntry = LeaderboardEntry(
            id = 0,
            name = stats?.let { "You" } ?: "You",
            avatarColor = "#FF6B35",
            xp = stats?.xp ?: 0,
            level = stats?.level ?: 1,
            rank = Rank.forLevel(stats?.level ?: 1),
            isCurrentUser = true
        )
        val allEntries = mockPlayers.map { p ->
            LeaderboardEntry(
                id = p.name.hashCode().toLong(),
                name = p.name,
                avatarColor = p.color,
                xp = p.xp,
                level = p.level,
                rank = Rank.forLevel(p.level)
            )
        } + userEntry
        emit(allEntries.sortedByDescending { it.xp })
    }

    override fun getWeeklyLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val stats = userStatsDao.getStats()
        val weeklyUserXp = (stats?.xp ?: 0) / 4
        val userEntry = LeaderboardEntry(
            id = 0,
            name = "You",
            avatarColor = "#FF6B35",
            xp = weeklyUserXp,
            level = stats?.level ?: 1,
            rank = Rank.forLevel(stats?.level ?: 1),
            isCurrentUser = true
        )
        val allEntries = mockPlayers.map { p ->
            val weeklyXp = p.xp / 4 + (p.name.hashCode() % 500)
            LeaderboardEntry(
                id = p.name.hashCode().toLong(),
                name = p.name,
                avatarColor = p.color,
                xp = weeklyXp,
                level = p.level,
                rank = Rank.forLevel(p.level)
            )
        } + userEntry
        emit(allEntries.sortedByDescending { it.xp })
    }

    private data class MockPlayer(
        val name: String,
        val color: String,
        val xp: Int,
        val level: Int
    )
}
```

- [ ] **Step 4: Update AppModule**

Edit `app/src/main/java/.../di/AppModule.kt` — add binding:
```kotlin
import com.benyaminrasouli.phoenixprotocol.core.data.repository.MockLeaderboardRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.LeaderboardRepository
```
```kotlin
@Binds
@Singleton
abstract fun bindLeaderboardRepository(impl: MockLeaderboardRepository): LeaderboardRepository
```

- [ ] **Step 5: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 2 — Bilingual Strings

### Task 2: All Phase 8 Strings

**Covers:** [S6]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

- [ ] **Step 1: Add English strings**

Edit `app/src/main/res/values/strings.xml` — add before `</resources>`:
```xml
<!-- Leaderboard -->
<string name="leaderboard_title">Leaderboard</string>
<string name="leaderboard_all_time">All-Time</string>
<string name="leaderboard_weekly">Weekly</string>
<string name="leaderboard_rank">Rank</string>
<string name="leaderboard_xp">XP</string>
<string name="leaderboard_level">Level</string>
<string name="leaderboard_empty">No leaderboard data</string>
<string name="leaderboard_you">You</string>
<string name="drawer_leaderboard">Leaderboard</string>
```

- [ ] **Step 2: Add Persian strings**

Edit `app/src/main/res/values-fa/strings.xml` — add before `</resources>`:
```xml
<!-- Leaderboard -->
<string name="leaderboard_title">جدول رتبه‌بندی</string>
<string name="leaderboard_all_time">همه زمان‌ها</string>
<string name="leaderboard_weekly">هفتگی</string>
<string name="leaderboard_rank">رتبه</string>
<string name="leaderboard_xp">امتیاز</string>
<string name="leaderboard_level">سطح</string>
<string name="leaderboard_empty">داده رتبه‌بندی موجود نیست</string>
<string name="leaderboard_you">شما</string>
<string name="drawer_leaderboard">رتبه‌بندی</string>
```

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 3 — UI Layer

### Task 3: LeaderboardViewModel + LeaderboardScreen

**Covers:** [S4]

**Files:**
- Create: `app/src/main/java/.../feature/leaderboard/LeaderboardViewModel.kt`
- Create: `app/src/main/java/.../feature/leaderboard/LeaderboardScreen.kt`

**Interfaces:**
- Consumes: LeaderboardRepository
- Produces: LeaderboardScreen composable

- [ ] **Step 1: Create LeaderboardViewModel**

Create `app/src/main/java/.../feature/leaderboard/LeaderboardViewModel.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.domain.model.LeaderboardEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class LeaderboardTab { ALL_TIME, WEEKLY }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LeaderboardTab.ALL_TIME)
    val selectedTab: StateFlow<LeaderboardTab> = _selectedTab

    val leaderboard: StateFlow<List<LeaderboardEntry>> = _selectedTab
        .flatMapLatest { tab ->
            when (tab) {
                LeaderboardTab.ALL_TIME -> leaderboardRepository.getAllTimeLeaderboard()
                LeaderboardTab.WEEKLY -> leaderboardRepository.getWeeklyLeaderboard()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: LeaderboardTab) {
        _selectedTab.value = tab
    }
}
```

- [ ] **Step 2: Create LeaderboardScreen**

Create `app/src/main/java/.../feature/leaderboard/LeaderboardScreen.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.domain.model.LeaderboardEntry
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val tabs = listOf(
        stringResource(R.string.leaderboard_all_time),
        stringResource(R.string.leaderboard_weekly)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.leaderboard_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = BackgroundDark,
            contentColor = PhoenixOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = PhoenixOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab.ordinal == index,
                    onClick = { viewModel.selectTab(LeaderboardTab.entries[index]) },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab.ordinal == index) PhoenixOrange else TextSecondary
                        )
                    }
                )
            }
        }

        if (leaderboard.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.leaderboard_empty),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top 3 podium
                val top3 = leaderboard.take(3)
                if (top3.size >= 3) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        PodiumRow(
                            second = top3[1],
                            first = top3[0],
                            third = top3[2]
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Full list starting from rank 4
                itemsIndexed(leaderboard.drop(3)) { index, entry ->
                    LeaderboardRow(
                        rank = index + 4,
                        entry = entry
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun PodiumRow(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd place
        PodiumCard(entry = second, medalColor = PhoenixGold.copy(alpha = 0.5f), height = 100.dp)
        // 1st place
        PodiumCard(entry = first, medalColor = PhoenixGold, height = 130.dp)
        // 3rd place
        PodiumCard(entry = third, medalColor = PhoenixGold.copy(alpha = 0.3f), height = 80.dp)
    }
}

@Composable
private fun PodiumCard(
    entry: LeaderboardEntry,
    medalColor: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        AvatarCircle(entry = entry, size = 48)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (entry.isCurrentUser) PhoenixOrange else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${entry.xp} XP",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(medalColor, MaterialTheme.shapes.medium)
        )
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) PhoenixOrange.copy(alpha = 0.15f) else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(12.dp))
            AvatarCircle(entry = entry, size = 40)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (entry.isCurrentUser) PhoenixOrange else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = entry.rank.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.xp}",
                    style = MaterialTheme.typography.titleMedium,
                    color = PhoenixOrange
                )
                Text(
                    text = stringResource(R.string.leaderboard_xp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AvatarCircle(entry: LeaderboardEntry, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(entry.avatarColor))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = entry.name.first().toString(),
            color = Color.White,
            fontSize = (size / 2).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
```

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 4 — Navigation Integration

### Task 4: Screen Route + NavGraph + Drawer + Dashboard Wiring

**Covers:** [S5]

**Files:**
- Modify: `app/src/main/java/.../core/navigation/Screen.kt`
- Modify: `app/src/main/java/.../core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/.../feature/drawer/DrawerScreen.kt`
- Modify: `app/src/main/java/.../feature/dashboard/DashboardScreen.kt`

- [ ] **Step 1: Add Screen.Leaderboard route**

Edit `app/src/main/java/.../core/navigation/Screen.kt` — add:
```kotlin
data object Leaderboard : Screen("leaderboard")
```

- [ ] **Step 2: Add NavGraph composable**

Edit `app/src/main/java/.../core/navigation/NavGraph.kt` — add import and composable:
```kotlin
import com.benyaminrasouli.phoenixprotocol.feature.leaderboard.LeaderboardScreen
```
```kotlin
composable(
    Screen.Leaderboard.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    LeaderboardScreen(navController = navController)
}
```

- [ ] **Step 3: Add Leaderboard to DrawerScreen**

Edit `app/src/main/java/.../feature/drawer/DrawerScreen.kt`:
- Add import: `import androidx.compose.material.icons.filled.Leaderboard`
- Add parameter: `onNavigateToLeaderboard: () -> Unit = {},`
- Add menu item after Achievements:
```kotlin
DrawerMenuItem(
    icon = Icons.Filled.Leaderboard,
    label = stringResource(R.string.drawer_leaderboard),
    onClick = onNavigateToLeaderboard
)
```

- [ ] **Step 4: Wire callback in DashboardScreen**

Edit `app/src/main/java/.../feature/dashboard/DashboardScreen.kt`:
- Add `onNavigateToLeaderboard` callback to DrawerScreen call:
```kotlin
onNavigateToLeaderboard = {
    navController.navigate(Screen.Leaderboard.route)
},
```

- [ ] **Step 5: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 5 — Final Verification

### Task 5: Full Build Verification

**Covers:** [S1, S2, S3, S4, S5, S6, S7]

- [ ] **Step 1: Full build verification**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Final commit**

```bash
git add -A
git commit -m "feat: Phase 8 complete - Social Leaderboard with mock data placeholder"
```
