<div align="center">

# PHOENIX PROTOCOL

### Rise from the ashes of procrastination.

**A gamified productivity app that turns your daily tasks into an epic RPG quest.**

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green?logo=android)
![License](https://img.shields.io/badge/License-MIT-orange)

</div>

---

## What is Phoenix Protocol?

Phoenix Protocol is not your average to-do list. It's a **gamified life system** where every task you complete earns XP, every day you stay consistent builds your streak, and every week a new **Boss** appears that you must defeat through discipline and focus.

Miss a task? Your **Shadow Level** rises. Recover from failure? You earn an epic achievement. This is productivity as an RPG — designed to keep you motivated through game mechanics that actually work.

---

## Core Mechanics

### XP & Ranking System
Complete tasks to earn XP and level up through 10 ranks:

| Rank | Level |
|------|-------|
| Initiate | 1 |
| Survivor | 5 |
| Hunter | 10 |
| Warrior | 15 |
| Elite | 20 |
| Commander | 30 |
| Phantom | 40 |
| Titan | 50 |
| Ascendant | 65 |
| **Phoenix** | **80** |

### Phoenix Energy
Your energy bar depletes as you take on tasks and recovers over time. Manage it wisely — burnout is real, even for a phoenix.

### Shadow Level
Skip tasks or break your streak? Shadow builds up. High shadow means higher penalties. Break through the darkness to earn the **Shadow Breaker** achievement.

### Weekly Bosses
Every week, a new **Boss** spawns with unique goals and a deadline. Defeat it by completing the required tasks before time runs out. Track your boss history and prove you're a true slayer.

### Daily Challenges
Three auto-generated challenges every day. Complete them for bonus XP and energy rewards. Consistency is the name of the game.

### Focus Timer
Built-in Pomodoro-style focus timer with session tracking. Stay locked in and earn focus XP.

---

## Features

- **Task Management** — Create tasks with difficulty levels, categories, priorities, and recurrence
- **Task Templates** — Save and reuse task templates for repetitive workflows
- **Categories** — Organize tasks into life areas
- **Achievements** — 15+ achievements across 4 rarity tiers (Common, Rare, Epic, Legendary)
- **Statistics Dashboard** — Track your XP, streaks, completion rate, and progress
- **Home Screen Widget** — Glance at your status without opening the app
- **Onboarding** — Personalized setup with identity path selection
- **Daily Motivational Slogans** — Rotating quotes to keep you going
- **Dark Theme** — Full dark mode with Phoenix Orange accent
- **Multi-language Support** — English and Persian (Farsi)

---

## Architecture

Built with **Clean Architecture** and **MVVM** pattern:

```
com.benyaminrasouli.phoniexprotocol/
├── core/
│   ├── data/          # Room DB, DAOs, Repositories, DataStore
│   ├── domain/        # Use cases, domain models
│   ├── navigation/    # Nav graph & screen routes
│   ├── notification/  # Notification helpers
│   ├── service/       # Foreground focus timer service
│   ├── ui/            # Reusable composables (EnergyBar, PhoenixCard, etc.)
│   ├── util/          # Locale helpers
│   └── work/          # WorkManager workers (boss deadlines, energy recovery, etc.)
├── di/                # Hilt modules (App, Database, Worker)
├── feature/           # Feature screens (Dashboard, Tasks, Boss, Focus, etc.)
├── ui/theme/          # Material3 theme, colors, typography
└── widget/            # Home screen widget
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Database | Room (10 entities, 9 DAOs) |
| Preferences | DataStore |
| Background | WorkManager |
| Navigation | Navigation Compose |
| Serialization | Kotlinx Serialization + Gson |
| Language | Kotlin |

---

## Getting Started

1. Clone the repo
   ```bash
   git clone https://github.com/BenyaminRasouli/PhoniexProtocol.git
   ```

2. Open in Android Studio (Ladybug or newer)

3. Sync Gradle and run on an emulator or device (minSdk 24)

---

## Screens

| Screen | Description |
|--------|-------------|
| Splash | App intro with Phoenix branding |
| Onboarding | User setup (language, identity, profile) |
| Dashboard | Main hub — status, tasks, boss, challenges |
| Task List | All tasks with filters |
| Create Task | New task with difficulty, category, recurrence |
| Boss Detail | Active boss fight progress |
| Boss History | Past boss战绩 |
| Daily Challenges | Today's challenges and rewards |
| Focus Timer | Pomodoro timer with modes |
| Statistics | XP, streaks, completion stats |
| Achievements | Badge collection with rarity |
| Profile | User info and identity path |
| Templates | Reusable task templates |
| Categories | Life area organization |
| Settings | App preferences |
| About | App info |
| Support | Help & feedback |

---

## Achievements

| Achievement | Rarity | Condition |
|-------------|--------|-----------|
| First Blood | Common | Complete your first task |
| 7-Day Warrior | Common | Maintain a 7-day streak |
| No Excuses | Common | Complete 10 tasks |
| Night Owl | Common | Complete task after midnight |
| Early Bird | Common | Complete task before 6 AM |
| 30-Day Legend | Rare | Maintain a 30-day streak |
| Shadow Breaker | Rare | Reduce Shadow to 0 after being > 30 |
| Speed Demon | Rare | Complete 5 tasks in one day |
| Marathon Runner | Rare | Complete 50 tasks total |
| Phoenix Rising | Epic | Recover from 3+ day absence |
| Boss Slayer | Epic | Complete 5 boss missions |
| Discipline Above Mood | Rare | Complete task when Shadow > 50 |
| Elite Consistency | Legendary | 90% completion rate for 7 days |
| Unbroken | Legendary | 100-day streak |
| Life Master | Legendary | Complete task in all 8 life areas |

---

<div align="center">

**Built with fire by [Benyamin Rasouli](https://github.com/Benyamin0711)**

*Discipline is the bridge between goals and accomplishment.*

</div>
