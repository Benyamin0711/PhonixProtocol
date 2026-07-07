# Dashboard Redesign Implementation Plan — Phoenix Protocol X

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the Dashboard to match the "Phoenix Protocol X" 60-day campaign system with daily missions, spirit system, academic tracking, dopamine control, and daily memory log.

**Architecture:** New Room entities for Campaign, DailyMission, Prayer, AcademicSubject. New Repository + Use Cases. Complete DashboardScreen rewrite with dark theme, glowing orange accents, 60-day grid, and mission toggles.

**Tech Stack:** Room, Hilt, Jetpack Compose, Material 3, Kotlin

## Global Constraints

- Dark theme only (#050505 base, #FF5E00 accent)
- All new strings: English + Persian (Finglish for user communication)
- Database at v11 — new migration MIGRATION_11_12
- Follow existing patterns: entity → dao → repository → use case → viewmodel → screen
- `@Inject constructor` for use cases, `@HiltViewModel` for ViewModels
- `collectAsStateWithLifecycle()` for StateFlow collection
- `WhileSubscribed(5000)` for ViewModel-backed StateFlows

---

## File Structure

### New Files
- `core/data/db/entity/Campaign.kt` — 60-day campaign state
- `core/data/db/entity/CampaignDay.kt` — Per-day data (missions, prayers, note, relapse)
- `core/data/db/entity/DailyMission.kt` — Mission definition (name, xp)
- `core/data/db/entity/Prayer.kt` — Prayer definition (name, xp)
- `core/data/db/entity/AcademicSubject.kt` — Academic subject (name, progress, risk)
- `core/data/db/dao/CampaignDao.kt` — DAO for campaign CRUD
- `core/domain/repository/CampaignRepository.kt` — Repository interface
- `core/data/repository/CampaignRepositoryImpl.kt` — Repository implementation
- `core/domain/usecase/campaign/GetCampaignStateUseCase.kt`
- `core/domain/usecase/campaign/ToggleMissionUseCase.kt`
- `core/domain/usecase/campaign/TogglePrayerUseCase.kt`
- `core/domain/usecase/campaign/SaveDailyNoteUseCase.kt`
- `core/domain/usecase/campaign/NavigateDayUseCase.kt`
- `core/domain/usecase/campaign/RecordRelapseUseCase.kt`
- `core/domain/usecase/campaign/RecoverFromAshUseCase.kt`
- `core/domain/usecase/campaign/ResetDayUseCase.kt`
- `feature/dashboard/DashboardViewModel.kt` — NEW (replaces old)
- `feature/dashboard/DashboardScreen.kt` — REWRITE
- `feature/dashboard/components/HeroSection.kt`
- `feature/dashboard/components/StatsGrid.kt`
- `feature/dashboard/components/MissionsSection.kt`
- `feature/dashboard/components/SpiritSection.kt`
- `feature/dashboard/components/DailyNoteSection.kt`
- `feature/dashboard/components/AcademicSection.kt`
- `feature/dashboard/components/CampaignGrid.kt`
- `feature/dashboard/components/DopamineControl.kt`

### Modified Files
- `core/data/db/PhoenixDatabase.kt` — add entities + DAO, bump to v12
- `di/DatabaseModule.kt` — add MIGRATION_11_12 + provide CampaignDao
- `di/AppModule.kt` — bind CampaignRepository
- `core/navigation/Screen.kt` — update if needed
- `res/values/strings.xml` — add dashboard strings
- `res/values-fa/strings.xml` — add dashboard strings

---

## Rank System (from HTML)

| Rank | XP Range |
|------|----------|
| D — Recovering | 0-149 |
| C — Rising | 150-449 |
| B — Forged | 450-899 |
| A — Dangerous | 900-1499 |
| S — Phoenix | 1500+ |

Level = floor(totalXP / 120) + 1
XP bar = (totalXP % 120) / 120 * 100%

## Default Missions (12 items)

| Mission | XP |
|---------|-----|
| 3 saat mothale'e daneshgah | 30 |
| hal tamrin riazi | 20 |
| hal tamrin fizik | 20 |
| kodnevisi HabitAway | 25 |
| baghesh / tamrin | 20 |
| bedoon porn | 30 |
| gitar | 10 |
| mothale'e azad | 10 |
| journal shab | 10 |
| scroll control shode | 15 |
| khabe muntazam | 15 |
| marrat sazie mahal | 5 |

## Default Prayers (5 items)

| Prayer | XP |
|--------|-----|
| namaz sobh | 8 |
| namaz zohr | 5 |
| namaz asr | 5 |
| namaz maghrib | 5 |
| namaz esha | 5 |

---

## Task 1: Data Entities + Migration + DAO + Database

**Covers:** Data model for Campaign, DayData, Missions, Prayers, Academic

**Files:**
- Create: `core/data/db/entity/Campaign.kt`
- Create: `core/data/db/entity/CampaignDay.kt`
- Create: `core/data/db/entity/DailyMission.kt`
- Create: `core/data/db/entity/Prayer.kt`
- Create: `core/data/db/entity/AcademicSubject.kt`
- Create: `core/data/db/dao/CampaignDao.kt`
- Modify: `core/data/db/PhoenixDatabase.kt`
- Modify: `di/DatabaseModule.kt`

- [ ] **Step 1: Create Campaign entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey val id: Long = 1,
    val currentDay: Int = 1,
    val totalXp: Int = 0,
    val startDate: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create CampaignDay entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaign_days")
data class CampaignDay(
    @PrimaryKey val dayNumber: Int,
    val completedMissions: String = "", // comma-separated indices
    val completedPrayers: String = "", // comma-separated indices
    val note: String = "",
    val relapseCount: Int = 0,
    val isAsh: Boolean = false
)
```

- [ ] **Step 3: Create DailyMission entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_missions")
data class DailyMission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val xp: Int,
    val isActive: Boolean = true
)
```

- [ ] **Step 4: Create Prayer entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayers")
data class Prayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val xp: Int,
    val isActive: Boolean = true
)
```

- [ ] **Step 5: Create AcademicSubject entity**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_subjects")
data class AcademicSubject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val progress: Float = 0f, // 0-100
    val riskLevel: String = "NORMAL", // BOSS_FIGHT, HIGH_RISK, MAIN_SKILL, NORMAL
    val isActive: Boolean = true
)
```

