# Shadow System UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the shadow system with a dedicated screen, task-completion-based recovery, XP penalties at high shadow levels, and shadow change history logging.

**Architecture:** New ShadowLog entity for history tracking. ShadowRepository manages shadow state and logs. StatsRepository gains decreaseShadowLevel and XP penalty logic. CompleteTaskUseCase triggers shadow reduction on task completion.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Material 3

## Global Constraints
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- All UI text via `stringResource()` — never hardcode
- Bilingual: English + Persian (RTL)
- Feature-based Clean Architecture with Hilt DI
- Build verified: `./gradlew assembleDebug` after each task
- Database at version 9 → target version 10
- All new strings must have both English and Persian locales

---

## STEP 1 — Database Layer

### Task 1: ShadowLog Entity + DAO + Migration 9→10

**Covers:** [S4]

**Files:**
- Create: `app/src/main/java/.../core/data/db/entity/ShadowLog.kt`
- Create: `app/src/main/java/.../core/data/db/dao/ShadowLogDao.kt`
- Modify: `app/src/main/java/.../core/data/db/PhoenixDatabase.kt`
- Modify: `app/src/main/java/.../di/DatabaseModule.kt`

**Interfaces:**
- Consumes: existing database (version 9)
- Produces: ShadowLog entity, ShadowLogDao, migration 9→10

- [ ] **Step 1: Create ShadowLog entity**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/ShadowLog.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shadow_log")
data class ShadowLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val amount: Int,
    val description: String
)
```

- [ ] **Step 2: Create ShadowLogDao**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/ShadowLogDao.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ShadowLogDao {
    @Insert
    suspend fun insert(log: ShadowLog)

    @Query("SELECT * FROM shadow_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 20): Flow<List<ShadowLog>>
}
```

- [ ] **Step 3: Update PhoenixDatabase**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/PhoenixDatabase.kt`:
- Add `ShadowLog::class` to entities array
- Change version 9 → 10
- Add `abstract fun shadowLogDao(): ShadowLogDao`
- Add imports for ShadowLog and ShadowLogDao

- [ ] **Step 4: Add migration to DatabaseModule**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/DatabaseModule.kt`:
- Add `import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.ShadowLogDao`
- Add migration:
```kotlin
private val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS shadow_log (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, action TEXT NOT NULL, amount INTEGER NOT NULL, description TEXT NOT NULL)")
    }
}
```
- Add to `.addMigrations(...)` chain (after MIGRATION_8_9)
- Add provider:
```kotlin
@Provides
fun provideShadowLogDao(db: PhoenixDatabase): ShadowLogDao = db.shadowLogDao()
```

- [ ] **Step 5: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 2 — Bilingual Strings

### Task 2: All Phase 9 Strings

**Covers:** [S9]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

- [ ] **Step 1: Add English strings**

Edit `app/src/main/res/values/strings.xml` — add before `</resources>`:
```xml
<!-- Shadow System -->
<string name="shadow_title">Shadow</string>
<string name="shadow_level">Shadow Level</string>
<string name="shadow_tier_safe">Safe</string>
<string name="shadow_tier_warning">Warning</string>
<string name="shadow_tier_critical">Critical</string>
<string name="shadow_tier_corrupted">Corrupted</string>
<string name="shadow_xp_penalty">XP Penalty</string>
<string name="shadow_no_penalty">No penalty</string>
<string name="shadow_recovery_tips">Recovery Tips</string>
<string name="shadow_recovery_tip_1">Complete tasks to reduce shadow</string>
<string name="shadow_recovery_tip_2">Hard tasks reduce shadow by 2</string>
<string name="shadow_recovery_tip_3">Priority tasks reduce shadow by 3</string>
<string name="shadow_history">Shadow History</string>
<string name="shadow_action_skip">Task skipped</string>
<string name="shadow_action_cancel">Task cancelled</string>
<string name="shadow_action_streak_break">Streak broken</string>
<string name="shadow_action_complete">Task completed</string>
<string name="drawer_shadow">Shadow</string>
```

- [ ] **Step 2: Add Persian strings**

