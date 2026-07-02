# Phase 5: Home Screen Widget Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Android App Widget (4x2) showing today's tasks, energy level, and streak with real-time updates.

**Architecture:** AppWidgetProvider + RemoteViews for display, Room DB for data, WorkManager for periodic refresh. Widget reads from existing TaskDao and UserStatsDao — no new tables needed.

**Tech Stack:** Kotlin, Android AppWidget, Room, Hilt, WorkManager, RemoteViews

## Global Constraints
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- All UI text via `stringResource()` — never hardcode
- Bilingual: English + Persian (RTL)
- Feature-based Clean Architecture with Hilt DI
- Build verified: `./gradlew assembleDebug` after each task
- Room DB at version 5
- Widget uses RemoteViews (no Compose)

---

### Task 1: Widget Layout & Metadata

**Covers:** [S5, S7]

**Files:**
- Create: `app/src/main/res/layout/widget_phoenix.xml`
- Create: `app/src/main/res/xml/widget_phoenix_info.xml`
- Modify: `app/src/main/AndroidManifest.xml` — add widget receiver

**Interfaces:**
- Consumes: existing AndroidManifest.xml
- Produces: Widget layout XML, widget info XML, manifest declaration

- [ ] **Step 1: Create widget layout**

Create `app/src/main/res/layout/widget_phoenix.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#0D0D0D"
    android:padding="8dp">

    <!-- Header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <ImageView
            android:id="@+id/widget_icon"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:src="@mipmap/ic_launcher"
            android:contentDescription="@string/app_name" />

        <TextView
            android:id="@+id/widget_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="4dp"
            android:text="@string/widget_title"
            android:textColor="#FFFFFF"
            android:textSize="14sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/widget_energy"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#FF6B35"
            android:textSize="12sp" />
    </LinearLayout>

    <!-- Divider -->
    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:layout_marginVertical="4dp"
        android:background="#1A1A1A" />

    <!-- Task List -->
    <LinearLayout
        android:id="@+id/widget_task_list"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:orientation="vertical" />

    <!-- Footer -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <TextView
            android:id="@+id/widget_progress"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:textColor="#AAAAAA"
            android:textSize="11sp" />

        <TextView
            android:id="@+id/widget_streak"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#FF6B35"
            android:textSize="11sp" />
    </LinearLayout>
</LinearLayout>
```

- [ ] **Step 2: Create widget item layout**

Create `app/src/main/res/layout/widget_task_item.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingVertical="2dp">

    <CheckBox
        android:id="@+id/task_checkbox"
        android:layout_width="12dp"
        android:layout_height="12dp"
        android:buttonTint="#FF6B35"
        android:focusable="false" />

    <TextView
        android:id="@+id/task_title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginStart="6dp"
        android:textColor="#FFFFFF"
        android:textSize="12sp"
        android:maxLines="1"
        android:ellipsize="end" />

    <TextView
        android:id="@+id/task_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="#AAAAAA"
        android:textSize="10sp" />
</LinearLayout>
```

- [ ] **Step 3: Create widget info XML**

Create `app/src/main/res/xml/widget_phoenix_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="110dp"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/widget_phoenix"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:description="@string/widget_description"
    android:previewLayout="@layout/widget_phoenix" />
```

- [ ] **Step 4: Add widget receiver to AndroidManifest**

Edit `app/src/main/AndroidManifest.xml` — add inside `<application>`:
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

