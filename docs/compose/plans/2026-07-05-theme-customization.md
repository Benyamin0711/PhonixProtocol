# Theme Customization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add accent color selection (8 presets) and background darkness control (3 presets + brightness slider) to the Settings screen with live theme switching.

**Architecture:** Dynamic Material 3 color scheme generated from user-selected accent color and background level. Theme state flows from SettingsDataStore through SettingsViewModel to MainActivity, which wraps the entire app in a dynamic PhoenixProtocolTheme composable.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, DataStore Preferences, Hilt

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `core/data/datastore/SettingsDataStore.kt` | Modify | Add theme keys + setters |
| `ui/theme/Color.kt` | Modify | Add accent map + background function |
| `ui/theme/Theme.kt` | Modify | Dynamic color scheme generation |
| `feature/settings/SettingsScreen.kt` | Modify | Add Theme section UI |
| `feature/settings/SettingsViewModel.kt` | Modify | Add theme state |
| `MainActivity.kt` | Modify | Read theme, pass to composable |
| `feature/dashboard/DashboardScreen.kt` | Modify | Replace hardcoded colors |

---

### Task 1: DataStore Theme Keys

**Covers:** [S3, S4]

**Files:**
- Modify: `core/data/datastore/SettingsDataStore.kt`

**Interfaces:**
- Produces: `accentColor: Flow<String>`, `backgroundLevel: Flow<Int>`, `brightness: Flow<Int>`, `setAccentColor(String)`, `setBackgroundLevel(Int)`, `setBrightness(Int)`

- [ ] **Step 1: Add theme keys to SettingsDataStore**

Add to `Keys` object:
```kotlin
val ACCENT_COLOR = stringPreferencesKey("accent_color")
val BACKGROUND_LEVEL = intPreferencesKey("background_level")
val BRIGHTNESS = intPreferencesKey("brightness")
```

- [ ] **Step 2: Add theme Flow properties**

Add after `widgetCategoryFilter`:
```kotlin
val accentColor: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[Keys.ACCENT_COLOR] ?: "orange"
}

val backgroundLevel: Flow<Int> = context.dataStore.data.map { prefs ->
    prefs[Keys.BACKGROUND_LEVEL] ?: 0
}

val brightness: Flow<Int> = context.dataStore.data.map { prefs ->
    prefs[Keys.BRIGHTNESS] ?: 100
}
```

- [ ] **Step 3: Add theme setter functions**

Add after `setWidgetCategoryFilter`:
```kotlin
suspend fun setAccentColor(color: String) {
    context.dataStore.edit { prefs ->
        prefs[Keys.ACCENT_COLOR] = color
    }
}

suspend fun setBackgroundLevel(level: Int) {
    context.dataStore.edit { prefs ->
        prefs[Keys.BACKGROUND_LEVEL] = level
    }
}

suspend fun setBrightness(brightness: Int) {
    context.dataStore.edit { prefs ->
        prefs[Keys.BRIGHTNESS] = brightness
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add core/data/datastore/SettingsDataStore.kt
git commit -m "feat(theme): add accent color, background level, brightness keys to DataStore"
```

---

### Task 2: Color Map & Background Function

**Covers:** [S3, S4]

**Files:**
- Modify: `ui/theme/Color.kt`

**Interfaces:**
- Produces: `accentColorMap: Map<String, Color>`, `getAccentColor(String): Color`, `getBackgroundColor(Int, Int): Color`

- [ ] **Step 1: Add accent color map**

Add to bottom of Color.kt:
```kotlin
val accentColorMap = mapOf(
    "orange" to Color(0xFFFF6B35),
    "blue" to Color(0xFF4A90D9),
    "green" to Color(0xFF4CAF50),
    "purple" to Color(0xFF9C27B0),
    "pink" to Color(0xFFE91E63),
    "red" to Color(0xFFE63946),
    "gold" to Color(0xFFFFD700),
    "teal" to Color(0xFF009688)
)

fun getAccentColor(name: String): Color = accentColorMap[name] ?: PhoenixOrange
```

- [ ] **Step 2: Add background color function**

```kotlin
fun getBackgroundColor(level: Int, brightness: Int): Color {
    val base = when (level) {
        1 -> Color(0xFF080808) // Darker
        2 -> Color(0xFF000000) // AMOLED
        else -> Color(0xFF0D0D0D) // Dark (default)
    }
    val factor = brightness.coerceIn(0, 100) / 100f
    return base.copy(red = base.red * factor, green = base.green * factor, blue = base.blue * factor)
}

fun getSurfaceColor(level: Int, brightness: Int): Color {
    val bg = getBackgroundColor(level, brightness)
    return bg.copy(
        red = (bg.red + 0.10f).coerceAtMost(1f),
        green = (bg.green + 0.10f).coerceAtMost(1f),
        blue = (bg.blue + 0.18f).coerceAtMost(1f)
    )
}
```

- [ ] **Step 3: Commit**

```bash
git add ui/theme/Color.kt
git commit -m "feat(theme): add accent color map and background color functions"
```

---