- [ ] **Step 6: Create CampaignDao**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.AcademicSubject
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    // Campaign
    @Query("SELECT * FROM campaigns WHERE id = 1")
    fun getCampaign(): Flow<Campaign?>

    @Query("SELECT * FROM campaigns WHERE id = 1")
    suspend fun getCampaignOnce(): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    // Campaign Days
    @Query("SELECT * FROM campaign_days WHERE dayNumber = :day")
    suspend fun getDay(day: Int): CampaignDay?

    @Query("SELECT * FROM campaign_days")
    fun getAllDays(): Flow<List<CampaignDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: CampaignDay)

    @Update
    suspend fun updateDay(day: CampaignDay)

    // Missions
    @Query("SELECT * FROM daily_missions WHERE isActive = 1")
    fun getMissions(): Flow<List<DailyMission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: DailyMission)

    // Prayers
    @Query("SELECT * FROM prayers WHERE isActive = 1")
    fun getPrayers(): Flow<List<Prayer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayer: Prayer)

    // Academic
    @Query("SELECT * FROM academic_subjects WHERE isActive = 1")
    fun getSubjects(): Flow<List<AcademicSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: AcademicSubject)

    @Update
    suspend fun updateSubject(subject: AcademicSubject)
}
```

- [ ] **Step 7: Update PhoenixDatabase**

Add to entities array: `Campaign::class`, `CampaignDay::class`, `DailyMission::class`, `Prayer::class`, `AcademicSubject::class`
Change `version = 11` to `version = 12`
Add: `abstract fun campaignDao(): CampaignDao`

- [ ] **Step 8: Update DatabaseModule — add MIGRATION_11_12 + provide CampaignDao**

Add migration:

```kotlin
private val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Campaigns table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS campaigns " +
            "(id INTEGER PRIMARY KEY NOT NULL, " +
            "currentDay INTEGER NOT NULL DEFAULT 1, " +
            "totalXp INTEGER NOT NULL DEFAULT 0, " +
            "startDate INTEGER NOT NULL)"
        )
        // Insert default campaign
        db.execSQL("INSERT INTO campaigns (id, currentDay, totalXp, startDate) VALUES (1, 1, 0, ${System.currentTimeMillis()})")

        // Campaign days table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS campaign_days " +
            "(dayNumber INTEGER PRIMARY KEY NOT NULL, " +
            "completedMissions TEXT NOT NULL DEFAULT '', " +
            "completedPrayers TEXT NOT NULL DEFAULT '', " +
            "note TEXT NOT NULL DEFAULT '', " +
            "relapseCount INTEGER NOT NULL DEFAULT 0, " +
            "isAsh INTEGER NOT NULL DEFAULT 0)"
        )

        // Daily missions table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS daily_missions " +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "name TEXT NOT NULL, " +
            "xp INTEGER NOT NULL, " +
            "isActive INTEGER NOT NULL DEFAULT 1)"
        )
        // Seed default missions
        val missions = arrayOf(
            arrayOf("3 saat mothale'e daneshgah", "30"),
            arrayOf("hal tamrin riazi", "20"),
            arrayOf("hal tamrin fizik", "20"),
            arrayOf("kodnevisi HabitAway", "25"),
            arrayOf("baghesh / tamrin", "20"),
            arrayOf("bedoon porn", "30"),
            arrayOf("gitar", "10"),
            arrayOf("mothale'e azad", "10"),
            arrayOf("journal shab", "10"),
            arrayOf("scroll control shode", "15"),
            arrayOf("khabe muntazam", "15"),
            arrayOf("marrat sazie mahal", "5")
        )
        missions.forEach { m ->
            db.execSQL(
                "INSERT INTO daily_missions (name, xp, isActive) VALUES (?, ?, 1)",
                m
            )
        }

        // Prayers table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS prayers " +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "name TEXT NOT NULL, " +
            "xp INTEGER NOT NULL, " +
            "isActive INTEGER NOT NULL DEFAULT 1)"
        )
        // Seed default prayers
        val prayers = arrayOf(
            arrayOf("namaz sobh", "8"),
            arrayOf("namaz zohr", "5"),
            arrayOf("namaz asr", "5"),
            arrayOf("namaz maghrib", "5"),
            arrayOf("namaz esha", "5")
        )
        prayers.forEach { p ->
            db.execSQL(
                "INSERT INTO prayers (name, xp, isActive) VALUES (?, ?, 1)",
                p
            )
        }

        // Academic subjects table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS academic_subjects " +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "name TEXT NOT NULL, " +
            "progress REAL NOT NULL DEFAULT 0, " +
            "riskLevel TEXT NOT NULL DEFAULT 'NORMAL', " +
            "isActive INTEGER NOT NULL DEFAULT 1)"
        )
        // Seed default subjects
        val subjects = arrayOf(
            arrayOf("riazi 1", "18", "BOSS_FIGHT"),
            arrayOf("fizik", "15", "HIGH_RISK"),
            arrayOf("barnameh nevisi", "35", "MAIN_SKILL")
        )
        subjects.forEach { s ->
            db.execSQL(
                "INSERT INTO academic_subjects (name, progress, riskLevel, isActive) VALUES (?, ?, ?, 1)",
                s
            )
        }
    }
}
```

Add `MIGRATION_11_12` to the `addMigrations()` call. Add provider:

```kotlin
@Provides
fun provideCampaignDao(db: PhoenixDatabase): CampaignDao = db.campaignDao()
```

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/Campaign.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/CampaignDay.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/DailyMission.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/Prayer.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/AcademicSubject.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/CampaignDao.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/PhoenixDatabase.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/DatabaseModule.kt
git commit -m "feat(campaign): add Campaign, DayData, Mission, Prayer, Academic entities and DAO with DB v12"
```

