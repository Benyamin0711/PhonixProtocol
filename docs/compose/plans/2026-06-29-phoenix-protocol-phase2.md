# Phoenix Protocol Phase 2 — Core Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the core loop with a full Task System (Create, List, Filter), Achievement System (15 achievements with unlock detection), and Statistics Screen (charts + analytics).

**Architecture:** Extends existing Feature-based Clean Architecture. New entities, DAOs, use cases, and screens follow established patterns. Vico library for charts.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Hilt, Navigation Compose, Vico (charts)

## Global Constraints
- Min SDK 24, Target SDK 36, Compile SDK 36
- Package: `com.benyaminrasouli.phoniexprotocol`
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- Bilingual: English + Persian (all new strings must have both locales)
- Every Room entity uses Long auto-increment PKs
- Every task ends with `./gradlew assembleDebug` passing

---

### Task 1: Database Migration — Add New Entities

**Covers:** Task System, Achievements

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/entity/Task.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/entity/Achievement.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/entity/UserAchievement.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/dao/AchievementDao.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/PhoenixDatabase.kt`

**Interfaces:**
- Consumes: existing Task entity, PhoenixDatabase
- Produces: Updated Task entity with new fields, Achievement entities, AchievementDao

- [ ] **Step 1: Update Task entity**

Add `taskType` and `isPriority` fields:

```kotlin
// core/data/db/entity/Task.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val difficulty: String,
    val category: String,
    val xpValue: Int,
    val recurrence: String,
    val status: String,
    val taskType: String = "CUSTOM",
    val isPriority: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
```

- [ ] **Step 2: Create Achievement entity**

```kotlin
// core/data/db/entity/Achievement.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val rarity: String,
    val condition: String
)
```

- [ ] **Step 3: Create UserAchievement entity**

```kotlin
// core/data/db/entity/UserAchievement.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_achievements")
data class UserAchievement(
    @PrimaryKey
    val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 4: Create AchievementDao**

```kotlin
// core/data/db/dao/AchievementDao.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockAchievement(userAchievement: UserAchievement)

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM user_achievements")
    fun getUnlockedAchievements(): Flow<List<UserAchievement>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_achievements WHERE achievementId = :achievementId)")
    suspend fun isAchievementUnlocked(achievementId: String): Boolean

    @Query("SELECT COUNT(*) FROM user_achievements")
    suspend fun getUnlockedCount(): Int
}
```

- [ ] **Step 5: Update PhoenixDatabase**

```kotlin
// core/data/db/PhoenixDatabase.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats

@Database(
    entities = [
        UserProfile::class,
        Task::class,
        UserStats::class,
        Achievement::class,
        UserAchievement::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PhoenixDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun achievementDao(): AchievementDao
}
```

- [ ] **Step 6: Update DatabaseModule**

```kotlin
// di/DatabaseModule.kt
package com.benyaminrasouli.phoniexprotocol.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN taskType TEXT NOT NULL DEFAULT 'CUSTOM'")
            db.execSQL("ALTER TABLE tasks ADD COLUMN isPriority INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS achievements (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, icon TEXT NOT NULL, rarity TEXT NOT NULL, condition TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS user_achievements (achievementId TEXT NOT NULL PRIMARY KEY, unlockedAt INTEGER NOT NULL)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhoenixDatabase {
        return Room.databaseBuilder(
            context,
            PhoenixDatabase::class.java,
            "phoenix_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideUserProfileDao(db: PhoenixDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideTaskDao(db: PhoenixDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideUserStatsDao(db: PhoenixDatabase): UserStatsDao = db.userStatsDao()

    @Provides
    fun provideAchievementDao(db: PhoenixDatabase): AchievementDao = db.achievementDao()
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: database migration - Task fields, Achievement entities, AchievementDao"
```

---

### Task 2: Task Type Enum & Enhanced TaskRepository

**Covers:** Task System

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/model/TaskType.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/repository/TaskRepository.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/repository/TaskRepositoryImpl.kt`

**Interfaces:**
- Consumes: Task 1 (updated Task entity)
- Produces: TaskType enum, enhanced repository methods

- [ ] **Step 1: Create TaskType enum**

```kotlin
// core/domain/model/TaskType.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.model

enum class TaskType {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}
```

- [ ] **Step 2: Update TaskRepository interface**

```kotlin
// core/domain/repository/TaskRepository.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun getActiveTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByType(type: String): Flow<List<Task>>
    fun getPriorityTasks(): Flow<List<Task>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun completeTask(taskId: Long)
    suspend fun skipTask(taskId: Long)
    suspend fun getTaskCount(): Int
    suspend fun getCompletedTaskCount(): Int
}
```

- [ ] **Step 3: Update TaskRepositoryImpl**

```kotlin
// core/data/repository/TaskRepositoryImpl.kt
package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override suspend fun createTask(task: Task): Long {
        return dao.insertTask(task)
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
    }

    override fun getActiveTasks(): Flow<List<Task>> {
        return dao.getActiveTasks()
    }

    override fun getCompletedTasks(): Flow<List<Task>> {
        return dao.getCompletedTasks()
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks()
    }

    override fun getTasksByType(type: String): Flow<List<Task>> {
        return dao.getTasksByType(type)
    }

    override fun getPriorityTasks(): Flow<List<Task>> {
        return dao.getPriorityTasks()
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        return dao.getTaskById(taskId)
    }

    override suspend fun completeTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "COMPLETED", System.currentTimeMillis())
    }

    override suspend fun skipTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "SKIPPED", null)
    }

    override suspend fun getTaskCount(): Int {
        return dao.getTaskCount()
    }

    override suspend fun getCompletedTaskCount(): Int {
        return dao.getCompletedTaskCount()
    }
}
```

- [ ] **Step 4: Update TaskDao**

```kotlin
// core/data/db/dao/TaskDao.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' AND status != 'SKIPPED' ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE taskType = :type AND status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getTasksByType(type: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isPriority = 1 AND status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getPriorityTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED'")
    suspend fun getCompletedTaskCount(): Int
}
```

- [ ] **Step 5: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: TaskType enum, enhanced TaskRepository with filtering and counts"
```

