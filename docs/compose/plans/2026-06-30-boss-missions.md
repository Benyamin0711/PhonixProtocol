# Boss Missions System — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Boss Missions system with time-limited challenges that spawn weekly, have random combined goals, and reward XP + achievement badges.

**Architecture:** Extends existing Feature-based Clean Architecture with new Boss entity, DAO, repository, use cases, and UI. Uses Room for persistence, Hilt for DI, and follows established patterns.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Hilt, Navigation Compose

## Global Constraints
- Min SDK 24, Target SDK 36, Compile SDK 36
- Package: `com.benyaminrasouli.phoniexprotocol`
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- Bilingual: English + Persian (all new strings must have both locales)
- Every Room entity uses Long auto-increment PKs
- Every task ends with `./gradlew assembleDebug` passing

---

### Task 1: Boss Entity & Database Migration

**Covers:** [S3], [S6]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/entity/Boss.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/db/PhoenixDatabase.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/di/DatabaseModule.kt`

**Interfaces:**
- Consumes: existing PhoenixDatabase (version 2)
- Produces: Boss entity, Database version 3

- [ ] **Step 1: Create Boss entity**

```kotlin
// core/data/db/entity/Boss.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bosses")
data class Boss(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val level: Int,
    val title: String,
    val description: String,
    val goals: String, // JSON array of GoalTemplate
    val deadline: Long,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, FAILED
    val rewardXp: Int,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Update PhoenixDatabase**

```kotlin
// core/data/db/PhoenixDatabase.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.BossDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
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
        UserAchievement::class,
        Boss::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PhoenixDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun bossDao(): BossDao
}
```

- [ ] **Step 3: Create BossDao**

```kotlin
// core/data/db/dao/BossDao.kt
package com.benyaminrasouli.phoniexprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import kotlinx.coroutines.flow.Flow

@Dao
interface BossDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoss(boss: Boss): Long

    @Query("SELECT * FROM bosses WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveBoss(): Boss?

    @Query("SELECT * FROM bosses WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveBossFlow(): Flow<Boss?>

    @Query("SELECT * FROM bosses ORDER BY createdAt DESC")
    fun getAllBosses(): Flow<List<Boss>>

    @Query("SELECT * FROM bosses WHERE status = 'COMPLETED' ORDER BY createdAt DESC")
    fun getCompletedBosses(): Flow<List<Boss>>

    @Query("UPDATE bosses SET status = :status WHERE id = :bossId")
    suspend fun updateBossStatus(bossId: Long, status: String)

    @Query("SELECT COUNT(*) FROM bosses WHERE status = 'COMPLETED'")
    suspend fun getCompletedBossCount(): Int

    @Query("SELECT * FROM bosses ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastBoss(): Boss?
}
```

- [ ] **Step 4: Update DatabaseModule**

```kotlin
// di/DatabaseModule.kt
package com.benyaminrasouli.phoniexprotocol.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.BossDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            // Seed achievements for existing users
            val achievements = arrayOf(
                arrayOf("first_blood", "First Blood", "Complete your first task", "emoji_events", "COMMON", "complete_1"),
                arrayOf("7day_warrior", "7-Day Warrior", "Maintain a 7-day streak", "local_fire_department", "COMMON", "streak_7"),
                arrayOf("30day_legend", "30-Day Legend", "Maintain a 30-day streak", "military_tech", "RARE", "streak_30"),
                arrayOf("no_excuses", "No Excuses", "Complete 10 tasks", "task_alt", "COMMON", "tasks_10"),
                arrayOf("discipline_above_mood", "Discipline Above Mood", "Complete task when Shadow > 50", "psychology", "RARE", "shadow_task"),
                arrayOf("phoenix_rising", "Phoenix Rising", "Recover from 3+ day absence", "trending_up", "EPIC", "recovery"),
                arrayOf("boss_slayer", "Boss Slayer", "Complete 5 boss missions", "castle", "EPIC", "boss_5"),
                arrayOf("shadow_breaker", "Shadow Breaker", "Reduce Shadow to 0 after being > 30", "dark_mode", "RARE", "shadow_break"),
                arrayOf("elite_consistency", "Elite Consistency", "90% completion rate for 7 days", "star", "LEGENDARY", "consistency_90"),
                arrayOf("unbroken", "Unbroken", "100-day streak", "whatshot", "LEGENDARY", "streak_100"),
                arrayOf("life_master", "Life Master", "Complete task in all 8 life areas", "public", "LEGENDARY", "all_areas"),
                arrayOf("night_owl", "Night Owl", "Complete task after midnight", "bedtime", "COMMON", "night"),
                arrayOf("early_bird", "Early Bird", "Complete task before 6 AM", "wb_sunny", "COMMON", "morning"),
                arrayOf("speed_demon", "Speed Demon", "Complete 5 tasks in one day", "bolt", "RARE", "speed_5"),
                arrayOf("marathon_runner", "Marathon Runner", "Complete 50 tasks total", "directions_run", "RARE", "tasks_50")
            )
            achievements.forEach { a ->
                db.execSQL(
                    "INSERT OR IGNORE INTO achievements (id, title, description, icon, rarity, condition) VALUES (?, ?, ?, ?, ?, ?)",
                    a
                )
            }
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS bosses (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, level INTEGER NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, goals TEXT NOT NULL, deadline INTEGER NOT NULL, status TEXT NOT NULL DEFAULT 'ACTIVE', rewardXp INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
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
                        achievements.forEach { a ->
                            db.execSQL(
                                "INSERT OR REPLACE INTO achievements (id, title, description, icon, rarity, condition) VALUES (?, ?, ?, ?, ?, ?)",
                                arrayOf(a.id, a.title, a.description, a.icon, a.rarity, a.condition)
                            )
                        }
                    }
                }
            })
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

    @Provides
    fun provideBossDao(db: PhoenixDatabase): BossDao = db.bossDao()
}
```

- [ ] **Step 5: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: Boss entity, BossDao, database migration v2->v3"
```

---

### Task 2: Goal Types & Boss Repository

**Covers:** [S3], [S7]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/model/BossGoal.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/model/GoalType.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/repository/BossRepository.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/data/repository/BossRepositoryImpl.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/di/AppModule.kt`

**Interfaces:**
- Consumes: Task 1 (BossDao, Boss entity)
- Produces: GoalType enum, BossGoal data class, BossRepository

- [ ] **Step 1: Create GoalType enum**

```kotlin
// core/domain/model/GoalType.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.model

enum class GoalType {
    COMPLETE_TASKS,
    STREAK_DAYS,
    PRIORITY_TASKS,
    XP_EARNED,
    DIFFICULTY_TASKS
}
```

- [ ] **Step 2: Create BossGoal data class**

```kotlin
// core/domain/model/BossGoal.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class BossGoal(
    val type: GoalType,
    val target: Int,
    var current: Int = 0,
    val description: String
) {
    val isCompleted: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)

    companion object {
        private val gson = Gson()

        fun toJson(goals: List<BossGoal>): String {
            return gson.toJson(goals)
        }

        fun fromJson(json: String): List<BossGoal> {
            val type = object : TypeToken<List<BossGoal>>() {}.type
            return gson.fromJson(json, type)
        }
    }
}
```

- [ ] **Step 3: Create BossRepository interface**

```kotlin
// core/domain/repository/BossRepository.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import kotlinx.coroutines.flow.Flow

interface BossRepository {
    suspend fun getActiveBoss(): Boss?
    fun getActiveBossFlow(): Flow<Boss?>
    fun getAllBosses(): Flow<List<Boss>>
    fun getCompletedBosses(): Flow<List<Boss>>
    suspend fun insertBoss(boss: Boss): Long
    suspend fun updateBossStatus(bossId: Long, status: String)
    suspend fun getCompletedBossCount(): Int
    suspend fun getLastBoss(): Boss?
}
```

- [ ] **Step 4: Create BossRepositoryImpl**

```kotlin
// core/data/repository/BossRepositoryImpl.kt
package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.BossDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BossRepositoryImpl @Inject constructor(
    private val dao: BossDao
) : BossRepository {

    override suspend fun getActiveBoss(): Boss? = dao.getActiveBoss()

    override fun getActiveBossFlow(): Flow<Boss?> = dao.getActiveBossFlow()

    override fun getAllBosses(): Flow<List<Boss>> = dao.getAllBosses()

    override fun getCompletedBosses(): Flow<List<Boss>> = dao.getCompletedBosses()

    override suspend fun insertBoss(boss: Boss): Long = dao.insertBoss(boss)

    override suspend fun updateBossStatus(bossId: Long, status: String) {
        dao.updateBossStatus(bossId, status)
    }

    override suspend fun getCompletedBossCount(): Int = dao.getCompletedBossCount()

    override suspend fun getLastBoss(): Boss? = dao.getLastBoss()
}
```

- [ ] **Step 5: Update AppModule**

```kotlin
// di/AppModule.kt
package com.benyaminrasouli.phoniexprotocol.di

import com.benyaminrasouli.phoniexprotocol.core.data.repository.AchievementRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.BossRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.StatsRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.TaskRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.data.repository.UserRepositoryImpl
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
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

    @Binds
    @Singleton
    abstract fun bindBossRepository(impl: BossRepositoryImpl): BossRepository
}
```

- [ ] **Step 6: Add Gson dependency**

Modify `app/build.gradle.kts` to add Gson:

```kotlin
dependencies {
    // ... existing dependencies
    implementation("com.google.code.gson:gson:2.11.0")
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: GoalType enum, BossGoal data class, BossRepository"
```

---

### Task 3: Boss Use Cases

**Covers:** [S4], [S8], [S9]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/usecase/SpawnWeeklyBossUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/usecase/GetActiveBossUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/usecase/UpdateBossProgressUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/usecase/CompleteBossUseCase.kt`

**Interfaces:**
- Consumes: Task 1 (BossRepository), Task 2 (BossGoal, GoalType)
- Produces: SpawnWeeklyBossUseCase, GetActiveBossUseCase, UpdateBossProgressUseCase, CompleteBossUseCase

- [ ] **Step 1: Create SpawnWeeklyBossUseCase**

```kotlin
// core/domain/usecase/SpawnWeeklyBossUseCase.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoniexprotocol.core.domain.model.GoalType
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import javax.inject.Inject

class SpawnWeeklyBossUseCase @Inject constructor(
    private val bossRepository: BossRepository
) {
    suspend operator fun invoke() {
        val activeBoss = bossRepository.getActiveBoss()
        if (activeBoss != null) return

        val lastBoss = bossRepository.getLastBoss()
        if (lastBoss != null) {
            val timeSinceLastBoss = System.currentTimeMillis() - lastBoss.createdAt
            val oneWeek = 7 * 24 * 60 * 60 * 1000L
            if (timeSinceLastBoss < oneWeek) return
        }

        val completedCount = bossRepository.getCompletedBossCount()
        val level = (completedCount + 1).coerceAtMost(5)

        val goals = generateGoals(level)
        val rewardXp = when (level) {
            1 -> 100
            2 -> 200
            3 -> 350
            4 -> 500
            else -> 700
        }

        val deadline = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)

        val boss = Boss(
            level = level,
            title = "Boss Level $level",
            description = "Complete all goals to defeat the boss!",
            goals = BossGoal.toJson(goals),
            deadline = deadline,
            rewardXp = rewardXp
        )

        bossRepository.insertBoss(boss)
    }

    private fun generateGoals(level: Int): List<BossGoal> {
        val goalCount = when (level) {
            1 -> 2
            2 -> 2
            3 -> 3
            4 -> 3
            else -> 4
        }

        val availableGoals = listOf(
            GoalType.COMPLETE_TASKS,
            GoalType.STREAK_DAYS,
            GoalType.PRIORITY_TASKS,
            GoalType.XP_EARNED,
            GoalType.DIFFICULTY_TASKS
        )

        val selectedGoals = availableGoals.shuffled().take(goalCount)

        return selectedGoals.map { type ->
            when (type) {
                GoalType.COMPLETE_TASKS -> BossGoal(
                    type = type,
                    target = (5 + level * 2).coerceAtMost(15),
                    description = "Complete ${(5 + level * 2).coerceAtMost(15)} tasks"
                )
                GoalType.STREAK_DAYS -> BossGoal(
                    type = type,
                    target = (3 + level).coerceAtMost(7),
                    description = "Maintain a ${(3 + level).coerceAtMost(7)}-day streak"
                )
                GoalType.PRIORITY_TASKS -> BossGoal(
                    type = type,
                    target = (2 + level).coerceAtMost(5),
                    description = "Complete ${(2 + level).coerceAtMost(5)} priority tasks"
                )
                GoalType.XP_EARNED -> BossGoal(
                    type = type,
                    target = (100 + level * 100).coerceAtMost(500),
                    description = "Earn ${(100 + level * 100).coerceAtMost(500)} XP"
                )
                GoalType.DIFFICULTY_TASKS -> BossGoal(
                    type = type,
                    target = (3 + level).coerceAtMost(8),
                    description = "Complete ${(3 + level).coerceAtMost(8)} HARD/EXTREME tasks"
                )
            }
        }
    }
}
```

- [ ] **Step 2: Create GetActiveBossUseCase**

```kotlin
// core/domain/usecase/GetActiveBossUseCase.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ActiveBoss(
    val boss: Boss,
    val goals: List<BossGoal>,
    val progress: Float,
    val isCompleted: Boolean,
    val isFailed: Boolean
)

