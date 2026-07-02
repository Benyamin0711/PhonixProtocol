# Phase 10: Analytics Dashboard — Design Spec

## [S1] Problem

The app has a Statistics screen showing basic numbers (XP, level, tasks, streaks) but no visual analytics. Users can't see trends, patterns, or distributions over time. No charts, no historical data visualization.

## [S2] Solution Overview

A dedicated Analytics Dashboard with four pure Compose Canvas charts: weekly task completion bars, XP trend line, category breakdown donut, and focus time bars. No new entities — all data queried from existing tables.

## [S3] Charts

### Weekly Task Completion (Bar Chart)

- X-axis: last 7 days (Mon–Sun)
- Y-axis: number of tasks completed
- Data: query TaskDao for completed tasks grouped by `completedAt` date
- Bar color: PhoenixOrange

### XP Trend (Line Chart)

- X-axis: last 30 days
- Y-axis: XP earned per day
- Data: query TaskDao for XP values grouped by `completedAt` date
- Line color: PhoenixGold, fill gradient below line

### Category Breakdown (Donut Chart)

- Segments: one per category with task count
- Data: query TaskDao for task counts grouped by `categoryId`, join with Category table for names/colors
- Center text: total task count

### Focus Time (Bar Chart)

- X-axis: last 7 days
- Y-axis: focus minutes per day
- Data: query FocusSessionDao for `durationSeconds` grouped by `startedAt` date
- Bar color: PhoenixOrange

## [S4] Data Layer

### New DAO Queries

**TaskDao additions:**
```kotlin
@Query("SELECT DATE(completedAt/1000, 'unixepoch', 'localtime') as day, COUNT(*) as count FROM tasks WHERE status = 'COMPLETED' AND completedAt > :since GROUP BY day ORDER BY day")
suspend fun getCompletedTasksByDay(since: Long): List<DayCount>

@Query("SELECT DATE(completedAt/1000, 'unixepoch', 'localtime') as day, SUM(xpValue) as total FROM tasks WHERE status = 'COMPLETED' AND completedAt > :since GROUP BY day ORDER BY day")
suspend fun getXpByDay(since: Long): List<DayTotal>

@Query("SELECT categoryId, COUNT(*) as count FROM tasks WHERE status = 'COMPLETED' GROUP BY categoryId")
suspend fun getTaskCountByCategory(): List<CategoryCount>
```

**FocusSessionDao additions:**
```kotlin
@Query("SELECT DATE(startedAt/1000, 'unixepoch', 'localtime') as day, SUM(durationSeconds)/60 as total FROM focus_sessions WHERE completed = 1 AND startedAt > :since GROUP BY day ORDER BY day")
suspend fun getFocusMinutesByDay(since: Long): List<DayTotal>
```

### Query Result Data Classes

```kotlin
data class DayCount(val day: String, val count: Int)
data class DayTotal(val day: String, val total: Int)
data class CategoryCount(val categoryId: Long?, val count: Int)
```

### AnalyticsRepository

```kotlin
interface AnalyticsRepository {
    suspend fun getWeeklyTaskCompletion(): List<DayCount>
    suspend fun getXpTrend(): List<DayTotal>
    suspend fun getCategoryBreakdown(): List<Pair<String, Int>>  // name to count
    suspend fun getWeeklyFocusMinutes(): List<DayTotal>
}
```

## [S5] Reusable Chart Components

### BarChart

- Parameters: data (List<Pair<String, Int>>), barColor, maxHeight
- Composable that draws bars using Canvas
- Labels below bars, values above bars

### LineChart

- Parameters: data (List<Pair<String, Int>>), lineColor, fillColor
- Composable that draws connected points with Canvas
- Gradient fill below line

### DonutChart

- Parameters: segments (List<Triple<String, Int, Color>>), centerText
- Composable that draws arc segments with Canvas
- Legend below chart

## [S6] Analytics Screen UI

- **TopAppBar**: "Analytics" title with back arrow
- **Tab Row**: This Week / This Month
- **Task Completion Card**: Bar chart of daily task counts
- **XP Trend Card**: Line chart of daily XP earned
- **Category Breakdown Card**: Donut chart of task distribution
- **Focus Time Card**: Bar chart of daily focus minutes

## [S7] Navigation

- Route: `Screen.Analytics` → `"analytics"`
- Drawer entry: below Leaderboard, icon = `Icons.Filled.Insights`
- Callback: `onNavigateToAnalytics` added to DrawerScreen

## [S8] Localization

| Key | English | Persian |
|-----|---------|---------|
| analytics_title | Analytics | تحلیل‌ها |
| analytics_this_week | This Week | این هفته |
| analytics_this_month | This Month | این ماه |
| analytics_tasks_completed | Tasks Completed | تسک‌های تکمیل شده |
| analytics_xp_trend | XP Trend | روند امتیاز |
| analytics_category_breakdown | Category Breakdown | تفکیک دسته‌بندی |
| analytics_focus_time | Focus Time | زمان تمرکز |
| analytics_total_tasks | Total Tasks | کل تسک‌ها |
| analytics_minutes | min | دقیقه |
| drawer_analytics | Analytics | تحلیل‌ها |

## [S9] Testing Strategy

- Unit test: AnalyticsRepository returns correct aggregated data
- UI test: Charts render with sample data
- Build verification: `./gradlew assembleDebug` after each task
