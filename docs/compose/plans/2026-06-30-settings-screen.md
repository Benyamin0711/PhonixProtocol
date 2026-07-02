# Settings Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a full Settings screen with Language, Notifications (placeholder), Theme Preview, Account, Data & Privacy, and About sections.

**Architecture:** Feature-based Clean Architecture with Hilt DI. New SettingsViewModel reads from SettingsDataStore and UserRepository. SettingsScreen composable follows existing dark theme patterns.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, DataStore Preferences

## Global Constraints
- Min SDK 24, Target SDK 36, Compile SDK 36
- Package: `com.benyaminrasouli.phoenixprotocol`
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- Bilingual: English + Persian (all new strings must have both locales)
- Every task ends with `./gradlew assembleDebug` passing

---

### Task 1: SettingsViewModel

**Covers:** [S3], [S6], [S7], [S8]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/settings/SettingsViewModel.kt`

**Interfaces:**
- Consumes: SettingsDataStore, UserRepository, StatsRepository
- Produces: SettingsViewModel with language, profile, stats, reset functionality

- [ ] **Step 1: Create SettingsViewModel**

```kotlin
// feature/settings/SettingsViewModel.kt
package com.benyaminrasouli.phoenixprotocol.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    val language: StateFlow<String> = settingsDataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val profile: Flow<UserProfile?> = userRepository.getProfile()

    val stats: Flow<UserStats?> = statsRepository.getStats()

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsDataStore.setOnboardingComplete(false)
            userRepository.clearProfile()
            statsRepository.clearStats()
            onComplete()
        }
    }
}
```

- [ ] **Step 2: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: SettingsViewModel with language, profile, and reset"
```

---

### Task 2: String Resources

**Covers:** [S11]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

**Interfaces:**
- Consumes: None
- Produces: Bilingual string resources for Settings screen

- [ ] **Step 1: Add English strings**

Add to `app/src/main/res/values/strings.xml`:

```xml
<!-- Settings -->
<string name="settings_title">Settings</string>
<string name="settings_language">Language</string>
<string name="settings_notifications">Notifications</string>
<string name="settings_theme">Theme</string>
<string name="settings_account">Account</string>
<string name="settings_data_privacy">Data &amp; Privacy</string>
<string name="settings_about">About</string>
<string name="settings_version">Version %s</string>
<string name="settings_edit_profile">Edit Profile</string>
<string name="settings_export_data">Export Data</string>
<string name="settings_reset_data">Reset All Data</string>
<string name="settings_reset_confirm">Are you sure? This will delete all your data and cannot be undone.</string>
<string name="settings_coming_soon">Coming soon</string>
<string name="settings_dark_theme">Dark Theme</string>
<string name="settings_theme_desc">Always on</string>
<string name="settings_about_title">Phoenix Protocol</string>
<string name="settings_about_desc">A gamified task management app that turns discipline into a RPG experience.</string>
```

- [ ] **Step 2: Add Persian strings**

Add to `app/src/main/res/values-fa/strings.xml`:

```xml
<!-- Settings -->
<string name="settings_title">تنظیمات</string>
<string name="settings_language">زبان</string>
<string name="settings_notifications">اعلان‌ها</string>
<string name="settings_theme">پوسته</string>
<string name="settings_account">حساب کاربری</string>
<string name="settings_data_privacy">داده‌ها و حریم خصوصی</string>
<string name="settings_about">درباره ما</string>
<string name="settings_version">نسخه %s</string>
<string name="settings_edit_profile">ویرایش پروفایل</string>
<string name="settings_export_data">خروجی داده‌ها</string>
<string name="settings_reset_data">بازنشانی تمام داده‌ها</string>
<string name="settings_reset_confirm">آبمطمئنید؟ تمام داده‌های شما حذف خواهند شد و قابل بازگشت نیستند.</string>
<string name="settings_coming_soon">به زودی</string>
<string name="settings_dark_theme">پوسته تاریک</string>
<string name="settings_theme_desc">همیشه فعال</string>
<string name="settings_about_title">پروتکل ققنوس</string>
<string name="settings_about_desc">برنامه مدیریت وظایف گیمیفاید که انضباط را به تجربه RPG تبدیل می‌کند.</string>
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: Bilingual string resources for Settings screen"
```