---

## Task 2: Repository + DI

**Files:**
- Create: `core/domain/repository/CampaignRepository.kt`
- Create: `core/data/repository/CampaignRepositoryImpl.kt`
- Modify: `di/AppModule.kt`

- [ ] **Step 1: Create CampaignRepository interface**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.AcademicSubject
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    fun getCampaign(): Flow<Campaign?>
    suspend fun getCampaignOnce(): Campaign?
    suspend fun updateCampaign(campaign: Campaign)
    suspend fun getDay(day: Int): CampaignDay?
    fun getAllDays(): Flow<List<CampaignDay>>
    suspend fun saveDay(day: CampaignDay)
    fun getMissions(): Flow<List<DailyMission>>
    fun getPrayers(): Flow<List<Prayer>>
    fun getSubjects(): Flow<List<AcademicSubject>>
    suspend fun updateSubject(subject: AcademicSubject)
}
```

- [ ] **Step 2: Create CampaignRepositoryImpl**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CampaignDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.AcademicSubject
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CampaignRepositoryImpl @Inject constructor(
    private val dao: CampaignDao
) : CampaignRepository {

    override fun getCampaign(): Flow<Campaign?> = dao.getCampaign()
    override suspend fun getCampaignOnce(): Campaign? = dao.getCampaignOnce()
    override suspend fun updateCampaign(campaign: Campaign) = dao.updateCampaign(campaign)
    override suspend fun getDay(day: Int): CampaignDay? = dao.getDay(day)
    override fun getAllDays(): Flow<List<CampaignDay>> = dao.getAllDays()
    override suspend fun saveDay(day: CampaignDay) = dao.insertDay(day)
    override fun getMissions(): Flow<List<DailyMission>> = dao.getMissions()
    override fun getPrayers(): Flow<List<Prayer>> = dao.getPrayers()
    override fun getSubjects(): Flow<List<AcademicSubject>> = dao.getSubjects()
    override suspend fun updateSubject(subject: AcademicSubject) = dao.updateSubject(subject)
}
```

