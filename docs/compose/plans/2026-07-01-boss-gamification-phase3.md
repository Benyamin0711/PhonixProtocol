# Phase 3: Boss & Gamification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fill 4 gaps in the Boss & Gamification system: Boss History screen, energy recovery mechanic, shadow level triggers, and boss visual animations.

**Architecture:** Feature-based Clean Architecture. New features follow existing patterns: UseCase → Repository → DAO for data, ViewModel → Composable for UI. WorkManager for background energy recovery. Pure Compose animations for visual effects.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, WorkManager, DataStore, Navigation Compose

## Global Constraints
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- All UI text via `stringResource()` — never hardcode
- Bilingual: English + Persian (RTL)
- Feature-based Clean Architecture with Hilt DI
- Build verified: `./gradlew assembleDebug` after each task
- Database is at version 4 — future changes need MIGRATION_4_5+
- Room stores Kotlin Boolean as INTEGER 0/1
- `collectAsStateWithLifecycle()` on StateFlow does not require `initialValue`
- `WhileSubscribed(5000)` for ViewModel-backed StateFlows

---

### Task 1: Dependencies & Navigation Setup

**Covers:** [S3]

**Files:**
- Modify: `gradle/libs.versions.toml` — add WorkManager version + library
- Modify: `app/build.gradle.kts` — add WorkManager + Hilt WorkManager dependencies
- Modify: `app/src/main/java/.../core/navigation/Screen.kt` — add BossHistory route
- Modify: `app/src/main/java/.../core/navigation/NavGraph.kt` — wire BossHistory route

**Interfaces:**
- Consumes: existing `Screen` sealed class, `NavGraph` NavHost
- Produces: `Screen.BossHistory` route, navGraph composable entry

- [ ] **Step 1: Add WorkManager to version catalog**

Edit `gradle/libs.versions.toml` — add under `[versions]`:
```
workManager = "2.10.1"
```

Add under `[libraries]`:
```
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hiltNavigationCompose" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hiltNavigationCompose" }
```

- [ ] **Step 2: Add dependencies to app/build.gradle.kts**

Add to `dependencies` block:
```kotlin
// WorkManager
implementation(libs.work.runtime)
implementation(libs.hilt.work)
ksp(libs.hilt.work.compiler)
```

- [ ] **Step 3: Add BossHistory route**

Edit `app/src/main/java/.../core/navigation/Screen.kt` — add:
```kotlin
data object BossHistory : Screen("boss_history")
```

- [ ] **Step 4: Wire BossHistory in NavGraph**

Edit `app/src/main/java/.../core/navigation/NavGraph.kt` — add composable:
```kotlin
composable(Screen.BossHistory.route) {
    BossHistoryScreen(navController = navController)
}
```

Add import for `BossHistoryScreen`.

- [ ] **Step 5: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git add app/src/main/java/.../core/navigation/Screen.kt
git add app/src/main/java/.../core/navigation/NavGraph.kt
git commit -m "feat: add WorkManager dependency and BossHistory navigation route"
```

---

### Task 2: Boss History Screen — ViewModel & UI

**Covers:** [S3]

**Files:**
- Create: `app/src/main/java/.../feature/boss/BossHistoryViewModel.kt`
- Create: `app/src/main/java/.../feature/boss/BossHistoryScreen.kt`
- Modify: `app/src/main/java/.../feature/drawer/DrawerScreen.kt` — add Boss History menu item

**Interfaces:**
- Consumes: `BossRepository.getAllBosses()`, `BossRepository.getActiveBosses()`, `BossRepository.getCompletedBosses()` (existing)
- Produces: `BossHistoryViewModel` with filter state, `BossHistoryScreen` composable

- [ ] **Step 1: Create BossHistoryViewModel**

Create `app/src/main/java/.../feature/boss/BossHistoryViewModel.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.feature.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BossFilter { ALL, ACTIVE, COMPLETED, FAILED }

data class BossHistoryState(
    val bosses: List<Boss> = emptyList(),
    val filter: BossFilter = BossFilter.ALL
)