### Task 3: Dynamic Theme Composable

**Covers:** [S3, S4, S6]

**Files:**
- Modify: `ui/theme/Theme.kt`

**Interfaces:**
- Consumes: `getAccentColor(String)`, `getBackgroundColor(Int, Int)`, `getSurfaceColor(Int, Int)`
- Produces: Updated `PhoenixProtocolTheme` composable with accent/bg params

- [ ] **Step 1: Update Theme.kt imports**

Add imports:
```kotlin
import androidx.compose.runtime.remember
```

- [ ] **Step 2: Replace PhoenixProtocolTheme with dynamic version**

Replace the entire `PhoenixProtocolTheme` composable:
```kotlin
@Composable
fun PhoenixProtocolTheme(
    accentColor: String = "orange",
    backgroundLevel: Int = 0,
    brightness: Int = 100,
    content: @Composable () -> Unit
) {
    val accent = remember(accentColor) { getAccentColor(accentColor) }
    val bg = remember(backgroundLevel, brightness) { getBackgroundColor(backgroundLevel, brightness) }
    val surface = remember(backgroundLevel, brightness) { getSurfaceColor(backgroundLevel, brightness) }

    val colorScheme = darkColorScheme(
        primary = accent,
        onPrimary = Color.White,
        primaryContainer = accent.copy(alpha = 0.15f),
        onPrimaryContainer = accent.copy(alpha = 0.8f),
        secondary = accent.copy(alpha = 0.7f),
        onSecondary = Color.White,
        secondaryContainer = accent.copy(alpha = 0.1f),
        onSecondaryContainer = accent.copy(alpha = 0.6f),
        tertiary = PhoenixGold,
        onTertiary = Color(0xFF1A1A1A),
        tertiaryContainer = PhoenixGold.copy(alpha = 0.15f),
        onTertiaryContainer = PhoenixGold.copy(alpha = 0.8f),
        background = bg,
        onBackground = Color.White,
        surface = surface,
        onSurface = Color.White,
        surfaceVariant = surface.copy(
            red = (surface.red + 0.05f).coerceAtMost(1f),
            green = (surface.green + 0.05f).coerceAtMost(1f),
            blue = (surface.blue + 0.08f).coerceAtMost(1f)
        ),
        onSurfaceVariant = Color(0xFFA0A0B0),
        error = Color(0xFFFF5252),
        onError = Color.White,
        errorContainer = Color(0xFF3B0000),
        onErrorContainer = Color(0xFFFFDAD6)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = bg.toArgb()
            window.navigationBarColor = bg.toArgb()
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

- [ ] **Step 3: Commit**

```bash
git add ui/theme/Theme.kt
git commit -m "feat(theme): make PhoenixProtocolTheme dynamic with accent and background params"
```

---

### Task 4: SettingsViewModel Theme State

**Covers:** [S5, S6]

**Files:**
- Modify: `feature/settings/SettingsViewModel.kt`

**Interfaces:**
- Consumes: `SettingsDataStore.accentColor`, `backgroundLevel`, `brightness`, `setAccentColor()`, `setBackgroundLevel()`, `setBrightness()`
- Produces: `accentColor: StateFlow<String>`, `backgroundLevel: StateFlow<Int>`, `brightness: StateFlow<Int>`, `setAccentColor(String)`, `setBackgroundLevel(Int)`, `setBrightness(Int)`

- [ ] **Step 1: Add theme state to SettingsViewModel**

Add to SettingsViewModel class:
```kotlin
val accentColor: StateFlow<String> = settingsDataStore.accentColor
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "orange")

val backgroundLevel: StateFlow<Int> = settingsDataStore.backgroundLevel
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

val brightness: StateFlow<Int> = settingsDataStore.brightness
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

fun setAccentColor(color: String) {
    viewModelScope.launch { settingsDataStore.setAccentColor(color) }
}

fun setBackgroundLevel(level: Int) {
    viewModelScope.launch { settingsDataStore.setBackgroundLevel(level) }
}

