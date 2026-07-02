# Phase 7: Focus Timer + Task Categories + Widget Customization — Design Spec

## [S1] Problem

The app has tasks, boss system, daily challenges, and a widget, but lacks:
1. **Time tracking** — no way to measure focus time per task
2. **Category management** — categories are free-text with no filtering or management
3. **Widget personalization** — single hardcoded layout with no options

## [S2] Solution Overview

Three independent features:
1. **Task Categories** — predefined + custom categories with CRUD and filtering
2. **Focus Timer** — Pomodoro and custom timer modes with foreground service
3. **Widget Customization** — theme and category filter options

## [S3] Task Categories

### Data Model

New `Category` entity:
```
categories:
  id: Long (PK, autoGenerate)
  name: String (unique)
  color: String (hex, e.g., "#FF6B35")
  isDefault: Boolean (true for predefined)
```

Predefined categories: Work, Health, Learning, Personal, Finance, Social

### Task Entity Change

Add `categoryId: Long?` to Task table. Migration v6→v7 required.

### UI

- **Category Management Screen**: List all categories, create new, delete custom ones
- **Category Picker in CreateTaskScreen**: FlowRow with FilterChip per category
- **Category Filter in TaskList**: Filter chips for categories

### Files

**Create:**
- `core/data/db/entity/Category.kt`
- `core/data/db/dao/CategoryDao.kt`
- `core/domain/repository/CategoryRepository.kt`
- `core/data/repository/CategoryRepositoryImpl.kt`
- `feature/categories/CategoriesScreen.kt`
- `feature/categories/CategoriesViewModel.kt`

**Modify:**
- `Task.kt` — add `categoryId: Long?`
- `TaskDao.kt` — add `getTasksByCategory()`, `getDistinctCategories()`
- `CreateTaskScreen.kt` — replace text field with category picker
- `CreateTaskViewModel.kt` — load categories, save with categoryId
- `PhoenixDatabase.kt` — add Category entity, bump to v7
- `DatabaseModule.kt` — add migration 6→7
- `Screen.kt` — add Categories route
- `NavGraph.kt` — add composable
- `DrawerScreen.kt` — add menu item

## [S4] Focus Timer

### Data Model

New `FocusSession` entity:
```
focus_sessions:
  id: Long (PK, autoGenerate)
  taskId: Long (FK to tasks)
  startedAt: Long
  endedAt: Long? (null while in progress)
  durationSeconds: Int
  completed: Boolean
  mode: String (POMODORO or CUSTOM)
```

### Timer Modes

- **Pomodoro**: 25min work → 5min break → repeat 4x → 15min long break
- **Custom**: User selects duration (15/25/30/45/60 min)

### Foreground Service

`FocusTimerService` runs as foreground service with persistent notification:
- Shows elapsed time
- Stop button
- Keeps timer alive when app is backgrounded

### UI

- Timer screen with circular progress
- Start/Pause/Stop buttons
- Mode selector (Pomodoro/Custom)
- Task association (optional)

### Files

**Create:**
- `core/data/db/entity/FocusSession.kt`
- `core/data/db/dao/FocusSessionDao.kt`
- `core/domain/repository/FocusRepository.kt`
- `core/data/repository/FocusRepositoryImpl.kt`
- `feature/focus/FocusTimerScreen.kt`
- `feature/focus/FocusTimerViewModel.kt`
- `core/service/FocusTimerService.kt`

**Modify:**
- `PhoenixDatabase.kt` — add FocusSession entity
- `DatabaseModule.kt` — add migration 6→7
- `StatisticsScreen.kt` — add total focus time stat
- `Screen.kt` — add FocusTimer route
- `NavGraph.kt` — add composable

## [S5] Widget Customization

### Preferences

New keys in SettingsDataStore:
- `widgetTheme`: String (DARK/LIGHT/ACCENT)
- `widgetCategoryFilter`: Long? (null = ALL, or specific category ID)

### Widget Config Activity

Shown when user first adds widget. Allows:
- Theme selection (3 options with preview)
- Category filter (ALL or specific category)

### Multiple Layouts

- `widget_phoenix_dark.xml` — current dark theme
- `widget_phoenix_light.xml` — light variant

### Files

**Create:**
- `widget/WidgetConfigActivity.kt`
- `res/layout/widget_phoenix_light.xml`

**Modify:**
- `SettingsDataStore.kt` — add widget preferences
- `WidgetRepository.kt` — add category filter logic
- `widget_phoenix_info.xml` — add `configure` attribute
- `AndroidManifest.xml` — add config Activity

## [S6] Database Migration

Current version: 6 → Target: 7

Migration 6→7:
- Create `categories` table
- Create `focus_sessions` table
- Add `categoryId` column to `tasks` table (nullable, default null)
- Seed predefined categories

## [S7] Localization

All new strings bilingual (EN + FA):

| Key | English | Persian |
|-----|---------|---------|
| categories_title | Categories | دسته‌بندی‌ها |
| categories_create | Create Category | ایجاد دسته‌بندی |
| categories_delete | Delete | حذف |
| categories_default | Default | پیش‌فرض |
| focus_timer | Focus Timer | تایمر تمرکز |
| focus_start | Start | شروع |
| focus_pause | Pause | توقف |
| focus_stop | Stop | پایان |
| focus_pomodoro | Pomodoro | پومودورو |
| focus_custom | Custom | سفارشی |
| focus_in_progress | Focus in progress | تمرکز در حال انجام |
| widget_theme | Widget Theme | پوسته ویجت |
| widget_dark | Dark | تاریک |
| widget_light | Light | روشن |
| widget_category_filter | Category Filter | فیلتر دسته‌بندی |
| widget_all_categories | All Categories | همه دسته‌بندی‌ها |

## [S8] Testing Strategy

- Unit test: CategoryRepository, FocusRepository
- Integration test: Category CRUD, FocusSession lifecycle
- UI test: Category picker, Timer controls, Widget config
- Build verification: `./gradlew assembleDebug` after each task