@HiltViewModel
class BossHistoryViewModel @Inject constructor(
    private val bossRepository: BossRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(BossFilter.ALL)
    private val _state = MutableStateFlow(BossHistoryState())
    val state: StateFlow<BossHistoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                bossRepository.getAllBosses(),
                _filter
            ) { bosses, filter ->
                val filtered = when (filter) {
                    BossFilter.ALL -> bosses
                    BossFilter.ACTIVE -> bosses.filter { it.status == "ACTIVE" }
                    BossFilter.COMPLETED -> bosses.filter { it.status == "COMPLETED" }
                    BossFilter.FAILED -> bosses.filter { it.status == "FAILED" }
                }
                BossHistoryState(bosses = filtered, filter = filter)
            }.collect { _state.value = it }
        }
    }

    fun setFilter(filter: BossFilter) {
        _filter.value = filter
    }
}
```

- [ ] **Step 2: Create BossHistoryScreen**

Create `app/src/main/java/.../feature/boss/BossHistoryScreen.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.feature.boss

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary
import com.benyaminrasouli.phoniexprotocol.ui.theme.CompletedGreen
import com.benyaminrasouli.phoniexprotocol.ui.theme.FailedRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BossHistoryScreen(
    navController: NavController,
    viewModel: BossHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.boss_history)) },
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

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BossFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = {
                        Text(
                            when (filter) {
                                BossFilter.ALL -> stringResource(R.string.boss_history_all)
                                BossFilter.ACTIVE -> stringResource(R.string.boss_history_active)
                                BossFilter.COMPLETED -> stringResource(R.string.boss_history_completed)
                                BossFilter.FAILED -> stringResource(R.string.boss_history_failed)
                            }
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.bosses.isEmpty()) {
            Text(
                text = stringResource(R.string.boss_history_empty),
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.bosses) { boss ->
                    BossHistoryItem(boss = boss)
                }
            }
        }
    }
}

