# Phoenix Protocol MVP Core — Design Spec

> [!NOTE]
> This document may not reflect the current implementation.
> See the final report for up-to-date state:
> [Final Report](../reports/phoenix-protocol-mvp.md)

## [S1] Problem
Phoenix Protocol is a gamified "Life Operating System" Android app. The full vision includes 10+ subsystems (Identity Engine, Shadow System, Boss Missions, Gamification, Life Areas, etc.). This spec covers the MVP Core: a working app with Splash → Onboarding → Dashboard → Task System, with bilingual support and a futuristic dark UI.

## [S2] Solution Overview
Build a feature-based Clean Architecture Android app with:
- Splash screen with Phoenix animation
- Onboarding flow (language, profile, identity path)
- Main dashboard with task lists, XP/rank display, daily slogan
- Basic task CRUD system
- Left navigation drawer
- Room database for persistence
- DataStore for settings/language
- English + Persian bilingual support from day one

## [S3] Architecture

### Module Structure
```
app/                          # Application entry, navigation host
core/
  ui/                         # Shared composables, theme, design tokens
  data/                       # Room DB, DataStore, repository implementations
  domain/                     # Models, repository interfaces
  navigation/                 # Nav graph, route definitions
feature/
  splash/                     # Splash screen + animation
  onboarding/                 # Profile creation, language, identity selection
  dashboard/                  # Main dashboard, task lists, slogan
  tasks/                      # Task CRUD, task types
  drawer/                     # Left navigation drawer
```

### Data Flow
```
UI (Compose) ← ViewModel ← UseCase ← Repository ← Room/DataStore
```

### Key Patterns
- MVVM with StateFlow
- Hilt for dependency injection
- Navigation Compose for routing
- Repository pattern for data access

## [S4] Splash Screen
- Dark background with Phoenix logo animation (Compose-based, no Lottie dependency)
- Auto-detect first run via DataStore flag
- Route to Onboarding if first run, Dashboard otherwise
- Animation: logo glow + fade-in effect, ~2 seconds

## [S5] Onboarding Flow
4-step wizard:
1. **Language Selection** — English / Persian (RTL switch)
2. **Profile Creation** — Full name, username, birth year (optional)
3. **Identity Path Selection** — 8 archetypes:
   - Warrior, Scholar, Builder, Monk, Commander, Creator, Strategist, Sentinel
   - Each with description + icon representation
4. **Review & Confirm** — Summary, save to Room DB

On completion: set first-run flag, save profile, route to Dashboard.

## [S6] Dashboard
Layout (top to bottom):
1. **Protocol Slogan** — Daily rotating motivational line
2. **Status Card** — XP, Level, Rank, Phoenix Energy bar
3. **Priority Tasks** — Today's important tasks
4. **Custom Tasks** — User-created tasks
5. **FAB** — Quick add task

Answers: "What matters today?", "What is my current state?", "What should I do next?"

## [S7] Task System (MVP)
### Task Model
- title: String
- description: String
- difficulty: Easy / Medium / Hard / Extreme
- category: Custom (user-defined)
- xpValue: Int (calculated from difficulty, adjustable)
- recurrence: None / Daily / Weekly / Monthly
- status: Pending / InProgress / Completed / Skipped
- createdAt: Long
- completedAt: Long?

### Task Operations
- Create (from FAB or task screen)
- View (in dashboard lists)
- Complete (tap to complete → earn XP, increase Phoenix Energy)
- Skip (tracked → increases Shadow Level)

### XP Calculation
- Easy: 10 XP, Medium: 25 XP, Hard: 50 XP, Extreme: 100 XP
- Streak bonus: +5% per consecutive day

## [S8] Gamification (MVP)
### XP & Leveling
- Levels require exponentially increasing XP
- Level 1: 0, Level 2: 100, Level 3: 250, Level 4: 500, etc.

### Ranks (unlock at level thresholds)
1. Initiate (Lv 1)
2. Survivor (Lv 5)
3. Hunter (Lv 10)
4. Warrior (Lv 15)
5. Elite (Lv 20)
6. Commander (Lv 30)
7. Phantom (Lv 40)
8. Titan (Lv 50)
9. Ascendant (Lv 65)
10. Phoenix (Lv 80)

### Phoenix Energy
- Starts at 50/100
- +5 on task complete, +10 on hard task, +25 on boss mission
- -10 on task skip, -20 on day missed, -50 on 3+ days inactive

### Shadow Level
- Starts at 0
- +5 on task skip, +10 on day missed, +15 on streak break
- Affects UI tone (visual indicator on dashboard)

## [S9] Left Drawer
Menu items:
- Profile (name, rank, avatar placeholder)
- Statistics (placeholder for MVP)
- Achievements (placeholder for MVP)
- Settings
- About Us
- Support Us

## [S10] Settings
- Language selector (English / Persian)
- Theme (dark only for MVP, toggle reserved)
- Account info (read-only for MVP)
- App version

## [S11] Localization
- `res/values/strings.xml` — English (default)
- `res/values-fa/strings.xml` — Persian
- All UI text, labels, empty states, system messages
- RTL layout support via `android:supportsRtl="true"`
- Language preference stored in DataStore

## [S12] Theme
- Dark background: #0D0D0D
- Surface: #1A1A2E
- Phoenix accent: #FF6B35 (orange)
- Phoenix red: #E63946
- Gold (achievements): #FFD700
- Text primary: #FFFFFF
- Text secondary: #A0A0B0
- Material 3 custom color scheme
- Futuristic, premium, minimal

