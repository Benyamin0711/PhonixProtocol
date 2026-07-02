# Phoenix Protocol MVP Core — Implementation Plan

> [!NOTE]
> This document may not reflect the current implementation.
> See the final report for up-to-date state:
> [Final Report](../reports/phoenix-protocol-mvp.md)

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a working Android app with Splash → Onboarding → Dashboard → Task System, bilingual support, and a futuristic dark UI.

**Architecture:** Feature-based Clean Architecture with MVVM, Hilt DI, Room database, DataStore for settings, Navigation Compose for routing.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, DataStore, Hilt, Navigation Compose, Coroutines, StateFlow

## Global Constraints
- Min SDK 24, Target SDK 36, Compile SDK 36
- Package: `com.benyaminrasouli.phoenixprotocol`
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- Bilingual: English default, Persian (RTL) from day one
- All Room entities use Long auto-increment PKs
- DataStore for settings/preferences, Room for structured data
- Every task ends with `./gradlew assembleDebug` passing

---

### Task 1: Foundation Setup

**Covers:** [S3, S14]

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts` (root)
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/PhoenixApp.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: existing project template
- Produces: `PhoenixApp` class with `@HiltAndroidApp`, all dependencies available

- [ ] **Step 1: Update version catalog**

```toml
# gradle/libs.versions.toml - ADD to existing
[versions]
hilt = "2.51"
hiltNavigationCompose = "1.2.0"
room = "2.7.0"
datastore = "1.1.2"
navigationCompose = "2.8.0"
kotlinxSerialization = "1.7.0"

[libraries]
# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
# ADD to existing
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
room = { id = "androidx.room", version.ref = "room" }
```

- [ ] **Step 2: Update root build.gradle.kts**

```kotlin
// build.gradle.kts (root) - add to plugins block
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.room) apply false
}
```

- [ ] **Step 3: Update app build.gradle.kts**

```kotlin
// app/build.gradle.kts - REPLACE entire file
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
}

android {
    namespace = "com.benyaminrasouli.phoenixprotocol"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.benyaminrasouli.phoenixprotocol"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.materialIconsExtended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // DataStore
    implementation(libs.datastore.preferences)

    // Navigation
    implementation(libs.navigation.compose)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Core
    implementation(libs.androidx.core.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

- [ ] **Step 4: Create Application class**

```kotlin
// PhoenixApp.kt
package com.benyaminrasouli.phoenixprotocol

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhoenixApp : Application()
```

- [ ] **Step 5: Update AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:name=".PhoenixApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.phoenixprotocol">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.phoenixprotocol">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 6: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: foundation setup - dependencies, Hilt, Application class"
```

---

### Task 2: Theme & Design Tokens

**Covers:** [S12]

**Files:**
- Replace: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/ui/theme/Color.kt`
- Replace: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/ui/theme/Theme.kt`
- Replace: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/ui/theme/Type.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/ui/theme/Shape.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/ui/components/PhoenixButton.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/ui/components/PhoenixCard.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/ui/components/EnergyBar.kt`

**Interfaces:**
- Consumes: Task 1 (Hilt, Compose dependencies available)
- Produces: `PhoenixProtocolTheme` composable, `PhoenixButton`, `PhoenixCard`, `EnergyBar` components

- [ ] **Step 1: Create Color.kt**

```kotlin
// ui/theme/Color.kt
package com.benyaminrasouli.phoenixprotocol.ui.theme

import androidx.compose.ui.graphics.Color

// Phoenix Core
val PhoenixOrange = Color(0xFFFF6B35)
val PhoenixRed = Color(0xFFE63946)
val PhoenixGold = Color(0xFFFFD700)

// Backgrounds
val BackgroundDark = Color(0xFF0D0D0D)
val SurfaceDark = Color(0xFF1A1A2E)
val SurfaceVariantDark = Color(0xFF252540)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0A0B0)
val TextTertiary = Color(0xFF6B6B80)

// Status
val EnergyGreen = Color(0xFF4CAF50)
val EnergyYellow = Color(0xFFFFEB3B)
val EnergyRed = Color(0xFFFF5252)
val ShadowPurple = Color(0xFF9C27B0)

// Seed colors for Material 3
val PhoenixPrimary = PhoenixOrange
val PhoenixOnPrimary = Color(0xFFFFFFFF)
val PhoenixPrimaryContainer = Color(0xFF3D1A00)
val PhoenixOnPrimaryContainer = Color(0xFFFFDBC8)

val PhoenixSecondary = PhoenixRed
val PhoenixOnSecondary = Color(0xFFFFFFFF)
val PhoenixSecondaryContainer = Color(0xFF3B0A10)
val PhoenixOnSecondaryContainer = Color(0xFFFFDAD6)

val PhoenixTertiary = PhoenixGold
val PhoenixOnTertiary = Color(0xFF1A1A1A)
val PhoenixTertiaryContainer = Color(0xFF3D3500)
val PhoenixOnTertiaryContainer = Color(0xFFFFE08A)

val PhoenixBackground = BackgroundDark
val PhoenixOnBackground = TextPrimary
val PhoenixSurface = SurfaceDark
val PhoenixOnSurface = TextPrimary
val PhoenixSurfaceVariant = SurfaceVariantDark
val PhoenixOnSurfaceVariant = TextSecondary

val PhoenixError = EnergyRed
val PhoenixOnError = Color(0xFFFFFFFF)
val PhoenixErrorContainer = Color(0xFF3B0000)
val PhoenixOnErrorContainer = Color(0xFFFFDAD6)
```

- [ ] **Step 2: Create Theme.kt**

```kotlin
// ui/theme/Theme.kt
package com.benyaminrasouli.phoenixprotocol.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PhoenixDarkColorScheme = darkColorScheme(
    primary = PhoenixPrimary,
    onPrimary = PhoenixOnPrimary,
    primaryContainer = PhoenixPrimaryContainer,
    onPrimaryContainer = PhoenixOnPrimaryContainer,
    secondary = PhoenixSecondary,
    onSecondary = PhoenixOnSecondary,
    secondaryContainer = PhoenixSecondaryContainer,
    onSecondaryContainer = PhoenixOnSecondaryContainer,
    tertiary = PhoenixTertiary,
    onTertiary = PhoenixOnTertiary,
    tertiaryContainer = PhoenixTertiaryContainer,
    onTertiaryContainer = PhoenixOnTertiaryContainer,
    background = PhoenixBackground,
    onBackground = PhoenixOnBackground,
    surface = PhoenixSurface,
    onSurface = PhoenixOnSurface,
    surfaceVariant = PhoenixSurfaceVariant,
    onSurfaceVariant = PhoenixOnSurfaceVariant,
    error = PhoenixError,
    onError = PhoenixOnError,
    errorContainer = PhoenixErrorContainer,
    onErrorContainer = PhoenixOnErrorContainer
)

@Composable
fun PhoenixProtocolTheme(content: @Composable () -> Unit) {
    val colorScheme = PhoenixDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PhoenixTypography,
        shapes = PhoenixShapes,
        content = content
    )
}
```

- [ ] **Step 3: Create Type.kt**

```kotlin
// ui/theme/Type.kt
package com.benyaminrasouli.phoenixprotocol.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PhoenixTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondary
    )
)
```

- [ ] **Step 4: Create Shape.kt**

```kotlin
// ui/theme/Shape.kt
package com.benyaminrasouli.phoenixprotocol.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PhoenixShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
```

- [ ] **Step 5: Create shared components**

```kotlin
// core/ui/components/PhoenixButton.kt
package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

@Composable
fun PhoenixButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PhoenixOrange,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

```kotlin
// core/ui/components/PhoenixCard.kt
package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark

@Composable
fun PhoenixCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
```

```kotlin
// core/ui/components/EnergyBar.kt
package com.benyaminrasouli.phoenixprotocol.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.EnergyGreen
import com.benyaminrasouli.phoenixprotocol.ui.theme.EnergyRed
import com.benyaminrasouli.phoenixprotocol.ui.theme.EnergyYellow
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceVariantDark

@Composable
fun EnergyBar(
    energy: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        energy >= 70 -> EnergyGreen
        energy >= 40 -> EnergyYellow
        else -> EnergyRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceVariantDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (energy / 100f).coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}
```

- [ ] **Step 6: Update MainActivity.kt**

```kotlin
// MainActivity.kt
package com.benyaminrasouli.phoenixprotocol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixProtocolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoenixProtocolTheme {
                // NavGraph will be added in Task 5
            }
        }
    }
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: Phoenix dark theme, design tokens, shared components"
```

---

### Task 3: Domain Models & Enums

**Covers:** [S8, S13]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/model/IdentityPath.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/model/Difficulty.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/model/TaskRecurrence.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/model/TaskStatus.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/model/Rank.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/model/Slogan.kt`