class GetActiveBossUseCase @Inject constructor(
    private val bossRepository: BossRepository
) {
    operator fun invoke(): Flow<ActiveBoss?> {
        return bossRepository.getActiveBossFlow().map { boss ->
            if (boss == null) return@map null

            val goals = BossGoal.fromJson(boss.goals)
            val isFailed = System.currentTimeMillis() > boss.deadline && boss.status == "ACTIVE"
            val isCompleted = goals.all { it.isCompleted }

            val totalProgress = goals.map { it.progress }.average().toFloat()

            ActiveBoss(
                boss = boss,
                goals = goals,
                progress = totalProgress,
                isCompleted = isCompleted,
                isFailed = isFailed
            )
        }
    }
}
```

- [ ] **Step 3: Create UpdateBossProgressUseCase**

```kotlin
// core/domain/usecase/UpdateBossProgressUseCase.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoniexprotocol.core.domain.model.GoalType
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateBossProgressUseCase @Inject constructor(
    private val bossRepository: BossRepository,
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke() {
        val boss = bossRepository.getActiveBoss() ?: return
        if (boss.status != "ACTIVE") return

        val goals = BossGoal.fromJson(boss.goals)
        var updated = false

        goals.forEach { goal ->
            when (goal.type) {
                GoalType.COMPLETE_TASKS -> {
                    val count = taskRepository.getCompletedTaskCount()
                    if (count > goal.current) {
                        goal.current = count
                        updated = true
                    }
                }
                GoalType.STREAK_DAYS -> {
                    val stats = statsRepository.getStatsOnce()
                    if (stats != null && stats.currentStreak > goal.current) {
                        goal.current = stats.currentStreak
                        updated = true
                    }
                }
                GoalType.PRIORITY_TASKS -> {
                    val count = taskRepository.getCompletedPriorityTaskCount()
                    if (count > goal.current) {
                        goal.current = count
                        updated = true
                    }
                }
                GoalType.XP_EARNED -> {
                    val stats = statsRepository.getStatsOnce()
                    if (stats != null && stats.totalXp > goal.current) {
                        goal.current = stats.totalXp
                        updated = true
                    }
                }
                GoalType.DIFFICULTY_TASKS -> {
                    val count = taskRepository.getCompletedHardTaskCount()
                    if (count > goal.current) {
                        goal.current = count
                        updated = true
                    }
                }
            }
        }

        if (updated) {
            val updatedBoss = boss.copy(goals = BossGoal.toJson(goals))
            bossRepository.insertBoss(updatedBoss)
        }
    }
}
```

- [ ] **Step 4: Create CompleteBossUseCase**

```kotlin
// core/domain/usecase/CompleteBossUseCase.kt
package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class CompleteBossUseCase @Inject constructor(
    private val bossRepository: BossRepository,
    private val statsRepository: StatsRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(bossId: Long) {
        val boss = bossRepository.getActiveBoss() ?: return
        if (boss.id != bossId) return

        bossRepository.updateBossStatus(bossId, "COMPLETED")

        statsRepository.addXp(boss.rewardXp)

        val completedCount = bossRepository.getCompletedBossCount()
        if (completedCount >= 5) {
            achievementRepository.unlock("boss_slayer")
        }
    }
}
```

- [ ] **Step 5: Update TaskRepository**

Add new methods to TaskRepository interface and implementation:

```kotlin
// core/domain/repository/TaskRepository.kt
interface TaskRepository {
    // ... existing methods
    suspend fun getCompletedPriorityTaskCount(): Int
    suspend fun getCompletedHardTaskCount(): Int
}