@Composable
private fun BossHistoryItem(boss: Boss) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Boss",
                        tint = when (boss.status) {
                            "COMPLETED" -> CompletedGreen
                            "FAILED" -> FailedRed
                            else -> PhoenixOrange
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = boss.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${stringResource(R.string.boss_level, boss.level)} • ${stringResource(R.string.boss_xp_reward, boss.rewardXp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = boss.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (boss.status) {
                        "COMPLETED" -> CompletedGreen
                        "FAILED" -> FailedRed
                        else -> PhoenixOrange
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = when (boss.status) {
                    "COMPLETED" -> CompletedGreen
                    "FAILED" -> FailedRed
                    else -> PhoenixOrange
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
```

- [ ] **Step 3: Add Boss History to Drawer**

Edit `app/src/main/java/.../feature/drawer/DrawerScreen.kt` — add menu item after existing boss menu:
```kotlin
// Add to DrawerScreen composable, after existing boss navigation item
DrawerMenuItem(
    icon = Icons.Filled.History,
    label = stringResource(R.string.boss_history),
    onClick = {
        coroutineScope.launch { drawerState.close() }
        onNavigateToBossHistory()
    }
)
```

Add `onNavigateToBossHistory` callback parameter to `DrawerScreen` composable.

- [ ] **Step 4: Wire Drawer navigation in NavGraph**

Edit `app/src/main/java/.../core/navigation/NavGraph.kt` — add `onNavigateToBossHistory` callback that navigates to `Screen.BossHistory.route`.

- [ ] **Step 5: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/.../feature/boss/BossHistoryViewModel.kt
git add app/src/main/java/.../feature/boss/BossHistoryScreen.kt
git add app/src/main/java/.../feature/drawer/DrawerScreen.kt
git add app/src/main/java/.../core/navigation/NavGraph.kt
git commit -m "feat: Boss History screen with filter tabs and navigation"
```

---

### Task 3: Energy Recovery — Use Case & Worker

**Covers:** [S4]

**Files:**
- Create: `app/src/main/java/.../core/domain/usecase/RecoverEnergyUseCase.kt`
- Create: `app/src/main/java/.../core/work/EnergyRecoveryWorker.kt`
- Create: `app/src/main/java/.../di/WorkerModule.kt`
- Modify: `app/src/main/java/.../PhoenixApp.kt` — enqueue worker on startup

**Interfaces:**
- Consumes: `StatsRepository.getStatsOnce()`, `StatsRepository.increasePhoenixEnergy()` (existing)
- Produces: `RecoverEnergyUseCase`, `EnergyRecoveryWorker`, worker enqueue on app start

- [ ] **Step 1: Create RecoverEnergyUseCase**

Create `app/src/main/java/.../core/domain/usecase/RecoverEnergyUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class RecoverEnergyUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke() {
        val stats = statsRepository.getStatsOnce() ?: return
        if (stats.phoenixEnergy < 100) {
            statsRepository.increasePhoenixEnergy(1)
        }
    }
}
```

- [ ] **Step 2: Create EnergyRecoveryWorker**

Create `app/src/main/java/.../core/work/EnergyRecoveryWorker.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.RecoverEnergyUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EnergyRecoveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recoverEnergyUseCase: RecoverEnergyUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            recoverEnergyUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

- [ ] **Step 3: Create WorkerModule for Hilt**

Create `app/src/main/java/.../di/WorkerModule.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.benyaminrasouli.phoniexprotocol.core.work.EnergyRecoveryWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    fun enqueueEnergyRecovery(workManager: WorkManager) {
        val request = PeriodicWorkRequestBuilder<EnergyRecoveryWorker>(
            30, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "energy_recovery",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
```

- [ ] **Step 4: Enqueue worker on app start**

Edit `app/src/main/java/.../PhoenixApp.kt` — add in `onCreate()`:
```kotlin
@Inject
lateinit var workManager: WorkManager

override fun onCreate() {
    super.onCreate()
    WorkerModule.enqueueEnergyRecovery(workManager)
}
```

Add import for `WorkerModule`.

- [ ] **Step 5: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/.../core/domain/usecase/RecoverEnergyUseCase.kt
git add app/src/main/java/.../core/work/EnergyRecoveryWorker.kt
git add app/src/main/java/.../di/WorkerModule.kt
git add app/src/main/java/.../PhoenixApp.kt
git commit -m "feat: energy recovery mechanic with WorkManager periodic task"
```

---

### Task 4: Shadow Level Triggers

**Covers:** [S5]

**Files:**
- Create: `app/src/main/java/.../core/domain/usecase/SkipTaskUseCase.kt`
- Create: `app/src/main/java/.../core/domain/usecase/CancelTaskUseCase.kt`
- Modify: `app/src/main/java/.../feature/tasks/TaskListViewModel.kt` — add skip/cancel functions
- Modify: `app/src/main/java/.../feature/tasks/TaskListScreen.kt` — add skip/cancel buttons

**Interfaces:**
- Consumes: `TaskRepository.deleteTask()`, `TaskRepository.skipTask()`, `StatsRepository.increaseShadowLevel()` (existing)
- Produces: `SkipTaskUseCase`, `CancelTaskUseCase`, ViewModel skip/cancel functions

- [ ] **Step 1: Create SkipTaskUseCase**

Create `app/src/main/java/.../core/domain/usecase/SkipTaskUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class SkipTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(task: Task) {
        taskRepository.skipTask(task.id)
        statsRepository.increaseShadowLevel(1)
    }
}
```

- [ ] **Step 2: Create CancelTaskUseCase**

Create `app/src/main/java/.../core/domain/usecase/CancelTaskUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class CancelTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(task: Task) {
        taskRepository.deleteTask(task)
        statsRepository.increaseShadowLevel(1)
    }
}
```

- [ ] **Step 3: Add skip/cancel to TaskListViewModel**

Edit `app/src/main/java/.../feature/tasks/TaskListViewModel.kt`:
- Add `SkipTaskUseCase` and `CancelTaskUseCase` to constructor
- Add `skipTask(task: Task)` function:
```kotlin
fun skipTask(task: Task) {
    viewModelScope.launch {
        skipTaskUseCase(task)
    }
}
```
- Add `cancelTask(task: Task)` function:
```kotlin
fun cancelTask(task: Task) {
    viewModelScope.launch {
        cancelTaskUseCase(task)
    }
}
```

- [ ] **Step 4: Add skip/cancel buttons to TaskListScreen**

Edit `app/src/main/java/.../feature/tasks/TaskListScreen.kt` — add swipe or button actions for skip and cancel on active tasks. Use existing `Icons.Filled.SkipNext` and `Icons.Filled.Delete`.

- [ ] **Step 5: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/.../core/domain/usecase/SkipTaskUseCase.kt
git add app/src/main/java/.../core/domain/usecase/CancelTaskUseCase.kt
git add app/src/main/java/.../feature/tasks/TaskListViewModel.kt
git add app/src/main/java/.../feature/tasks/TaskListScreen.kt
git commit -m "feat: shadow level triggers on task skip/cancel"
```

---

### Task 5: Streak Break Shadow Trigger

**Covers:** [S5]

**Files:**
- Modify: `app/src/main/java/.../core/data/repository/StatsRepositoryImpl.kt` — detect streak break
- OR Create: `app/src/main/java/.../core/domain/usecase/UpdateStreakUseCase.kt`

**Interfaces:**
- Consumes: `StatsRepository.getStatsOnce()`, `StatsRepository.increaseShadowLevel()` (existing)
- Produces: streak break detection that increases shadow by 2

- [ ] **Step 1: Create UpdateStreakUseCase**

Create `app/src/main/java/.../core/domain/usecase/UpdateStreakUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class UpdateStreakUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(taskCompletedToday: Boolean) {
        val stats = statsRepository.getStatsOnce() ?: return

        if (!taskCompletedToday && stats.currentStreak > 0) {
            // Streak broken — increase shadow level by 2
            statsRepository.increaseShadowLevel(2)
        }
    }
}
```

- [ ] **Step 2: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/.../core/domain/usecase/UpdateStreakUseCase.kt
git commit -m "feat: streak break shadow level trigger"
```

---

### Task 6: Boss Visual Animations — AnimatedProgressBar

**Covers:** [S6]

**Files:**
- Create: `app/src/main/java/.../core/ui/components/AnimatedProgressBar.kt`

**Interfaces:**
- Consumes: Compose animation APIs
- Produces: `AnimatedProgressBar` composable with glow effect

- [ ] **Step 1: Create AnimatedProgressBar**

Create `app/src/main/java/.../core/ui/components/AnimatedProgressBar.kt`:
```kotlin
package com.benyaminrasouli.phoniexprotocol.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange

@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    trackColor: Color = PhoenixOrange.copy(alpha = 0.2f),
    progressColor: Color = PhoenixOrange
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animatedProgress)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(height / 2),
                    ambientColor = progressColor.copy(alpha = 0.5f),
                    spotColor = progressColor.copy(alpha = 0.5f)
                )
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.8f),
                            progressColor
                        )
                    )
                )
        )
    }
}
```

- [ ] **Step 2: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/.../core/ui/components/AnimatedProgressBar.kt
git commit -m "feat: AnimatedProgressBar component with glow effect"
```

---

### Task 7: Boss Card & Detail Animations

**Covers:** [S6]

**Files:**
- Modify: `app/src/main/java/.../feature/boss/BossCard.kt` — entry animation + animated progress
- Modify: `app/src/main/java/.../feature/boss/BossDetailScreen.kt` — animated progress + goal animations

**Interfaces:**
- Consumes: `AnimatedProgressBar` (from Task 6)
- Produces: animated BossCard and BossDetailScreen

- [ ] **Step 1: Update BossCard with entry animation**

Edit `app/src/main/java/.../feature/boss/BossCard.kt`:
- Add `AnimatedVisibility` wrapper with `slideInVertically` + `fadeIn`
- Replace `LinearProgressIndicator` with `AnimatedProgressBar`
- Add `animateContentSize()` for smooth expansion

- [ ] **Step 2: Update BossDetailScreen with progress animation**

Edit `app/src/main/java/.../feature/boss/BossDetailScreen.kt`:
- Replace `LinearProgressIndicator` in `BossHeader` with `AnimatedProgressBar`
- Add `AnimatedVisibility` for goal completion checkmark
- Add `animateColorAsState` for goal status color transitions

- [ ] **Step 3: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/.../feature/boss/BossCard.kt
git add app/src/main/java/.../feature/boss/BossDetailScreen.kt
git commit -m "feat: boss card and detail screen animations"
```

---

### Task 8: Localization — All New Strings

**Covers:** [S7]

**Files:**
- Modify: `app/src/main/res/values/strings.xml` — English strings
- Modify: `app/src/main/res/values-fa/strings.xml` — Persian strings

**Interfaces:**
- Consumes: string keys from spec [S7]
- Produces: bilingual string resources

- [ ] **Step 1: Add English strings**

Edit `app/src/main/res/values/strings.xml` — add:
```xml
<string name="boss_history">Boss History</string>
<string name="boss_history_all">All</string>
<string name="boss_history_active">Active</string>
<string name="boss_history_completed">Completed</string>
<string name="boss_history_failed">Failed</string>
<string name="boss_history_empty">No bosses found</string>
<string name="energy_recovering">Energy recovering…</string>
<string name="shadow_increase">Shadow level increased</string>
<string name="skip_task">Skip Task</string>
<string name="cancel_task">Cancel Task</string>
```

- [ ] **Step 2: Add Persian strings**

Edit `app/src/main/res/values-fa/strings.xml` — add:
```xml
<string name="boss_history">تاریخچه باس</string>
<string name="boss_history_all">همه</string>
<string name="boss_history_active">فعال</string>
<string name="boss_history_completed">تکمیل شده</string>
<string name="boss_history_failed">ناموفق</string>
<string name="boss_history_empty">باسی یافت نشد</string>
<string name="energy_recovering">انرژی در حال بازیابی…</string>
<string name="shadow_increase">سطح سایه افزایش یافت</string>
<string name="skip_task">رد کردن تسک</string>
<string name="cancel_task">لغو تسک</string>
```

- [ ] **Step 3: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml
git add app/src/main/res/values-fa/strings.xml
git commit -m "feat: bilingual string resources for Phase 3 features"
```

---

### Task 9: Final Build Verification & Cleanup

**Covers:** [S1, S2, S3, S4, S5, S6, S7, S8]

**Files:**
- Verify all created/modified files compile
- Run full build

**Interfaces:**
- Consumes: all previous tasks
- Produces: passing build, clean codebase

- [ ] **Step 1: Full build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify no warnings about new code**

Check build output for warnings related to new files.

- [ ] **Step 3: Final commit if needed**

```bash
git add -A
git commit -m "feat: Phase 3 Boss & Gamification complete"
```