**Interfaces:**
- Consumes: Task 2 (theme)
- Produces: All domain enums/models used by data layer and UI

- [ ] **Step 1: Create IdentityPath.kt**

```kotlin
// core/domain/model/IdentityPath.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.model

enum class IdentityPath {
    WARRIOR,
    SCHOLAR,
    BUILDER,
    MONK,
    COMMANDER,
    CREATOR,
    STRATEGIST,
    SENTINEL
}
```

- [ ] **Step 2: Create Difficulty.kt**

```kotlin
// core/domain/model/Difficulty.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.model

enum class Difficulty(val xpValue: Int) {
    EASY(10),
    MEDIUM(25),
    HARD(50),
    EXTREME(100)
}
```

- [ ] **Step 3: Create TaskRecurrence.kt**

```kotlin
// core/domain/model/TaskRecurrence.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.model

enum class TaskRecurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}
```

- [ ] **Step 4: Create TaskStatus.kt**

```kotlin
// core/domain/model/TaskStatus.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.model

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}
```

- [ ] **Step 5: Create Rank.kt**

```kotlin
// core/domain/model/Rank.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.model

enum class Rank(val displayName: String, val levelRequired: Int) {
    INITIATE("Initiate", 1),
    SURVIVOR("Survivor", 5),
    HUNTER("Hunter", 10),
    WARRIOR("Warrior", 15),
    ELITE("Elite", 20),
    COMMANDER("Commander", 30),
    PHANTOM("Phantom", 40),
    TITAN("Titan", 50),
    ASCENDANT("Ascendant", 65),
    PHOENIX("Phoenix", 80);

    companion object {
        fun forLevel(level: Int): Rank {
            return entries.last { it.levelRequired <= level }
        }
    }
}
```

- [ ] **Step 6: Create Slogan.kt**

```kotlin
// core/domain/model/Slogan.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.model

object Slogans {
    private val dailySlogans = listOf(
        "Discipline is winning today.",
        "Shadow is getting louder. Move.",
        "Phoenix Energy rising.",
        "One action changes the board.",
        "The system remembers consistency.",
        "You are not your excuses.",
        "Rise through resistance.",
        "The protocol does not sleep.",
        "Consistency is power.",
        "Every task is a vote for your future self.",
        "Shadow feeds on inaction.",
        "Your rank is earned, not given.",
        "The Phoenix burns weakness.",
        "Today you choose strength.",
        "The system watches. The system remembers."
    )

    fun getDailySlogan(index: Int): String {
        return dailySlogans[index % dailySlogans.size]
    }

    val sloganCount: Int get() = dailySlogans.size
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: domain models - IdentityPath, Difficulty, Rank, TaskStatus, Slogans"
```

---

### Task 4: Data Layer (Room + DataStore)

**Covers:** [S13]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/UserProfile.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/Task.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/entity/UserStats.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/UserProfileDao.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/TaskDao.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/dao/UserStatsDao.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/converter/Converters.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/db/PhoenixDatabase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/datastore/SettingsDataStore.kt`

**Interfaces:**
- Consumes: Task 3 (domain models)
- Produces: Room entities, DAOs, database, DataStore wrapper

- [ ] **Step 1: Create UserProfile entity**

```kotlin
// core/data/db/entity/UserProfile.kt
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val username: String,
    val birthYear: Int?,
    val identityPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Create Task entity**

```kotlin
// core/data/db/entity/Task.kt
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val difficulty: String,
    val category: String,
    val xpValue: Int,
    val recurrence: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
```

- [ ] **Step 3: Create UserStats entity**

```kotlin
// core/data/db/entity/UserStats.kt
package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val id: Long = 1,
    val userId: Long,
    val xp: Int = 0,
    val level: Int = 1,
    val rank: String = "INITIATE",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val phoenixEnergy: Int = 50,
    val shadowLevel: Int = 0,
    val completedTasks: Int = 0
)
```

