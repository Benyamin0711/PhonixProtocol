# Phase 9: Shadow System UI — Design Spec

## [S1] Problem

The app tracks shadow level (increases on task skip/cancel/streak break) but has no dedicated screen, no recovery mechanics, and no consequences. The shadow system is incomplete — users can see their shadow level on Dashboard/Statistics but can't understand what it means, how to reduce it, or what effects it has.

## [S2] Solution Overview

Complete the shadow system with: a dedicated Shadow Screen showing level/tier/penalties/history, task-completion-based recovery (shadow decreases when tasks are completed), and XP penalties at high shadow levels to create meaningful consequences.

## [S3] Shadow Mechanics

### Increases (already implemented)
- Task skip: +1
- Task cancel: +1
- Streak break: +2

### Decreases (new)
- Task completion: -1 (normal difficulty)
- Task completion: -2 (hard difficulty)
- Task completion: -3 (priority task)

### XP Penalty (new)
- Shadow 0–29: No penalty (Safe tier)
- Shadow 30–59: -10% XP earned (Warning tier)
- Shadow 60–89: -25% XP earned (Critical tier)
- Shadow 90–100: -50% XP earned (Corrupted tier)

## [S4] Data Model

### ShadowLog Entity

```kotlin
@Entity(tableName = "shadow_log")
data class ShadowLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,  // "SKIP", "CANCEL", "STREAK_BREAK", "TASK_COMPLETE"
    val amount: Int,     // +N or -N
    val description: String
)
```

### ShadowLogDao

```kotlin
@Dao
interface ShadowLogDao {
    @Insert
    suspend fun insert(log: ShadowLog)

    @Query("SELECT * FROM shadow_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 20): Flow<List<ShadowLog>>

    @Query("SELECT * FROM shadow_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ShadowLog>>
}
```

### ShadowRepository Interface

```kotlin
interface ShadowRepository {
    fun getShadowLevel(): Flow<Int>
    fun getShadowTier(): Flow<ShadowTier>
    fun getXpPenaltyPercent(): Flow<Int>
    fun getRecentLogs(limit: Int): Flow<List<ShadowLog>>
    suspend fun logShadowChange(action: String, amount: Int, description: String)
    suspend fun decreaseShadow(amount: Int)
}
```

### DB Migration v9→v10

```sql
CREATE TABLE IF NOT EXISTS shadow_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    timestamp INTEGER NOT NULL,
    action TEXT NOT NULL,
    amount INTEGER NOT NULL,
    description TEXT NOT NULL
)
```

## [S5] Shadow Tiers

| Tier | Range | Color | XP Penalty | Label |
|------|-------|-------|------------|-------|
| Safe | 0–29 | PhoenixOrange | 0% | Safe |
| Warning | 30–59 | Yellow | -10% | Warning |
| Critical | 60–89 | Red | -25% | Critical |
| Corrupted | 90–100 | DarkRed/Purple | -50% | Corrupted |

## [S6] Shadow Screen UI

- **TopAppBar**: "Shadow" title with back arrow
- **Shadow Level Circle**: Large circular indicator (0–100) with darkening gradient based on level. Center shows level number.
- **Tier Badge**: Colored chip showing current tier name
- **XP Penalty**: Text showing current penalty percentage
- **Recovery Tips**: Card listing ways to reduce shadow (complete tasks, especially hard/priority)
- **Shadow History**: LazyColumn of recent shadow changes with timestamp, action icon, amount (+/-), description

### Visual Hierarchy

```
┌─────────────────────────┐
│  ← Shadow               │
├─────────────────────────┤
│      ┌─────────┐        │
│      │  ████   │        │
│      │  █ 45 █ │        │
│      │  ████   │        │
│      └─────────┘        │
│      [Warning]          │
│    XP Penalty: -10%     │
├─────────────────────────┤
│  Recovery Tips          │
│  • Complete tasks       │
│  • Hard tasks: -2       │
│  • Priority tasks: -3   │
├─────────────────────────┤
│  Shadow History         │
│  ↓ Skip task    +1  2m ago│
│  ↑ Complete     -1  1h ago│
│  ↑ Complete     -2  3h ago│
└─────────────────────────┘
```

## [S7] Integration Points

### StatsRepositoryImpl

- Add `decreaseShadow(amount: Int)` method
- Add `logShadowChange(action, amount, description)` method
- Both update UserStats and insert ShadowLog

### CompleteTaskUseCase

- After task completion, call `statsRepository.decreaseShadow(amount)` based on task difficulty
- Amount: normal=-1, hard=-2, priority=-3
- Log the change

### AddXp (StatsRepositoryImpl)

- Before adding XP, check shadow level
- Apply XP penalty based on tier: `actualXp = xp * (1 - penaltyPercent / 100)`
- This affects all XP earnings (task completion, boss rewards, etc.)

## [S8] Navigation

- Route: `Screen.Shadow` → `"shadow"`
- Drawer entry: below Statistics, icon = `Icons.Filled.Shield` or `Icons.Filled.Warning`
- Callback: `onNavigateToShadow` added to DrawerScreen

## [S9] Localization

| Key | English | Persian |
|-----|---------|---------|
| shadow_title | Shadow | سایه |
| shadow_level | Shadow Level | سطح سایه |
| shadow_tier_safe | Safe | ایمن |
| shadow_tier_warning | Warning | هشدار |
| shadow_tier_critical | Critical | بحرانی |
| shadow_tier_corrupted | Corrupted | آلوده |
| shadow_xp_penalty | XP Penalty | جریمه امتیاز |
| shadow_no_penalty | No penalty | بدون جریمه |
| shadow_recovery_tips | Recovery Tips | راهنمای بازیابی |
| shadow_recovery_tip_1 | Complete tasks to reduce shadow | تسک‌ها رو کامل کن تا سایه کم بشه |
| shadow_recovery_tip_2 | Hard tasks reduce shadow by 2 | تسک‌های سخت ۲ واحد کم می‌کنن |
| shadow_recovery_tip_3 | Priority tasks reduce shadow by 3 | تسک‌های اولویت‌دار ۳ واحد کم می‌کنن |
| shadow_history | Shadow History | تاریخچه سایه |
| shadow_action_skip | Task skipped | تسک رد شد |
| shadow_action_cancel | Task cancelled | تسک لغو شد |
| shadow_action_streak_break | Streak broken | серия شکسته شد |
| shadow_action_complete | Task completed | تسک کامل شد |
| drawer_shadow | Shadow | سایه |

## [S10] Testing Strategy

- Unit test: ShadowRepository returns correct tier/penalty for each level range
- Unit test: XP penalty calculation applies correctly
- Unit test: Shadow decrease on task completion with correct amounts
- UI test: Shadow screen displays correct tier, penalty, history
- Build verification: `./gradlew assembleDebug` after each task