- [ ] **Step 3: Update AppModule — add CampaignRepository binding**

Add to AppModule:

```kotlin
import com.benyaminrasouli.phoenixprotocol.core.data.repository.CampaignRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository

// In AppModule class:
@Binds
@Singleton
abstract fun bindCampaignRepository(impl: CampaignRepositoryImpl): CampaignRepository
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/CampaignRepository.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/CampaignRepositoryImpl.kt \
  app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/AppModule.kt
git commit -m "feat(campaign): add CampaignRepository interface and implementation"
```

---

## Task 3: Use Cases

**Files:**
- Create: `core/domain/usecase/campaign/GetCampaignStateUseCase.kt`
- Create: `core/domain/usecase/campaign/ToggleMissionUseCase.kt`
- Create: `core/domain/usecase/campaign/TogglePrayerUseCase.kt`
- Create: `core/domain/usecase/campaign/SaveDailyNoteUseCase.kt`
- Create: `core/domain/usecase/campaign/NavigateDayUseCase.kt`
- Create: `core/domain/usecase/campaign/RecordRelapseUseCase.kt`
- Create: `core/domain/usecase/campaign/RecoverFromAshUseCase.kt`
- Create: `core/domain/usecase/campaign/ResetDayUseCase.kt`

- [ ] **Step 1: Create GetCampaignStateUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.AcademicSubject
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import kotlinx.coroutines.flow.Flow

data class CampaignState(
    val campaign: Campaign?,
    val currentDay: CampaignDay?,
    val allDays: List<CampaignDay>,
    val missions: List<DailyMission>,
    val prayers: List<Prayer>,
    val subjects: List<AcademicSubject>,
    val totalXp: Int = 0,
    val level: Int = 1,
    val rank: String = "D — Recovering",
    val xpProgress: Float = 0f,
    val discipline: Int = 0,
    val isAsh: Boolean = false
)

class GetCampaignStateUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    operator fun invoke(): Flow<CampaignState> {
        // This will be implemented as a combine flow
        // Returns current campaign state with all data
    }
}
```

- [ ] **Step 2: Create ToggleMissionUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class ToggleMissionUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int, missionIndex: Int) {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        val current = day.completedMissions.split(",").filter { it.isNotBlank() }.map { it.toInt() }
        val newMissions = if (missionIndex in current) {
            current.filter { it != missionIndex }
        } else {
            current + missionIndex
        }
        repository.saveDay(day.copy(completedMissions = newMissions.joinToString(",")))
    }
}
```

- [ ] **Step 3: Create TogglePrayerUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class TogglePrayerUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int, prayerIndex: Int) {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        val current = day.completedPrayers.split(",").filter { it.isNotBlank() }.map { it.toInt() }
        val newPrayers = if (prayerIndex in current) {
            current.filter { it != prayerIndex }
        } else {
            current + prayerIndex
        }
        repository.saveDay(day.copy(completedPrayers = newPrayers.joinToString(",")))
    }
}
```

- [ ] **Step 4: Create SaveDailyNoteUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class SaveDailyNoteUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int, note: String) {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        repository.saveDay(day.copy(note = note))
    }
}
```