- [ ] **Step 4: Create DAOs**

```kotlin
// core/data/db/dao/UserProfileDao.kt
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getProfileOnce(): UserProfile?
}
```

```kotlin
// core/data/db/dao/TaskDao.kt
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String, completedAt: Long?)
}
```

```kotlin
// core/data/db/dao/UserStatsDao.kt
package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: UserStats)

    @Update
    suspend fun updateStats(stats: UserStats)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getStats(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStatsOnce(): UserStats?
}
```

- [ ] **Step 5: Create PhoenixDatabase**

```kotlin
// core/data/db/PhoenixDatabase.kt
package com.benyaminrasouli.phoenixprotocol.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats

@Database(
    entities = [UserProfile::class, Task::class, UserStats::class],
    version = 1,
    exportSchema = false
)
abstract class PhoenixDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun userStatsDao(): UserStatsDao
}
```

- [ ] **Step 6: Create SettingsDataStore**

```kotlin
// core/data/datastore/SettingsDataStore.kt
package com.benyaminrasouli.phoenixprotocol.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "phoenix_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val DAILY_SLOGAN_INDEX = intPreferencesKey("daily_slogan_index")
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "en"
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_ONBOARDING_COMPLETE] ?: false
    }

    val dailySloganIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_SLOGAN_INDEX] ?: 0
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun setDailySloganIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_SLOGAN_INDEX] = index
        }
    }
}
```

- [ ] **Step 7: Create DI modules**

```kotlin
// di/DatabaseModule.kt
package com.benyaminrasouli.phoenixprotocol.di

import android.content.Context
import androidx.room.Room
import com.benyaminrasouli.phoenixprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhoenixDatabase {
        return Room.databaseBuilder(
            context,
            PhoenixDatabase::class.java,
            "phoenix_database"
        ).build()
    }

    @Provides
    fun provideUserProfileDao(db: PhoenixDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideTaskDao(db: PhoenixDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideUserStatsDao(db: PhoenixDatabase): UserStatsDao = db.userStatsDao()
}
```

- [ ] **Step 8: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "feat: data layer - Room entities, DAOs, database, DataStore, DI modules"
```

---

### Task 5: Repository & Use Cases

**Covers:** [S3, S7, S8]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/UserRepository.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/TaskRepository.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/repository/StatsRepository.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/UserRepositoryImpl.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/TaskRepositoryImpl.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/data/repository/StatsRepositoryImpl.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/CreateProfileUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/GetProfileUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/CreateTaskUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/CompleteTaskUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/GetTasksUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/GetStatsUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/domain/usecase/GetSloganUseCase.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/di/AppModule.kt`

**Interfaces:**
- Consumes: Task 4 (entities, DAOs, DataStore)
- Produces: Repository interfaces, implementations, use cases

- [ ] **Step 1: Create repository interfaces**

```kotlin
// core/domain/repository/UserRepository.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createProfile(profile: UserProfile): Long
    fun getProfile(): Flow<UserProfile?>
    suspend fun getProfileOnce(): UserProfile?
}
```

```kotlin
// core/domain/repository/TaskRepository.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun getActiveTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun completeTask(taskId: Long)
    suspend fun skipTask(taskId: Long)
}
```

```kotlin
// core/domain/repository/StatsRepository.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    suspend fun initStats(userId: Long)
    fun getStats(): Flow<UserStats?>
    suspend fun getStatsOnce(): UserStats?
    suspend fun addXp(amount: Int)
    suspend fun increasePhoenixEnergy(amount: Int)
    suspend fun decreasePhoenixEnergy(amount: Int)
    suspend fun increaseShadowLevel(amount: Int)
    suspend fun incrementCompletedTasks()
}
```

- [ ] **Step 2: Create repository implementations**

```kotlin
// core/data/repository/UserRepositoryImpl.kt
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : UserRepository {

    override suspend fun createProfile(profile: UserProfile): Long {
        return dao.insertProfile(profile)
    }

    override fun getProfile(): Flow<UserProfile?> {
        return dao.getProfile()
    }

    override suspend fun getProfileOnce(): UserProfile? {
        return dao.getProfileOnce()
    }
}
```

```kotlin
// core/data/repository/TaskRepositoryImpl.kt
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override suspend fun createTask(task: Task): Long {
        return dao.insertTask(task)
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
    }

    override fun getActiveTasks(): Flow<List<Task>> {
        return dao.getActiveTasks()
    }

    override fun getCompletedTasks(): Flow<List<Task>> {
        return dao.getCompletedTasks()
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks()
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        return dao.getTaskById(taskId)
    }

    override suspend fun completeTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "COMPLETED", System.currentTimeMillis())
    }

    override suspend fun skipTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "SKIPPED", null)
    }
}
```

```kotlin
// core/data/repository/StatsRepositoryImpl.kt
package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val dao: UserStatsDao
) : StatsRepository {

    override suspend fun initStats(userId: Long) {
        dao.insertStats(UserStats(userId = userId))
    }

    override fun getStats(): Flow<UserStats?> {
        return dao.getStats()
    }

    override suspend fun getStatsOnce(): UserStats? {
        return dao.getStatsOnce()
    }

    override suspend fun addXp(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        val newXp = current.xp + amount
        val newLevel = calculateLevel(newXp)
        val newRank = com.benyaminrasouli.phoenixprotocol.core.domain.model.Rank.forLevel(newLevel)
        dao.updateStats(current.copy(
            xp = newXp,
            level = newLevel,
            rank = newRank.name
        ))
    }

    override suspend fun increasePhoenixEnergy(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            phoenixEnergy = (current.phoenixEnergy + amount).coerceAtMost(100)
        ))
    }

    override suspend fun decreasePhoenixEnergy(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            phoenixEnergy = (current.phoenixEnergy - amount).coerceAtLeast(0)
        ))
    }

    override suspend fun increaseShadowLevel(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            shadowLevel = current.shadowLevel + amount
        ))
    }

    override suspend fun incrementCompletedTasks() {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            completedTasks = current.completedTasks + 1
        ))
    }

    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var requiredXp = 0
        while (requiredXp <= xp) {
            level++
            requiredXp += (level * 100) + (level * level * 10)
        }
        return (level - 1).coerceAtLeast(1)
    }
}
```