- [ ] **Step 5: Add widget strings**

Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="widget_title">Phoenix Protocol</string>
<string name="widget_energy">⚡ %d</string>
<string name="widget_progress">Progress: %d/%d</string>
<string name="widget_streak">🔥 %d</string>
<string name="widget_no_tasks">No active tasks</string>
<string name="widget_task_complete">Done</string>
<string name="widget_task_pending">Pending</string>
<string name="widget_description">Daily task progress widget</string>
```

Edit `app/src/main/res/values-fa/strings.xml`:
```xml
<string name="widget_title">پروتکل فینیکس</string>
<string name="widget_energy">⚡ %d</string>
<string name="widget_progress">پیشرفت: %d/%d</string>
<string name="widget_streak">🔥 %d</string>
<string name="widget_no_tasks">تسک فعالی نیست</string>
<string name="widget_task_complete">انجام شد</string>
<string name="widget_task_pending">در انتظار</string>
<string name="widget_description">ویجت پیشرفت تسک‌های روزانه</string>
```

- [ ] **Step 6: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/layout/widget_phoenix.xml
git add app/src/main/res/layout/widget_task_item.xml
git add app/src/main/res/xml/widget_phoenix_info.xml
git add app/src/main/AndroidManifest.xml
git add app/src/main/res/values/strings.xml
git add app/src/main/res/values-fa/strings.xml
git commit -m "feat: widget layout, metadata, and manifest declaration"
```

---

### Task 2: Widget Repository

**Covers:** [S3, S6]

**Files:**
- Create: `app/src/main/java/.../widget/PhoenixWidgetRepository.kt`

**Interfaces:**
- Consumes: `TaskDao`, `UserStatsDao` (existing)
- Produces: `PhoenixWidgetRepository` with data reading methods

- [ ] **Step 1: Create PhoenixWidgetRepository**

Create `app/src/main/java/.../widget/PhoenixWidgetRepository.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.widget

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class WidgetData(
    val activeTasks: List<WidgetTask>,
    val completedCount: Int,
    val totalCount: Int,
    val energy: Int,
    val streak: Int
)

data class WidgetTask(
    val title: String,
    val isCompleted: Boolean
)

@Singleton
class PhoenixWidgetRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val userStatsDao: UserStatsDao
) {
    suspend fun getWidgetData(): WidgetData {
        val allTasks = taskDao.getAllTasks().first()
        val stats = userStatsDao.getStatsOnce()

        val activeTasks = allTasks
            .filter { it.status != "COMPLETED" && it.status != "SKIPPED" }
            .take(4)
            .map { WidgetTask(title = it.title, isCompleted = false) }

        val completedTasks = allTasks.filter { it.status == "COMPLETED" }
        val todayCompleted = completedTasks.count {
            val today = java.time.LocalDate.now().toString()
            it.completedAt?.let { completedAt ->
                java.time.Instant.ofEpochMilli(completedAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().toString() == today
            } ?: false
        }

        return WidgetData(
            activeTasks = activeTasks,
            completedCount = todayCompleted,
            totalCount = allTasks.size,
            energy = stats?.phoenixEnergy ?: 0,
            streak = stats?.currentStreak ?: 0
        )
    }
}
```