- [ ] **Step 5: Create NavigateDayUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class NavigateDayUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(newDay: Int): Int {
        val campaign = repository.getCampaignOnce() ?: return 1
        val clampedDay = newDay.coerceIn(1, 60)
        repository.updateCampaign(campaign.copy(currentDay = clampedDay))
        return clampedDay
    }
}
```

- [ ] **Step 6: Create RecordRelapseUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class RecordRelapseUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int): Boolean {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        val newCount = day.relapseCount + 1
        val isAsh = newCount >= 2
        repository.saveDay(day.copy(relapseCount = newCount, isAsh = isAsh))
        return isAsh
    }
}
```

- [ ] **Step 7: Create RecoverFromAshUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class RecoverFromAshUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int) {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        repository.saveDay(day.copy(isAsh = false, relapseCount = 0))
    }
}
```

- [ ] **Step 8: Create ResetDayUseCase**

```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class ResetDayUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int) {
        repository.saveDay(CampaignDay(dayNumber = dayNumber))
    }
}
```

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/campaign/
git commit -m "feat(campaign): add campaign use cases (Get, ToggleMission, TogglePrayer, Note, Navigate, Relapse, Recover, Reset)"
```

---

## Task 4: DashboardViewModel + UI Components

**Files:**
- Create: `feature/dashboard/DashboardViewModel.kt` (REPLACE old)
- Create: `feature/dashboard/components/HeroSection.kt`
- Create: `feature/dashboard/components/StatsGrid.kt`
- Create: `feature/dashboard/components/MissionsSection.kt`
- Create: `feature/dashboard/components/SpiritSection.kt`
- Create: `feature/dashboard/components/DailyNoteSection.kt`
- Create: `feature/dashboard/components/AcademicSection.kt`
- Create: `feature/dashboard/components/CampaignGrid.kt`
- Create: `feature/dashboard/components/DopamineControl.kt`

- [ ] **Step 1: Create DashboardViewModel**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val currentDay: Int = 1,
    val totalXp: Int = 0,
    val level: Int = 1,
    val rank: String = "D — Recovering",
    val xpProgress: Float = 0f,
    val discipline: Int = 0,
    val isAsh: Boolean = false,
    val currentDayData: CampaignDay? = null,
    val allDays: List<CampaignDay> = emptyList(),
    val missions: List<MissionItem> = emptyList(),
    val prayers: List<PrayerItem> = emptyList(),
    val subjects: List<SubjectItem> = emptyList(),
    val note: String = ""
)

data class MissionItem(
    val name: String,
    val xp: Int,
    val isCompleted: Boolean
)

data class PrayerItem(
    val name: String,
    val xp: Int,
    val isCompleted: Boolean
)