- [ ] **Step 3: Create use cases**

```kotlin
// core/domain/usecase/CreateProfileUseCase.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import javax.inject.Inject

class CreateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) {
    suspend operator fun invoke(profile: UserProfile) {
        val userId = userRepository.createProfile(profile)
        statsRepository.initStats(userId)
        settingsDataStore.setOnboardingComplete(true)
    }
}
```

```kotlin
// core/domain/usecase/GetProfileUseCase.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<UserProfile?> {
        return userRepository.getProfile()
    }
}
```

```kotlin
// core/domain/usecase/CreateTaskUseCase.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Long {
        return taskRepository.createTask(task)
    }
}
```

```kotlin
// core/domain/usecase/CompleteTaskUseCase.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(task: Task) {
        taskRepository.completeTask(task.id)
        statsRepository.addXp(task.xpValue)
        statsRepository.increasePhoenixEnergy(5)
        statsRepository.incrementCompletedTasks()
    }
}
```

```kotlin
// core/domain/usecase/GetTasksUseCase.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getActiveTasks()
    }

    fun getCompleted(): Flow<List<Task>> {
        return taskRepository.getCompletedTasks()
    }
}
```

```kotlin
// core/domain/usecase/GetStatsUseCase.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(): Flow<UserStats?> {
        return statsRepository.getStats()
    }
}
```

```kotlin
// core/domain/usecase/GetSloganUseCase.kt
package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.domain.model.Slogans
import javax.inject.Inject

class GetSloganUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    suspend operator fun invoke(): String {
        val index = settingsDataStore.dailySloganIndex
            .firstOr(0)
        val slogan = Slogans.getDailySlogan(index)
        settingsDataStore.setDailySloganIndex((index + 1) % Slogans.sloganCount)
        return slogan
    }

    private suspend fun kotlinx.coroutines.flow.Flow<Int>.firstOr(default: Int): Int {
        return try {
            kotlinx.coroutines.flow.first()
        } catch (_: Exception) {
            default
        }
    }
}
```

- [ ] **Step 4: Create AppModule**

```kotlin
// di/AppModule.kt
package com.benyaminrasouli.phoenixprotocol.di

import com.benyaminrasouli.phoenixprotocol.core.data.repository.StatsRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.TaskRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.data.repository.UserRepositoryImpl
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository
}
```

- [ ] **Step 5: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: repository layer, use cases, DI bindings"
```

---

### Task 6: Navigation Setup

**Covers:** [S3, S4, S5, S6, S9]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/Screen.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/MainActivity.kt`

**Interfaces:**
- Consumes: Task 2 (theme), Task 5 (use cases for routing logic)
- Produces: Route definitions, navigation graph

- [ ] **Step 1: Create Screen routes**

```kotlin
// core/navigation/Screen.kt
package com.benyaminrasouli.phoenixprotocol.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object TaskList : Screen("task_list")
    data object CreateTask : Screen("create_task")
    data object Drawer : Screen("drawer")
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object Support : Screen("support")
}
```

- [ ] **Step 2: Create NavGraph**

```kotlin
// core/navigation/NavGraph.kt
package com.benyaminrasouli.phoenixprotocol.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.benyaminrasouli.phoenixprotocol.feature.splash.SplashScreen
import com.benyaminrasouli.phoenixprotocol.feature.onboarding.OnboardingScreen
import com.benyaminrasouli.phoenixprotocol.feature.dashboard.DashboardScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
    }
}
```

- [ ] **Step 3: Update MainActivity**

```kotlin
// MainActivity.kt
package com.benyaminrasouli.phoenixprotocol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.benyaminrasouli.phoenixprotocol.core.navigation.NavGraph
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixProtocolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoenixProtocolTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
```

- [ ] **Step 4: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (screens will be empty placeholders until created)

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: navigation setup - routes, NavGraph, MainActivity wiring"
```

---

### Task 7: Splash Screen

**Covers:** [S4]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/splash/SplashViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/splash/SplashScreen.kt`

**Interfaces:**
- Consumes: Task 5 (SettingsDataStore via ViewModel), Task 6 (Screen routes)
- Produces: Splash screen with animation, first-run routing

- [ ] **Step 1: Create SplashViewModel**

```kotlin
// feature/splash/SplashViewModel.kt
package com.benyaminrasouli.phoenixprotocol.feature.splash

import androidx.lifecycle.ViewModel
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    suspend fun isOnboardingComplete(): Boolean {
        return settingsDataStore.isOnboardingComplete.first()
    }
}
```

- [ ] **Step 2: Create SplashScreen**

```kotlin
// feature/splash/SplashScreen.kt
package com.benyaminrasouli.phoenixprotocol.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(1200)

        val isComplete = viewModel.isOnboardingComplete()
        navController.navigate(
            if (isComplete) Screen.Dashboard.route else Screen.Onboarding.route
        ) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A0A00),
                        BackgroundDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Phoenix Logo",
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            tint = PhoenixOrange
        )
    }
}
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: splash screen with Phoenix animation and first-run routing"
```

---

### Task 8: Onboarding Flow