Edit `app/src/main/res/values-fa/strings.xml` — add before `</resources>`:
```xml
<!-- Shadow System -->
<string name="shadow_title">سایه</string>
<string name="shadow_level">سطح سایه</string>
<string name="shadow_tier_safe">ایمن</string>
<string name="shadow_tier_warning">هشدار</string>
<string name="shadow_tier_critical">بحرانی</string>
<string name="shadow_tier_corrupted">آلوده</string>
<string name="shadow_xp_penalty">جریمه امتیاز</string>
<string name="shadow_no_penalty">بدون جریمه</string>
<string name="shadow_recovery_tips">راهنمای بازیابی</string>
<string name="shadow_recovery_tip_1">تسک‌ها رو کامل کن تا سایه کم بشه</string>
<string name="shadow_recovery_tip_2">تسک‌های سخت ۲ واحد کم می‌کنن</string>
<string name="shadow_recovery_tip_3">تسک‌های اولویت‌دار ۳ واحد کم می‌کنن</string>
<string name="shadow_history">تاریخچه سایه</string>
<string name="shadow_action_skip">تسک رد شد</string>
<string name="shadow_action_cancel">تسک لغو شد</string>
<string name="shadow_action_streak_break">سری شکسته شد</string>
<string name="shadow_action_complete">تسک کامل شد</string>
<string name="drawer_shadow">سایه</string>
```

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 3 — Shadow Repository Layer

### Task 3: ShadowRepository Interface + Impl + StatsRepository Updates

**Covers:** [S3, S4, S7]

**Files:**
- Create: `app/src/main/java/.../core/domain/repository/ShadowRepository.kt`
- Create: `app/src/main/java/.../core/data/repository/ShadowRepositoryImpl.kt`
- Modify: `app/src/main/java/.../core/domain/repository/StatsRepository.kt`
- Modify: `app/src/main/java/.../core/data/repository/StatsRepositoryImpl.kt`
- Modify: `app/src/main/java/.../di/AppModule.kt`

**Interfaces:**
- Consumes: ShadowLogDao, UserStatsDao
- Produces: ShadowRepository interface, ShadowRepositoryImpl, updated StatsRepository

- [ ] **Step 1: Create ShadowRepository interface**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/ShadowRepository.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import kotlinx.coroutines.flow.Flow

interface ShadowRepository {
    fun getShadowLevel(): Flow<Int>
    fun getShadowTier(): Flow<String>
    fun getXpPenaltyPercent(): Flow<Int>
    fun getRecentLogs(limit: Int): Flow<List<ShadowLog>>
    suspend fun logShadowChange(action: String, amount: Int, description: String)
    suspend fun decreaseShadow(amount: Int)
}
```

- [ ] **Step 2: Create ShadowRepositoryImpl**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/ShadowRepositoryImpl.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.ShadowLogDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShadowRepositoryImpl @Inject constructor(
    private val shadowLogDao: ShadowLogDao,
    private val userStatsDao: UserStatsDao
) : ShadowRepository {

    override fun getShadowLevel(): Flow<Int> {
        return userStatsDao.getStats().map { it?.shadowLevel ?: 0 }
    }

    override fun getShadowTier(): Flow<String> {
        return getShadowLevel().map { level ->
            when {
                level >= 90 -> "CORRUPTED"
                level >= 60 -> "CRITICAL"
                level >= 30 -> "WARNING"
                else -> "SAFE"
            }
        }
    }

    override fun getXpPenaltyPercent(): Flow<Int> {
        return getShadowLevel().map { level ->
            when {
                level >= 90 -> 50
                level >= 60 -> 25
                level >= 30 -> 10
                else -> 0
            }
        }
    }

    override fun getRecentLogs(limit: Int): Flow<List<ShadowLog>> {
        return shadowLogDao.getRecentLogs(limit)
    }

    override suspend fun logShadowChange(action: String, amount: Int, description: String) {
        shadowLogDao.insert(ShadowLog(action = action, amount = amount, description = description))
    }

    override suspend fun decreaseShadow(amount: Int) {
        val current = userStatsDao.getStatsOnce() ?: return
        val newLevel = (current.shadowLevel - amount).coerceAtLeast(0)
        userStatsDao.updateStats(current.copy(shadowLevel = newLevel))
    }
}
```

- [ ] **Step 3: Add decreaseShadowLevel to StatsRepository interface**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/StatsRepository.kt` — add:
```kotlin
suspend fun decreaseShadowLevel(amount: Int)
```

- [ ] **Step 4: Add decreaseShadowLevel to StatsRepositoryImpl + XP penalty**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/StatsRepositoryImpl.kt`:

Add `decreaseShadowLevel`:
```kotlin
override suspend fun decreaseShadowLevel(amount: Int) {
    val current = dao.getStatsOnce() ?: return
    val newLevel = (current.shadowLevel - amount).coerceAtLeast(0)
    dao.updateStats(current.copy(shadowLevel = newLevel))
}
```

Modify `addXp` to apply shadow penalty:
```kotlin
override suspend fun addXp(amount: Int) {
    val current = dao.getStatsOnce() ?: return
    val penaltyPercent = when {
        current.shadowLevel >= 90 -> 50
        current.shadowLevel >= 60 -> 25
        current.shadowLevel >= 30 -> 10
        else -> 0
    }
    val actualXp = amount * (100 - penaltyPercent) / 100
    val newXp = current.xp + actualXp
    val newLevel = calculateLevel(newXp)
    val newRank = com.benyaminrasouli.phoenixprotocol.core.domain.model.Rank.forLevel(newLevel)
    dao.updateStats(current.copy(
        xp = newXp,
        level = newLevel,
        rank = newRank.name
    ))
}
```

- [ ] **Step 5: Update AppModule**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/AppModule.kt` — add:
```kotlin
import com.benyaminrasouli.phoenixprotocol.core.data.repository.ShadowRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
```
```kotlin
@Binds
@Singleton
abstract fun bindShadowRepository(impl: ShadowRepositoryImpl): ShadowRepository
```

- [ ] **Step 6: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 4 — Shadow Reduction on Task Completion

### Task 4: CompleteTaskUseCase Shadow Integration

**Covers:** [S3]

**Files:**
- Modify: `app/src/main/java/.../core/domain/usecase/CompleteTaskUseCase.kt`

**Interfaces:**
- Consumes: ShadowRepository, Task entity (difficulty, isPriority)
- Produces: Updated CompleteTaskUseCase with shadow reduction

- [ ] **Step 1: Update CompleteTaskUseCase**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/CompleteTaskUseCase.kt`:

Add import:
```kotlin
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
```

Add ShadowRepository to constructor:
```kotlin
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val trackDailyChallengeUseCase: TrackDailyChallengeUseCase,
    private val shadowRepository: ShadowRepository
)
```

Add shadow reduction after task completion:
```kotlin
suspend operator fun invoke(task: Task) {
    taskRepository.completeTask(task.id)
    statsRepository.addXp(task.xpValue)
    statsRepository.increasePhoenixEnergy(5)
    statsRepository.incrementCompletedTasks()

    // Shadow reduction
    val shadowReduction = when {
        task.isPriority -> 3
        task.difficulty == "HARD" || task.difficulty == "EXTREME" -> 2
        else -> 1
    }
    shadowRepository.decreaseShadow(shadowReduction)
    shadowRepository.logShadowChange(
        action = "TASK_COMPLETE",
        amount = -shadowReduction,
        description = "Completed: ${task.title}"
    )

    // Track daily challenge progress
    val todayChallenges = dailyChallengeRepository.getTodayChallengesOnce()
    trackDailyChallengeUseCase(
        challenges = todayChallenges,
        completedTaskType = task.difficulty,
        earnedXp = task.xpValue
    )
}
```

- [ ] **Step 2: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 5 — Shadow Logging on Increase

### Task 5: Log Shadow Increases

**Covers:** [S4]

**Files:**
- Modify: `app/src/main/java/.../core/data/repository/StatsRepositoryImpl.kt`

**Interfaces:**
- Consumes: ShadowLogDao (new dependency)
- Produces: Updated increaseShadowLevel with logging