// core/data/repository/TaskRepositoryImpl.kt
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {
    // ... existing methods
    override suspend fun getCompletedPriorityTaskCount(): Int {
        return dao.getCompletedPriorityTaskCount()
    }

    override suspend fun getCompletedHardTaskCount(): Int {
        return dao.getCompletedHardTaskCount()
    }
}
```

- [ ] **Step 6: Update TaskDao**

Add new queries:

```kotlin
// core/data/db/dao/TaskDao.kt
@Dao
interface TaskDao {
    // ... existing methods
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND isPriority = 1")
    suspend fun getCompletedPriorityTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND (difficulty = 'HARD' OR difficulty = 'EXTREME')")
    suspend fun getCompletedHardTaskCount(): Int
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: Boss use cases - Spawn, GetActive, UpdateProgress, Complete"
```

---

### Task 4: Boss UI Components

**Covers:** [S5]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/boss/BossViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/boss/BossCard.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/boss/BossDetailScreen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/Screen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: Task 3 (GetActiveBossUseCase, UpdateBossProgressUseCase, CompleteBossUseCase)
- Produces: BossViewModel, BossCard, BossDetailScreen

- [ ] **Step 1: Create BossViewModel**

```kotlin
// feature/boss/BossViewModel.kt
package com.benyaminrasouli.phoniexprotocol.feature.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.ActiveBoss
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CompleteBossUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.GetActiveBossUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.SpawnWeeklyBossUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.UpdateBossProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BossViewModel @Inject constructor(
    private val getActiveBossUseCase: GetActiveBossUseCase,
    private val spawnWeeklyBossUseCase: SpawnWeeklyBossUseCase,
    private val updateBossProgressUseCase: UpdateBossProgressUseCase,
    private val completeBossUseCase: CompleteBossUseCase
) : ViewModel() {

    val activeBoss: Flow<ActiveBoss?> = getActiveBossUseCase()

    init {
        viewModelScope.launch {
            spawnWeeklyBossUseCase()
        }
    }

    fun updateProgress() {
        viewModelScope.launch {
            updateBossProgressUseCase()
        }
    }

    fun completeBoss(bossId: Long) {
        viewModelScope.launch {
            completeBossUseCase(bossId)
        }
    }
}
```

- [ ] **Step 2: Create BossCard**

```kotlin
// feature/boss/BossCard.kt
package com.benyaminrasouli.phoniexprotocol.feature.boss

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.ActiveBoss
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@Composable
fun BossCard(
    boss: ActiveBoss?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (boss == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
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
                        tint = PhoenixOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = boss.boss.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Level ${boss.boss.level} • ${boss.boss.rewardXp} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { boss.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = PhoenixOrange,
                trackColor = PhoenixOrange.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(boss.progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
```

- [ ] **Step 3: Create BossDetailScreen**

```kotlin
// feature/boss/BossDetailScreen.kt
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.ActiveBoss
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BossDetailScreen(
    navController: NavController,
    viewModel: BossViewModel = hiltViewModel()
) {
    val activeBoss by viewModel.activeBoss.collectAsStateWithLifecycle(initialValue = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text("Boss Mission") },
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

        if (activeBoss == null) {
            Text(
                text = "No active boss mission",
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            BossDetailContent(
                activeBoss = activeBoss!!,
                onComplete = { viewModel.completeBoss(activeBoss!!.boss.id) }
            )
        }
    }
}

@Composable
private fun BossDetailContent(
    activeBoss: ActiveBoss,
    onComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BossHeader(activeBoss = activeBoss)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Goals",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(activeBoss.goals) { goal ->
            GoalItem(goal = goal)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }

        if (activeBoss.isCompleted) {
            item {
                androidx.compose.material3.Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Claim Rewards")
                }
            }
        }
    }
}

@Composable
private fun BossHeader(activeBoss: ActiveBoss) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = "Boss",
                tint = PhoenixOrange,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = activeBoss.boss.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Level ${activeBoss.boss.level} • ${activeBoss.boss.rewardXp} XP Reward",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        LinearProgressIndicator(
            progress = { activeBoss.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = PhoenixOrange,
            trackColor = PhoenixOrange.copy(alpha = 0.2f)
        )

        Text(
            text = "${(activeBoss.progress * 100).toInt()}% Complete",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun GoalItem(goal: BossGoal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${goal.current}/${goal.target}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (goal.isCompleted) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = PhoenixOrange,
                modifier = Modifier.size(24.dp)
            )
        } else {
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .width(100.dp)
                    .height(8.dp),
                color = PhoenixOrange,
                trackColor = PhoenixOrange.copy(alpha = 0.2f)
            )
        }
    }
}
```

- [ ] **Step 4: Update Screen.kt**

Add BossDetail route:

```kotlin
// core/navigation/Screen.kt
sealed class Screen(val route: String) {
    // ... existing screens
    object BossDetail : Screen("boss_detail")
}
```

- [ ] **Step 5: Update NavGraph.kt**

Add BossDetail composable:

```kotlin
// core/navigation/NavGraph.kt
composable(Screen.BossDetail.route) {
    BossDetailScreen(navController = navController)
}
```

- [ ] **Step 6: Add BossCard to Dashboard**

Update DashboardScreen.kt to include BossCard:

```kotlin
// feature/dashboard/DashboardScreen.kt
// Add import
import com.benyaminrasouli.phoniexprotocol.feature.boss.BossCard
import com.benyaminrasouli.phoniexprotocol.feature.boss.BossViewModel
import androidx.hilt.navigation.compose.hiltViewModel

