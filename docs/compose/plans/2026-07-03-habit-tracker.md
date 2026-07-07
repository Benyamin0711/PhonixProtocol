# Habit Tracker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Habit Tracker feature with numeric daily tracking, streaks, XP rewards, calendar heatmap, and reminder notifications.

**Architecture:** Feature-based Clean Architecture — Habit entity + HabitLog entity in Room DB (v11), HabitDao, HabitRepository, use cases, ViewModel, Compose UI screens, WorkManager for reminders. Follows existing patterns exactly (see CategoryRepository/CategoryDao as reference).

**Tech Stack:** Room, Hilt, Jetpack Compose, WorkManager, Material 3

## Global Constraints

- Dark theme only (#0D0D0D base, #FF6B35 accent)
- All new strings must have both English and Persian locales
- Use Finglish for user communication
- Database is at version 10 — new migration is MIGRATION_10_11
- Follow existing patterns: entity → dao → repository → use case → viewmodel → screen
- `@Inject constructor` for use cases, `@HiltViewModel` for ViewModels
- `collectAsStateWithLifecycle()` for StateFlow collection (no initialValue param)
- `WhileSubscribed(5000)` for ViewModel-backed StateFlows

---

## File Structure

### New Files
- `core/data/db/entity/Habit.kt` — Habit entity
- `core/data/db/entity/HabitLog.kt` — Daily log entity
- `core/data/db/dao/HabitDao.kt` — DAO interface
- `core/domain/repository/HabitRepository.kt` — Repository interface
- `core/data/repository/HabitRepositoryImpl.kt` — Repository implementation
- `core/domain/usecase/habit/CreateHabitUseCase.kt`
- `core/domain/usecase/habit/LogHabitUseCase.kt`
- `core/domain/usecase/habit/GetHabitsWithLogsUseCase.kt`
- `core/domain/usecase/habit/GetHabitStreakUseCase.kt`
- `core/domain/usecase/habit/DeleteHabitUseCase.kt`
- `feature/habits/HabitsViewModel.kt`
- `feature/habits/HabitsScreen.kt`
- `feature/habits/HabitDetailScreen.kt`
- `feature/habits/AddEditHabitSheet.kt`
- `feature/habits/HabitDashboardCard.kt`
- `worker/HabitReminderWorker.kt`

### Modified Files
- `core/data/db/PhoenixDatabase.kt` — add entities + abstract fun, bump to v11
- `di/DatabaseModule.kt` — add MIGRATION_10_11 + provide HabitDao
- `core/navigation/Screen.kt` — add Habits + HabitDetail routes
- `core/navigation/NavGraph.kt` — add habit routes
- `feature/drawer/DrawerScreen.kt` — add onNavigateToHabits callback + menu item
- `feature/dashboard/DashboardScreen.kt` — add HabitDashboardCard
- `res/values/strings.xml` — add habit strings (English)
- `res/values-fa/strings.xml` — add habit strings (Persian)

---

### Task 1: Entities + Migration + DAO + Database Wiring

**Covers:** [S1, S2]

**Files:**
- Create: `core/data/db/entity/Habit.kt`
- Create: `core/data/db/entity/HabitLog.kt`
- Create: `core/data/db/dao/HabitDao.kt`
- Modify: `core/data/db/PhoenixDatabase.kt`
- Modify: `di/DatabaseModule.kt`

**Interfaces:**
- Consumes: (none — first task)
- Produces: `HabitDao` interface with all query methods, `PhoenixDatabase` at v11

- [ ] **Step 1: Create Habit entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val target: Int,
    val unit: String,
    val categoryId: Long? = null,
    val color: String = "#FF6B35",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
```

- [ ] **Step 2: Create HabitLog entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val value: Int,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 3: Create HabitDao**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    fun getLogsForHabit(habitId: Long, startDate: String, endDate: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun getTodayLog(habitId: Long, date: String): HabitLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}
```

- [ ] **Step 4: Update PhoenixDatabase — add entities + abstract fun, bump to v11**

In `PhoenixDatabase.kt`, add to entities array: `Habit::class`, `HabitLog::class`. Change `version = 10` to `version = 11`. Add abstract funs:

```kotlin
abstract fun habitDao(): HabitDao
```

Add imports for Habit, HabitLog, HabitDao.

- [ ] **Step 5: Update DatabaseModule — add MIGRATION_10_11 + provide HabitDao**

Add migration:

```kotlin
private val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS habits " +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "name TEXT NOT NULL, " +
            "description TEXT NOT NULL DEFAULT '', " +
            "target INTEGER NOT NULL, " +
            "unit TEXT NOT NULL, " +
            "categoryId INTEGER, " +
            "color TEXT NOT NULL DEFAULT '#FF6B35', " +
            "reminderEnabled INTEGER NOT NULL DEFAULT 0, " +
            "reminderHour INTEGER NOT NULL DEFAULT 9, " +
            "reminderMinute INTEGER NOT NULL DEFAULT 0, " +
            "createdAt INTEGER NOT NULL, " +
            "isActive INTEGER NOT NULL DEFAULT 1)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS habit_logs " +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "habitId INTEGER NOT NULL, " +
            "date TEXT NOT NULL, " +
            "value INTEGER NOT NULL, " +
            "createdAt INTEGER NOT NULL)"
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_logs_habitId_date ON habit_logs (habitId, date)")
    }
}
```

Add `MIGRATION_10_11` to the `addMigrations()` call. Add provider:

```kotlin
@Provides
fun provideHabitDao(db: PhoenixDatabase): HabitDao = db.habitDao()
```

- [ ] **Step 6: Build to verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/Habit.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/HabitLog.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/HabitDao.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/PhoenixDatabase.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/DatabaseModule.kt
git commit -m "feat(habits): add Habit + HabitLog entities, DAO, and DB migration v11"
```

---

### Task 2: Repository Layer

**Covers:** [S3]

**Files:**
- Create: `core/domain/repository/HabitRepository.kt`
- Create: `core/data/repository/HabitRepositoryImpl.kt`

**Interfaces:**
- Consumes: `HabitDao` (from Task 1)
- Produces: `HabitRepository` interface used by use cases

- [ ] **Step 1: Create HabitRepository interface**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.HabitLog
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun createHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    fun getLogsForDate(date: String): Flow<List<HabitLog>>
    fun getLogsForHabit(habitId: Long, startDate: String, endDate: String): Flow<List<HabitLog>>
    suspend fun getTodayLog(habitId: Long, date: String): HabitLog?
    suspend fun logHabit(habitId: Long, date: String, value: Int): HabitLog
    suspend fun getStreak(habitId: Long): Int
    suspend fun getBestStreak(habitId: Long): Int
}
```

- [ ] **Step 2: Create HabitRepositoryImpl**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.HabitDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.HabitLog
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> = dao.getAllHabits()

    override suspend fun getHabitById(id: Long): Habit? = dao.getHabitById(id)

    override suspend fun createHabit(habit: Habit): Long = dao.insertHabit(habit)

    override suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)

    override suspend fun deleteHabit(habit: Habit) {
        dao.deleteLogsForHabit(habit.id)
        dao.deleteHabit(habit)
    }

    override fun getLogsForDate(date: String): Flow<List<HabitLog>> = dao.getLogsForDate(date)

    override fun getLogsForHabit(habitId: Long, startDate: String, endDate: String): Flow<List<HabitLog>> =
        dao.getLogsForHabit(habitId, startDate, endDate)

    override suspend fun getTodayLog(habitId: Long, date: String): HabitLog? = dao.getTodayLog(habitId, date)

    override suspend fun logHabit(habitId: Long, date: String, value: Int): HabitLog {
        val log = HabitLog(habitId = habitId, date = date, value = value)
        dao.insertOrUpdateLog(log)
        return log
    }

    override suspend fun getStreak(habitId: Long): Int {
        val habit = dao.getHabitById(habitId) ?: return 0
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val log = dao.getTodayLog(habitId, date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            if (log != null && log.value >= habit.target) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    override suspend fun getBestStreak(habitId: Long): Int {
        val habit = dao.getHabitById(habitId) ?: return 0
        val startDate = LocalDate.ofEpochDay(0).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val logs = dao.getLogsForHabit(habitId, startDate, endDate)
        // Collect all dates where target was met
        val completedDates = logs
            .filter { it.value >= habit.target }
            .map { LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) }
            .sorted()
        if (completedDates.isEmpty()) return 0
        var bestStreak = 1
        var currentStreak = 1
        for (i in 1 until completedDates.size) {
            if (completedDates[i].minusDays(1) == completedDates[i - 1]) {
                currentStreak++
                if (currentStreak > bestStreak) bestStreak = currentStreak
            } else {
                currentStreak = 1
            }
        }
        return bestStreak
    }
}
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/HabitRepository.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/HabitRepositoryImpl.kt
git commit -m "feat(habits): add HabitRepository interface and implementation"
```

---

### Task 3: Use Cases

**Covers:** [S4]

**Files:**
- Create: `core/domain/usecase/habit/CreateHabitUseCase.kt`
- Create: `core/domain/usecase/habit/LogHabitUseCase.kt`
- Create: `core/domain/usecase/habit/GetHabitsWithLogsUseCase.kt`
- Create: `core/domain/usecase/habit/GetHabitStreakUseCase.kt`
- Create: `core/domain/usecase/habit/DeleteHabitUseCase.kt`

**Interfaces:**
- Consumes: `HabitRepository`, `StatsRepository` (for XP)
- Produces: Use cases consumed by HabitsViewModel

- [ ] **Step 1: Create CreateHabitUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.HabitRepository
import javax.inject.Inject

class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit): Long {
        return repository.createHabit(habit)
    }
}
```

