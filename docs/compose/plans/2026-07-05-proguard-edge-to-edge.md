# ProGuard + Edge-to-edge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable R8/ProGuard minification for release builds and switch to modern edge-to-edge API.

**Architecture:** ProGuard rules added to proguard-rules.pro, minify enabled in build.gradle.kts, edge-to-edge via enableEdgeToEdge() API.

**Tech Stack:** Kotlin, AndroidX Activity, R8/ProGuard

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `app/build.gradle.kts` | Modify | Enable minify + shrinkResources |
| `app/proguard-rules.pro` | Modify | Add keep rules |
| `ui/theme/Theme.kt` | Modify | Remove deprecated window colors |
| `MainActivity.kt` | Modify | Add enableEdgeToEdge() |

---

### Task 1: ProGuard Configuration

**Covers:** [S3]

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/proguard-rules.pro`

- [ ] **Step 1: Enable minify and shrinkResources in build.gradle.kts**

Change release buildType:
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

- [ ] **Step 2: Add ProGuard rules to proguard-rules.pro**

Replace the entire file with:
```proguard
# ProGuard rules for PhoenixProtocol

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Gson entity classes (Room entities used with Gson)
-keep class com.benyaminrasouli.phoenixprotocol.core.data.db.entity.** { *; }
-keep class com.benyaminrasouli.phoenixprotocol.core.domain.model.** { *; }

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Compose
-dontwarn androidx.compose.**

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
```

- [ ] **Step 3: Build release variant to verify**

```bash
.\gradlew.bat assembleRelease
```

Expected: BUILD SUCCESSFUL (may take longer due to minification)

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts app/proguard-rules.pro
git commit -m "build: enable R8/ProGuard minification for release builds"
```

---

### Task 2: Edge-to-edge Setup

**Covers:** [S4]

**Files:**
- Modify: `MainActivity.kt`
- Modify: `ui/theme/Theme.kt`

- [ ] **Step 1: Add enableEdgeToEdge to MainActivity.kt**

In `onCreate`, before `setContent`:
```kotlin
enableEdgeToEdge()
```

Add import:
```kotlin
import androidx.activity.enableEdgeToEdge
```

- [ ] **Step 2: Remove deprecated window colors from Theme.kt**

Remove the `SideEffect` block that sets `window.statusBarColor` and `window.navigationBarColor`. Replace with:
```kotlin
val view = LocalView.current
if (!view.isInEditMode) {
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
}
```

- [ ] **Step 3: Build and verify**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add MainActivity.kt ui/theme/Theme.kt
git commit -m "feat: switch to enableEdgeToEdge() API"
```

---

### Task 3: Final Build Verification

**Covers:** [S3, S4]

- [ ] **Step 1: Full debug build**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Full release build**

```bash
.\gradlew.bat assembleRelease
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "build: ProGuard + edge-to-edge complete for Play Store readiness"
```