// Add viewModel parameter
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    bossViewModel: BossViewModel = hiltViewModel()
) {
    // ... existing code

    val activeBoss by bossViewModel.activeBoss.collectAsStateWithLifecycle(initialValue = null)

    // Inside Column, after existing content:
    Spacer(modifier = Modifier.height(16.dp))
    BossCard(
        boss = activeBoss,
        onClick = { navController.navigate(Screen.BossDetail.route) }
    )
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: Boss UI - BossCard, BossDetailScreen, navigation, Dashboard integration"
```

---

### Task 5: String Resources & Final Integration

**Covers:** [S5], [S10]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

**Interfaces:**
- Consumes: Task 4 (UI components)
- Produces: Bilingual string resources

- [ ] **Step 1: Update English strings**

```xml
<!-- values/strings.xml -->
<!-- Add these strings -->
<string name="boss_mission">Boss Mission</string>
<string name="boss_level">Level %d</string>
<string name="boss_xp_reward">%d XP Reward</string>
<string name="boss_complete">%d%% Complete</string>
<string name="boss_goals">Goals</string>
<string name="boss_claim_rewards">Claim Rewards</string>
<string name="boss_no_active">No active boss mission</string>
<string name="boss_complete_tasks">Complete %d tasks</string>
<string name="boss_streak_days">Maintain a %d-day streak</string>
<string name="boss_priority_tasks">Complete %d priority tasks</string>
<string name="boss_xp_earned">Earn %d XP</string>
<string name="boss_difficulty_tasks">Complete %d HARD/EXTREME tasks</string>
```

- [ ] **Step 2: Update Persian strings**

```xml
<!-- values-fa/strings.xml -->
<!-- Add these strings -->
<string name="boss_mission">مأموریت باس</string>
<string name="boss_level">سطح %d</string>
<string name="boss_xp_reward">%d امتیاز تجربه</string>
<string name="boss_complete">%d%% تکمیل شده</string>
<string name="boss_goals">اهداف</string>
<string name="boss_claim_rewards">دریافت پاداش‌ها</string>
<string name="boss_no_active">مأموریت باس فعالی وجود ندارد</string>
<string name="boss_complete_tasks">%d کار را تکمیل کنید</string>
<string name="boss_streak_days">%d روز پیاپی فعال باشید</string>
<string name="boss_priority_tasks">%d کار اولویت‌دار را تکمیل کنید</string>
<string name="boss_xp_earned">%d امتیاز تجربه کسب کنید</string>
<string name="boss_difficulty_tasks">%d کار سخت/-extreme را تکمیل کنید</string>
```

- [ ] **Step 3: Update BossCard to use strings**

```kotlin
// feature/boss/BossCard.kt
// Update Text composables to use stringResource
Text(
    text = stringResource(R.string.boss_level, boss.boss.level),
    // ...
)
```

- [ ] **Step 4: Update BossDetailScreen to use strings**

```kotlin
// feature/boss/BossDetailScreen.kt
// Update Text composables to use stringResource
Text(
    text = stringResource(R.string.boss_mission),
    // ...
)
```

- [ ] **Step 5: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: Bilingual string resources for Boss system"
```

---

### Task 6: Boss Failure Handling & Cleanup

**Covers:** [S10]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/domain/usecase/SpawnWeeklyBossUseCase.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/boss/BossViewModel.kt`

**Interfaces:**
- Consumes: Task 3 (SpawnWeeklyBossUseCase)
- Produces: Boss failure handling

- [ ] **Step 1: Update SpawnWeeklyBossUseCase**

Add failure check:

```kotlin
// core/domain/usecase/SpawnWeeklyBossUseCase.kt
suspend operator fun invoke() {
    // Check for failed bosses
    val activeBoss = bossRepository.getActiveBoss()
    if (activeBoss != null) {
        if (System.currentTimeMillis() > activeBoss.deadline) {
            bossRepository.updateBossStatus(activeBoss.id, "FAILED")
        } else {
            return // Active boss still valid
        }
    }

    // Check cooldown
    val lastBoss = bossRepository.getLastBoss()
    if (lastBoss != null) {
        val timeSinceLastBoss = System.currentTimeMillis() - lastBoss.createdAt
        val oneWeek = 7 * 24 * 60 * 60 * 1000L
        if (timeSinceLastBoss < oneWeek) return
    }

    // Spawn new boss
    // ... rest of existing code
}
```

- [ ] **Step 2: Update BossViewModel**

Add periodic progress check:

```kotlin
// feature/boss/BossViewModel.kt
init {
    viewModelScope.launch {
        spawnWeeklyBossUseCase()
        // Check progress periodically
        while (true) {
            kotlinx.coroutines.delay(60_000) // Check every minute
            updateBossProgressUseCase()
            spawnWeeklyBossUseCase() // Check for failed bosses
        }
    }
}
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: Boss failure handling and periodic progress check"
```

---

### Task 7: Final Build Verification

**Covers:** All sections

**Files:** None (verification only)

**Interfaces:**
- Consumes: All previous tasks
- Produces: Final verified build

- [ ] **Step 1: Clean build**

Run: `./gradlew clean`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: Boss Missions System - Phase 3 complete"
```
