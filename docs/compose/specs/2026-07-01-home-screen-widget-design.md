# Phase 5: Home Screen Widget — Design Spec

## [S1] Problem

Users need to see their daily progress without opening the app. A home screen widget provides at-a-glance visibility into tasks, energy, and streaks, increasing engagement and reducing friction.

## [S2] Solution Overview

Android App Widget (4x2 medium) showing today's tasks with progress, energy level, and streak. Static display — tap opens app. Real-time updates via Room Flow and WorkManager.

## [S3] Widget Data

### Display Elements
- **Header:** App icon + "Phoenix Protocol" title + energy level
- **Task list:** Up to 4 active tasks with completion status (checkbox + title)
- **Progress bar:** Overall daily progress (completed tasks / total tasks)
- **Streak:** Current streak count with fire icon

### Data Sources
- `TaskDao.getActiveTasks()` — today's active tasks
- `UserStatsDao.getStats()` — energy level, streak, completed tasks
- All data read from existing Room DB (no new tables)

## [S4] Widget Architecture

### New Files
- `app/src/main/java/.../widget/PhoenixWidgetProvider.kt` — AppWidgetProvider subclass
- `app/src/main/java/.../widget/PhoenixWidgetRepository.kt` — reads data from Room
- `app/src/main/java/.../widget/PhoenixWidgetWorker.kt` — periodic refresh worker
- `app/src/main/res/layout/widget_phoenix.xml` — widget layout (RemoteViews)
- `app/src/main/res/xml/widget_phoenix_info.xml` — widget metadata
- `app/src/main/AndroidManifest.xml` — widget receiver declaration

### Dependencies
- Room DB (existing) for task and stats data
- Hilt for dependency injection
- WorkManager for periodic refresh
- RemoteViews for widget layout

## [S5] Widget Layout (4x2)

### Visual Structure
```
┌─────────────────────────────┐
│ 🔥 Phoenix Protocol    ⚡85 │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━  │
│ ☐ Morning workout     0/1  │
│ ☐ Read 30 pages       0/1  │
│ ☐ Meditate            1/1 ✓│
│ ━━━━━━━━━━━━━━━━━━━━━━━━━  │
│ Progress: 1/3  Streak: 5🔥 │
└─────────────────────────────┘
```

### Layout Components
- **Header row:** App icon (16dp) + Title (14sp bold) + Energy icon + value
- **Divider:** 1dp line with PhoenixOrange color
- **Task items:** Checkbox (12dp) + Title (12sp) + Status text
- **Footer row:** Progress text + Divider + Streak text

### Colors
- Background: #0D0D0D (BackgroundDark)
- Text: #FFFFFF (white)
- Accent: #FF6B35 (PhoenixOrange)
- Completed: #4CAF50 (CompletedGreen)
- Divider: #1A1A1A

## [S6] Widget Updates

### Real-time Updates
- Room Flow emits on data changes → triggers widget update
- `PhoenixWidgetProvider.onUpdate()` called by system
- `PhoenixWidgetRepository` reads current data and builds RemoteViews

### Periodic Refresh
- WorkManager periodic task: every 30 minutes
- Fallback for when Flow doesn't trigger (e.g., app killed)
- `PhoenixWidgetWorker` calls `AppWidgetManager.updateAppWidget()`

### Manual Refresh
- On app open, force widget update via `AppWidgetManager`
- Widget tap opens MainActivity (PendingIntent)

## [S7] Widget Registration

### AndroidManifest.xml
```xml
<receiver
    android:name=".widget.PhoenixWidgetProvider"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/widget_phoenix_info" />
</receiver>
```

### Widget Info (widget_phoenix_info.xml)
- minWidth: 250dp (4 cells)
- minHeight: 110dp (2 cells)
- updatePeriodMillis: 1800000 (30 min)
- initialLayout: @layout/widget_phoenix
- resizeMode: horizontal|vertical
- widgetCategory: home_screen

## [S8] Localization

All widget text bilingual (EN + FA):

| Key | English | Persian |
|-----|---------|---------|
| widget_title | Phoenix Protocol | پروتکس فینیکس |
| widget_energy | Energy | انرژی |
| widget_progress | Progress | پیشرفت |
| widget_streak | Streak | سِری |
| widget_no_tasks | No active tasks | تسک فعالی نیست |
| widget_task_complete | Done | انجام شد |
| widget_task_pending | Pending | در انتظار |

## [S9] Testing Strategy

- Unit test: PhoenixWidgetRepository data reading
- Integration test: Widget updates on data change
- UI test: Widget layout rendering
- Build verification: `./gradlew assembleDebug` after each task
