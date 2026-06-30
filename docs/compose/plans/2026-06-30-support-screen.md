# Support Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a full Support screen with Contact Form, FAQ, Rate App, and Social Media sections.

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
- Produces: Bilingual string resources for Support screen

- [ ] **Step 1: Add English strings**

Add to `app/src/main/res/values/strings.xml`:

```xml
<!-- Support -->
<string name="support_title">Support Us</string>
<string name="support_thank_you">Thank you for using Phoenix Protocol!</string>
<string name="support_contact">Contact Us</string>
<string name="support_name">Name</string>
<string name="support_email">Email</string>
<string name="support_message">Message</string>
<string name="support_send">Send Message</string>
<string name="support_message_sent">Message sent! We\'ll get back to you soon.</string>
<string name="support_faq">Frequently Asked Questions</string>
<string name="support_faq_1_q">What is Phoenix Protocol?</string>
<string name="support_faq_1_a">Phoenix Protocol is a gamified task management app that turns discipline into an RPG experience. Complete tasks, defeat bosses, and level up!</string>
<string name="support_faq_2_q">How do boss missions work?</string>
<string name="support_faq_2_a">Boss missions spawn weekly with random goals. Complete all goals before the deadline to defeat the boss and earn XP rewards!</string>
<string name="support_faq_3_q">How do I earn XP?</string>
<string name="support_faq_3_a">Earn XP by completing tasks, maintaining streaks, and defeating bosses. Higher difficulty tasks give more XP.</string>
<string name="support_rate">Rate Us</string>
<string name="support_rate_button">Rate on Play Store</string>
<string name="support_social">Follow Us</string>
<string name="support_instagram">Instagram</string>
<string name="support_twitter">Twitter / X</string>
<string name="support_github">GitHub</string>
```

- [ ] **Step 2: Add Persian strings**

Add to `app/src/main/res/values-fa/strings.xml`:

```xml
<!-- Support -->
<string name="support_title">حمایت از ما</string>
<string name="support_thank_you">از استفاده شما از پروتکل ققنوس متشکریم!</string>
<string name="support_contact">تماس با ما</string>
<string name="support_name">نام</string>
<string name="support_email">ایمیل</string>
<string name="support_message">پیام</string>
<string name="support_send">ارسال پیام</string>
<string name="support_message_sent">پیام ارسال شد! به زودی با شما تماس خواهیم گرفت.</string>
<string name="support_faq">سوالات متداول</string>
<string name="support_faq_1_q">پروتکل ققنوس چیست؟</string>
<string name="support_faq_1_a">پروتکل ققنوس یک برنامه مدیریت وظایف گیمیفاید است که انضباط را به تجربه RPG تبدیل می‌کند. وظایف را تکمیل کنید، باس‌ها را شکست دهید و سطح خود را ارتقا دهید!</string>
<string name="support_faq_2_q">مأموریت‌های باس چگونه کار می‌کنند؟</string>
<string name="support_faq_2_a">مأموریت‌های باس هر هفته با اهداف تصادفی ظاهر می‌شوند. تمام اهداف را قبل از مهلت تکمیل کنید تا باس را شکست دهید و پاداش XP دریافت کنید!</string>
<string name="support_faq_3_q">چگونه XP کسب کنم؟</string>
<string name="support_faq_3_a">با تکمیل وظایف، حفظ سلسله و شکست دادن باس‌ها XP کسب کنید. وظایف با دشواری بیشتر XP بیشتری می‌دهند.</string>
<string name="support_rate">امتیاز دهید</string>
<string name="support_rate_button">امتیاز در پلی استور</string>
<string name="support_social">ما را دنبال کنید</string>
<string name="support_instagram">اینستاگرام</string>
<string name="support_twitter">توییتر / X</string>
<string name="support_github">گیت‌هاب</string>
```

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: Bilingual string resources for Support screen"
```

---

### Task 2: SupportScreen UI

**Covers:** [S3], [S4], [S5], [S6], [S7]

**Files:**
- Create: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/support/SupportScreen.kt`

**Interfaces:**
- Consumes: None
- Produces: SupportScreen composable

- [ ] **Step 1: Create SupportScreen**

