# About Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a full About screen with App Header, Description, Developer Info, Social Links, and Legal sections.

**Architecture:** Feature-based Clean Architecture with Hilt DI. Simple composable screen with no ViewModel (static content only).

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt

## Global Constraints
- Min SDK 24, Target SDK 36, Compile SDK 36
- Package: `com.benyaminrasouli.phoniexprotocol`
- Dark theme only (#0D0D0D base, #FF6B35 accent)
- Bilingual: English + Persian (all new strings must have both locales)
- Every task ends with `./gradlew assembleDebug` passing

---

### Task 1: String Resources

**Covers:** [S10]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

**Interfaces:**
- Consumes: None
- Produces: Bilingual string resources for About screen

- [ ] **Step 1: Add English strings**

Add to `app/src/main/res/values/strings.xml`:

```xml
<!-- About -->
<string name="about_title">About</string>
<string name="about_app_name">Phoenix Protocol</string>
<string name="about_version">Version %s</string>
<string name="about_description">A gamified task management app that turns discipline into an RPG experience.</string>
<string name="about_developer">Developer</string>
<string name="about_developer_name">Benyamin Rasouli</string>
<string name="about_contact">Contact</string>
<string name="about_contact_email">support@phoenixprotocol.app</string>
<string name="about_github">GitHub</string>
<string name="about_github_url">github.com/phoenixprotocol</string>
<string name="about_website">Website</string>
<string name="about_website_url">phoenixprotocol.app</string>
<string name="about_privacy_policy">Privacy Policy</string>
<string name="about_terms_of_service">Terms of Service</string>
```

- [ ] **Step 2: Add Persian strings**

Add to `app/src/main/res/values-fa/strings.xml`:

```xml
<!-- About -->
<string name="about_title">درباره ما</string>
<string name="about_app_name">پروتکل ققنوس</string>
<string name="about_version">نسخه %s</string>
<string name="about_description">برنامه مدیریت وظایف گیمیفاید که انضباط را به تجربه RPG تبدیل می‌کند.</string>
<string name="about_developer">توسعه‌دهنده</string>
<string name="about_developer_name">بنیامین رسولی</string>
<string name="about_contact">تماس</string>
<string name="about_contact_email">support@phoenixprotocol.app</string>
<string name="about_github">گیت‌هاب</string>
<string name="about_github_url">github.com/phoenixprotocol</string>
<string name="about_website">وب‌سایت</string>
<string name="about_website_url">phoenixprotocol.app</string>
<string name="about_privacy_policy">سیاست حریم خصوصی</string>
<string name="about_terms_of_service">شرایط استفاده</string>
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: Bilingual string resources for About screen"
```

---

### Task 2: AboutScreen UI

**Covers:** [S3], [S4], [S5], [S6], [S7]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/about/AboutScreen.kt`

**Interfaces:**
- Consumes: BuildConfig.VERSION_NAME
- Produces: AboutScreen composable

- [ ] **Step 1: Create AboutScreen**

```kotlin
// feature/about/AboutScreen.kt
package com.benyaminrasouli.phoniexprotocol.feature.about

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.BuildConfig
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.about_title)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            item {
                AppHeader()
            }

            // Description
            item {
                DescriptionSection()
            }

            // Developer Info
            item {
                DeveloperSection()
            }

            // Social Links
            item {
                SocialLinksSection(
                    onGitHubClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/phoenixprotocol"))
                        context.startActivity(intent)
                    },
                    onWebsiteClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://phoenixprotocol.app"))
                        context.startActivity(intent)
                    }
                )
            }

            // Legal
            item {
                LegalSection(
                    onPrivacyClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://phoenixprotocol.app/privacy"))
                        context.startActivity(intent)
                    },
                    onTermsClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://phoenixprotocol.app/terms"))
                        context.startActivity(intent)
                    }
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = PhoenixOrange,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.about_app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun DescriptionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Text(
            text = stringResource(R.string.about_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun DeveloperSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.about_developer),
                style = MaterialTheme.typography.labelMedium,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.about_developer_name),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.about_contact_email),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SocialLinksSection(
    onGitHubClick: () -> Unit,
    onWebsiteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AboutLinkItem(
                icon = Icons.Filled.Code,
                title = stringResource(R.string.about_github),
                subtitle = stringResource(R.string.about_github_url),
                onClick = onGitHubClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            AboutLinkItem(
                icon = Icons.Filled.Language,
                title = stringResource(R.string.about_website),
                subtitle = stringResource(R.string.about_website_url),
                onClick = onWebsiteClick
            )
        }
    }
}

@Composable
private fun LegalSection(
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AboutLinkItem(
                icon = Icons.Filled.Security,
                title = stringResource(R.string.about_privacy_policy),
                subtitle = null,
                onClick = onPrivacyClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            AboutLinkItem(
                icon = Icons.Filled.Policy,
                title = stringResource(R.string.about_terms_of_service),
                subtitle = null,
                onClick = onTermsClick
            )
        }
    }
}

@Composable
private fun AboutLinkItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
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
                color = MaterialTheme.colorScheme.onSurface
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
```

- [ ] **Step 2: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: AboutScreen with app info, developer, social links, and legal"
```

---

### Task 3: Navigation Integration

**Covers:** [S8]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/dashboard/DashboardScreen.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/drawer/DrawerScreen.kt`

**Interfaces:**
- Consumes: AboutScreen
- Produces: Navigation from Drawer to About screen

- [ ] **Step 1: Update NavGraph.kt**

Add import at top of NavGraph.kt:

```kotlin
import com.benyaminrasouli.phoniexprotocol.feature.about.AboutScreen
```

Add About composable route:

```kotlin
composable(
    Screen.About.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    AboutScreen(navController = navController)
}
```

- [ ] **Step 2: Update DrawerScreen.kt**

Add About menu item before the Support item:

```kotlin
DrawerMenuItem(
    icon = Icons.Filled.Info,
    label = stringResource(R.string.about_title),
    onClick = onNavigateToAbout
)
```

- [ ] **Step 3: Update DashboardScreen.kt**

Add onNavigateToAbout callback to DrawerScreen call:

```kotlin
DrawerScreen(
    onNavigateToSettings = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.Settings.route)
    },
    onNavigateToAbout = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.About.route)
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

- [ ] **Step 4: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: Navigation from Drawer to About screen"
```

---

### Task 4: Final Verification

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
git commit -m "feat: About screen implementation complete"
```
