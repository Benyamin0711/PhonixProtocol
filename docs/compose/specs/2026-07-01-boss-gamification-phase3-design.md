# Phase 3: Boss & Gamification — Design Spec

## [S1] Problem

Phase 2 built the core Boss & Gamification system (boss lifecycle, XP/level/rank, energy, shadow level, 15 achievements, 8 identity archetypes). However, several functional gaps remain:

1. No Boss History screen — past bosses are invisible to the user
2. No energy recovery mechanic — energy only increases on task completion (+5), no passive regen
3. Shadow level trigger logic is dead code — nothing calls `increaseShadowLevel()`
4. No boss-specific visual theming or animations — uses plain Material 3

## [S2] Solution Overview

Fill all 4 gaps in a single phase:

1. **Boss History Screen** — List all past bosses with filtering (All/Active/Completed/Failed)
2. **Energy Recovery Mechanic** — Timed regeneration: +1 energy every 30 minutes, max 100
3. **Shadow Level Triggers** — Multiple triggers: task skip (+1), task cancel (+1), streak break (+2)
4. **Boss Visual Animations** — Progress bar glow, card entry/exit animations, goal completion effects

## [S3] Boss History Screen

### Files
- `feature/boss/BossHistoryScreen.kt` — New screen
- `feature/boss/BossHistoryViewModel.kt` — New ViewModel
- `core/navigation/Screen.kt` — Add `BossHistory` route
- `core/navigation/NavGraph.kt` — Wire route
- `feature/drawer/DrawerScreen.kt` — Add navigation callback

### Behavior
- Filter tabs: All / Active / Completed / Failed
- Each boss card: title, level, status badge, progress %, deadline, reward XP
- Empty state message when no bosses match filter
- Pull from existing `BossDao.getAllBosses()`, `getActiveBosses()`, `getCompletedBosses()`

### Navigation
- Drawer menu item: "Boss History"
- Dashboard boss card tap navigates here

## [S4] Energy Recovery Mechanic

### Files
- `core/domain/usecase/RecoverEnergyUseCase.kt` — New use case
- `core/work/EnergyRecoveryWorker.kt` — New WorkManager worker
- `di/WorkerModule.kt` — Hilt worker binding
- `feature/energy/EnergyViewModel.kt` — New or extend StatisticsViewModel

### Behavior
- WorkManager periodic task: every 30 minutes
- Each tick: if `phoenixEnergy < 100`, increment by 1
- Cap at 100 — never exceed
- ViewModel polls energy state for UI updates
- Worker enqueued on app start (MainActivity or Application)

### Energy Rules
- Max: 100
- Regen: +1 per 30 minutes
- Task complete: +5 (existing)
- Boss complete: bonus energy (existing)

## [S5] Shadow Level Triggers

### Files
- `core/domain/usecase/SkipTaskUseCase.kt` — New use case
- `core/domain/usecase/CancelTaskUseCase.kt` — New use case
- `feature/tasks/TaskListViewModel.kt` — Wire skip/cancel actions

### Triggers
| Event | Shadow Level Increase |
|-------|----------------------|
| Task skipped | +1 |
| Task cancelled | +1 |
| Streak broken | +2 |

### Implementation
- Call `statsRepository.increaseShadowLevel()` in each trigger point
- SkipTaskUseCase: mark task as SKIPPED, increase shadow
- CancelTaskUseCase: delete task, increase shadow
- Streak logic: detect streak decrease, increase shadow by 2

## [S6] Boss Visual Animations

### Files
- `feature/boss/BossCard.kt` — Add entry animation
- `feature/boss/BossDetailScreen.kt` — Add progress animation + goal effects
- `core/ui/components/AnimatedProgressBar.kt` — New animated component

### Animation Types
1. **Progress bar:** Animated fill with orange glow pulse (`animateFloatAsState` + `spring`)
2. **Boss card entry:** Slide up + fade in (`AnimatedVisibility` + `slideInVertically`)
3. **Goal completion:** Checkmark reveal + color transition (green for complete)
4. **Boss status colors:** Active=orange, Completed=green, Failed=red

### Implementation
- Compose animations: `animateFloatAsState`, `AnimatedVisibility`, `updateTransition`
- Spring physics for natural feel
- No external animation library — pure Compose

## [S7] Localization

All new strings bilingual (EN + FA):

| Key | English | Persian |
|-----|---------|---------|
| boss_history | Boss History | تاریخچه باس |
| boss_history_all | All | همه |
| boss_history_active | Active | فعال |
| boss_history_completed | Completed | تکمیل شده |
| boss_history_failed | Failed | ناموفق |
| boss_history_empty | No bosses found | باسی یافت نشد |
| energy_recovering | Energy recovering... | انرژی در حال بازیابی... |
| shadow_increase | Shadow level increased | سطح سایه افزایش یافت |
| skip_task | Skip Task | رد کردن تسک |
| cancel_task | Cancel Task | لغو تسک |

## [S8] Testing Strategy

- Unit tests for: RecoverEnergyUseCase, SkipTaskUseCase, CancelTaskUseCase
- Integration test: BossDao queries for history filtering
- UI test: BossHistoryScreen filter tabs, empty state
- Build verification: `./gradlew assembleDebug` after each task