- [ ] **Step 2: Create LogHabitUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit

import com.benyaminrasouli.phoenixprotocol.core.domain.repository.HabitRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class LogHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(habitId: Long, value: Int) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        repository.logHabit(habitId, today, value)
        val habit = repository.getHabitById(habitId) ?: return
        if (value >= habit.target) {
            statsRepository.addXp(10)
            val streak = repository.getStreak(habitId)
            if (streak == 7) statsRepository.addXp(50)
            if (streak == 30) statsRepository.addXp(200)
        }
    }
}
```

- [ ] **Step 3: Create GetHabitsWithLogsUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.HabitLog
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HabitWithLog(
    val habit: Habit,
    val todayLog: HabitLog?,
    val streak: Int
)

class GetHabitsWithLogsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<List<HabitWithLog>> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return combine(
            repository.getAllHabits(),
            repository.getLogsForDate(today)
        ) { habits, logs ->
            val logsByHabit = logs.groupBy { it.habitId }
            habits.map { habit ->
                HabitWithLog(
                    habit = habit,
                    todayLog = logsByHabit[habit.id]?.firstOrNull(),
                    streak = 0 // computed on demand in ViewModel
                )
            }
        }
    }
}
```

- [ ] **Step 4: Create GetHabitStreakUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit

import com.benyaminrasouli.phoenixprotocol.core.domain.repository.HabitRepository
import javax.inject.Inject

class GetHabitStreakUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: Long): Pair<Int, Int> {
        return Pair(repository.getStreak(habitId), repository.getBestStreak(habitId))
    }
}
```

- [ ] **Step 5: Create DeleteHabitUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.HabitRepository
import javax.inject.Inject

class DeleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        repository.deleteHabit(habit)
    }
}
```

- [ ] **Step 6: Build to verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/habit/
git commit -m "feat(habits): add habit use cases (Create, Log, GetWithLogs, GetStreak, Delete)"
```

---

### Task 4: ViewModel + Habits Screen

**Covers:** [S5]

**Files:**
- Create: `feature/habits/HabitsViewModel.kt`
- Create: `feature/habits/HabitsScreen.kt`

**Interfaces:**
- Consumes: All use cases from Task 3
- Produces: `HabitsScreen` composable used by NavGraph

- [ ] **Step 1: Create HabitsViewModel**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.CreateHabitUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.DeleteHabitUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.GetHabitStreakUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.GetHabitsWithLogsUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.HabitWithLog
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.LogHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HabitsState(
    val habits: List<HabitWithLog> = emptyList(),
    val showAddSheet: Boolean = false,
    val editingHabit: Habit? = null
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    getHabitsWithLogsUseCase: GetHabitsWithLogsUseCase,
    private val createHabitUseCase: CreateHabitUseCase,
    private val logHabitUseCase: LogHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val getHabitStreakUseCase: GetHabitStreakUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HabitsState())
    val state: StateFlow<HabitsState> = _state

    val habitsWithLogs: StateFlow<List<HabitWithLog>> = getHabitsWithLogsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createHabit(habit: Habit) {
        viewModelScope.launch {
            createHabitUseCase(habit)
            _state.value = _state.value.copy(showAddSheet = false, editingHabit = null)
        }
    }

    fun logHabit(habitId: Long, value: Int) {
        viewModelScope.launch {
            logHabitUseCase(habitId, value)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            deleteHabitUseCase(habit)
        }
    }

    fun showAddSheet(habit: Habit? = null) {
        _state.value = _state.value.copy(showAddSheet = true, editingHabit = habit)
    }

    fun hideAddSheet() {
        _state.value = _state.value.copy(showAddSheet = false, editingHabit = null)
    }

    suspend fun getStreak(habitId: Long): Pair<Int, Int> {
        return getHabitStreakUseCase(habitId)
    }
}
```

