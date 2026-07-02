# Phase 4: Daily Challenges Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add auto-generated daily challenges (2-5 per day) with XP/energy/achievement rewards, dashboard card, and dedicated screen.

**Architecture:** Feature-based Clean Architecture. New entity `DailyChallenge` with DAO, repository, use cases. WorkManager for daily reset. Dashboard card + dedicated screen for UI.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, WorkManager, Navigation Compose

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

### Task 1: Database Migration & Entity

**Covers:** [S3]

**Files:**
- Create: `app/src/main/java/.../core/data/db/entity/DailyChallenge.kt`
- Create: `app/src/main/java/.../core/data/db/dao/DailyChallengeDao.kt`
- Modify: `app/src/main/java/.../core/data/db/PhoenixDatabase.kt` — add entity + DAO
- Modify: `app/src/main/java/.../di/DatabaseModule.kt` — add MIGRATION_4_5 + provider

**Interfaces:**
- Consumes: existing `PhoenixDatabase`, `DatabaseModule`
- Produces: `DailyChallenge` entity, `DailyChallengeDao`, DB migration

- [ ] **Step 1: Create DailyChallenge entity**

Create `app/src/main/java/.../core/data/db/entity/DailyChallenge.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val type: String,
    val target: Int,
    val current: Int = 0,
    val rewardXp: Int,
    val rewardEnergy: Int,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create DailyChallengeDao**

Create `app/src/main/java/.../core/data/db/dao/DailyChallengeDao.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: DailyChallenge): Long

    @Query("SELECT * FROM daily_challenges WHERE date = :date ORDER BY createdAt ASC")
    fun getChallengesByDate(date: String): Flow<List<DailyChallenge>>

    @Query("SELECT * FROM daily_challenges WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getChallengesByDateOnce(date: String): List<DailyChallenge>

    @Query("UPDATE daily_challenges SET current = :current WHERE id = :id")
    suspend fun updateChallengeProgress(id: Long, current: Int)

    @Query("UPDATE daily_challenges SET completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("UPDATE daily_challenges SET claimed = 1 WHERE id = :id")
    suspend fun markClaimed(id: Long)

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE date = :date AND completed = 1")
    suspend fun getCompletedCountByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE date = :date")
    suspend fun getTotalCountByDate(date: String): Int
}
```

- [ ] **Step 3: Add to PhoenixDatabase**

Edit `app/src/main/java/.../core/data/db/PhoenixDatabase.kt`:
- Add `DailyChallenge::class` to `@Database(entities = [...])`
- Add abstract fun `dailyChallengeDao(): DailyChallengeDao`

- [ ] **Step 4: Add migration and provider**

Edit `app/src/main/java/.../di/DatabaseModule.kt`:
- Add `MIGRATION_4_5`:
```kotlin
private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS daily_challenges (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, type TEXT NOT NULL, target INTEGER NOT NULL, current INTEGER NOT NULL DEFAULT 0, rewardXp INTEGER NOT NULL, rewardEnergy INTEGER NOT NULL, completed INTEGER NOT NULL DEFAULT 0, claimed INTEGER NOT NULL DEFAULT 0, date TEXT NOT NULL, createdAt INTEGER NOT NULL)")
    }
}
```
- Add `MIGRATION_4_5` to `addMigrations()` call
- Add `provideDailyChallengeDao(db: PhoenixDatabase): DailyChallengeDao = db.dailyChallengeDao()`

- [ ] **Step 5: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/.../core/data/db/entity/DailyChallenge.kt
git add app/src/main/java/.../core/data/db/dao/DailyChallengeDao.kt
git add app/src/main/java/.../core/data/db/PhoenixDatabase.kt
git add app/src/main/java/.../di/DatabaseModule.kt
git commit -m "feat: DailyChallenge entity, DAO, and DB migration v4→v5"
```

---

### Task 2: Repository & Use Cases

**Covers:** [S4, S5, S6]

**Files:**
- Create: `app/src/main/java/.../core/domain/repository/DailyChallengeRepository.kt`
- Create: `app/src/main/java/.../core/data/repository/DailyChallengeRepositoryImpl.kt`
- Create: `app/src/main/java/.../core/domain/usecase/GetDailyChallengesUseCase.kt`
- Create: `app/src/main/java/.../core/domain/usecase/ClaimChallengeRewardUseCase.kt`
- Create: `app/src/main/java/.../core/domain/usecase/GenerateDailyChallengesUseCase.kt`
- Modify: `app/src/main/java/.../di/AppModule.kt` — bind repository