- [ ] **Step 2: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/.../widget/PhoenixWidgetRepository.kt
git commit -m "feat: PhoenixWidgetRepository for widget data"
```

---

### Task 3: Widget Provider

**Covers:** [S4, S6]

**Files:**
- Create: `app/src/main/java/.../widget/PhoenixWidgetProvider.kt`

**Interfaces:**
- Consumes: `PhoenixWidgetRepository` (from Task 2)
- Produces: `PhoenixWidgetProvider` AppWidgetProvider subclass

- [ ] **Step 1: Create PhoenixWidgetProvider**

Create `app/src/main/java/.../widget/PhoenixWidgetProvider.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.benyaminrasouli.phoenixprotocol.MainActivity
import com.benyaminrasouli.phoenixprotocol.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhoenixWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var repository: PhoenixWidgetRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            try {
                val data = repository.getWidgetData()
                val views = RemoteViews(context.packageName, R.layout.widget_phoenix)

                // Header
                views.setTextViewText(R.widget_title, context.getString(R.string.widget_title))
                views.setTextViewText(R.widget_energy, context.getString(R.string.widget_energy, data.energy))

                // Task list
                views.removeAllViews(R.widget_task_list)

                if (data.activeTasks.isEmpty()) {
                    val noTasksView = RemoteViews(context.packageName, R.layout.widget_task_item)
                    noTasksView.setTextViewText(R.task_title, context.getString(R.string.widget_no_tasks))
                    noTasksView.setTextColor(R.task_title, 0xFFAAAAAA.toInt())
                    views.addView(R.widget_task_list, noTasksView)
                } else {
                    for (task in data.activeTasks) {
                        val taskView = RemoteViews(context.packageName, R.layout.widget_task_item)
                        taskView.setTextViewText(R.task_title, task.title)
                        taskView.setTextViewText(R.task_status, context.getString(R.string.widget_task_pending))
                        views.addView(R.widget_task_list, taskView)
                    }
                }

                // Footer
                views.setTextViewText(R.widget_progress, context.getString(R.string.widget_progress, data.completedCount, data.totalCount))
                views.setTextViewText(R.widget_streak, context.getString(R.string.widget_streak, data.streak))

                // Tap to open app
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.widget_title, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        PhoenixWidgetWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        PhoenixWidgetWorker.cancel(context)
    }
}
```

- [ ] **Step 2: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/.../widget/PhoenixWidgetProvider.kt
git commit -m "feat: PhoenixWidgetProvider with real-time updates"
```

---

### Task 4: Widget Worker

**Covers:** [S6]

**Files:**
- Create: `app/src/main/java/.../widget/PhoenixWidgetWorker.kt`

**Interfaces:**
- Consumes: `PhoenixWidgetRepository` (from Task 2)
- Produces: `PhoenixWidgetWorker` for periodic refresh

- [ ] **Step 1: Create PhoenixWidgetWorker**

Create `app/src/main/java/.../widget/PhoenixWidgetWorker.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class PhoenixWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val componentName = ComponentName(applicationContext, PhoenixWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

        // Trigger widget update
        val intent = android.content.Intent(applicationContext, PhoenixWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        }
        applicationContext.sendBroadcast(intent)

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_refresh"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<PhoenixWidgetWorker>(
                30, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
```

- [ ] **Step 2: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/.../widget/PhoenixWidgetWorker.kt
git commit -m "feat: PhoenixWidgetWorker for periodic widget refresh"
```

---

### Task 5: Hilt Widget Setup

**Covers:** [S4]

**Files:**
- Modify: `app/src/main/java/.../di/AppModule.kt` — add widget worker binding

**Interfaces:**
- Consumes: `PhoenixWidgetWorker` (from Task 4)
- Produces: Hilt configuration for widget worker

- [ ] **Step 1: Add Hilt worker configuration**

Edit `app/src/main/java/.../di/AppModule.kt` — add:
```kotlin
@Suppress("UNCHECKED_CAST")
@Binds
abstract fun bindPhoenixWidgetWorkerFactory(
    factory: PhoenixWidgetWorkerFactory
) : androidx.work.WorkerFactory
```

Actually, the `@HiltWorker` annotation on `PhoenixWidgetWorker` should handle DI automatically via `HiltWorkerFactory`. No additional binding needed if `Application` implements `Configuration.Provider`.

Check if `PhoenixApp.kt` implements `Configuration.Provider`. If not, add it:

Edit `app/src/main/java/.../PhoenixApp.kt`:
```kotlin
import androidx.work.Configuration

@HiltAndroidApp
class PhoenixApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

- [ ] **Step 2: Build verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/.../PhoenixApp.kt
git commit -m "feat: Hilt WorkManager configuration for widget worker"
```

---

### Task 6: Final Build Verification

**Covers:** [S1, S2, S3, S4, S5, S6, S7, S8, S9]

- [ ] **Step 1: Full build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Final commit if needed**

```bash
git add -A
git commit -m "feat: Phase 5 Home Screen Widget complete"
```
