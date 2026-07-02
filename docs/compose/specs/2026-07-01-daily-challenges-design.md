# Phase 4: Daily Challenges — Design Spec

## [S1] Problem

The app has tasks, boss missions, and achievements, but no time-limited engagement mechanic. Users need a reason to open the app daily. Daily challenges provide short-term goals with immediate rewards, increasing retention and engagement.

## [S2] Solution Overview

Auto-generated daily challenges (2-5 per day) that reset at midnight. Users complete challenges by doing regular tasks. Rewards include XP, energy, and new achievements. UI includes a dashboard card for quick access and a dedicated screen for full details.

## [S3] Data Model

### New Entity: DailyChallenge

```
daily_challenges:
  id: Long (PK, autoGenerate)
  title: String
  description: String
  type: String (COMPLETE_TASKS, COMPLETE_HARD, COMPLETE_PRIORITY, EARN_XP, COMPLETE_CATEGORY)
  target: Int
  current: Int
  rewardXp: Int
  rewardEnergy: Int
  completed: Boolean
  claimed: Boolean
  date: String (YYYY-MM-DD)
  createdAt: Long
```

### New DAO: DailyChallengeDao

- `getTodayChallenges(): Flow<List<DailyChallenge>>` — today's challenges
- `getChallengesByDate(date: String): Flow<List<DailyChallenge>>` — historical
- `insertChallenge(challenge: DailyChallenge)`
- `updateChallengeProgress(id: Long, current: Int)`
- `markCompleted(id: Long)`
- `markClaimed(id: Long)`
- `getCompletedCountToday(): Int`
- `getStreakDays(): Int` — consecutive days with all challenges completed

### Database Migration

- MIGRATION_4_5: Create `daily_challenges` table

## [S4] Challenge Generation

### DailyChallengeGenerator

- Runs at midnight via WorkManager (like EnergyRecoveryWorker)
- Generates 2-5 random challenges based on user level
- Challenge pool:

| Type | Target Range | Description |
|------|-------------|-------------|
| COMPLETE_TASKS | 1-5 | Complete N tasks today |
| COMPLETE_HARD | 1-2 | Complete a HARD difficulty task |
| COMPLETE_PRIORITY | 1-3 | Complete PRIORITY tasks |
| EARN_XP | 50-200 | Earn X XP today |
| COMPLETE_CATEGORY | 1-3 | Complete tasks in a category |

### Difficulty Scaling

- Level 1-10: Easy challenges (1 task, 50 XP)
- Level 11-30: Medium challenges (2-3 tasks, 100 XP)
- Level 31+: Hard challenges (3-5 tasks, 200 XP)

## [S5] Challenge Tracking

### DailyChallengeTracker

- Monitors task completions via Flow
- When a task is completed:
  - Check if any active challenge matches the task type
  - Update `current` field incrementally
  - Mark as `completed` when `current >= target`
- Runs in ViewModel scope, not background

## [S6] Rewards

### Reward Tiers

| Challenge Difficulty | XP Reward | Energy Reward |
|---------------------|-----------|---------------|
| Easy (target ≤ 2) | 20-50 | 5 |
| Medium (target 3-4) | 50-100 | 10 |
| Hard (target ≥ 5) | 100-200 | 20 |

### Claiming

- User must manually claim rewards after completion
- Unclaimed rewards expire at midnight (new day resets)
- Claiming triggers: `addXp()`, `increasePhoenixEnergy()`, `CheckAchievementsUseCase()`

## [S7] Achievements

New achievements for daily challenges:

| ID | Title | Condition | Rarity |
|----|-------|-----------|--------|
| daily_warrior | Daily Warrior | Complete all challenges 7 days in a row | RARE |
| challenge_master | Challenge Master | Complete 100 total challenges | EPIC |
| perfect_day | Perfect Day | Complete all challenges in a single day | COMMON |

## [S8] UI

### Dashboard Card

- Shows "Daily Challenges" header with countdown to midnight
- Lists today's challenges (max 3 visible, "See all" for more)
- Each challenge: title, progress bar, reward icons
- Tap navigates to full challenge screen

### Challenge Screen

- TopAppBar with back navigation
- Today's challenges section
- History section (past 7 days)
- Each challenge card: title, description, progress, rewards, claim button
- Empty state when no challenges

### Navigation

- From Dashboard card tap
- From Drawer menu ("Daily Challenges")

## [S9] Localization

All new strings bilingual (EN + FA):

| Key | English | Persian |
|-----|---------|---------|
| daily_challenges | Daily Challenges | چالش‌های روزانه |
| challenge_complete_tasks | Complete %d tasks | %d تسک رو کامل کن |
| challenge_complete_hard | Complete a HARD task | یه تسک سخت رو کامل کن |
| challenge_complete_priority | Complete %d priority tasks | %d تسک اولویت‌دار رو کامل کن |
| challenge_earn_xp | Earn %d XP | %d ایکس‌پی کسب کن |
| challenge_complete_category | Complete tasks in %s | تسک‌های %s رو کامل کن |
| challenge_claim | Claim Reward | دریافت پاداش |
| challenge_claimed | Claimed | دریافت شد |
| challenge_progress | %d/%d | %d/%d |
| challenge_time_left | Time left: %s | زمان باقی‌مانده: %s |
| challenge_no_challenges | No challenges today | امروز چالشی نیست |
| daily_warrior | Daily Warrior | جنگجوی روزانه |
| challenge_master | Challenge Master | استاد چالش |
| perfect_day | Perfect Day | روز بی‌نقص |
