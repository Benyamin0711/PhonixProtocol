# Phase 12: Habit Tracker — Design Spec

## [S1] Problem

The app has tasks for one-off work and boss missions for gamified challenges, but no system for tracking recurring daily habits with numeric targets (e.g., "drink 8 glasses of water", "read 30 pages"). Users need a way to build consistency through daily habit tracking with streaks and visual progress.

## [S2] Data Model

### Habit Entity

```kotlin
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val target: Int,              // daily target (e.g. 8)
    val unit: String,             // "glasses", "pages", "minutes"
    val categoryId: Long?,        // FK to categories table
    val color: String = "#FF6B35",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
```

### HabitLog Entity

```kotlin
@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String,             // "2026-07-03" format
    val value: Int,               // amount completed today
    val createdAt: Long = System.currentTimeMillis()
)
```

### Database Migration

MIGRATION_10_11 creates both tables. DB version → 11.

## [S3] DAO & Repository

### HabitDao

- `getAllHabits()` → `Flow<List<Habit>>` (active habits only)
- `getHabitById(id)` → `Habit?` (suspend)
- `insertHabit(habit)` → Long
- `updateHabit(habit)` / `deleteHabit(habit)`
- `getLogsForDate(date)` → `Flow<List<HabitLog>>` (all habits' logs for a day)
- `getLogsForHabit(habitId, startDate, endDate)` → `Flow<List<HabitLog>>` (for heatmap)
- `getTodayLog(habitId, date)` → `HabitLog?` (suspend)
- `insertOrUpdateLog(log)` — upsert by (habitId, date) unique constraint

### HabitRepository

Wraps DAO calls, computes streaks, handles XP rewards on target completion.

- `getStreak(habitId)` — counts consecutive days where `log.value >= habit.target`
- `logHabit(habitId, date, value)` — inserts/updates log, awards XP if target hit, checks streak achievements

## [S4] Use Cases

- `CreateHabitUseCase` — validates and inserts a new habit
- `LogHabitUseCase` — logs daily value, triggers XP + streak check
- `GetHabitsWithLogsUseCase` — returns habits merged with today's log status
- `GetHabitStreakUseCase` — computes current + best streak
- `DeleteHabitUseCase` — hard delete habit and its logs

## [S5] UI

### HabitsScreen (Full Screen)

Accessible from drawer menu + dashboard card.

- Top bar: "Habits" title + add button
- Today's habits list: each card shows habit name, target/unit, current value, +/- buttons, streak count
- Streak flame icon with day count
- Completed habits (value >= target) get a checkmark overlay

### Add/Edit Habit Dialog

Bottom sheet with:
- Name, description, target number, unit text
- Category dropdown (from existing categories)
- Color picker (preset palette matching dark theme)
- Reminder toggle + time picker

### Habit Detail Screen

Tap a habit card to view:
- Calendar heatmap (monthly view, color intensity = % of target met)
- Current streak + best streak stats
- History log list below calendar

### Dashboard Card

Compact summary:
- "X/Y habits completed today" progress bar
- Tap to navigate to full Habits screen

## [S6] Gamification

### XP Rewards

- Log a habit at/above target: +10 XP base
- Maintain a 7-day streak: +50 XP bonus
- Maintain a 30-day streak: +200 XP bonus
- XP awarded via existing `StatsRepository.addXp()`

### Shadow System

Missing a habit target does NOT increase shadow (habits are voluntary).

### New Achievements

- `habit_streak_7` — RARE — Maintain 7-day habit streak
- `habit_streak_30` — EPIC — Maintain 30-day habit streak
- `habit_master` — LEGENDARY — Complete all habits for 7 consecutive days

## [S7] Notifications

- WorkManager periodic check for enabled reminders
- Per-habit reminder at configured time
- Uses existing notification channel pattern
- Requires MIGRATION_10_11 to add tables

## [S8] Navigation

- Add `Habits` screen route to `Screen.kt`
- Add `HabitDetail` screen route (with habitId argument)
- Add drawer menu item "Habits" with `Icons.AutoMirrored.Filled.List` or `repeat` icon
- Wire navigation in `NavGraph.kt`

## [S9] File List

New files:
- `core/data/db/entity/Habit.kt`
- `core/data/db/entity/HabitLog.kt`
- `core/data/db/dao/HabitDao.kt`
- `core/domain/repository/HabitRepository.kt`
- `core/data/repository/HabitRepositoryImpl.kt`
- `core/domain/usecase/habit/CreateHabitUseCase.kt`
- `core/domain/usecase/habit/LogHabitUseCase.kt`
- `core/domain/usecase/habit/GetHabitsWithLogsUseCase.kt`
- `core/domain/usecase/habit/GetHabitStreakUseCase.kt`
- `core/domain/usecase/habit/DeleteHabitUseCase.kt`
- `ui/habits/HabitsViewModel.kt`
- `ui/habits/HabitsScreen.kt`
- `ui/habits/HabitDetailScreen.kt`
- `ui/habits/AddEditHabitSheet.kt`
- `ui/dashboard/HabitDashboardCard.kt`
- `worker/HabitReminderWorker.kt`

Modified files:
- `core/data/db/PhoenixDatabase.kt` — add entities + DAO, bump to v11
- `di/DatabaseModule.kt` — add MIGRATION_10_11, provide HabitDao
- `core/navigation/Screen.kt` — add Habits + HabitDetail routes
- `core/navigation/NavGraph.kt` — add habit routes
- `ui/drawer/DrawerScreen.kt` — add Habits menu item
- `ui/dashboard/DashboardScreen.kt` — add habit summary card
- String resources (English + Persian)
