# Data Export/Backup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add JSON export/import of all database tables from Settings screen.

**Architecture:** DataExportManager handles serialization/deserialization using Gson. SettingsViewModel exposes export/import functions. SettingsScreen gets Export/Import buttons in Data & Privacy section.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Gson, ActivityResultContracts

## Global Constraints
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- All UI text via `stringResource()` — never hardcode
- Bilingual: English + Persian (RTL)
- Feature-based Clean Architecture with Hilt DI
- Build verified: `./gradlew assembleDebug` after each task
- No new DB migration — reads/writes existing tables only
- All new strings must have both English and Persian locales

---

## STEP 1 — Data Layer

### Task 1: DataExportManager

**Covers:** [S3, S4]

**Files:**
- Create: `app/src/main/java/.../core/data/export/DataExportManager.kt`

**Interfaces:**
- Consumes: All DAOs (via Hilt injection)
- Produces: DataExportManager with export() and import() methods

- [ ] **Step 1: Create DataExportManager**

Create `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/export/DataExportManager.kt`:
```kotlin
package com.benyaminrasouli.phoenixprotocol.core.data.export

import android.content.Context
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class ExportResult(
    val success: Boolean,
    val filePath: String? = null,
    val error: String? = null
)

data class ImportResult(
    val success: Boolean,
    val error: String? = null
)

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao,
    private val userProfileDao: UserProfileDao,
    private val userStatsDao: UserStatsDao,
    private val achievementDao: AchievementDao,
    private val bossDao: BossDao,
    private val dailyChallengeDao: DailyChallengeDao,
    private val templateDao: TemplateDao,
    private val categoryDao: CategoryDao,
    private val focusSessionDao: FocusSessionDao,
    private val shadowLogDao: ShadowLogDao
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val exportVersion = 10

    suspend fun export(): ExportResult {
        return try {
            val json = JSONObject()
            json.put("version", exportVersion)
            json.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()))
            json.put("appName", "PhoenixProtocol")

            // Profile
            userProfileDao.getProfile()?.let {
                json.put("profile", JSONObject(gson.toJson(it)))
            }

            // Stats
            userStatsDao.getStatsOnce()?.let {
                json.put("stats", JSONObject(gson.toJson(it)))
            }

            // Tasks
            val tasks = taskDao.getAllTasksOnce()
            json.put("tasks", JSONArray(gson.toJson(tasks)))

            // Categories
            val categories = categoryDao.getAllCategoriesOnce()
            json.put("categories", JSONArray(gson.toJson(categories)))

            // Templates
            val templates = templateDao.getAllTemplatesOnce()
            json.put("templates", JSONArray(gson.toJson(templates)))

            // Achievements
            val achievements = achievementDao.getAllAchievementsOnce()
            json.put("achievements", JSONArray(gson.toJson(achievements)))

            // User Achievements
            val userAchievements = achievementDao.getAllUnlockedOnce()
            json.put("userAchievements", JSONArray(gson.toJson(userAchievements)))

            // Bosses
            val bosses = bossDao.getAllBossesOnce()
            json.put("bosses", JSONArray(gson.toJson(bosses)))

            // Daily Challenges
            val challenges = dailyChallengeDao.getAllChallengesOnce()
            json.put("dailyChallenges", JSONArray(gson.toJson(challenges)))

            // Focus Sessions
            val sessions = focusSessionDao.getAllSessionsOnce()
            json.put("focusSessions", JSONArray(gson.toJson(sessions)))

            // Shadow Log
            val shadowLogs = shadowLogDao.getAllLogsOnce()
            json.put("shadowLog", JSONArray(gson.toJson(shadowLogs)))

            // Write to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "phoenix_protocol_backup_$timestamp.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(json.toString(2))

            ExportResult(success = true, filePath = file.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message)
        }
    }

    suspend fun import(jsonString: String): ImportResult {
        return try {
            val json = JSONObject(jsonString)
            val version = json.optInt("version", 0)
            if (version > exportVersion) {
                return ImportResult(success = false, error = "Incompatible backup version")
            }

            // Clear existing data (order matters for FK)
            shadowLogDao.deleteAll()
            focusSessionDao.deleteAll()
            dailyChallengeDao.deleteAll()
            bossDao.deleteAll()
            achievementDao.deleteAllUserAchievements()
            achievementDao.deleteAll()
            templateDao.deleteAll()
            taskDao.deleteAll()
            categoryDao.deleteAll()
            userStatsDao.deleteAllStats()
            userProfileDao.deleteAll()

            // Import data
            json.optJSONObject("profile")?.let {
                userProfileDao.insertProfile(gson.fromJson(it.toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile::class.java))
            }
            json.optJSONObject("stats")?.let {
                userStatsDao.insertStats(gson.fromJson(it.toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats::class.java))
            }
            json.optJSONArray("tasks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    taskDao.insertTask(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task::class.java))
                }
            }
            json.optJSONArray("categories")?.let { arr ->
                for (i in 0 until arr.length()) {
                    categoryDao.insertCategory(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Category::class.java))
                }
            }
            json.optJSONArray("templates")?.let { arr ->
                for (i in 0 until arr.length()) {
                    templateDao.insertTemplate(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Template::class.java))
                }
            }
            json.optJSONArray("achievements")?.let { arr ->
                for (i in 0 until arr.length()) {
                    achievementDao.insertAchievement(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Achievement::class.java))
                }
            }
            json.optJSONArray("userAchievements")?.let { arr ->
                for (i in 0 until arr.length()) {
                    achievementDao.insertUserAchievement(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserAchievement::class.java))
                }
            }
            json.optJSONArray("bosses")?.let { arr ->
                for (i in 0 until arr.length()) {
                    bossDao.insertBoss(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Boss::class.java))
                }
            }
            json.optJSONArray("dailyChallenges")?.let { arr ->
                for (i in 0 until arr.length()) {
                    dailyChallengeDao.insertChallenge(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge::class.java))
                }
            }
            json.optJSONArray("focusSessions")?.let { arr ->
                for (i in 0 until arr.length()) {
                    focusSessionDao.insertSession(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.FocusSession::class.java))
                }
            }
            json.optJSONArray("shadowLog")?.let { arr ->
                for (i in 0 until arr.length()) {
                    shadowLogDao.insert(gson.fromJson(arr[i].toString(), com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog::class.java))
                }
            }

            ImportResult(success = true)
        } catch (e: Exception) {
            ImportResult(success = false, error = e.message)
        }
    }
}
```