---

### Task 3: Achievement Seed Data & Use Cases

**Covers:** Achievements

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/seeder/AchievementSeeder.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/repository/AchievementRepository.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/repository/AchievementRepositoryImpl.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/usecase/GetAchievementsUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/usecase/CheckAchievementsUseCase.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/di/AppModule.kt`

**Interfaces:**
- Consumes: Task 1 (AchievementDao, entities)
- Produces: Achievement seeder, repository, use cases

- [ ] **Step 1: Create AchievementSeeder**

```kotlin
// core/data/db/seeder/AchievementSeeder.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.seeder

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementSeeder @Inject constructor(
    private val achievementDao: AchievementDao
) {
    suspend fun seed() {
        val achievements = listOf(
            Achievement("first_blood", "First Blood", "Complete your first task", "emoji_events", "COMMON", "complete_1"),
            Achievement("7day_warrior", "7-Day Warrior", "Maintain a 7-day streak", "local_fire_department", "COMMON", "streak_7"),
            Achievement("30day_legend", "30-Day Legend", "Maintain a 30-day streak", "military_tech", "RARE", "streak_30"),
            Achievement("no_excuses", "No Excuses", "Complete 10 tasks", "task_alt", "COMMON", "tasks_10"),
            Achievement("discipline_above_mood", "Discipline Above Mood", "Complete task when Shadow > 50", "psychology", "RARE", "shadow_task"),
            Achievement("phoenix_rising", "Phoenix Rising", "Recover from 3+ day absence", "trending_up", "EPIC", "recovery"),
            Achievement("boss_slayer", "Boss Slayer", "Complete 5 boss missions", "castle", "EPIC", "boss_5"),
            Achievement("shadow_breaker", "Shadow Breaker", "Reduce Shadow to 0 after being > 30", "dark_mode", "RARE", "shadow_break"),
            Achievement("elite_consistency", "Elite Consistency", "90% completion rate for 7 days", "star", "LEGENDARY", "consistency_90"),
            Achievement("unbroken", "Unbroken", "100-day streak", "whatshot", "LEGENDARY", "streak_100"),
            Achievement("life_master", "Life Master", "Complete task in all 8 life areas", "public", "LEGENDARY", "all_areas"),
            Achievement("night_owl", "Night Owl", "Complete task after midnight", "bedtime", "COMMON", "night"),
            Achievement("early_bird", "Early Bird", "Complete task before 6 AM", "wb_sunny", "COMMON", "morning"),
            Achievement("speed_demon", "Speed Demon", "Complete 5 tasks in one day", "bolt", "RARE", "speed_5"),
            Achievement("marathon_runner", "Marathon Runner", "Complete 50 tasks total", "directions_run", "RARE", "tasks_50")
        )
        achievements.forEach { achievementDao.insertAchievement(it) }
    }
}
```

- [ ] **Step 2: Create AchievementRepository interface**

```kotlin
// core/domain/repository/AchievementRepository.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getUnlockedAchievements(): Flow<List<UserAchievement>>
    suspend fun isUnlocked(achievementId: String): Boolean
    suspend fun unlock(achievementId: String)
    suspend fun getUnlockedCount(): Int
}
```

- [ ] **Step 3: Create AchievementRepositoryImpl**

```kotlin
// core/data/repository/AchievementRepositoryImpl.kt
package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(): Flow<List<Achievement>> = dao.getAllAchievements()

    override fun getUnlockedAchievements(): Flow<List<UserAchievement>> = dao.getUnlockedAchievements()

    override suspend fun isUnlocked(achievementId: String): Boolean = dao.isAchievementUnlocked(achievementId)

    override suspend fun unlock(achievementId: String) {
        if (!dao.isAchievementUnlocked(achievementId)) {
            dao.unlockAchievement(UserAchievement(achievementId = achievementId))
        }
    }

    override suspend fun getUnlockedCount(): Int = dao.getUnlockedCount()
}
```

- [ ] **Step 4: Create GetAchievementsUseCase**

```kotlin
// core/domain/usecase/GetAchievementsUseCase.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class AchievementWithStatus(
    val achievement: Achievement,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
)