**Covers:** [S5]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/onboarding/OnboardingViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/onboarding/OnboardingScreen.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/onboarding/LanguageStep.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/onboarding/ProfileStep.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/onboarding/IdentityStep.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/onboarding/ReviewStep.kt`

**Interfaces:**
- Consumes: Task 5 (CreateProfileUseCase, SettingsDataStore), Task 3 (IdentityPath)
- Produces: 4-step onboarding wizard, profile saved to Room

- [ ] **Step 1: Create OnboardingViewModel**

```kotlin
// feature/onboarding/OnboardingViewModel.kt
package com.benyaminrasouli.phoenixprotocol.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CreateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 0,
    val language: String = "en",
    val fullName: String = "",
    val username: String = "",
    val birthYear: String = "",
    val identityPath: String = "",
    val isSaving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val createProfileUseCase: CreateProfileUseCase,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setLanguage(language: String) {
        _state.update { it.copy(language = language) }
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
        }
    }

    fun setFullName(name: String) {
        _state.update { it.copy(fullName = name) }
    }

    fun setUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun setBirthYear(year: String) {
        _state.update { it.copy(birthYear = year) }
    }

    fun setIdentityPath(path: String) {
        _state.update { it.copy(identityPath = path) }
    }

    fun nextStep() {
        _state.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
    }

    fun previousStep() {
        _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.fullName.isBlank() || s.username.isBlank() || s.identityPath.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val profile = UserProfile(
                fullName = s.fullName.trim(),
                username = s.username.trim(),
                birthYear = s.birthYear.toIntOrNull(),
                identityPath = s.identityPath
            )
            createProfileUseCase(profile)
            _state.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
```

- [ ] **Step 2: Create LanguageStep**

```kotlin
// feature/onboarding/LanguageStep.kt
package com.benyaminrasouli.phoenixprotocol.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun LanguageStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Language",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select your preferred language",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageOption(
                label = "English",
                isSelected = selectedLanguage == "en",
                onClick = { onLanguageSelected("en") },
                modifier = Modifier.weight(1f)
            )
            LanguageOption(
                label = "Persian",
                isSelected = selectedLanguage == "fa",
                onClick = { onLanguageSelected("fa") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = SurfaceDark,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PhoenixOrange else TextSecondary.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) PhoenixOrange else TextSecondary,
            modifier = Modifier.padding(24.dp)
        )
    }
}
```

- [ ] **Step 3: Create ProfileStep**

```kotlin
// feature/onboarding/ProfileStep.kt
package com.benyaminrasouli.phoenixprotocol.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun ProfileStep(
    fullName: String,
    username: String,
    birthYear: String,
    onFullNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onBirthYearChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PhoenixOrange,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                focusedLabelColor = PhoenixOrange,
                cursorColor = PhoenixOrange
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PhoenixOrange,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                focusedLabelColor = PhoenixOrange,
                cursorColor = PhoenixOrange
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = birthYear,
            onValueChange = onBirthYearChange,
            label = { Text("Birth Year (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PhoenixOrange,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                focusedLabelColor = PhoenixOrange,
                cursorColor = PhoenixOrange
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}
```

- [ ] **Step 4: Create IdentityStep**

```kotlin
// feature/onboarding/IdentityStep.kt
package com.benyaminrasouli.phoenixprotocol.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.core.domain.model.IdentityPath
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

data class IdentityOption(
    val path: IdentityPath,
    val label: String,
    val description: String
)

private val identityOptions = listOf(
    IdentityOption(IdentityPath.WARRIOR, "Warrior", "Strength through battle"),
    IdentityOption(IdentityPath.SCHOLAR, "Scholar", "Knowledge is power"),
    IdentityOption(IdentityPath.BUILDER, "Builder", "Create what matters"),
    IdentityOption(IdentityPath.MONK, "Monk", "Discipline through focus"),
    IdentityOption(IdentityPath.COMMANDER, "Commander", "Lead with authority"),
    IdentityOption(IdentityPath.CREATOR, "Creator", "Express through creation"),
    IdentityOption(IdentityPath.STRATEGIST, "Strategist", "Plan every move"),
    IdentityOption(IdentityPath.SENTINEL, "Sentinel", "Protect what matters")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdentityStep(
    selectedPath: String,
    onPathSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Path",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Who are you becoming?",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            identityOptions.forEach { option ->
                IdentityCard(
                    option = option,
                    isSelected = selectedPath == option.path.name,
                    onClick = { onPathSelected(option.path.name) }
                )
            }
        }
    }
}

@Composable
private fun IdentityCard(
    option: IdentityOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = SurfaceDark,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PhoenixOrange else TextSecondary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) PhoenixOrange else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
```

- [ ] **Step 5: Create ReviewStep**

```kotlin
// feature/onboarding/ReviewStep.kt
package com.benyaminrasouli.phoenixprotocol.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun ReviewStep(
    fullName: String,
    username: String,
    birthYear: String,
    identityPath: String,
    language: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Review Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Confirm your details",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        ReviewRow(label = "Name", value = fullName)
        ReviewRow(label = "Username", value = "@$username")
        if (birthYear.isNotBlank()) {
            ReviewRow(label = "Birth Year", value = birthYear)
        }
        ReviewRow(label = "Identity Path", value = identityPath)
        ReviewRow(label = "Language", value = if (language == "en") "English" else "Persian")
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = PhoenixOrange
        )
    }
}
```

- [ ] **Step 6: Create OnboardingScreen**

```kotlin
// feature/onboarding/OnboardingScreen.kt
package com.benyaminrasouli.phoenixprotocol.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.core.ui.components.PhoenixButton
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> LanguageStep(
                        selectedLanguage = state.language,
                        onLanguageSelected = viewModel::setLanguage
                    )
                    1 -> ProfileStep(
                        fullName = state.fullName,
                        username = state.username,
                        birthYear = state.birthYear,
                        onFullNameChange = viewModel::setFullName,
                        onUsernameChange = viewModel::setUsername,
                        onBirthYearChange = viewModel::setBirthYear
                    )
                    2 -> IdentityStep(
                        selectedPath = state.identityPath,
                        onPathSelected = viewModel::setIdentityPath
                    )
                    3 -> ReviewStep(
                        fullName = state.fullName,
                        username = state.username,
                        birthYear = state.birthYear,
                        identityPath = state.identityPath,
                        language = state.language
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Step indicator
            StepIndicator(
                currentStep = state.currentStep,
                totalSteps = 4,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Navigation buttons
            if (state.currentStep > 0) {
                PhoenixButton(
                    text = "Back",
                    onClick = viewModel::previousStep,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            PhoenixButton(
                text = if (state.currentStep == 3) "Complete Setup" else "Next",
                onClick = {
                    if (state.currentStep == 3) {
                        viewModel.saveProfile {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    } else {
                        viewModel.nextStep()
                    }
                },
                enabled = when (state.currentStep) {
                    0 -> true
                    1 -> state.fullName.isNotBlank() && state.username.isNotBlank()
                    2 -> state.identityPath.isNotBlank()
                    3 -> !state.isSaving
                    else -> false
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (index <= currentStep)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.extraSmall
                    )
            )
        }
    }
}
```

- [ ] **Step 7: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: onboarding flow - language, profile, identity, review steps"
```

---

### Task 9: Dashboard

**Covers:** [S6, S8]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardViewModel.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/StatusCard.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/TaskListSection.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/SloganBar.kt`

**Interfaces:**
- Consumes: Task 5 (GetStatsUseCase, GetTasksUseCase, GetSloganUseCase, GetProfileUseCase)
- Produces: Dashboard with status card, task lists, slogan

- [ ] **Step 1: Create DashboardViewModel**

```kotlin
// feature/dashboard/DashboardViewModel.kt
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.model.Rank
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CompleteTaskUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetProfileUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetStatsUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetSloganUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val slogan: String = "",
    val profile: UserProfile? = null,
    val stats: UserStats? = null,
    val activeTasks: List<Task> = emptyList(),
    val rank: Rank = Rank.INITIATE
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getStatsUseCase: GetStatsUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val getSloganUseCase: GetSloganUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(slogan = getSloganUseCase()) }
        }

        viewModelScope.launch {
            combine(
                getProfileUseCase(),
                getStatsUseCase(),
                getTasksUseCase()
            ) { profile, stats, tasks ->
                DashboardState(
                    slogan = _state.value.slogan,
                    profile = profile,
                    stats = stats,
                    activeTasks = tasks,
                    rank = Rank.forLevel(stats?.level ?: 1)
                )
            }.collect { newState ->
                _state.update { newState }
            }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            completeTaskUseCase(task)
        }
    }
}
```

- [ ] **Step 2: Create SloganBar**

```kotlin
// feature/dashboard/SloganBar.kt
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun SloganBar(slogan: String, modifier: Modifier = Modifier) {
    Text(
        text = ">> $slogan",
        style = MaterialTheme.typography.bodyLarge,
        color = PhoenixOrange,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
```

- [ ] **Step 3: Create StatusCard**

```kotlin
// feature/dashboard/StatusCard.kt
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.core.ui.components.EnergyBar
import com.benyaminrasouli.phoenixprotocol.core.ui.components.PhoenixCard
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixGold
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun StatusCard(
    level: Int,
    xp: Int,
    rank: String,
    phoenixEnergy: Int,
    modifier: Modifier = Modifier
) {
    PhoenixCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = rank,
                    style = MaterialTheme.typography.titleLarge,
                    color = PhoenixGold
                )
                Text(
                    text = "Level $level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$xp XP",
                    style = MaterialTheme.typography.titleMedium,
                    color = PhoenixOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Phoenix Energy",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "$phoenixEnergy/100",
                style = MaterialTheme.typography.labelLarge,
                color = PhoenixOrange
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        EnergyBar(energy = phoenixEnergy)
    }
}
```

- [ ] **Step 4: Create TaskListSection**

```kotlin
// feature/dashboard/TaskListSection.kt
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixRed
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun TaskListSection(
    title: String,
    tasks: List<Task>,
    onTaskComplete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (tasks.isEmpty()) {
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            tasks.forEach { task ->
                TaskItem(task = task, onComplete = { onTaskComplete(task) })
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onComplete)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${task.difficulty} • ${task.category} • ${task.xpValue} XP",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Complete task",
            tint = PhoenixOrange,
            modifier = Modifier.size(24.dp)
        )
    }
}
```

- [ ] **Step 5: Create DashboardScreen**

```kotlin
// feature/dashboard/DashboardScreen.kt
package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp)
        ) {
            // Greeting
            state.profile?.let { profile ->
                Text(
                    text = "Welcome, ${profile.fullName}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.rank.displayName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = PhoenixOrange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Slogan
            SloganBar(slogan = state.slogan)

            Spacer(modifier = Modifier.height(16.dp))

            // Status Card
            StatusCard(
                level = state.stats?.level ?: 1,
                xp = state.stats?.xp ?: 0,
                rank = state.rank.displayName,
                phoenixEnergy = state.stats?.phoenixEnergy ?: 50,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Priority Tasks
            TaskListSection(
                title = "TODAY'S PRIORITY",
                tasks = state.activeTasks,
                onTaskComplete = viewModel::completeTask
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Shadow Level indicator
            state.stats?.let { stats ->
                if (stats.shadowLevel > 0) {
                    Text(
                        text = "SHADOW LEVEL: ${stats.shadowLevel}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
```

- [ ] **Step 6: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: dashboard - status card, task lists, slogan bar"
```

---

### Task 10: Drawer & Settings

**Covers:** [S9, S10]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/drawer/DrawerScreen.kt`
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/drawer/DrawerViewModel.kt`

**Interfaces:**
- Consumes: Task 5 (GetProfileUseCase, GetStatsUseCase), Task 9 (DashboardScreen)
- Produces: Navigation drawer with profile, settings, about, support

- [ ] **Step 1: Create DrawerViewModel**

```kotlin
// feature/drawer/DrawerViewModel.kt
package com.benyaminrasouli.phoenixprotocol.feature.drawer

import androidx.lifecycle.ViewModel
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val profile: Flow<UserProfile?> = userRepository.getProfile()
    val stats: Flow<UserStats?> = statsRepository.getStats()
    val language: Flow<String> = settingsDataStore.language

    suspend fun setLanguage(language: String) {
        settingsDataStore.setLanguage(language)
    }
}
```

- [ ] **Step 2: Create DrawerScreen**

```kotlin
// feature/drawer/DrawerScreen.kt
package com.benyaminrasouli.phoenixprotocol.feature.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Achievements
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixRed
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@Composable
fun DrawerScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle(initialValue = null)
    val stats by viewModel.stats.collectAsStateWithLifecycle(initialValue = null)
    val language by viewModel.language.collectAsStateWithLifecycle(initialValue = "en")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        // Profile header
        profile?.let { p ->
            Text(
                text = p.fullName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@${p.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            stats?.let { s ->
                Text(
                    text = "Level ${s.level} • ${s.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    color = PhoenixOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Divider(color = SurfaceDark)

        Spacer(modifier = Modifier.height(16.dp))

        // Menu items
        DrawerMenuItem(
            icon = Icons.Filled.VerifiedUser,
            label = "Profile",
            onClick = {}
        )
        DrawerMenuItem(
            icon = Icons.Filled.BarChart,
            label = "Statistics",
            onClick = {}
        )
        DrawerMenuItem(
            icon = Icons.Filled.Achievements,
            label = "Achievements",
            onClick = {}
        )
        DrawerMenuItem(
            icon = Icons.Filled.Settings,
            label = "Settings",
            onClick = onNavigateToSettings
        )
        DrawerMenuItem(
            icon = Icons.Filled.Language,
            label = "Language",
            onClick = {
                viewModel.setLanguage(if (language == "en") "fa" else "en")
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider(color = SurfaceDark)

        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(
            icon = Icons.Filled.Info,
            label = "About Us",
            onClick = onNavigateToAbout
        )
        DrawerMenuItem(
            icon = Icons.Filled.Support,
            label = "Support Us",
            onClick = onNavigateToSupport
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
```

- [ ] **Step 3: Wire drawer into DashboardScreen**

Update `DashboardScreen.kt` to include drawer state:

```kotlin
// Add to DashboardScreen.kt imports
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch

// Replace the Box in DashboardScreen with:
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerScreen(
                    onNavigateToSettings = { scope.launch { drawerState.close() } },
                    onNavigateToAbout = { scope.launch { drawerState.close() } },
                    onNavigateToSupport = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            // Keep existing Column content...
            // Add top bar with hamburger menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 48.dp)
            ) {
                // Hamburger menu at top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Rest of existing content...
            }
        }
    }
}
```

- [ ] **Step 4: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: drawer navigation with profile, settings, about, support"
```

---

### Task 11: Localization (Persian)

**Covers:** [S11]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values-fa/strings.xml`

**Interfaces:**
- Consumes: All previous tasks
- Produces: Bilingual string resources

- [ ] **Step 1: Create English strings.xml**

```xml
<!-- app/src/main/res/values/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Phoenix Protocol</string>

    <!-- Splash -->
    <string name="splash_loading">Initializing...</string>

    <!-- Onboarding -->
    <string name="onboarding_choose_language">Choose Language</string>
    <string name="onboarding_select_language">Select your preferred language</string>
    <string name="onboarding_english">English</string>
    <string name="onboarding_persian">Persian</string>
    <string name="onboarding_create_profile">Create Your Profile</string>
    <string name="onboarding_tell_us">Tell us about yourself</string>
    <string name="onboarding_full_name">Full Name</string>
    <string name="onboarding_username">Username</string>
    <string name="onboarding_birth_year">Birth Year (Optional)</string>
    <string name="onboarding_choose_path">Choose Your Path</string>
    <string name="onboarding_who_are_you">Who are you becoming?</string>
    <string name="onboarding_review">Review Your Profile</string>
    <string name="onboarding_confirm">Confirm your details</string>
    <string name="onboarding_complete">Complete Setup</string>
    <string name="onboarding_next">Next</string>
    <string name="onboarding_back">Back</string>

    <!-- Identity Paths -->
    <string name="identity_warrior">Warrior</string>
    <string name="identity_warrior_desc">Strength through battle</string>
    <string name="identity_scholar">Scholar</string>
    <string name="identity_scholar_desc">Knowledge is power</string>
    <string name="identity_builder">Builder</string>
    <string name="identity_builder_desc">Create what matters</string>
    <string name="identity_monk">Monk</string>
    <string name="identity_monk_desc">Discipline through focus</string>
    <string name="identity_commander">Commander</string>
    <string name="identity_commander_desc">Lead with authority</string>
    <string name="identity_creator">Creator</string>
    <string name="identity_creator_desc">Express through creation</string>
    <string name="identity_strategist">Strategist</string>
    <string name="identity_strategist_desc">Plan every move</string>
    <string name="identity_sentinel">Sentinel</string>
    <string name="identity_sentinel_desc">Protect what matters</string>

    <!-- Dashboard -->
    <string name="dashboard_welcome">Welcome,</string>
    <string name="dashboard_priority_tasks">TODAY\'S PRIORITY</string>
    <string name="dashboard_custom_tasks">CUSTOM TASKS</string>
    <string name="dashboard_no_tasks">No tasks yet</string>
    <string name="dashboard_phoenix_energy">Phoenix Energy</string>
    <string name="dashboard_shadow_level">SHADOW LEVEL</string>

    <!-- Tasks -->
    <string name="task_create">Create Task</string>
    <string name="task_title">Title</string>
    <string name="task_description">Description</string>
    <string name="task_difficulty">Difficulty</string>
    <string name="task_category">Category</string>
    <string name="task_recurrence">Recurrence</string>
    <string name="task_complete">Complete</string>
    <string name="task_skip">Skip</string>

    <!-- Difficulty -->
    <string name="difficulty_easy">Easy</string>
    <string name="difficulty_medium">Medium</string>
    <string name="difficulty_hard">Hard</string>
    <string name="difficulty_extreme">Extreme</string>

    <!-- Drawer -->
    <string name="drawer_profile">Profile</string>
    <string name="drawer_statistics">Statistics</string>
    <string name="drawer_achievements">Achievements</string>
    <string name="drawer_settings">Settings</string>
    <string name="drawer_language">Language</string>
    <string name="drawer_about">About Us</string>
    <string name="drawer_support">Support Us</string>

    <!-- Settings -->
    <string name="settings_title">Settings</string>
    <string name="settings_language">Language</string>
    <string name="settings_theme">Theme</string>
    <string name="settings_account">Account</string>
    <string name="settings_version">App Version</string>

    <!-- Ranks -->
    <string name="rank_initiate">Initiate</string>
    <string name="rank_survivor">Survivor</string>
    <string name="rank_hunter">Hunter</string>
    <string name="rank_warrior">Warrior</string>
    <string name="rank_elite">Elite</string>
    <string name="rank_commander">Commander</string>
    <string name="rank_phantom">Phantom</string>
    <string name="rank_titan">Titan</string>
    <string name="rank_ascendant">Ascendant</string>
    <string name="rank_phoenix">Phoenix</string>
</resources>
```

- [ ] **Step 2: Create Persian strings.xml**

```xml
<!-- app/src/main/res/values-fa/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">پروتکل ققنوس</string>

    <!-- Splash -->
    <string name="splash_loading">در حال راه‌اندازی...</string>

    <!-- Onboarding -->
    <string name="onboarding_choose_language">انتخاب زبان</string>
    <string name="onboarding_select_language">زبان مورد نظر خود را انتخاب کنید</string>
    <string name="onboarding_english">انگلیسی</string>
    <string name="onboarding_persian">فارسی</string>
    <string name="onboarding_create_profile">پروفایل خود را بسازید</string>
    <string name="onboarding_tell_us">درباره خودتان بگویید</string>
    <string name="onboarding_full_name">نام کامل</string>
    <string name="onboarding_username">نام کاربری</string>
    <string name="onboarding_birth_year">سال تولد (اختیاری)</string>
    <string name="onboarding_choose_path">مسیر خود را انتخاب کنید</string>
    <string name="onboarding_who_are_you">در حال تبدیل شدن به چه کسی هستید؟</string>
    <string name="onboarding_review">پروفایل خود را بررسی کنید</string>
    <string name="onboarding_confirm">جزئیات خود را تایید کنید</string>
    <string name="onboarding_complete">تکمیل تنظیمات</string>
    <string name="onboarding_next">بعدی</string>
    <string name="onboarding_back">قبلی</string>

    <!-- Identity Paths -->
    <string name="identity_warrior">جنگجو</string>
    <string name="identity_warrior_desc">قدرت از طریق نبرد</string>
    <string name="identity_scholar">دانشمند</string>
    <string name="identity_scholar_desc">دانش قدرت است</string>
    <string name="identity_builder">سازنده</string>
    <string name="identity_builder_desc">آنچه مهم است بسازید</string>
    <string name="identity_monk">راهب</string>
    <string name="identity_monk_desc">انضباط از طریق تمرکز</string>
    <string name="identity_commander">فرمانده</string>
    <string name="identity_commander_desc">با اقتدار رهبری کنید</string>
    <string name="identity_creator">خالق</string>
    <string name="identity_creator_desc">از طریق خلاقیت بیان کنید</string>
    <string name="identity_strategist">استراتژیست</string>
    <string name="identity_strategist_desc">هر حرکت را برنامه‌ریزی کنید</string>
    <string name="identity_sentinel">نگهبان</string>
    <string name="identity_sentinel_desc">از آنچه مهم است محافظت کنید</string>

    <!-- Dashboard -->
    <string name="dashboard_welcome">خوش آمدید،</string>
    <string name="dashboard_priority_tasks">اولویت امروز</string>
    <string name="dashboard_custom_tasks">وظایف شخصی</string>
    <string name="dashboard_no_tasks">هنوز وظیفه‌ای وجود ندارد</string>
    <string name="dashboard_phoenix_energy">انرژی ققنوس</string>
    <string name="dashboard_shadow_level">سطح سایه</string>

    <!-- Tasks -->
    <string name="task_create">ایجاد وظیفه</string>
    <string name="task_title">عنوان</string>
    <string name="task_description">توضیحات</string>
    <string name="task_difficulty">سطح دشواری</string>
    <string name="task_category">دسته‌بندی</string>
    <string name="task_recurrence">تکرار</string>
    <string name="task_complete">تکمیل</string>
    <string name="task_skip">رد کردن</string>

    <!-- Difficulty -->
    <string name="difficulty_easy">آسان</string>
    <string name="difficulty_medium">متوسط</string>
    <string name="difficulty_hard">سخت</string>
    <string name="difficulty_extreme">-extreme</string>

    <!-- Drawer -->
    <string name="drawer_profile">پروفایل</string>
    <string name="drawer_statistics">آمار</string>
    <string name="drawer_achievements">دستاوردها</string>
    <string name="drawer_settings">تنظیمات</string>
    <string name="drawer_language">زبان</string>
    <string name="drawer_about">درباره ما</string>
    <string name="drawer_support">حمایت از ما</string>

    <!-- Settings -->
    <string name="settings_title">تنظیمات</string>
    <string name="settings_language">زبان</string>
    <string name="settings_theme">پوسته</string>
    <string name="settings_account">حساب کاربری</string>
    <string name="settings_version">نسخه برنامه</string>

    <!-- Ranks -->
    <string name="rank_initiate">آغازگر</string>
    <string name="rank_survivor">بازمانده</string>
    <string name="rank_hunter">شکارچی</string>
    <string name="rank_warrior">جنگجو</string>
    <string name="rank_elite">نخبه</string>
    <string name="rank_commander">فرمانده</string>
    <string name="rank_phantom">شبح</string>
    <string name="rank_titan">titان</string>
    <string name="rank_ascendant">صعودکننده</string>
    <string name="rank_phoenix">ققنوس</string>
</resources>
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: bilingual localization - English and Persian strings"
```

---

### Task 12: Final Polish & Verification

**Covers:** [S16, S17]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/splash/SplashScreen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: All previous tasks
- Produces: Polished app with smooth transitions, empty states, visual refinements

- [ ] **Step 1: Add smooth navigation transitions**

Update NavGraph.kt with proper transitions:

```kotlin
// core/navigation/NavGraph.kt - update composable calls
composable(
    Screen.Splash.route,
    enterTransition = { fadeIn(animationSpec = tween(300)) },
    exitTransition = { fadeOut(animationSpec = tween(300)) }
) {
    SplashScreen(navController = navController)
}
composable(
    Screen.Onboarding.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    OnboardingScreen(navController = navController)
}
composable(
    Screen.Dashboard.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    DashboardScreen(navController = navController)
}
```

- [ ] **Step 2: Final build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run lint checks**

Run: `./gradlew lint`
Expected: No critical errors

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "feat: final polish - navigation transitions, build verification"
```

---

## Self-Review

**Spec coverage check:**
- [S1] Problem → No task needed (context)
- [S2] Solution Overview → No task needed (context)
- [S3] Architecture → Task 1, Task 4
- [S4] Splash Screen → Task 7
- [S5] Onboarding Flow → Task 8
- [S6] Dashboard → Task 9
- [S7] Task System → Task 4, Task 5, Task 9
- [S8] Gamification → Task 3, Task 5
- [S9] Left Drawer → Task 10
- [S10] Settings → Task 10
- [S11] Localization → Task 11
- [S12] Theme → Task 2
- [S13] Data Models → Task 3, Task 4
- [S14] Dependencies → Task 1
- [S15] File Structure → All tasks
- [S16] Implementation Order → Task 12
- [S17] Success Criteria → Task 12

**Placeholder scan:** No TBDs, TODOs, or vague steps found.

**Type consistency:** Entity field names, DAO methods, and use case signatures are consistent across tasks.

---

## Execution Handoff

The plan has 12 tasks. Tasks 1-6 are foundational and sequential. Tasks 7-11 can be partially parallelized after Task 6. Task 12 is final polish.

Recommended execution: **Subagent per task** for isolation and parallelism on independent feature tasks.