**Note:** This requires adding `getAllTasksOnce()`, `getAllCategoriesOnce()`, `getAllTemplatesOnce()`, `getAllAchievementsOnce()`, `getAllUnlockedOnce()`, `getAllBossesOnce()`, `getAllChallengesOnce()`, `getAllSessionsOnce()`, `getAllLogsOnce()` suspend functions to existing DAOs. Also needs `deleteAll()` methods where missing.

- [ ] **Step 2: Add missing DAO methods**

Add suspend functions to existing DAOs where needed:
- TaskDao: `suspend fun getAllTasksOnce(): List<Task>`, `suspend fun deleteAll()`
- CategoryDao: `suspend fun getAllCategoriesOnce(): List<Category>`, `suspend fun deleteAll()`
- TemplateDao: `suspend fun getAllTemplatesOnce(): List<Template>`, `suspend fun deleteAll()`
- AchievementDao: `suspend fun getAllAchievementsOnce(): List<Achievement>`, `suspend fun getAllUnlockedOnce(): List<UserAchievement>`, `suspend fun deleteAll()`, `suspend fun deleteAllUserAchievements()`
- BossDao: `suspend fun getAllBossesOnce(): List<Boss>`, `suspend fun deleteAll()`
- DailyChallengeDao: `suspend fun getAllChallengesOnce(): List<DailyChallenge>`, `suspend fun deleteAll()`
- FocusSessionDao: `suspend fun getAllSessionsOnce(): List<FocusSession>`, `suspend fun deleteAll()`
- ShadowLogDao: `suspend fun getAllLogsOnce(): List<ShadowLog>`, `suspend fun deleteAll()`
- UserProfileDao: `suspend fun deleteAll()`

- [ ] **Step 3: Build verify**

Run: `.\gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

## STEP 2 — Bilingual Strings

### Task 2: All Phase 11 Strings

**Covers:** [S7]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

- [ ] **Step 1: Add English strings**

```xml
<!-- Data Export -->
<string name="settings_export_data">Export Data</string>
<string name="settings_import_data">Import Data</string>
<string name="settings_export_success">Data exported successfully</string>
<string name="settings_export_error">Export failed</string>
<string name="settings_import_confirm">This will replace all current data. Continue?</string>
<string name="settings_import_success">Data imported successfully</string>
<string name="settings_import_error">Import failed — invalid file</string>
<string name="settings_import_wrong_version">Incompatible backup version</string>
```

- [ ] **Step 2: Add Persian strings**

```xml
<!-- Data Export -->
<string name="settings_export_data">خروجی داده‌ها</string>
<string name="settings_import_data">ورودی داده‌ها</string>
<string name="settings_export_success">داده‌ها با موفقیت خروجی گرفته شد</string>
<string name="settings_export_error">خروجی ناموفق بود</string>
<string name="settings_import_confirm">این تمام داده‌های فعلی رو جایگزین می‌کنه. ادامه؟</string>
<string name="settings_import_success">داده‌ها با موفقیت وارد شد</string>
<string name="settings_import_error">ورودی ناموفق — فایل نامعتبر</string>
<string name="settings_import_wrong_version">نسخه بکاپ سازگار نیست</string>
```

- [ ] **Step 3: Build verify**

---

## STEP 3 — UI Integration

### Task 3: SettingsViewModel + SettingsScreen Updates

**Covers:** [S5, S6]

**Files:**
- Modify: `app/src/main/java/.../feature/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/.../feature/settings/SettingsScreen.kt`

- [ ] **Step 1: Update SettingsViewModel**

Add DataExportManager injection and export/import methods.

- [ ] **Step 2: Update SettingsScreen**

Add Export/Import buttons in Data & Privacy section. Add file picker launcher. Add confirmation dialog for import.

- [ ] **Step 3: Build verify**

---

## STEP 4 — Final Verification

### Task 4: Full Build Verification

**Covers:** [S1, S2, S3, S4, S5, S6, S7, S8]

- [ ] **Step 1: Full build verification**

Run: `.\gradlew clean assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Final commit**

```bash
git add -A
git commit -m "feat: Phase 11 complete - Data Export/Backup with JSON export/import"
```