data class SubjectItem(
    val name: String,
    val progress: Float,
    val riskLevel: String
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCampaignStateUseCase: GetCampaignStateUseCase,
    private val toggleMissionUseCase: ToggleMissionUseCase,
    private val togglePrayerUseCase: TogglePrayerUseCase,
    private val saveDailyNoteUseCase: SaveDailyNoteUseCase,
    private val navigateDayUseCase: NavigateDayUseCase,
    private val recordRelapseUseCase: RecordRelapseUseCase,
    private val recoverFromAshUseCase: RecoverFromAshUseCase,
    private val resetDayUseCase: ResetDayUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init {
        viewModelScope.launch {
            getCampaignStateUseCase().collect { campaignState ->
                _state.value = DashboardState(
                    currentDay = campaignState.campaign?.currentDay ?: 1,
                    totalXp = campaignState.totalXp,
                    level = campaignState.level,
                    rank = campaignState.rank,
                    xpProgress = campaignState.xpProgress,
                    discipline = campaignState.discipline,
                    isAsh = campaignState.isAsh,
                    currentDayData = campaignState.currentDay,
                    allDays = campaignState.allDays,
                    missions = campaignState.missions.mapIndexed { index, m ->
                        MissionItem(
                            name = m.name,
                            xp = m.xp,
                            isCompleted = campaignState.currentDay?.completedMissions
                                ?.split(",")
                                ?.filter { it.isNotBlank() }
                                ?.map { it.toInt() }
                                ?.contains(index) == true
                        )
                    },
                    prayers = campaignState.prayers.mapIndexed { index, p ->
                        PrayerItem(
                            name = p.name,
                            xp = p.xp,
                            isCompleted = campaignState.currentDay?.completedPrayers
                                ?.split(",")
                                ?.filter { it.isNotBlank() }
                                ?.map { it.toInt() }
                                ?.contains(index) == true
                        )
                    },
                    subjects = campaignState.subjects.map { s ->
                        SubjectItem(
                            name = s.name,
                            progress = s.progress,
                            riskLevel = s.riskLevel
                        )
                    },
                    note = campaignState.currentDay?.note ?: ""
                )
            }
        }
    }

    fun toggleMission(index: Int) {
        viewModelScope.launch {
            toggleMissionUseCase(_state.value.currentDay, index)
        }
    }

    fun togglePrayer(index: Int) {
        viewModelScope.launch {
            togglePrayerUseCase(_state.value.currentDay, index)
        }
    }

    fun saveNote(note: String) {
        viewModelScope.launch {
            saveDailyNoteUseCase(_state.value.currentDay, note)
        }
    }

    fun nextDay() {
        viewModelScope.launch {
            navigateDayUseCase(_state.value.currentDay + 1)
        }
    }

    fun prevDay() {
        viewModelScope.launch {
            navigateDayUseCase(_state.value.currentDay - 1)
        }
    }

    fun goToDay(day: Int) {
        viewModelScope.launch {
            navigateDayUseCase(day)
        }
    }

    fun recordRelapse() {
        viewModelScope.launch {
            recordRelapseUseCase(_state.value.currentDay)
        }
    }

    fun recover() {
        viewModelScope.launch {
            recoverFromAshUseCase(_state.value.currentDay)
        }
    }

    fun resetDay() {
        viewModelScope.launch {
            resetDayUseCase(_state.value.currentDay)
        }
    }
}
```

- [ ] **Step 2: Create HeroSection component**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

@Composable
fun HeroSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF141414), Color(0xFF090909))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Phoenix Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PhoenixOrange, Color(0xFF3D1400))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\uD83D\uDC26\u200D\uD83D\uDD25",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PHOENIX PROTOCOL X",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = PhoenixOrange,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "60 Day Tactical Rebuild System\nFrom Ashes We Rise.",
                fontSize = 14.sp,
                color = Color(0xFF8E8E8E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quote
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "«از خاکسترها برمی‌خیزیم.»",
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E8E),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
```

- [ ] **Step 3: Create StatsGrid component**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