fun setBrightness(brightness: Int) {
    viewModelScope.launch { settingsDataStore.setBrightness(brightness) }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/settings/SettingsViewModel.kt
git commit -m "feat(theme): add theme state and setters to SettingsViewModel"
```

---

### Task 5: Settings Screen Theme Section

**Covers:** [S5]

**Files:**
- Modify: `feature/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: `SettingsViewModel.accentColor`, `backgroundLevel`, `brightness`, `setAccentColor()`, `setBackgroundLevel()`, `setBrightness()`

- [ ] **Step 1: Add theme imports**

Add to SettingsScreen.kt imports:
```kotlin
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.benyaminrasouli.phoenixprotocol.ui.theme.accentColorMap
```

- [ ] **Step 2: Add Theme section to SettingsScreen**

After the Language section (after the last `HorizontalDivider()`), add:
```kotlin
// Theme Section
Text(
    text = stringResource(R.string.settings_theme),
    style = MaterialTheme.typography.titleMedium,
    color = PhoenixOrange,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
)

// Accent Color
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.settings_accent_color),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            accentColorMap.forEach { (name, color) ->
                val isSelected = currentAccentColor == name
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                            else Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                        .clickable { onAccentColorSelected(name) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

Spacer(modifier = Modifier.height(8.dp))

// Background Level
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.settings_background),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        backgroundOptions.forEach { (level, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackgroundLevelSelected(level) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentBackgroundLevel == level,
                    onClick = { onBackgroundLevelSelected(level) },
                    colors = RadioButtonDefaults.colors(selectedColor = PhoenixOrange)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, color = Color.White)
            }
        }
    }
}

Spacer(modifier = Modifier.height(8.dp))

// Brightness Slider
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.settings_brightness),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${currentBrightness}%",
                color = PhoenixOrange,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = currentBrightness.toFloat(),
            onValueChange = { onBrightnessChanged(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = PhoenixOrange,
                activeTrackColor = PhoenixOrange
            )
        )
    }
}

Spacer(modifier = Modifier.height(8.dp))
HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
Spacer(modifier = Modifier.height(8.dp))
```

- [ ] **Step 3: Add helper data to SettingsScreen**

Add at top of file (after imports, before composable):
```kotlin
private val backgroundOptions = listOf(
    0 to "Dark",
    1 to "Darker",
    2 to "AMOLED"
)
```

- [ ] **Step 4: Add state variables to SettingsScreen composable**

Inside the SettingsScreen composable, add:
```kotlin
val currentAccentColor by viewModel.accentColor.collectAsState()
val currentBackgroundLevel by viewModel.backgroundLevel.collectAsState()
val currentBrightness by viewModel.brightness.collectAsState()
```

- [ ] **Step 5: Add string resources**

Add to `app/src/main/res/values/strings.xml`:
```xml
<string name="settings_theme">Theme</string>
<string name="settings_accent_color">Accent Color</string>
<string name="settings_background">Background</string>
<string name="settings_brightness">Brightness</string>
```

Add to `app/src/main/res/values-fa/strings.xml`:
```xml
<string name="settings_theme">تم</string>
<string name="settings_accent_color">رنگ لهجه</string>
<string name="settings_background">پس‌زمینه</string>
<string name="settings_brightness">روشنایی</string>
```

- [ ] **Step 6: Commit**

```bash
git add feature/settings/SettingsScreen.kt app/src/main/res/values/strings.xml app/src/main/res/values-fa/strings.xml
git commit -m "feat(theme): add theme section to Settings screen"
```

---

### Task 6: MainActivity Theme Integration

**Covers:** [S6]

**Files:**
- Modify: `MainActivity.kt`

**Interfaces:**
- Consumes: `SettingsDataStore.accentColor`, `backgroundLevel`, `brightness`
- Produces: Theme state passed to PhoenixProtocolTheme

- [ ] **Step 1: Add theme imports**

Add to MainActivity.kt:
```kotlin
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
```

- [ ] **Step 2: Read theme state and pass to PhoenixProtocolTheme**

In the `onCreate` method, after `setContent`:
```kotlin
val accentColor by settingsDataStore.accentColor.collectAsState(initial = "orange")
val backgroundLevel by settingsDataStore.backgroundLevel.collectAsState(initial = 0)
val brightness by settingsDataStore.brightness.collectAsState(initial = 100)

PhoenixProtocolTheme(
    accentColor = accentColor,
    backgroundLevel = backgroundLevel,
    brightness = brightness
) {
    PhoenixProtocolNavHost(...)
}
```

- [ ] **Step 3: Commit**

```bash
git add MainActivity.kt
git commit -m "feat(theme): integrate dynamic theme into MainActivity"
```

---

### Task 7: DashboardScreen Hardcoded Color Cleanup

**Caves:** [S8]

**Files:**
- Modify: `feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: `MaterialTheme.colorScheme.background`, `MaterialTheme.colorScheme.surface`

- [ ] **Step 1: Replace hardcoded colors in DashboardScreen**

Find and replace in DashboardScreen.kt:
- `Color(0xFF050505)` -> `MaterialTheme.colorScheme.background`
- `Color(0xFF0D0D0D)` -> `MaterialTheme.colorScheme.background`
- `Color(0xFF1A1A2E)` -> `MaterialTheme.colorScheme.surface`
- `Color(0xFF252540)` -> `MaterialTheme.colorScheme.surfaceVariant`
- `PhoenixOrange` -> `MaterialTheme.colorScheme.primary`

- [ ] **Step 2: Add MaterialTheme import if needed**

```kotlin
import androidx.compose.material3.MaterialTheme
```

- [ ] **Step 3: Build and verify**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add feature/dashboard/DashboardScreen.kt
git commit -m "feat(theme): replace hardcoded colors with theme colors in DashboardScreen"
```

---

### Task 8: Build Verification

**Covers:** [S6, S8]

**Files:**
- None (verification only)

- [ ] **Step 1: Full build**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify no hardcoded color warnings**

Check build output for any color-related warnings.

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat(theme): Theme customization complete - accent colors + background levels + brightness"
```