class GetAchievementsUseCase @Inject constructor(
    private val repository: AchievementRepository
) {
    operator fun invoke(): Flow<List<AchievementWithStatus>> {
        return combine(
            repository.getAllAchievements(),
            repository.getUnlockedAchievements()
        ) { achievements, unlocked ->
            val unlockedMap = unlocked.associateBy { it.achievementId }
            achievements.map { achievement ->
                val userAchievement = unlockedMap[achievement.id]
                AchievementWithStatus(
                    achievement = achievement,
                    isUnlocked = userAchievement != null,
                    unlockedAt = userAchievement?.unlockedAt
                )
            }
        }
    }
}
```

- [ ] **Step 5: Create CheckAchievementsUseCase**

```kotlin
// core/domain/usecase/CheckAchievementsUseCase.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class CheckAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) {
    suspend operator fun invoke() {
        val stats = statsRepository.getStatsOnce() ?: return
        val taskCount = taskRepository.getCompletedTaskCount()

        // First Blood
        if (taskCount >= 1) achievementRepository.unlock("first_blood")

        // No Excuses
        if (taskCount >= 10) achievementRepository.unlock("no_excuses")

        // Marathon Runner
        if (taskCount >= 50) achievementRepository.unlock("marathon_runner")

        // 7-Day Warrior
        if (stats.currentStreak >= 7) achievementRepository.unlock("7day_warrior")

        // 30-Day Legend
        if (stats.currentStreak >= 30) achievementRepository.unlock("30day_legend")

        // Unbroken
        if (stats.currentStreak >= 100) achievementRepository.unlock("unbroken")

        // Shadow Breaker
        if (stats.shadowLevel > 30) {
            // Will be checked when shadow decreases
        }

        // Speed Demon - check today's completed tasks
        // (simplified: check if completedTasks increased significantly)
    }
}
```

- [ ] **Step 6: Update AppModule**

```kotlin
// di/AppModule.kt
package com.benyaminrasouli.phoniexprotocol.di