**Interfaces:**
- Consumes: `DailyChallengeDao` (from Task 1), `StatsRepository` (existing)
- Produces: Repository interface + impl, 3 use cases

- [ ] **Step 1: Create Repository interface**

Create `app/src/main/java/.../core/domain/repository/DailyChallengeRepository.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import kotlinx.coroutines.flow.Flow

interface DailyChallengeRepository {
    fun getTodayChallenges(): Flow<List<DailyChallenge>>
    suspend fun getTodayChallengesOnce(): List<DailyChallenge>
    suspend fun insertChallenge(challenge: DailyChallenge): Long
    suspend fun updateProgress(id: Long, current: Int)
    suspend fun markCompleted(id: Long)
    suspend fun markClaimed(id: Long)
    suspend fun getCompletedCountToday(): Int
    suspend fun getTotalCountToday(): Int
}
```

- [ ] **Step 2: Create Repository implementation**

Create `app/src/main/java/.../core/data/repository/DailyChallengeRepositoryImpl.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DailyChallengeDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.DailyChallengeRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class DailyChallengeRepositoryImpl @Inject constructor(
    private val dao: DailyChallengeDao
) : DailyChallengeRepository {

    private fun todayDate(): String = LocalDate.now().toString()

    override fun getTodayChallenges(): Flow<List<DailyChallenge>> {
        return dao.getChallengesByDate(todayDate())
    }

    override suspend fun getTodayChallengesOnce(): List<DailyChallenge> {
        return dao.getChallengesByDateOnce(todayDate())
    }

    override suspend fun insertChallenge(challenge: DailyChallenge): Long {
        return dao.insertChallenge(challenge)
    }

    override suspend fun updateProgress(id: Long, current: Int) {
        dao.updateChallengeProgress(id, current)
    }

    override suspend fun markCompleted(id: Long) {
        dao.markCompleted(id)
    }

    override suspend fun markClaimed(id: Long) {
        dao.markClaimed(id)
    }

    override suspend fun getCompletedCountToday(): Int {
        return dao.getCompletedCountByDate(todayDate())
    }

    override suspend fun getTotalCountToday(): Int {
        return dao.getTotalCountByDate(todayDate())
    }
}
```

- [ ] **Step 3: Bind repository in AppModule**

Edit `app/src/main/java/.../di/AppModule.kt`:
```kotlin
@Binds
abstract fun bindDailyChallengeRepository(impl: DailyChallengeRepositoryImpl): DailyChallengeRepository
```

- [ ] **Step 4: Create GetDailyChallengesUseCase**

Create `app/src/main/java/.../core/domain/usecase/GetDailyChallengesUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.DailyChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyChallengesUseCase @Inject constructor(
    private val repository: DailyChallengeRepository
) {
    operator fun invoke(): Flow<List<DailyChallenge>> {
        return repository.getTodayChallenges()
    }
}
```

- [ ] **Step 5: Create ClaimChallengeRewardUseCase**

Create `app/src/main/java/.../core/domain/usecase/ClaimChallengeRewardUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.DailyChallengeRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class ClaimChallengeRewardUseCase @Inject constructor(
    private val challengeRepository: DailyChallengeRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(challenge: DailyChallenge) {
        if (challenge.completed && !challenge.claimed) {
            statsRepository.addXp(challenge.rewardXp)
            statsRepository.increasePhoenixEnergy(challenge.rewardEnergy)
            challengeRepository.markClaimed(challenge.id)
        }
    }
}
```

- [ ] **Step 6: Create GenerateDailyChallengesUseCase**