- [ ] **Step 2: Create HabitsScreen**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.habits

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.HabitWithLog
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val habits by viewModel.habitsWithLogs.collectAsState()
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.habits_title),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            text = "\u2190",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.habits_empty),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habits) { habitWithLog ->
                        HabitCard(
                            habitWithLog = habitWithLog,
                            onIncrement = { viewModel.logHabit(habitWithLog.habit.id, (habitWithLog.todayLog?.value ?: 0) + 1) },
                            onDecrement = {
                                val current = habitWithLog.todayLog?.value ?: 0
                                if (current > 0) viewModel.logHabit(habitWithLog.habit.id, current - 1)
                            },
                            onClick = { onNavigateToDetail(habitWithLog.habit.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.showAddSheet() },
            containerColor = PhoenixOrange,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.habits_add))
        }
    }

    if (state.showAddSheet) {
        AddEditHabitSheet(
            habit = state.editingHabit,
            onDismiss = { viewModel.hideAddSheet() },
            onSave = { viewModel.createHabit(it) }
        )
    }
}

@Composable
private fun HabitCard(
    habitWithLog: HabitWithLog,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit
) {
    val habit = habitWithLog.habit
    val current = habitWithLog.todayLog?.value ?: 0
    val completed = current >= habit.target
    val scope = rememberCoroutineScope()
    var streak by remember { mutableStateOf(0) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) PhoenixOrange.copy(alpha = 0.15f) else SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$current / ${habit.target} ${habit.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Decrease",
                        tint = TextSecondary
                    )
                }
                Text(
                    text = "$current",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (completed) PhoenixOrange else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrement) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Increase",
                        tint = PhoenixOrange
                    )
                }
            }

            if (completed) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PhoenixOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/habits/HabitsViewModel.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/habits/HabitsScreen.kt
git commit -m "feat(habits): add HabitsViewModel and HabitsScreen with daily tracking"
```

---

### Task 5: Add/Edit Sheet + Detail Screen

**Covers:** [S5]

**Files:**
- Create: `feature/habits/AddEditHabitSheet.kt`
- Create: `feature/habits/HabitDetailScreen.kt`

**Interfaces:**
- Consumes: `HabitsViewModel` (from Task 4)
- Produces: `AddEditHabitSheet` and `HabitDetailScreen` composables

- [ ] **Step 1: Create AddEditHabitSheet**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Habit
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitSheet(
    habit: Habit? = null,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf(habit?.name ?: "") }
    var description by remember { mutableStateOf(habit?.description ?: "") }
    var target by remember { mutableStateOf(habit?.target?.toString() ?: "1") }
    var unit by remember { mutableStateOf(habit?.unit ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = if (habit != null) stringResource(R.string.habits_edit) else stringResource(R.string.habits_add_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.habits_name)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = SurfaceDark,
                    focusedLabelColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.habits_description)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = SurfaceDark,
                    focusedLabelColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = target,
                onValueChange = { target = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.habits_target)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = SurfaceDark,
                    focusedLabelColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text(stringResource(R.string.habits_unit)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = SurfaceDark,
                    focusedLabelColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val targetValue = target.toIntOrNull() ?: 1
                    if (name.isNotBlank() && unit.isNotBlank()) {
                        onSave(
                            Habit(
                                id = habit?.id ?: 0,
                                name = name.trim(),
                                description = description.trim(),
                                target = targetValue,
                                unit = unit.trim(),
                                categoryId = habit?.categoryId,
                                color = habit?.color ?: "#FF6B35",
                                reminderEnabled = habit?.reminderEnabled ?: false,
                                reminderHour = habit?.reminderHour ?: 9,
                                reminderMinute = habit?.reminderMinute ?: 0,
                                createdAt = habit?.createdAt ?: System.currentTimeMillis(),
                                isActive = true
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.habits_save),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
```