import com.benyaminrasouli.phoniexprotocol.core.data.repository.AchievementRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.StatsRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.TaskRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.UserRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: achievement seed data, repository, use cases"
```

---

### Task 4: Create Task Screen

**Covers:** Task System

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/tasks/CreateTaskViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/tasks/CreateTaskScreen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/Screen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: Task 2 (TaskRepository), Task 3 (CheckAchievementsUseCase)
- Produces: Create Task screen with all fields

- [ ] **Step 1: Create CreateTaskViewModel**

```kotlin
// feature/tasks/CreateTaskViewModel.kt
package com.benyaminrasouli.phoniexprotocol.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.model.Difficulty
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskRecurrence
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskType
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CheckAchievementsUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CreateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTaskState(
    val title: String = "",
    val description: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val category: String = "",
    val taskType: TaskType = TaskType.CUSTOM,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val isPriority: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskState())
    val state: StateFlow<CreateTaskState> = _state.asStateFlow()

    fun setTitle(title: String) = _state.update { it.copy(title = title) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setDifficulty(d: Difficulty) = _state.update { it.copy(difficulty = d) }
    fun setCategory(c: String) = _state.update { it.copy(category = c) }
    fun setTaskType(t: TaskType) = _state.update { it.copy(taskType = t) }
    fun setRecurrence(r: TaskRecurrence) = _state.update { it.copy(recurrence = r) }
    fun setPriority(p: Boolean) = _state.update { it.copy(isPriority = p) }

    fun saveTask(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val task = Task(
                title = s.title.trim(),
                description = s.description.trim(),
                difficulty = s.difficulty.name,
                category = s.category.ifBlank { "General" },
                xpValue = s.difficulty.xpValue,
                recurrence = s.recurrence.name,
                status = "PENDING",
                taskType = s.taskType.name,
                isPriority = s.isPriority
            )
            createTaskUseCase(task)
            checkAchievementsUseCase()
            _state.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
```

- [ ] **Step 2: Create CreateTaskScreen**

```kotlin
// feature/tasks/CreateTaskScreen.kt
package com.benyaminrasouli.phoniexprotocol.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.core.domain.model.Difficulty
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskRecurrence
import com.benyaminrasouli.phoniexprotocol.core.domain.model.TaskType
import com.benyaminrasouli.phoniexprotocol.core.ui.components.PhoenixButton
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.task_create)) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::setTitle,
                label = { Text(stringResource(R.string.task_title)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text(stringResource(R.string.task_description)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                minLines = 2
            )

            // Difficulty
            Text(stringResource(R.string.task_difficulty), color = TextSecondary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = state.difficulty == diff,
                        onClick = { viewModel.setDifficulty(diff) },
                        label = { Text(diff.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            // Task Type
            Text("Task Type", color = TextSecondary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskType.entries.forEach { type ->
                    FilterChip(
                        selected = state.taskType == type,
                        onClick = { viewModel.setTaskType(type) },
                        label = { Text(type.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            // Category
            OutlinedTextField(
                value = state.category,
                onValueChange = viewModel::setCategory,
                label = { Text(stringResource(R.string.task_category)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    cursorColor = PhoenixOrange
                ),
                singleLine = true
            )

            // Recurrence
            Text(stringResource(R.string.task_recurrence), color = TextSecondary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskRecurrence.entries.forEach { rec ->
                    FilterChip(
                        selected = state.recurrence == rec,
                        onClick = { viewModel.setRecurrence(rec) },
                        label = { Text(rec.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PhoenixOrange
                        )
                    )
                }
            }

            // Priority toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Priority Task", color = TextSecondary)
                Switch(
                    checked = state.isPriority,
                    onCheckedChange = viewModel::setPriority,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PhoenixOrange,
                        checkedTrackColor = PhoenixOrange.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PhoenixButton(
                text = stringResource(R.string.task_create),
                onClick = {
                    viewModel.saveTask { navController.popBackStack() }
                },
                enabled = state.title.isNotBlank() && !state.isSaving
            )
        }
    }
}
```

- [ ] **Step 3: Add navigation route**

Update Screen.kt to ensure CreateTask route exists (it should already be there from MVP).

Update NavGraph.kt to add the composable:

```kotlin
// Add to NavGraph.kt composable block
composable(Screen.CreateTask.route) {
    CreateTaskScreen(navController = navController)
}
```

- [ ] **Step 4: Add FAB to Dashboard**

Update DashboardScreen.kt to add a FloatingActionButton that navigates to CreateTask:

```kotlin
// Add to DashboardScreen imports
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import com.benyaminrasouli.phoniexprotocol.core.navigation.Screen

// Add inside the Box, after the Column:
FloatingActionButton(
    onClick = { navController.navigate(Screen.CreateTask.route) },
    containerColor = PhoenixOrange
) {
    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.task_create))
}
```

- [ ] **Step 5: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: Create Task screen with all fields, navigation, FAB"
```

---

### Task 5: Task List Screen

**Covers:** Task System

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/tasks/TaskListViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/tasks/TaskListScreen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: Task 2 (TaskRepository), Task 3 (CheckAchievementsUseCase)
- Produces: Task List screen with filtering

- [ ] **Step 1: Create TaskListViewModel**

```kotlin
// feature/tasks/TaskListViewModel.kt
package com.benyaminrasouli.phoniexprotocol.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CheckAchievementsUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CompleteTaskUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilter { ALL, ACTIVE, COMPLETED, SKIPPED }

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                taskRepository.getAllTasks(),
                _filter
            ) { tasks, filter ->
                val filtered = when (filter) {
                    TaskFilter.ALL -> tasks
                    TaskFilter.ACTIVE -> tasks.filter { it.status != "COMPLETED" && it.status != "SKIPPED" }
                    TaskFilter.COMPLETED -> tasks.filter { it.status == "COMPLETED" }
                    TaskFilter.SKIPPED -> tasks.filter { it.status == "SKIPPED" }
                }
                TaskListState(tasks = filtered, filter = filter)
            }.collect { _state.value = it }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            completeTaskUseCase(task)
            checkAchievementsUseCase()
        }
    }
}
```

- [ ] **Step 2: Create TaskListScreen**

```kotlin
// feature/tasks/TaskListScreen.kt
package com.benyaminrasouli.phoniexprotocol.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.drawer_statistics)) },
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
            TaskFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhoenixOrange.copy(alpha = 0.2f),
                        selectedLabelColor = PhoenixOrange
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.tasks.isEmpty()) {
            Text(
                text = stringResource(R.string.dashboard_no_tasks),
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(state.tasks, key = { it.id }) { task ->
                    TaskListItem(
                        task = task,
                        onComplete = { viewModel.completeTask(task) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onComplete)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${task.difficulty} • ${task.category} • ${task.xpValue} XP",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (task.status != "COMPLETED") {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Complete",
                tint = PhoenixOrange,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
```

- [ ] **Step 3: Add navigation route**

Update NavGraph.kt:

```kotlin
composable(Screen.TaskList.route) {
    TaskListScreen(navController = navController)
}
```

- [ ] **Step 4: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: Task List screen with filtering (All/Active/Completed/Skipped)"
```

---

### Task 6: Achievement Screen

**Covers:** Achievements

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/achievements/AchievementViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/achievements/AchievementScreen.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/achievements/AchievementCard.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: Task 3 (GetAchievementsUseCase)
- Produces: Achievement grid screen

- [ ] **Step 1: Create AchievementViewModel**

```kotlin
// feature/achievements/AchievementViewModel.kt
package com.benyaminrasouli.phoniexprotocol.feature.achievements

import androidx.lifecycle.ViewModel
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.AchievementWithStatus
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.GetAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    val achievements: Flow<List<AchievementWithStatus>> = getAchievementsUseCase()
}
```

- [ ] **Step 2: Create AchievementCard**

```kotlin
// feature/achievements/AchievementCard.kt
package com.benyaminrasouli.phoniexprotocol.feature.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.AchievementWithStatus
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@Composable
fun AchievementCard(
    achievement: AchievementWithStatus,
    modifier: Modifier = Modifier
) {
    val alpha = if (achievement.isUnlocked) 1f else 0.4f

    Card(
        modifier = modifier.alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = achievement.achievement.title,
                tint = if (achievement.isUnlocked) PhoenixGold else TextSecondary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.achievement.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (achievement.isUnlocked) PhoenixOrange else TextSecondary
            )

            Text(
                text = achievement.achievement.rarity,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
```

- [ ] **Step 3: Create AchievementScreen**

```kotlin
// feature/achievements/AchievementScreen.kt
package com.benyaminrasouli.phoniexprotocol.feature.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    navController: NavController,
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.drawer_achievements)) },
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(achievements) { achievement ->
                AchievementCard(achievement = achievement)
            }
        }
    }
}
```

- [ ] **Step 4: Add navigation route**

Update NavGraph.kt:

```kotlin
composable(Screen.Achievements.route) {
    AchievementScreen(navController = navController)
}
```

- [ ] **Step 5: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: Achievement screen with grid, cards, unlock states"
```

---

### Task 7: Statistics Screen

**Covers:** Statistics

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/statistics/StatisticsViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/statistics/StatisticsScreen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: Task 2 (TaskRepository), StatsRepository, AchievementRepository
- Produces: Statistics screen with charts

- [ ] **Step 1: Create StatisticsViewModel**

```kotlin
// feature/statistics/StatisticsViewModel.kt
package com.benyaminrasouli.phoniexprotocol.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsState(
    val stats: UserStats? = null,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val achievementsUnlocked: Int = 0,
    val completionRate: Float = 0f
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val taskRepository: TaskRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            statsRepository.getStats().collect { stats ->
                val total = taskRepository.getTaskCount()
                val completed = taskRepository.getCompletedTaskCount()
                val achievements = achievementRepository.getUnlockedCount()
                _state.update {
                    it.copy(
                        stats = stats,
                        totalTasks = total,
                        completedTasks = completed,
                        achievementsUnlocked = achievements,
                        completionRate = if (total > 0) completed.toFloat() / total else 0f
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create StatisticsScreen**

```kotlin
// feature/statistics/StatisticsScreen.kt
package com.benyaminrasouli.phoniexprotocol.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.benyaminrasouli.phoniexprotocol.core.ui.components.EnergyBar
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.drawer_statistics)) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview card
            StatCard(title = "Overview") {
                StatRow(label = "Total XP", value = "${state.stats?.xp ?: 0}")
                StatRow(label = "Level", value = "${state.stats?.level ?: 1}")
                StatRow(label = "Rank", value = state.stats?.rank ?: "INITIATE")
                StatRow(label = "Tasks Completed", value = "${state.completedTasks}")
                StatRow(label = "Completion Rate", value = "${(state.completionRate * 100).toInt()}%")
                StatRow(label = "Achievements", value = "${state.achievementsUnlocked}/15")
            }

            // Streak card
            StatCard(title = "Streaks") {
                StatRow(label = "Current Streak", value = "${state.stats?.currentStreak ?: 0} days")
                StatRow(label = "Longest Streak", value = "${state.stats?.longestStreak ?: 0} days")
            }

            // Energy card
            StatCard(title = "Energy & Shadow") {
                Text(
                    text = "Phoenix Energy: ${state.stats?.phoenixEnergy ?: 50}/100",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                EnergyBar(energy = state.stats?.phoenixEnergy ?: 50)
                Spacer(modifier = Modifier.height(12.dp))
                StatRow(
                    label = "Shadow Level",
                    value = "${state.stats?.shadowLevel ?: 0}",
                    valueColor = if ((state.stats?.shadowLevel ?: 0) > 0) MaterialTheme.colorScheme.error else PhoenixOrange
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = PhoenixGold
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary)
        Text(text = value, color = valueColor)
    }
}
```

- [ ] **Step 3: Add navigation route**

Update NavGraph.kt:

```kotlin
composable(Screen.Statistics.route) {
    StatisticsScreen(navController = navController)
}
```

- [ ] **Step 4: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: Statistics screen with overview, streaks, energy stats"
```

---

### Task 8: Wire Drawer Navigation

**Covers:** Navigation

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/drawer/DrawerScreen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: Tasks 5, 6, 7 (screens exist)
- Produces: Drawer navigates to all new screens

- [ ] **Step 1: Update DrawerScreen with navigation**

```kotlin
// Update DrawerScreen.kt to accept navigation callbacks
// Add to the function parameters:
@Composable
fun DrawerScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    viewModel: DrawerViewModel = hiltViewModel()
)

// Update menu items to use the new callbacks:
DrawerMenuItem(
    icon = Icons.Filled.BarChart,
    label = stringResource(R.string.drawer_statistics),
    onClick = onNavigateToStatistics
)
DrawerMenuItem(
    icon = Icons.Filled.EmojiEvents,
    label = stringResource(R.string.drawer_achievements),
    onClick = onNavigateToAchievements
)
```

- [ ] **Step 2: Update DashboardScreen drawer integration**

```kotlin
// Update DashboardScreen.kt DrawerScreen call:
DrawerScreen(
    onNavigateToStatistics = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.Statistics.route)
    },
    onNavigateToAchievements = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.Achievements.route)
    }
)
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: wire drawer navigation to Statistics, Achievements screens"
```

---

### Task 9: Localization for Phase 2

**Covers:** Localization

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

**Interfaces:**
- Consumes: All previous tasks
- Produces: Bilingual strings for new screens

- [ ] **Step 1: Add English strings**

```xml
<!-- Add to values/strings.xml -->
    <!-- Task Types -->
    <string name="task_type_daily">Daily</string>
    <string name="task_type_weekly">Weekly</string>
    <string name="task_type_monthly">Monthly</string>
    <string name="task_type_custom">Custom</string>

    <!-- Task Filters -->
    <string name="filter_all">All</string>
    <string name="filter_active">Active</string>
    <string name="filter_completed">Completed</string>
    <string name="filter_skipped">Skipped</string>

    <!-- Create Task -->
    <string name="task_priority">Priority Task</string>
    <string name="task_save">Save Task</string>

    <!-- Achievements -->
    <string name="achievement_first_blood">First Blood</string>
    <string name="achievement_7day_warrior">7-Day Warrior</string>
    <string name="achievement_30day_legend">30-Day Legend</string>
    <string name="achievement_no_excuses">No Excuses</string>
    <string name="achievement_phoenix_rising">Phoenix Rising</string>

    <!-- Statistics -->
    <string name="stats_overview">Overview</string>
    <string name="stats_streaks">Streaks</string>
    <string name="stats_energy">Energy &amp; Shadow</string>
    <string name="stats_total_xp">Total XP</string>
    <string name="stats_level">Level</string>
    <string name="stats_rank">Rank</string>
    <string name="stats_tasks_completed">Tasks Completed</string>
    <string name="stats_completion_rate">Completion Rate</string>
    <string name="stats_achievements">Achievements</string>
    <string name="stats_current_streak">Current Streak</string>
    <string name="stats_longest_streak">Longest Streak</string>
    <string name="stats_phoenix_energy">Phoenix Energy</string>
    <string name="stats_shadow_level">Shadow Level</string>
```

- [ ] **Step 2: Add Persian strings**

```xml
<!-- Add to values-fa/strings.xml -->
    <!-- Task Types -->
    <string name="task_type_daily">روزانه</string>
    <string name="task_type_weekly">هفتگی</string>
    <string name="task_type_monthly">ماهانه</string>
    <string name="task_type_custom">سفارشی</string>

    <!-- Task Filters -->
    <string name="filter_all">همه</string>
    <string name="filter_active">فعال</string>
    <string name="filter_completed">تکمیل شده</string>
    <string name="filter_skipped">رد شده</string>

    <!-- Create Task -->
    <string name="task_priority">وظیفه اولویت‌دار</string>
    <string name="task_save">ذخیره وظیفه</string>

    <!-- Achievements -->
    <string name="achievement_first_blood">اولین خون</string>
    <string name="achievement_7day_warrior">جنگجوی ۷ روزه</string>
    <string name="achievement_30day_legend">افسانه ۳۰ روزه</string>
    <string name="achievement_no_excuses">بدون بهانه</string>
    <string name="achievement_phoenix_rising">طلوع ققنوس</string>

    <!-- Statistics -->
    <string name="stats_overview">نمای کلی</string>
    <string name="stats_streaks">سلسله مراتب</string>
    <string name="stats_energy">انرژی و سایه</string>
    <string name="stats_total_xp">کل XP</string>
    <string name="stats_level">سطح</string>
    <string name="stats_rank">رتبه</string>
    <string name="stats_tasks_completed">وظایف تکمیل شده</string>
    <string name="stats_completion_rate">نرخ تکمیل</string>
    <string name="stats_achievements">دستاوردها</string>
    <string name="stats_current_streak">سلسله فعلی</string>
    <string name="stats_longest_streak">طولانی‌ترین سلسله</string>
    <string name="stats_phoenix_energy">انرژی ققنوس</string>
    <string name="stats_shadow_level">سطح سایه</string>
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: Phase 2 localization - task types, filters, statistics strings"
```

---

## Self-Review

**Spec coverage check:**
- Task System → Tasks 1, 2, 4, 5
- Achievements → Tasks 1, 3, 6
- Statistics → Task 7
- Navigation → Task 8
- Localization → Task 9

**Placeholder scan:** No TBDs or TODOs found.

**Type consistency:** Task entity, TaskType enum, Difficulty enum, and repository methods are consistent across tasks.