Create `app/src/main/java/.../core/domain/usecase/GenerateDailyChallengesUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.DailyChallengeRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

class GenerateDailyChallengesUseCase @Inject constructor(
    private val challengeRepository: DailyChallengeRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke() {
        val today = LocalDate.now().toString()
        val existing = challengeRepository.getTodayChallengesOnce()
        if (existing.isNotEmpty()) return

        val stats = statsRepository.getStatsOnce()
        val level = stats?.level ?: 1
        val challengeCount = Random.nextInt(2, 6)

        val challenges = generateChallenges(level, challengeCount, today)
        challenges.forEach { challengeRepository.insertChallenge(it) }
    }

    private fun generateChallenges(level: Int, count: Int, date: String): List<DailyChallenge> {
        val pool = mutableListOf<DailyChallenge>()

        // Easy challenges
        pool.add(DailyChallenge(
            title = "Complete a task",
            description = "Complete 1 task today",
            type = "COMPLETE_TASKS",
            target = 1,
            rewardXp = 20,
            rewardEnergy = 5,
            date = date
        ))
        pool.add(DailyChallenge(
            title = "Complete 2 tasks",
            description = "Complete 2 tasks today",
            type = "COMPLETE_TASKS",
            target = 2,
            rewardXp = 40,
            rewardEnergy = 8,
            date = date
        ))

        // Medium challenges
        pool.add(DailyChallenge(
            title = "Complete 3 tasks",
            description = "Complete 3 tasks today",
            type = "COMPLETE_TASKS",
            target = 3,
            rewardXp = 60,
            rewardEnergy = 10,
            date = date
        ))
        pool.add(DailyChallenge(
            title = "Hard task",
            description = "Complete a HARD difficulty task",
            type = "COMPLETE_HARD",
            target = 1,
            rewardXp = 80,
            rewardEnergy = 15,
            date = date
        ))
        pool.add(DailyChallenge(
            title = "Priority focus",
            description = "Complete 2 priority tasks",
            type = "COMPLETE_PRIORITY",
            target = 2,
            rewardXp = 70,
            rewardEnergy = 12,
            date = date
        ))

        // Hard challenges
        if (level >= 11) {
            pool.add(DailyChallenge(
                title = "XP grinder",
                description = "Earn 100 XP today",
                type = "EARN_XP",
                target = 100,
                rewardXp = 100,
                rewardEnergy = 20,
                date = date
            ))
            pool.add(DailyChallenge(
                title = "Task marathon",
                description = "Complete 5 tasks today",
                type = "COMPLETE_TASKS",
                target = 5,
                rewardXp = 150,
                rewardEnergy = 20,
                date = date
            ))
        }

        pool.shuffle()
        return pool.take(count)
    }
}
```

- [ ] **Step 7: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/.../core/domain/repository/DailyChallengeRepository.kt
git add app/src/main/java/.../core/data/repository/DailyChallengeRepositoryImpl.kt
git add app/src/main/java/.../core/domain/usecase/GetDailyChallengesUseCase.kt
git add app/src/main/java/.../core/domain/usecase/ClaimChallengeRewardUseCase.kt
git add app/src/main/java/.../core/domain/usecase/GenerateDailyChallengesUseCase.kt
git add app/src/main/java/.../di/AppModule.kt
git commit -m "feat: DailyChallenge repository, use cases, and generation logic"
```

---

### Task 3: Challenge Tracker & Worker

**Covers:** [S4, S5]

**Files:**
- Create: `app/src/main/java/.../core/domain/usecase/TrackDailyChallengeUseCase.kt`
- Create: `app/src/main/java/.../core/work/DailyChallengeResetWorker.kt`
- Modify: `app/src/main/java/.../di/WorkerModule.kt` — add reset worker
- Modify: `app/src/main/java/.../PhoenixApp.kt` — enqueue reset worker

**Interfaces:**
- Consumes: `DailyChallengeRepository`, `StatsRepository` (existing)
- Produces: Challenge tracker use case, reset worker

- [ ] **Step 1: Create TrackDailyChallengeUseCase**

Create `app/src/main/java/.../core/domain/usecase/TrackDailyChallengeUseCase.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.DailyChallengeRepository
import javax.inject.Inject