---

### Task 3: SettingsScreen UI

**Covers:** [S3], [S4], [S5], [S6], [S7], [S8]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: SettingsViewModel
- Produces: SettingsScreen composable with all 6 sections

- [ ] **Step 1: Create SettingsScreen**

```kotlin
// feature/settings/SettingsScreen.kt
package com.benyaminrasouli.phoenixprotocol.feature.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.BuildConfig
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
import com.benyaminrasouli.phoenixprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoenixprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoenixprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val language by viewModel.language.collectAsState()
    val profile by viewModel.profile.collectAsState(initial = null)
    val stats by viewModel.stats.collectAsState(initial = null)
    var showResetDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language Section
            item {
                SettingsSection(title = stringResource(R.string.settings_language)) {
                    SettingsClickableItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = if (language == "en") "English" else "فارسی",
                        onClick = {
                            viewModel.setLanguage(if (language == "en") "fa" else "en")
                        }
                    )
                }
            }

            // Notifications Section (Placeholder)
            item {
                SettingsSection(title = stringResource(R.string.settings_notifications)) {
                    SettingsToggleItem(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.settings_notifications),
                        subtitle = stringResource(R.string.settings_coming_soon),
                        checked = false,
                        enabled = false,
                        onCheckedChange = {}
                    )
                }
            }

            // Theme Preview Section
            item {
                SettingsSection(title = stringResource(R.string.settings_theme)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Palette,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_dark_theme),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_theme_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Active",
                                    tint = PhoenixOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Color swatches
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ColorSwatch(
                                    color = BackgroundDark,
                                    label = "BG",
                                    modifier = Modifier.weight(1f)
                                )
                                ColorSwatch(
                                    color = SurfaceDark,
                                    label = "Surface",
                                    modifier = Modifier.weight(1f)
                                )
                                ColorSwatch(
                                    color = PhoenixOrange,
                                    label = "Accent",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Account Section
            item {
                SettingsSection(title = stringResource(R.string.settings_account)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = profile?.fullName ?: "User",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "@${profile?.username ?: "username"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = {
                                    Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_edit_profile),
                                    color = PhoenixOrange
                                )
                            }
                        }
                    }
                }
            }

            // Data & Privacy Section
            item {
                SettingsSection(title = stringResource(R.string.settings_data_privacy)) {
                    SettingsClickableItem(
                        icon = Icons.Filled.Download,
                        title = stringResource(R.string.settings_export_data),
                        subtitle = stringResource(R.string.settings_coming_soon),
                        onClick = {
                            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    SettingsClickableItem(
                        icon = Icons.Filled.Delete,
                        title = stringResource(R.string.settings_reset_data),
                        subtitle = null,
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showResetDialog = true }
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = stringResource(R.string.settings_about)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = PhoenixOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_about_title),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.settings_about_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsClickableItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = stringResource(R.string.drawer_support),
                        subtitle = null,
                        onClick = { navController.navigate(Screen.Support.route) }
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_data)) },
            text = { Text(stringResource(R.string.settings_reset_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetAllData {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = PhoenixOrange,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else TextSecondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PhoenixOrange,
                checkedTrackColor = PhoenixOrange.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ColorSwatch(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
```

- [ ] **Step 2: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: SettingsScreen with all 6 sections"
```

---

### Task 4: Navigation Integration

**Covers:** [S9]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoenixprotocol/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: SettingsScreen
- Produces: Navigation from Drawer to Settings

- [ ] **Step 1: Update DashboardScreen to pass settings navigation**

Add import at top of DashboardScreen.kt:

```kotlin
import com.benyaminrasouli.phoenixprotocol.feature.settings.SettingsScreen
```

Update the DrawerScreen call to include onNavigateToSettings:

```kotlin
DrawerScreen(
    onNavigateToSettings = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.Settings.route)
    },
    onNavigateToStatistics = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.Statistics.route)
    },
    onNavigateToAchievements = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.Achievements.route)
    }
)
```

- [ ] **Step 2: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: Navigation from Drawer to Settings screen"
```

---

### Task 5: Final Verification

**Covers:** All sections

**Files:** None (verification only)

- [ ] **Step 1: Clean build**

Run: `./gradlew clean`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: Settings screen implementation complete"
```