@Composable
fun StatsGrid(
    rank: String,
    level: Int,
    totalXp: Int,
    discipline: Int,
    xpProgress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Row 1: Rank + Level
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "RANK",
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = rank,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PhoenixOrange
                )
            }

            StatCard(
                title = "LEVEL",
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$level",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = PhoenixOrange,
                    trackColor = Color(0xFF1F1F1F)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 2: XP + Discipline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "TOTAL XP",
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$totalXp",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            StatCard(
                title = "DISCIPLINE",
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$discipline%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00C853)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF101010))
            .padding(18.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            color = Color(0xFF8E8E8E)
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
```

- [ ] **Step 4: Create MissionsSection component**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.MissionItem
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

@Composable
fun MissionsSection(
    missions: List<MissionItem>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF101010))
            .padding(18.dp)
    ) {
        Text(
            text = "DAILY MISSIONS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PhoenixOrange
        )

        Spacer(modifier = Modifier.height(18.dp))

        missions.forEachIndexed { index, mission ->
            MissionRow(
                mission = mission,
                onToggle = { onToggle(index) }
            )
            if (index < missions.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MissionRow(
    mission: MissionItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF181818))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mission.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (mission.isCompleted) Color(0xFF00C853) else Color.White
            )
            Text(
                text = "+${mission.xp} XP",
                fontSize = 12.sp,
                color = Color(0xFFFFB300)
            )
        }

        Button(
            onClick = onToggle,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (mission.isCompleted) Color(0xFF00C853) else Color(0xFF1F1F1F)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (mission.isCompleted) "UNDO" else "START",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

- [ ] **Step 5: Create SpiritSection, DailyNoteSection, AcademicSection, CampaignGrid, DopamineControl**

(Similar pattern to MissionsSection — create each as a separate composable file)

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/
git commit -m "feat(campaign): add DashboardViewModel and UI components for Phoenix Protocol X dashboard"
```

---

## Task 5: DashboardScreen Rewrite + Navigation

**Files:**
- Modify: `feature/dashboard/DashboardScreen.kt` (REWRITE)
- Modify: `core/navigation/Screen.kt` (if needed)
- Modify: `res/values/strings.xml`
- Modify: `res/values-fa/strings.xml`

- [ ] **Step 1: Rewrite DashboardScreen**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.components.*
import com.benyaminrasouli.phoenixprotocol.feature.drawer.DrawerScreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerScreen(
                    // ... existing callbacks
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF050505))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero
            HeroSection()

            // Day Navigation
            DayNavigation(
                currentDay = state.currentDay,
                onPrevDay = { viewModel.prevDay() },
                onNextDay = { viewModel.nextDay() }
            )

            // Stats Grid
            StatsGrid(
                rank = state.rank,
                level = state.level,
                totalXp = state.totalXp,
                discipline = state.discipline,
                xpProgress = state.xpProgress
            )

            // Missions
            MissionsSection(
                missions = state.missions,
                onToggle = { viewModel.toggleMission(it) }
            )

            // Spirit System
            SpiritSection(
                prayers = state.prayers,
                onToggle = { viewModel.togglePrayer(it) }
            )

            // Daily Note
            DailyNoteSection(
                note = state.note,
                onNoteChange = { viewModel.saveNote(it) }
            )

            // Academic
            AcademicSection(subjects = state.subjects)

            // 60 Day Campaign
            CampaignGrid(
                currentDay = state.currentDay,
                allDays = state.allDays,
                onDayClick = { viewModel.goToDay(it) }
            )

            // Dopamine Control
            DopamineControl(
                isAsh = state.isAsh,
                onRelapse = { viewModel.recordRelapse() },
                onRecover = { viewModel.recover() },
                onReset = { viewModel.resetDay() }
            )
        }
    }
}
```

- [ ] **Step 2: Add string resources**

```xml
<!-- Dashboard X -->
<string name="dashboard_title">PHOENIX PROTOCOL X</string>
<string name="dashboard_subtitle">60 Day Tactical Rebuild System</string>
<string name="dashboard_quote">«از خاکسترها برمی‌خیزیم.»</string>
<string name="dashboard_missions">DAILY MISSIONS</string>
<string name="dashboard_spirit">SPIRIT SYSTEM</string>
<string name="dashboard_note">DAILY MEMORY LOG</string>
<string name="dashboard_academic">ACADEMIC WAR ROOM</string>
<string name="dashboard_campaign">60 DAY CAMPAIGN</string>
<string name="dashboard_dopamine">DOPAMINE CONTROL</string>
<string name="dashboard_prev">روز قبل</string>
<string name="dashboard_next">روز بعد</string>
<string name="dashboard_start">START</string>
<string name="dashboard_undo">UNDO</string>
<string name="dashboard_relapese">ثبت Relapse</string>
<string name="dashboard_recover">Recovery</string>
<string name="dashboard_reset">ریست روز</string>
<string name="dashboard_ash_warning">ASH MODE ACTIVE\nتمرکز افت کرده.\nکنترل ذهن ضعیف شده.</string>
<string name="dashboard_recovery_warning">RECOVERY MODE ACTIVE\nبازگشت ثبت شد.</string>
<string name="dashboard_stable">وضعیت پایدار.\nهنوز وارد Ash Mode نشده‌ای.</string>
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt \
  app/src/main/res/values/strings.xml \
  app/src/main/res/values-fa/strings.xml
git commit -m "feat(campaign): rewrite DashboardScreen for Phoenix Protocol X with 60-day campaign UI"
```

---

## Summary

| Task | Files | Description |
|------|-------|-------------|
| 1 | 8 files | Entities + DAO + Migration + DB |
| 2 | 3 files | Repository + DI |
| 3 | 8 files | Use Cases |
| 4 | 9 files | ViewModel + UI Components |
| 5 | 3 files | DashboardScreen + Strings |
| **Total** | **31 files** | Complete dashboard redesign |