class TrackDailyChallengeUseCase @Inject constructor(
    private val challengeRepository: DailyChallengeRepository
) {
    suspend operator fun invoke(challenges: List<DailyChallenge>, completedTaskType: String? = null, earnedXp: Int = 0) {
        for (challenge in challenges) {
            if (challenge.completed) continue

            val shouldIncrement = when (challenge.type) {
                "COMPLETE_TASKS" -> completedTaskType != null
                "COMPLETE_HARD" -> completedTaskType == "HARD"
                "COMPLETE_PRIORITY" -> completedTaskType == "PRIORITY"
                "EARN_XP" -> earnedXp > 0
                else -> false
            }

            if (shouldIncrement) {
                val newCurrent = challenge.current + 1
                challengeRepository.updateProgress(challenge.id, newCurrent)
                if (newCurrent >= challenge.target) {
                    challengeRepository.markCompleted(challenge.id)
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create DailyChallengeResetWorker**

Create `app/src/main/java/.../core/work/DailyChallengeResetWorker.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GenerateDailyChallengesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyChallengeResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val generateDailyChallengesUseCase: GenerateDailyChallengesUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            generateDailyChallengesUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

- [ ] **Step 3: Add reset worker to WorkerModule**

Edit `app/src/main/java/.../di/WorkerModule.kt` — add:
```kotlin
fun enqueueDailyChallengeReset(workManager: WorkManager) {
    val request = PeriodicWorkRequestBuilder<DailyChallengeResetWorker>(
        24, TimeUnit.HOURS
    ).setInitialDelay(1, TimeUnit.HOURS)
        .build()

    workManager.enqueueUniquePeriodicWork(
        "daily_challenge_reset",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
```

- [ ] **Step 4: Enqueue worker on app start**

Edit `app/src/main/java/.../PhoenixApp.kt` — add in `onCreate()`:
```kotlin
WorkerModule.enqueueDailyChallengeReset(workManager)
```

- [ ] **Step 5: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/.../core/domain/usecase/TrackDailyChallengeUseCase.kt
git add app/src/main/java/.../core/work/DailyChallengeResetWorker.kt
git add app/src/main/java/.../di/WorkerModule.kt
git add app/src/main/java/.../PhoenixApp.kt
git commit -m "feat: daily challenge tracker and midnight reset worker"
```

---

### Task 4: Achievements

**Covers:** [S7]

**Files:**
- Modify: `app/src/main/java/.../di/DatabaseModule.kt` — add 3 new achievements to seeder

**Interfaces:**
- Consumes: existing achievement seeding in DatabaseModule
- Produces: 3 new achievement entries

- [ ] **Step 1: Add new achievements to onCreate seeder**

Edit `app/src/main/java/.../di/DatabaseModule.kt` — add to the `achievements` list in `onCreate()`:
```kotlin
Achievement("daily_warrior", "Daily Warrior", "Complete all challenges 7 days in a row", "emoji_events", "RARE", "daily_7"),
Achievement("challenge_master", "Challenge Master", "Complete 100 total challenges", "military_tech", "EPIC", "challenges_100"),
Achievement("perfect_day", "Perfect Day", "Complete all challenges in a single day", "star", "COMMON", "perfect_day")
```

- [ ] **Step 2: Add same achievements to MIGRATION_1_2**

Edit `app/src/main/java/.../di/DatabaseModule.kt` — add to the `achievements` array in `MIGRATION_1_2`:
```kotlin
arrayOf("daily_warrior", "Daily Warrior", "Complete all challenges 7 days in a row", "emoji_events", "RARE", "daily_7"),
arrayOf("challenge_master", "Challenge Master", "Complete 100 total challenges", "military_tech", "EPIC", "challenges_100"),
arrayOf("perfect_day", "Perfect Day", "Complete all challenges in a single day", "star", "COMMON", "perfect_day")
```

- [ ] **Step 3: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/.../di/DatabaseModule.kt
git commit -m "feat: daily challenge achievements (daily_warrior, challenge_master, perfect_day)"
```

---

### Task 5: Dashboard Card & Navigation

**Covers:** [S8]

**Files:**
- Create: `app/src/main/java/.../feature/challenges/DailyChallengeCard.kt`
- Modify: `app/src/main/java/.../core/navigation/Screen.kt` — add DailyChallenges route
- Modify: `app/src/main/java/.../core/navigation/NavGraph.kt` — wire route
- Modify: `app/src/main/java/.../feature/dashboard/DashboardScreen.kt` — add card
- Modify: `app/src/main/java/.../feature/drawer/DrawerScreen.kt` — add menu item

**Interfaces:**
- Consumes: `GetDailyChallengesUseCase` (from Task 2)
- Produces: Dashboard card composable, navigation routes

- [ ] **Step 1: Create DailyChallengeCard**

Create `app/src/main/java/.../feature/challenges/DailyChallengeCard.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.challenges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun DailyChallengeCard(
    challenges: List<DailyChallenge>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (challenges.isEmpty()) return

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
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
                            contentDescription = "Challenge",
                            tint = PhoenixOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.daily_challenges),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${challenges.count { it.completed }}/${challenges.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                challenges.take(3).forEach { challenge ->
                    ChallengeItem(challenge = challenge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (challenges.size > 3) {
                    Text(
                        text = stringResource(R.string.see_all),
                        style = MaterialTheme.typography.bodySmall,
                        color = PhoenixOrange,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeItem(challenge: DailyChallenge) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${challenge.current}/${challenge.target}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (challenge.completed) {
            Text(
                text = if (challenge.claimed) stringResource(R.string.challenge_claimed) else stringResource(R.string.challenge_claim),
                style = MaterialTheme.typography.labelSmall,
                color = PhoenixOrange
            )
        } else {
            LinearProgressIndicator(
                progress = { challenge.current.toFloat() / challenge.target },
                modifier = Modifier
                    .width(80.dp)
                    .height(6.dp),
                color = PhoenixOrange,
                trackColor = PhoenixOrange.copy(alpha = 0.2f)
            )
        }
    }
}
```

- [ ] **Step 2: Add DailyChallenges route**

Edit `app/src/main/java/.../core/navigation/Screen.kt`:
```kotlin
data object DailyChallenges : Screen("daily_challenges")
```

- [ ] **Step 3: Wire route in NavGraph**

Edit `app/src/main/java/.../core/navigation/NavGraph.kt` — add composable:
```kotlin
composable(Screen.DailyChallenges.route) {
    DailyChallengeScreen(navController = navController)
}
```

- [ ] **Step 4: Add card to Dashboard**

Edit `app/src/main/java/.../feature/dashboard/DashboardScreen.kt`:
- Inject `GetDailyChallengesUseCase`
- Collect challenges flow
- Add `DailyChallengeCard` below the boss card

- [ ] **Step 5: Add to Drawer**

Edit `app/src/main/java/.../feature/drawer/DrawerScreen.kt`:
- Add `onNavigateToDailyChallenges` callback
- Add DrawerMenuItem with Challenges icon

- [ ] **Step 6: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/.../feature/challenges/DailyChallengeCard.kt
git add app/src/main/java/.../core/navigation/Screen.kt
git add app/src/main/java/.../core/navigation/NavGraph.kt
git add app/src/main/java/.../feature/dashboard/DashboardScreen.kt
git add app/src/main/java/.../feature/drawer/DrawerScreen.kt
git commit -m "feat: DailyChallengeCard for dashboard and navigation routes"
```

---

### Task 6: Challenge Screen

**Covers:** [S8]

**Files:**
- Create: `app/src/main/java/.../feature/challenges/DailyChallengeViewModel.kt`
- Create: `app/src/main/java/.../feature/challenges/DailyChallengeScreen.kt`

**Interfaces:**
- Consumes: `GetDailyChallengesUseCase`, `ClaimChallengeRewardUseCase`, `GenerateDailyChallengesUseCase` (from Task 2)
- Produces: Challenge screen with claim functionality

- [ ] **Step 1: Create DailyChallengeViewModel**

Create `app/src/main/java/.../feature/challenges/DailyChallengeViewModel.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.ClaimChallengeRewardUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GenerateDailyChallengesUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetDailyChallengesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    private val getDailyChallengesUseCase: GetDailyChallengesUseCase,
    private val claimChallengeRewardUseCase: ClaimChallengeRewardUseCase,
    private val generateDailyChallengesUseCase: GenerateDailyChallengesUseCase
) : ViewModel() {

    private val _challenges = MutableStateFlow<List<DailyChallenge>>(emptyList())
    val challenges: StateFlow<List<DailyChallenge>> = _challenges.asStateFlow()

    init {
        viewModelScope.launch {
            generateDailyChallengesUseCase()
            getDailyChallengesUseCase().collect { _challenges.value = it }
        }
    }

    fun claimReward(challenge: DailyChallenge) {
        viewModelScope.launch {
            claimChallengeRewardUseCase(challenge)
        }
    }
}
```

- [ ] **Step 2: Create DailyChallengeScreen**

Create `app/src/main/java/.../feature/challenges/DailyChallengeScreen.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.challenges

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.CompletedGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    navController: NavController,
    viewModel: DailyChallengeViewModel = hiltViewModel()
) {
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.daily_challenges)) },
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

        if (challenges.isEmpty()) {
            Text(
                text = stringResource(R.string.challenge_no_challenges),
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(challenges) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onClaim = { viewModel.claimReward(challenge) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: DailyChallenge,
    onClaim: () -> Unit
) {
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
                        contentDescription = "Challenge",
                        tint = if (challenge.completed) CompletedGreen else PhoenixOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { challenge.current.toFloat() / challenge.target },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (challenge.completed) CompletedGreen else PhoenixOrange,
                trackColor = PhoenixOrange.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${challenge.current}/${challenge.target} • +${challenge.rewardXp} XP • +${challenge.rewardEnergy} Energy",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                if (challenge.completed && !challenge.claimed) {
                    Button(onClick = onClaim) {
                        Text(stringResource(R.string.challenge_claim))
                    }
                } else if (challenge.claimed) {
                    Text(
                        text = stringResource(R.string.challenge_claimed),
                        style = MaterialTheme.typography.labelSmall,
                        color = CompletedGreen
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/.../feature/challenges/DailyChallengeViewModel.kt
git add app/src/main/java/.../feature/challenges/DailyChallengeScreen.kt
git commit -m "feat: DailyChallengeScreen with claim functionality"
```

---

### Task 7: Localization

**Covers:** [S9]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

**Interfaces:**
- Consumes: string keys from spec [S9]
- Produces: bilingual string resources

- [ ] **Step 1: Add English strings**

Edit `app/src/main/res/values/strings.xml` — add:
```xml
<string name="daily_challenges">Daily Challenges</string>
<string name="challenge_complete_tasks">Complete %d tasks</string>
<string name="challenge_complete_hard">Complete a HARD task</string>
<string name="challenge_complete_priority">Complete %d priority tasks</string>
<string name="challenge_earn_xp">Earn %d XP</string>
<string name="challenge_complete_category">Complete tasks in %s</string>
<string name="challenge_claim">Claim Reward</string>
<string name="challenge_claimed">Claimed</string>
<string name="challenge_progress">%d/%d</string>
<string name="challenge_time_left">Time left: %s</string>
<string name="challenge_no_challenges">No challenges today</string>
<string name="daily_warrior">Daily Warrior</string>
<string name="challenge_master">Challenge Master</string>
<string name="perfect_day">Perfect Day</string>
<string name="see_all">See all</string>
```

- [ ] **Step 2: Add Persian strings**

Edit `app/src/main/res/values-fa/strings.xml` — add:
```xml
<string name="daily_challenges">چالش‌های روزانه</string>
<string name="challenge_complete_tasks">%d تسک رو کامل کن</string>
<string name="challenge_complete_hard">یه تسک سخت رو کامل کن</string>
<string name="challenge_complete_priority">%d تسک اولویت‌دار رو کامل کن</string>
<string name="challenge_earn_xp">%d ایکس‌پی کسب کن</string>
<string name="challenge_complete_category">تسک‌های %s رو کامل کن</string>
<string name="challenge_claim">دریافت پاداش</string>
<string name="challenge_claimed">دریافت شد</string>
<string name="challenge_progress">%d/%d</string>
<string name="challenge_time_left">زمان باقی‌مانده: %s</string>
<string name="challenge_no_challenges">امروز چالشی نیست</string>
<string name="daily_warrior">جنگجوی روزانه</string>
<string name="challenge_master">استاد چالش</string>
<string name="perfect_day">روز بی‌نقص</string>
<string name="see_all">همه رو ببین</string>
```

- [ ] **Step 3: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml
git add app/src/main/res/values-fa/strings.xml
git commit -m "feat: bilingual string resources for daily challenges"
```

---

### Task 8: Final Build Verification

**Covers:** [S1, S2, S3, S4, S5, S6, S7, S8, S9]

**Files:** All created/modified files

- [ ] **Step 1: Full build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Final commit if needed**

```bash
git add -A
git commit -m "feat: Phase 4 Daily Challenges complete"
```