## [S13] Data Models

### User Profile (Room Entity)
- id: Long (PK, auto)
- fullName: String
- username: String
- birthYear: Int?
- identityPath: String (archetype name)
- createdAt: Long

### Task (Room Entity)
- id: Long (PK, auto)
- title: String
- description: String
- difficulty: String
- category: String
- xpValue: Int
- recurrence: String
- status: String
- createdAt: Long
- completedAt: Long?

### UserStats (Room Entity)
- id: Long (PK)
- userId: Long (FK)
- xp: Int
- level: Int
- rank: String
- currentStreak: Int
- longestStreak: Int
- phoenixEnergy: Int
- shadowLevel: Int
- completedTasks: Int

### Settings (DataStore)
- language: String ("en" / "fa")
- isOnboardingComplete: Boolean
- dailySloganIndex: Int

## [S14] Dependencies

### Required additions to build.gradle.kts
- Hilt (DI)
- Room (database)
- DataStore (preferences)
- Navigation Compose
- Material Icons Extended
- Kotlin Serialization (for type converters)

### Version catalog additions
- hilt = "2.51"
- hilt-navigation-compose = "1.2.0"
- room = "2.7.0"
- datastore = "1.1.2"
- navigation-compose = "2.8.0"
- material-icons-extended (from Compose BOM)
- kotlinx-serialization = "1.7.0"

## [S15] File Structure

```
app/src/main/java/com/benyaminrasouli/phoniexprotocol/
├── PhoenixApp.kt                          # Application class (@HiltAndroidApp)
├── MainActivity.kt                         # Entry point, nav host
├── core/
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt                   # Phoenix color palette
│   │   │   ├── Theme.kt                   # Dark theme
│   │   │   ├── Type.kt                    # Typography
│   │   │   └── Shape.kt                   # Shapes
│   │   └── components/
│   │       ├── PhoenixButton.kt
│   │       ├── PhoenixCard.kt
│   │       ├── EnergyBar.kt
│   │       └── ...
│   ├── data/
│   │   ├── db/
│   │   │   ├── PhoenixDatabase.kt
│   │   │   ├── entity/
│   │   │   │   ├── UserProfile.kt
│   │   │   │   ├── Task.kt
│   │   │   │   └── UserStats.kt
│   │   │   ├── dao/
│   │   │   │   ├── UserProfileDao.kt
│   │   │   │   ├── TaskDao.kt
│   │   │   │   └── UserStatsDao.kt
│   │   │   └── converter/
│   │   │       └── Converters.kt
│   │   ├── datastore/
│   │   │   └── SettingsDataStore.kt
│   │   └── repository/
│   │       ├── UserRepositoryImpl.kt
│   │       ├── TaskRepositoryImpl.kt
│   │       └── StatsRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/
│   │   │   ├── IdentityPath.kt
│   │   │   ├── Difficulty.kt
│   │   │   ├── TaskRecurrence.kt
│   │   │   ├── TaskStatus.kt
│   │   │   └── Rank.kt
│   │   ├── repository/
│   │   │   ├── UserRepository.kt
│   │   │   ├── TaskRepository.kt
│   │   │   └── StatsRepository.kt
│   │   └── usecase/
│   │       ├── CreateProfileUseCase.kt
│   │       ├── GetProfileUseCase.kt
│   │       ├── CreateTaskUseCase.kt
│   │       ├── CompleteTaskUseCase.kt
│   │       ├── GetTasksUseCase.kt
│   │       └── GetStatsUseCase.kt
│   └── navigation/
│       ├── Screen.kt
│       └── NavGraph.kt
├── feature/
│   ├── splash/
│   │   ├── SplashScreen.kt
│   │   └── SplashViewModel.kt
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt
│   │   ├── OnboardingViewModel.kt
│   │   ├── LanguageStep.kt
│   │   ├── ProfileStep.kt
│   │   ├── IdentityStep.kt
│   │   └── ReviewStep.kt
│   ├── dashboard/
│   │   ├── DashboardScreen.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── StatusCard.kt
│   │   ├── TaskListSection.kt
│   │   └── SloganBar.kt
│   ├── tasks/
│   │   ├── TaskListScreen.kt
│   │   ├── CreateTaskScreen.kt
│   │   ├── TaskViewModel.kt
│   │   └── TaskComponents.kt
│   └── drawer/
│       ├── DrawerScreen.kt
│       └── DrawerViewModel.kt
└── di/
    ├── AppModule.kt
    └── DatabaseModule.kt
```

## [S16] Implementation Order

1. **Foundation** — Dependencies, theme, DI setup, database, data models
2. **Core UI** — Shared composables, design tokens
3. **Splash** — Screen + animation + first-run detection
4. **Onboarding** — 4-step wizard + profile save
5. **Dashboard** — Main screen layout + status card + task lists
6. **Task System** — CRUD + task lists integration
7. **Drawer** — Navigation drawer + settings
8. **Localization** — Persian strings + RTL
9. **Polish** — Animations, transitions, empty states

## [S17] Success Criteria
- App launches with splash animation
- First run → onboarding completes → profile saved
- Returning user → splash → dashboard
- Tasks can be created, viewed, completed
- XP increases on completion
- Language switches between English and Persian
- Dark futuristic theme throughout
- Navigation drawer opens with all menu items
