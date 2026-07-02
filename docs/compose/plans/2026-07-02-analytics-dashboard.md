# Analytics Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an analytics dashboard with four pure Compose Canvas charts: weekly task completion, XP trend, category breakdown, and focus time.

**Architecture:** New DAO queries for aggregated data, AnalyticsRepository for data access, three reusable chart composables (BarChart, LineChart, DonutChart), and AnalyticsScreen with tab switching.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Material 3, Compose Canvas

## Global Constraints
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- All UI text via `stringResource()` — never hardcode
- Bilingual: English + Persian (RTL)
- Feature-based Clean Architecture with Hilt DI
- Build verified: `./gradlew assembleDebug` after each task
- No new DB migration — queries only on existing tables
- All new strings must have both English and Persian locales

---

## STEP 1 — Data Layer

### Task 1: DAO Queries + AnalyticsRepository

**Covers:** [S4]

**Files:**
- Modify: `app/src/main/java/.../core/data/db/dao/TaskDao.kt`
- Modify: `app/src/main/java/.../core/data/db/dao/FocusSessionDao.kt`
- Create: `app/src/main/java/.../core/domain/repository/AnalyticsRepository.kt`
- Create: `app/src/main/java/.../core/data/repository/AnalyticsRepositoryImpl.kt`
- Modify: `app/src/main/java/.../di/AppModule.kt`

**Interfaces:**
- Consumes: TaskDao, FocusSessionDao, CategoryDao
- Produces: AnalyticsRepository

- [ ] **Step 1: Add query result data classes**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/AnalyticsModels.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

data class DayCount(val day: String, val count: Int)
data class DayTotal(val day: String, val total: Int)
data class CategoryCount(val categoryId: Long?, val count: Int)
```

- [ ] **Step 2: Add queries to TaskDao**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/TaskDao.kt` — add:
```kotlin
@Query("SELECT DATE(completedAt/1000, 'unixepoch', 'localtime') as day, COUNT(*) as count FROM tasks WHERE status = 'COMPLETED' AND completedAt > :since GROUP BY day ORDER BY day")
suspend fun getCompletedTasksByDay(since: Long): List<DayCount>

@Query("SELECT DATE(completedAt/1000, 'unixepoch', 'localtime') as day, SUM(xpValue) as total FROM tasks WHERE status = 'COMPLETED' AND completedAt > :since GROUP BY day ORDER BY day")
suspend fun getXpByDay(since: Long): List<DayTotal>

@Query("SELECT categoryId, COUNT(*) as count FROM tasks WHERE status = 'COMPLETED' GROUP BY categoryId")
suspend fun getTaskCountByCategory(): List<CategoryCount>
```

Add import for the data classes.

- [ ] **Step 3: Add query to FocusSessionDao**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/FocusSessionDao.kt` — add:
```kotlin
@Query("SELECT DATE(startedAt/1000, 'unixepoch', 'localtime') as day, SUM(durationSeconds)/60 as total FROM focus_sessions WHERE completed = 1 AND startedAt > :since GROUP BY day ORDER BY day")
suspend fun getFocusMinutesByDay(since: Long): List<DayTotal>
```

Add import for DayTotal.

- [ ] **Step 4: Create AnalyticsRepository interface**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/AnalyticsRepository.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayTotal
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryCount

interface AnalyticsRepository {
    suspend fun getWeeklyTaskCompletion(): List<DayCount>
    suspend fun getXpTrend(): List<DayTotal>
    suspend fun getCategoryBreakdown(): List<CategoryCount>
    suspend fun getWeeklyFocusMinutes(): List<DayTotal>
}
```

