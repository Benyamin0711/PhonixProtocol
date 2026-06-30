# Boss Missions System — Design Spec

## [S1] Problem
Karbaran niaz daran be challenge haye sakht tar ke unhan ra motaharrek negah dare. Boss missions systeme time-limited challenge ha ro faraham mikone ke har hafe auto spawn mishe va sakht tar az ghabli mishе. Har boss 7 ruz time dare va goals random combine mishe.

## [S2] Solution Overview
- **Boss Entity:** Har boss 1 ID, 1 level, 2-4 goals random, 1 deadline (7 ruz), va 1 status (ACTIVE/COMPLETED/FAILED)
- **Goal Pool:** Majmooe e goal ha ke az unha random entekhab mishe (masalan: "Complete 10 tasks", "3-day streak", "Complete 3 priority tasks")
- **Progression:** Boss level 1 shoru mishe, har boss e tamoom shode level e badi ro unlock mikone
- **Auto Spawn:** Har hafe yek boss jadid auto generate mishe
- **Rewards:** XP bishtar + Achievement badge baraye har boss

## [S3] Architecture

**Entities:**
- `Boss` - id, level, title, description, goals (JSON string), deadline, status, rewardXp, createdAt
- `BossGoal` - parsed from Boss.goals, contains: type, target, current, description
- `UserBoss` - tracks which boss is active, completion status

**Goal Types:**
- `COMPLETE_TASKS` - Complete N tasks (any type)
- `STREAK_DAYS` - Maintain N-day streak
- `PRIORITY_TASKS` - Complete N priority tasks
- `XP_EARNED` - Earn N XP
- `DIFFICULTY_TASKS` - Complete N tasks of specific difficulty

**Repositories:**
- `BossRepository` - CRUD + active boss + spawn logic
- `BossGoalRepository` - goal progress tracking

**Use Cases:**
- `SpawnWeeklyBossUseCase` - Generates new boss if none active
- `GetActiveBossUseCase` - Returns current boss with progress
- `UpdateBossProgressUseCase` - Updates goal progress after task completion
- `CompleteBossUseCase` - Marks boss as completed, awards XP + badge

## [S4] Data Flow

1. **App Start:** `SpawnWeeklyBossUseCase` check mikone aya boss e active hast. Agar nabashe, boss e jadid generate mikone.
2. **Task Completion:** Vaqt i ke user task i ro complete mikone, `UpdateBossProgressUseCase` call mishe va goal progress ro update mikone.
3. **Boss UI:** Dashboard boss card neshoon mide ke目標, progress, va deadline ro neshoon mide.
4. **Boss Completion:** Vaqt i ke hame goals tamoom shod, `CompleteBossUseCase` XP va badge ro award mikone.

## [S5] UI Components

- **Boss Card (Dashboard):** Card i ke boss e fa'ol ro neshoon mide ba progress bar va goals
- **Boss Detail Screen:** Screen e joda baraye didan e tamom e goals va progress
- **Boss History:** List e boss haye ghabl (completed/failed)
- **Boss Badge:** Icon e khas baraye boss haye completed

## [S6] Database Schema

```sql
CREATE TABLE bosses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    level INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    goals TEXT NOT NULL, -- JSON array of goals
    deadline INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    rewardXp INTEGER NOT NULL,
    createdAt INTEGER NOT NULL
);
```

## [S7] Goal Pool Definition

```kotlin
enum class GoalType {
    COMPLETE_TASKS,
    STREAK_DAYS,
    PRIORITY_TASKS,
    XP_EARNED,
    DIFFICULTY_TASKS
}

data class GoalTemplate(
    val type: GoalType,
    val minTarget: Int,
    val maxTarget: Int,
    val descriptionTemplate: String
)
```

**Goal Templates:**
- COMPLETE_TASKS: 5-15 tasks
- STREAK_DAYS: 3-7 days
- PRIORITY_TASKS: 2-5 priority tasks
- XP_EARNED: 100-500 XP
- DIFFICULTY_TASKS: 3-8 tasks of HARD/EXTREME

## [S8] Boss Level Progression

- Level 1: 2 goals, reward 100 XP
- Level 2: 2-3 goals, reward 200 XP
- Level 3: 3 goals, reward 350 XP
- Level 4: 3-4 goals, reward 500 XP
- Level 5+: 4 goals, reward 700 XP

## [S9] Spawn Logic

```kotlin
fun shouldSpawnNewBoss(): Boolean {
    val activeBoss = getActiveBoss()
    if (activeBoss != null) return false
    
    val lastCompletedBoss = getLastCompletedBoss()
    if (lastCompletedBoss == null) return true // First boss
    
    val timeSinceLastBoss = System.currentTimeMillis() - lastCompletedBoss.completedAt
    val oneWeek = 7 * 24 * 60 * 60 * 1000L
    return timeSinceLastBoss >= oneWeek
}
```

## [S10] Error Handling

- If boss deadline passes without completion → status = FAILED
- If user completes all goals before deadline → status = COMPLETED
- If database error occurs during spawn → retry on next app start
- If goal progress update fails → log error, continue normally

## [S11] Testing Strategy

- Unit tests for spawn logic
- Unit tests for goal progress calculation
- Integration tests for boss completion flow
- UI tests for boss card display
- Edge case: boss spawn on first app install
- Edge case: multiple boss completions in same session
