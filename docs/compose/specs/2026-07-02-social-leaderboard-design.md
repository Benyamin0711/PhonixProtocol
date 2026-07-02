# Phase 8: Social Leaderboard — Design Spec

## [S1] Problem

The app has a full gamification system (XP, levels, ranks, boss fights, achievements, daily challenges) but no competitive element. Users have no way to see how they compare to others, which reduces motivation. The feature is designed as a placeholder for a future backend — mock data now, API-ready architecture.

## [S2] Solution Overview

A leaderboard screen showing simulated players ranked by XP. Two views: all-time and weekly. User's own position highlighted. Repository pattern allows swapping mock data for a real API later.

## [S3] Data Model

### LeaderboardEntry

```kotlin
data class LeaderboardEntry(
    val id: Long,
    val name: String,
    val avatarColor: String,  // hex color for avatar circle
    val xp: Int,
    val level: Int,
    val rank: Rank,
    val isCurrentUser: Boolean = false
)
```

### LeaderboardRepository Interface

```kotlin
interface LeaderboardRepository {
    fun getAllTimeLeaderboard(): Flow<List<LeaderboardEntry>>
    fun getWeeklyLeaderboard(): Flow<List<LeaderboardEntry>>
}
```

### MockLeaderboardRepository

- Generates 15 simulated players with randomized XP (100–50000) and levels (1–80)
- Player names: hardcoded list of thematic names (ShadowHunter, PhoenixRider, etc.)
- Avatar colors: random from the app's color palette
- User's real stats injected from `UserStatsDao` as `isCurrentUser = true`
- Weekly leaderboard: same mock players but with reduced XP (simulating weekly activity)

## [S4] UI

### LeaderboardScreen

- **TopAppBar**: "Leaderboard" title with back arrow
- **Tab Row**: All-Time / Weekly tabs
- **Top 3 Podium**: Three cards side by side — Gold (#FFD700), Silver (#C0C0C0), Bronze (#CD7F32). Shows rank icon, name, XP
- **Full List**: LazyColumn with:
  - Rank number (1–N)
  - Avatar circle (colored circle with first letter)
  - Player name
  - XP value
  - Level badge
  - User's row highlighted with PhoenixOrange background
- **Empty state**: "No leaderboard data" message

### Visual Hierarchy

```
┌─────────────────────────┐
│  ← Leaderboard          │
├─────────────────────────┤
│  [All-Time] [Weekly]    │
├─────────────────────────┤
│   🥇    🥈    🥇       │
│  Name  Name  Name       │
│  50K   45K   40K        │
├─────────────────────────┤
│  4  Avatar  Player   35K│
│  5  Avatar  Player   30K│
│  ►  Avatar  You     25K │  ← highlighted
│  7  Avatar  Player   20K│
│  ...                    │
└─────────────────────────┘
```

## [S5] Navigation

- Route: `Screen.Leaderboard` → `"leaderboard"`
- Drawer entry: below Achievements, icon = `Icons.Filled.Leaderboard`
- Callback: `onNavigateToLeaderboard` added to DrawerScreen

## [S6] Localization

| Key | English | Persian |
|-----|---------|---------|
| leaderboard_title | Leaderboard | جدول رتبه‌بندی |
| leaderboard_all_time | All-Time | همه زمان‌ها |
| leaderboard_weekly | Weekly | هفتگی |
| leaderboard_rank | Rank | رتبه |
| leaderboard_xp | XP | امتیاز |
| leaderboard_level | Level | سطح |
| leaderboard_empty | No leaderboard data | داده رتبه‌بندی موجود نیست |
| leaderboard_you | You | شما |
| drawer_leaderboard | Leaderboard | رتبه‌بندی |

## [S7] Testing Strategy

- Unit test: MockLeaderboardRepository returns correct data, user entry marked
- UI test: Tab switching, user highlight visible, podium shows top 3
- Build verification: `./gradlew assembleDebug` after each task