- [ ] **Step 1: Update StatsRepositoryImpl to log shadow changes**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/StatsRepositoryImpl.kt`:

Add ShadowLogDao to constructor:
```kotlin
class StatsRepositoryImpl @Inject constructor(
    private val dao: UserStatsDao,
    private val shadowLogDao: com.benyaminrasouli.phoenixprotocol.core.data.db.dao.ShadowLogDao
) : StatsRepository {
```

Update `increaseShadowLevel`:
```kotlin
override suspend fun increaseShadowLevel(amount: Int) {
    val current = dao.getStatsOnce() ?: return
    dao.updateStats(current.copy(
        shadowLevel = current.shadowLevel + amount
    ))
    val action = when (amount) {
        1 -> "SKIP"
        2 -> "STREAK_BREAK"
        else -> "CANCEL"
    }
    val description = when (action) {
        "SKIP" -> "Task skipped"
        "STREAK_BREAK" -> "Streak broken"
        else -> "Task cancelled"
    }
    shadowLogDao.insert(
        com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog(
            action = action,
            amount = amount,
            description = description
        )
    )
}
```

- [ ] **Step 2: Update DatabaseModule provider**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/DatabaseModule.kt` — update `provideStatsRepository`:
```kotlin
@Provides
fun provideStatsRepository(
    db: PhoenixDatabase,
    shadowLogDao: ShadowLogDao
): StatsRepository = StatsRepositoryImpl(db.userStatsDao(), shadowLogDao)
```

(Note: StatsRepositoryImpl is provided via @Provides, not @Binds, since it now has two dependencies)

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 6 — UI Layer

### Task 6: ShadowViewModel + ShadowScreen

**Covers:** [S5, S6]

**Files:**
- Create: `app/src/main/java/.../feature/shadow/ShadowViewModel.kt`
- Create: `app/src/main/java/.../feature/shadow/ShadowScreen.kt`

**Interfaces:**
- Consumes: ShadowRepository
- Produces: ShadowScreen composable

- [ ] **Step 1: Create ShadowViewModel**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/shadow/ShadowViewModel.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.shadow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShadowViewModel @Inject constructor(
    shadowRepository: ShadowRepository
) : ViewModel() {

    val shadowLevel: StateFlow<Int> = shadowRepository.getShadowLevel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val shadowTier: StateFlow<String> = shadowRepository.getShadowTier()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SAFE")

    val xpPenalty: StateFlow<Int> = shadowRepository.getXpPenaltyPercent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentLogs: StateFlow<List<ShadowLog>> = shadowRepository.getRecentLogs(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

- [ ] **Step 2: Create ShadowScreen**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/shadow/ShadowScreen.kt`:

The screen should include:
- TopAppBar with back arrow
- Large circular shadow level indicator (0–100) with darkening gradient
- Tier badge (Safe/Warning/Critical/Corrupted) with appropriate color
- XP penalty display
- Recovery tips section (3 tips)
- Shadow history log (LazyColumn of recent changes)

Use existing theme colors: BackgroundDark, SurfaceDark, PhoenixOrange, TextSecondary.
Follow existing screen patterns (e.g., StatisticsScreen, AchievementScreen).

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 7 — Navigation Integration

### Task 7: Screen Route + NavGraph + Drawer + Dashboard Wiring

**Covers:** [S8]

**Files:**
- Modify: `app/src/main/java/.../core/navigation/Screen.kt`
- Modify: `app/src/main/java/.../core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/.../feature/drawer/DrawerScreen.kt`
- Modify: `app/src/main/java/.../feature/dashboard/DashboardScreen.kt`

- [ ] **Step 1: Add Screen.Shadow route**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/Screen.kt` — add:
```kotlin
data object Shadow : Screen("shadow")
```

- [ ] **Step 2: Add NavGraph composable**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/NavGraph.kt`:
- Add import: `import com.benyaminrasouli.phoenixprotocol.feature.shadow.ShadowScreen`
- Add composable:
```kotlin
composable(
    Screen.Shadow.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    ShadowScreen(navController = navController)
}
```

- [ ] **Step 3: Add Shadow to DrawerScreen**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/drawer/DrawerScreen.kt`:
- Add import: `import androidx.compose.material.icons.filled.Shield`
- Add parameter: `onNavigateToShadow: () -> Unit = {},`
- Add menu item below Statistics:
```kotlin
DrawerMenuItem(
    icon = Icons.Filled.Shield,
    label = stringResource(R.string.drawer_shadow),
    onClick = onNavigateToShadow
)
```

- [ ] **Step 4: Wire callback in DashboardScreen**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt`:
- Add `onNavigateToShadow` callback to DrawerScreen call:
```kotlin
onNavigateToShadow = {
    navController.navigate(Screen.Shadow.route)
},
```

- [ ] **Step 5: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 8 — Final Verification

### Task 8: Full Build Verification

**Covers:** [S1, S2, S3, S4, S5, S6, S7, S8, S9, S10]

- [ ] **Step 1: Full build verification**

Run: `.\gradlew clean assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Final commit**

```bash
git add -A
git commit -m "feat: Phase 9 complete - Shadow System UI with recovery, penalties, and history"
```