- [ ] **Step 2: Create HabitDetailScreen**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.HabitLog
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val habits by viewModel.habitsWithLogs.collectAsState()
    val habitWithLog = habits.find { it.habit.id == habitId }
    val habit = habitWithLog?.habit

    var currentStreak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }

    LaunchedEffect(habitId) {
        val (current, best) = viewModel.getStreak(habitId)
        currentStreak = current
        bestStreak = best
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = habit?.name ?: "",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )

            habit?.let { h ->
                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = stringResource(R.string.habits_current_streak),
                        value = "$currentStreak",
                        icon = "🔥"
                    )
                    StatItem(
                        label = stringResource(R.string.habits_best_streak),
                        value = "$bestStreak",
                        icon = "🏆"
                    )
                    StatItem(
                        label = stringResource(R.string.habits_target),
                        value = "${h.target} ${h.unit}",
                        icon = "🎯"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Calendar heatmap
                CalendarHeatmap(
                    habitId = habitId,
                    viewModel = viewModel,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PhoenixOrange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun CalendarHeatmap(
    habitId: Long,
    viewModel: HabitsViewModel,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val daysInMonth = remember { currentMonth.lengthOfMonth() }
    val firstDayOfWeek = remember { currentMonth.atDay(1).dayOfWeek }
    val startOffset = remember { (firstDayOfWeek.value % 7) }

    var logs by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    LaunchedEffect(habitId) {
        val startDate = currentMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDate = currentMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)
        // This would need a method to get logs for the month
        // For now, use empty map — logs will be populated via Flow
    }

    Column(modifier = modifier) {
        Text(
            text = currentMonth.month.name.take(3) + " " + currentMonth.year,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Empty cells for offset
            items(startOffset) {
                Box(modifier = Modifier.aspectRatio(1f))
            }
            // Day cells
            items(daysInMonth) { day ->
                val intensity = logs[day + 1] ?: 0
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                intensity == 0 -> SurfaceDark
                                intensity < 50 -> PhoenixOrange.copy(alpha = 0.3f)
                                intensity < 100 -> PhoenixOrange.copy(alpha = 0.6f)
                                else -> PhoenixOrange
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (intensity > 0) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/habits/AddEditHabitSheet.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/habits/HabitDetailScreen.kt
git commit -m "feat(habits): add AddEditHabitSheet and HabitDetailScreen with calendar heatmap"
```

---

### Task 6: Navigation + Drawer + Dashboard Card

**Covers:** [S8]

**Files:**
- Modify: `core/navigation/Screen.kt`
- Modify: `core/navigation/NavGraph.kt`
- Modify: `feature/drawer/DrawerScreen.kt`
- Modify: `feature/dashboard/DashboardScreen.kt`
- Create: `feature/habits/HabitDashboardCard.kt`

**Interfaces:**
- Consumes: `HabitsScreen`, `HabitDetailScreen` (from Tasks 4-5)
- Produces: Navigation wired, drawer menu item, dashboard card

- [ ] **Step 1: Add routes to Screen.kt**

Add after `Analytics`:

```kotlin
data object Habits : Screen("habits")
data object HabitDetail : Screen("habit_detail/{habitId}") {
    fun createRoute(habitId: Long) = "habit_detail/$habitId"
}
```

- [ ] **Step 2: Add routes to NavGraph.kt**

Add import for HabitsScreen and HabitDetailScreen. Add composable routes:

```kotlin
composable(
    Screen.Habits.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    HabitsScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToDetail = { habitId ->
            navController.navigate(Screen.HabitDetail.createRoute(habitId))
        }
    )
}
composable(
    Screen.HabitDetail.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) { backStackEntry ->
    val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull() ?: return@composable
    HabitDetailScreen(
        habitId = habitId,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 3: Add drawer menu item**

In `DrawerScreen.kt`:
- Add `onNavigateToHabits: () -> Unit = {}` parameter
- Add menu item after existing items:

```kotlin
DrawerMenuItem(
    icon = Icons.Filled.Repeat,
    label = stringResource(R.string.drawer_habits),
    onClick = onNavigateToHabits
)
```

Add import: `import androidx.compose.material.icons.filled.Repeat`

- [ ] **Step 4: Wire drawer in DashboardScreen**

In `DashboardScreen.kt`:
- Add to DrawerScreen call:

```kotlin
onNavigateToHabits = {
    scope.launch { drawerState.close() }
    navController.navigate(Screen.Habits.route)
},
```

- [ ] **Step 5: Create HabitDashboardCard**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.habit.HabitWithLog
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun HabitDashboardCard(
    habits: List<HabitWithLog>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completed = habits.count { (it.todayLog?.value ?: 0) >= it.habit.target }
    val total = habits.size
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.habits_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$completed/$total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PhoenixOrange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PhoenixOrange,
                trackColor = SurfaceDark
            )
        }
    }
}
```

- [ ] **Step 6: Add HabitDashboardCard to DashboardScreen**

In `DashboardScreen.kt`:
- Add import for `HabitDashboardCard` and `HabitsViewModel`
- Add state collection:

```kotlin
val habitsViewModel: HabitsViewModel = hiltViewModel()
val habits by habitsViewModel.habitsWithLogs.collectAsState()
```

- Add card after DailyChallengeCard:

```kotlin
Spacer(modifier = Modifier.height(16.dp))

HabitDashboardCard(
    habits = habits,
    onClick = { navController.navigate(Screen.Habits.route) },
    modifier = Modifier.padding(horizontal = 16.dp)
)
```

- [ ] **Step 7: Build to verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/Screen.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/NavGraph.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/drawer/DrawerScreen.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/habits/HabitDashboardCard.kt
git commit -m "feat(habits): wire navigation, drawer menu item, and dashboard card"
```

---

### Task 7: String Resources

**Covers:** [S9]

**Files:**
- Modify: `res/values/strings.xml`
- Modify: `res/values-fa/strings.xml`

**Interfaces:**
- Consumes: (none)
- Produces: String resources used by all habit UI files

- [ ] **Step 1: Add English strings**

Add before `</resources>` in `res/values/strings.xml`:

```xml
<!-- Habits -->
<string name="habits_title">Habits</string>
<string name="habits_empty">No habits yet. Tap + to create one.</string>
<string name="habits_add">Add Habit</string>
<string name="habits_add_title">New Habit</string>
<string name="habits_edit">Edit Habit</string>
<string name="habits_name">Name</string>
<string name="habits_description">Description</string>
<string name="habits_target">Daily Target</string>
<string name="habits_unit">Unit (e.g. glasses, pages)</string>
<string name="habits_save">Save</string>
<string name="habits_current_streak">Current</string>
<string name="habits_best_streak">Best</string>
<string name="drawer_habits">Habits</string>
```

- [ ] **Step 2: Add Persian strings**

Add before `</resources>` in `res/values-fa/strings.xml`:

```xml
<!-- Habits -->
<string name="habits_title">عادات</string>
<string name="habits_empty">هنوز عادتی ندارید. + را بزنید تا یکی بسازید.</string>
<string name="habits_add">افزودن عادت</string>
<string name="habits_add_title">عادت جدید</string>
<string name="habits_edit">ویرایش عادت</string>
<string name="habits_name">نام</string>
<string name="habits_description">توضیحات</string>
<string name="habits_target">هدف روزانه</string>
<string name="habits_unit">واحد (مثلاً لیوان، صفحه)</string>
<string name="habits_save">ذخیره</string>
<string name="habits_current_streak">فعلی</string>
<string name="habits_best_streak">بهترین</string>
<string name="drawer_habits">عادات</string>
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-fa/strings.xml
git commit -m "feat(habits): add English and Persian string resources"
```

---

### Task 8: Build Verification

**Covers:** All

**Files:** (none — verification only)

**Interfaces:**
- Consumes: All previous tasks
- Produces: Confirmed working build

- [ ] **Step 1: Full clean build**

Run: `./gradlew clean assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Final commit if any fixups needed**

```bash
git add -A
git commit -m "feat(habits): Phase 12 complete - Habit Tracker with daily tracking, streaks, and heatmap"
```