- [ ] **Step 5: Create AnalyticsRepositoryImpl**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/AnalyticsRepositoryImpl.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayTotal
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.FocusSessionDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AnalyticsRepository
import java.util.Calendar
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val focusSessionDao: FocusSessionDao
) : AnalyticsRepository {

    private fun sevenDaysAgo(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun thirtyDaysAgo(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    override suspend fun getWeeklyTaskCompletion(): List<DayCount> {
        return taskDao.getCompletedTasksByDay(sevenDaysAgo())
    }

    override suspend fun getXpTrend(): List<DayTotal> {
        return taskDao.getXpByDay(thirtyDaysAgo())
    }

    override suspend fun getCategoryBreakdown(): List<CategoryCount> {
        return taskDao.getTaskCountByCategory()
    }

    override suspend fun getWeeklyFocusMinutes(): List<DayTotal> {
        return focusSessionDao.getFocusMinutesByDay(sevenDaysAgo())
    }
}
```

- [ ] **Step 6: Update AppModule**

Edit `app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/AppModule.kt` — add:
```kotlin
import com.benyaminrasouli.phoenixprotocol.core.data.repository.AnalyticsRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AnalyticsRepository
```
```kotlin
@Binds
@Singleton
abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
```

- [ ] **Step 7: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 2 — Bilingual Strings

### Task 2: All Phase 10 Strings

**Covers:** [S8]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

- [ ] **Step 1: Add English strings**

Edit `app/src/main/res/values/strings.xml` — add before `</resources>`:
```xml
<!-- Analytics -->
<string name="analytics_title">Analytics</string>
<string name="analytics_this_week">This Week</string>
<string name="analytics_this_month">This Month</string>
<string name="analytics_tasks_completed">Tasks Completed</string>
<string name="analytics_xp_trend">XP Trend</string>
<string name="analytics_category_breakdown">Category Breakdown</string>
<string name="analytics_focus_time">Focus Time</string>
<string name="analytics_total_tasks">Total Tasks</string>
<string name="analytics_minutes">min</string>
<string name="drawer_analytics">Analytics</string>
```

- [ ] **Step 2: Add Persian strings**

Edit `app/src/main/res/values-fa/strings.xml` — add before `</resources>`:
```xml
<!-- Analytics -->
<string name="analytics_title">تحلیل‌ها</string>
<string name="analytics_this_week">این هفته</string>
<string name="analytics_this_month">این ماه</string>
<string name="analytics_tasks_completed">تسک‌های تکمیل شده</string>
<string name="analytics_xp_trend">رونداوتیاز</string>
<string name="analytics_category_breakdown">تفکیک دسته‌بندی</string>
<string name="analytics_focus_time">زمان تمرکز</string>
<string name="analytics_total_tasks">کل تسک‌ها</string>
<string name="analytics_minutes">دقیقه</string>
<string name="drawer_analytics">تحلیل‌ها</string>
```

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 3 — Chart Components

### Task 3: Reusable BarChart + LineChart + DonutChart

**Covers:** [S5]

**Files:**
- Create: `app/src/main/java/.../core/ui/components/BarChart.kt`
- Create: `app/src/main/java/.../core/ui/components/LineChart.kt`
- Create: `app/src/main/java/.../core/ui/components/DonutChart.kt`

**Interfaces:**
- Consumes: Compose Canvas, theme colors
- Produces: 3 reusable chart composables

- [ ] **Step 1: Create BarChart composable**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/ui/components/BarChart.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun BarChart(
    data: List<Pair<String, Int>>,
    barColor: Color = PhoenixOrange,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val barWidth = size.width / (data.size * 2f)
        val chartHeight = size.height - 30.dp.toPx()

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value.toFloat() / maxValue) * chartHeight
            val x = (index * 2 + 1) * barWidth

            // Bar
            drawRect(
                color = barColor,
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidth, barHeight)
            )

            // Value above bar
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    value.toString(),
                    x + barWidth / 2,
                    chartHeight - barHeight - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // Label below bar
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    label,
                    x + barWidth / 2,
                    size.height - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = TextSecondary.hashCode()
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
```

- [ ] **Step 2: Create LineChart composable**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/ui/components/LineChart.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold

@Composable
fun LineChart(
    data: List<Pair<String, Int>>,
    lineColor: Color = PhoenixGold,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val chartHeight = size.height - 30.dp.toPx()
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        val linePath = Path()
        val fillPath = Path()

        data.forEachIndexed { index, (_, value) ->
            val x = index * stepX
            val y = chartHeight - (value.toFloat() / maxValue) * chartHeight

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, chartHeight)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Fill gradient
        fillPath.lineTo(size.width, chartHeight)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
```

- [ ] **Step 3: Create DonutChart composable**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/ui/components/DonutChart.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

@Composable
fun DonutChart(
    segments: List<Triple<String, Int, Color>>,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty()) return

    val total = segments.sumOf { it.second }.coerceAtLeast(1)

    Canvas(
        modifier = modifier.size(150.dp)
    ) {
        val strokeWidth = 30.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        var startAngle = -90f

        segments.forEach { (_, count, color) ->
            val sweepAngle = (count.toFloat() / total) * 360f
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}
```

- [ ] **Step 4: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 4 — UI Layer

### Task 4: AnalyticsViewModel + AnalyticsScreen

**Covers:** [S6]

**Files:**
- Create: `app/src/main/java/.../feature/analytics/AnalyticsViewModel.kt`
- Create: `app/src/main/java/.../feature/analytics/AnalyticsScreen.kt`

**Interfaces:**
- Consumes: AnalyticsRepository, CategoryDao
- Produces: AnalyticsScreen composable

- [ ] **Step 1: Create AnalyticsViewModel**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/analytics/AnalyticsViewModel.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayTotal
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsState(
    val weeklyTaskCompletion: List<DayCount> = emptyList(),
    val xpTrend: List<DayTotal> = emptyList(),
    val categoryBreakdown: List<CategoryCount> = emptyList(),
    val weeklyFocusMinutes: List<DayTotal> = emptyList()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    weeklyTaskCompletion = analyticsRepository.getWeeklyTaskCompletion(),
                    xpTrend = analyticsRepository.getXpTrend(),
                    categoryBreakdown = analyticsRepository.getCategoryBreakdown(),
                    weeklyFocusMinutes = analyticsRepository.getWeeklyFocusMinutes()
                )
            }
        }
    }
}
```

- [ ] **Step 2: Create AnalyticsScreen**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/analytics/AnalyticsScreen.kt`:

The screen should include:
- TopAppBar with back arrow
- Column with 4 chart cards:
  1. Task Completion — BarChart with weekly data
  2. XP Trend — LineChart with 30-day data
  3. Category Breakdown — DonutChart with category colors
  4. Focus Time — BarChart with weekly focus minutes

Use existing theme colors and chart components from Task 3.

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 5 — Navigation Integration

### Task 5: Screen Route + NavGraph + Drawer + Dashboard Wiring

**Covers:** [S7]

**Files:**
- Modify: `app/src/main/java/.../core/navigation/Screen.kt`
- Modify: `app/src/main/java/.../core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/.../feature/drawer/DrawerScreen.kt`
- Modify: `app/src/main/java/.../feature/dashboard/DashboardScreen.kt`

- [ ] **Step 1: Add Screen.Analytics route**

Edit `Screen.kt` — add:
```kotlin
data object Analytics : Screen("analytics")
```

- [ ] **Step 2: Add NavGraph composable**

Edit `NavGraph.kt`:
- Import: `import com.benyaminrasouli.phoenixprotocol.feature.analytics.AnalyticsScreen`
- Add composable block

- [ ] **Step 3: Add Analytics to DrawerScreen**

Edit `DrawerScreen.kt`:
- Add import: `import androidx.compose.material.icons.filled.Insights`
- Add parameter: `onNavigateToAnalytics: () -> Unit = {},`
- Add menu item below Leaderboard

- [ ] **Step 4: Wire callback in DashboardScreen**

Edit `DashboardScreen.kt` — add callback to DrawerScreen call

- [ ] **Step 5: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 6 — Final Verification

### Task 6: Full Build Verification

**Covers:** [S1, S2, S3, S4, S5, S6, S7, S8, S9]

- [ ] **Step 1: Full build verification**

Run: `.\gradlew clean assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Final commit**

```bash
git add -A
git commit -m "feat: Phase 10 complete - Analytics Dashboard with Canvas charts"
```