```kotlin
// feature/support/SupportScreen.kt
package com.benyaminrasouli.phoniexprotocol.feature.support

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benyaminrasouli.phoniexprotocol.R
import com.benyaminrasouli.phoniexprotocol.ui.theme.BackgroundDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.SurfaceVariantDark
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    navController: NavController
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.support_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back))
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
            // Header
            item {
                SupportHeader()
            }

            // Contact Form
            item {
                ContactFormSection()
            }

            // FAQ
            item {
                FAQSection()
            }

            // Rate App
            item {
                RateAppSection(
                    onRateClick = {
                        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Social Media
            item {
                SocialMediaSection(
                    onInstagramClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/phoenixprotocol"))
                        context.startActivity(intent)
                    },
                    onTwitterClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/phoenixprotocol"))
                        context.startActivity(intent)
                    },
                    onGitHubClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/phoenixprotocol"))
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
private fun SupportHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Help,
            contentDescription = null,
            tint = PhoenixOrange,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.support_thank_you),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}

@Composable
private fun ContactFormSection() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.support_contact),
                style = MaterialTheme.typography.titleMedium,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.support_name)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.support_email)) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.support_message)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    Toast.makeText(context, context.getString(R.string.support_message_sent), Toast.LENGTH_SHORT).show()
                    name = ""
                    email = ""
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
            ) {
                Icon(Icons.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.support_send))
            }
        }
    }
}

@Composable
private fun FAQSection() {
    var expandedItem by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.support_faq),
                style = MaterialTheme.typography.titleMedium,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(12.dp))

            FAQItem(
                question = stringResource(R.string.support_faq_1_q),
                answer = stringResource(R.string.support_faq_1_a),
                isExpanded = expandedItem == 1,
                onClick = { expandedItem = if (expandedItem == 1) null else 1 }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            FAQItem(
                question = stringResource(R.string.support_faq_2_q),
                answer = stringResource(R.string.support_faq_2_a),
                isExpanded = expandedItem == 2,
                onClick = { expandedItem = if (expandedItem == 2) null else 2 }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            FAQItem(
                question = stringResource(R.string.support_faq_3_q),
                answer = stringResource(R.string.support_faq_3_a),
                isExpanded = expandedItem == 3,
                onClick = { expandedItem = if (expandedItem == 3) null else 3 }
            )
        }
    }
}

@Composable
private fun FAQItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = TextSecondary
            )
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun RateAppSection(
    onRateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.support_rate),
                style = MaterialTheme.typography.titleMedium,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Star rating display
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (index < 4) PhoenixOrange else TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRateClick,
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
            ) {
                Text(stringResource(R.string.support_rate_button))
            }
        }
    }
}

@Composable
private fun SocialMediaSection(
    onInstagramClick: () -> Unit,
    onTwitterClick: () -> Unit,
    onGitHubClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.support_social),
                style = MaterialTheme.typography.titleMedium,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(12.dp))

            SocialLinkItem(
                title = stringResource(R.string.support_instagram),
                onClick = onInstagramClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            SocialLinkItem(
                title = stringResource(R.string.support_twitter),
                onClick = onTwitterClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            SocialLinkItem(
                title = stringResource(R.string.support_github),
                onClick = onGitHubClick
            )
        }
    }
}

@Composable
private fun SocialLinkItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
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
git commit -m "feat: SupportScreen with contact form, FAQ, rate app, and social links"
```

---

### Task 3: Navigation Integration

**Covers:** [S8]

**Files:**
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/core/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/benyaminrasouli/phoniexprotocol/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Consumes: SupportScreen
- Produces: Navigation from Drawer to Support screen

- [ ] **Step 1: Update NavGraph.kt**

Add import at top of NavGraph.kt:

```kotlin
import com.benyaminrasouli.phoniexprotocol.feature.support.SupportScreen
```

Add Support composable route:

```kotlin
composable(
    Screen.Support.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    SupportScreen(navController = navController)
}
```

- [ ] **Step 2: Update DashboardScreen.kt**

Add onNavigateToSupport callback to DrawerScreen call:

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
    onNavigateToSupport = {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.Support.route)
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

- [ ] **Step 3: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: Navigation from Drawer to Support screen"
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
git commit -m "feat: Support screen implementation complete"
```
