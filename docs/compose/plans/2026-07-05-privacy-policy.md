# Privacy Policy + Play Store Content Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add in-app Privacy Policy screen and Play Store listing content for Play Store submission.

**Architecture:** Simple Compose screen with LazyColumn, navigation route, and string resources.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `feature/privacy/PrivacyPolicyScreen.kt` | Create | In-app privacy policy |
| `core/navigation/Screen.kt` | Modify | Add PrivacyPolicy route |
| `core/navigation/NavGraph.kt` | Modify | Add composable |
| `feature/about/AboutScreen.kt` | Modify | Update privacy link |
| `app/src/main/res/values/strings.xml` | Modify | Privacy content EN |
| `app/src/main/res/values-fa/strings.xml` | Modify | Privacy content FA |
| `docs/play-store/listing.md` | Create | Play Store descriptions |

---

### Task 1: PrivacyPolicyScreen

**Covers:** [S3, S4]

**Files:**
- Create: `feature/privacy/PrivacyPolicyScreen.kt`

- [ ] **Step 1: Create PrivacyPolicyScreen composable**

```kotlin
package com.benyaminrasouli.phoenixprotocol.feature.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benyaminrasouli.phoenixprotocol.R
import com.benyaminrasouli.phoenixprotocol.ui.theme.PhoenixOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_policy_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                PrivacySection(
                    title = stringResource(R.string.privacy_section_1_title),
                    content = stringResource(R.string.privacy_section_1_content)
                )
            }

            item {
                PrivacySection(
                    title = stringResource(R.string.privacy_section_2_title),
                    content = stringResource(R.string.privacy_section_2_content)
                )
            }

            item {
                PrivacySection(
                    title = stringResource(R.string.privacy_section_3_title),
                    content = stringResource(R.string.privacy_section_3_content)
                )
            }

            item {
                PrivacySection(
                    title = stringResource(R.string.privacy_section_4_title),
                    content = stringResource(R.string.privacy_section_4_content)
                )
            }

            item {
                PrivacySection(
                    title = stringResource(R.string.privacy_section_5_title),
                    content = stringResource(R.string.privacy_section_5_content)
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PhoenixOrange
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color(0xFFA0A0B0),
                lineHeight = 22.sp
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/privacy/PrivacyPolicyScreen.kt
git commit -m "feat: add PrivacyPolicyScreen composable"
```

---

### Task 2: Navigation + About Screen Update

**Covers:** [S5]

**Files:**
- Modify: `core/navigation/Screen.kt`
- Modify: `core/navigation/NavGraph.kt`
- Modify: `feature/about/AboutScreen.kt`

- [ ] **Step 1: Add route to Screen.kt**

Add after MoodTracker:
```kotlin
data object PrivacyPolicy : Screen("privacy_policy")
```

- [ ] **Step 2: Add NavGraph composable**

Add import:
```kotlin
import com.benyaminrasouli.phoenixprotocol.feature.privacy.PrivacyPolicyScreen
```

Add composable:
```kotlin
composable(
    Screen.PrivacyPolicy.route,
    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
) {
    PrivacyPolicyScreen(navController = navController)
}
```

- [ ] **Step 3: Update About screen privacy link**

In AboutScreen.kt, change the Privacy Policy onClick from:
```kotlin
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$websiteUrl/privacy"))
context.startActivity(intent)
```

To:
```kotlin
navController.navigate(Screen.PrivacyPolicy.route)
```

Add import if needed:
```kotlin
import com.benyaminrasouli.phoenixprotocol.core.navigation.Screen
```

- [ ] **Step 4: Build and verify**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add core/navigation/Screen.kt core/navigation/NavGraph.kt feature/about/AboutScreen.kt
git commit -m "feat: wire PrivacyPolicy into navigation and About screen"
```

---

### Task 3: Privacy Policy Content (EN/FA)

**Covers:** [S3]

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

- [ ] **Step 1: Add English strings**

Add to `values/strings.xml`:
```xml
<!-- Privacy Policy -->
<string name="privacy_policy_title">Privacy Policy</string>
<string name="privacy_section_1_title">Data Collection</string>
<string name="privacy_section_1_content">Phoenix Protocol collects only the data you create within the app: tasks, habits, mood entries, achievements, and profile information. All data is created by you and stored locally on your device.</string>
<string name="privacy_section_2_title">Data Storage</string>
<string name="privacy_section_2_content">All your data is stored locally on your device using Room database and DataStore preferences. No data is transmitted to external servers. Your data never leaves your device unless you explicitly export it.</string>
<string name="privacy_section_3_title">No Internet Access</string>
<string name="privacy_section_3_content">Phoenix Protocol does not require or use internet connectivity. The app functions entirely offline. No analytics, advertising, or tracking SDKs are included.</string>
<string name="privacy_section_4_title">Children\'s Privacy</string>
<string name="privacy_section_4_content">Phoenix Protocol is not directed at children under 13 years of age. We do not knowingly collect personal information from children under 13.</string>
<string name="privacy_section_5_title">Changes to This Policy</string>
<string name="privacy_section_5_content">We may update this privacy policy from time to time. Changes will be reflected in the app update. Continued use of the app after changes constitutes acceptance of the updated policy.</string>
```

- [ ] **Step 2: Add Persian strings**

Add to `values-fa/strings.xml`:
```xml
<!-- Privacy Policy -->
<string name="privacy_policy_title">حریم خصوصی</string>
<string name="privacy_section_1_title">جمع‌آوری داده‌ها</string>
<string name="privacy_section_1_content">پروتکل فینیکس فقط داده‌هایی را که شما در اپلیکیشن ایجاد می‌کنید جمع‌آوری می‌کند: وظایف، عادت‌ها، ورودی‌های خلق‌وخو، دستاوردها و اطلاعات پروفایل. تمام داده‌ها توسط شما ایجاد شده و به صورت محلی در دستگاه شما ذخیره می‌شوند.</string>
<string name="privacy_section_2_title">ذخیره‌سازی داده‌ها</string>
<string name="privacy_section_2_content">تمام داده‌های شما به صورت محلی در دستگاه شما با استفاده از پایگاه داده Room و تنظیمات DataStore ذخیره می‌شوند. هیچ داده‌ای به سرورهای خارجی ارسال نمی‌شود. داده‌های شما دستگاه شما را ترک نمی‌کنند مگر اینکه صراحتاً آنها را خروجی بگیرید.</string>
<string name="privacy_section_3_title">بدون دسترسی به اینترنت</string>
<string name="privacy_section_3_content">پروتکل فینیکس نیازی به اتصال اینترنت ندارد و از آن استفاده نمی‌کند. اپلیکیشن کاملاً آفلاین کار می‌کند. هیچ SDK تحلیلی، تبلیغاتی یا ردیابی در آن گنجانده نشده است.</string>
<string name="privacy_section_4_title">حریم خصوصی کودکان</string>
<string name="privacy_section_4_content">پروتکل فینیکس برای کودکان زیر 13 سال طراحی نشده است. ما عمداً اطلاعات شخصی کودکان زیر 13 سال را جمع‌آوری نمی‌کنیم.</string>
<string name="privacy_section_5_title">تغییرات در این سیاست</string>
<string name="privacy_section_5_content">ممکن است این سیاست حفظ حریم خصوصی را از زمانی به زمان دیگر به‌روز کنیم. تغییرات در به‌روزرسانی اپلیکیشن بازتاب داده می‌شوند. استفاده ادامه‌دار از اپلیکیشن پس از تغییرات، به معنای پذیرش سیاست به‌روز شده است.</string>
```

- [ ] **Step 3: Build and verify**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-fa/strings.xml
git commit -m "feat: add privacy policy content in EN and FA"
```

---

### Task 4: Play Store Listing Content

**Covers:** [S6]

**Files:**
- Create: `docs/play-store/listing.md`

- [ ] **Step 1: Create Play Store listing document**

```markdown
# Phoenix Protocol - Play Store Listing

## Short Description (80 chars max)
Gamified productivity app with XP, bosses, and daily challenges.

## Full Description (EN)

**Phoenix Protocol** — Rise from the ashes of procrastination.

Transform your daily tasks into an epic RPG adventure. Complete tasks to earn XP, level up through 10 ranks, and face weekly Boss challenges that test your discipline.

**Core Features:**
- **Task Management** — Create tasks with difficulty levels, categories, and priorities
- **XP & Ranking System** — Earn experience points and climb from Initiate to Phoenix rank
- **Weekly Bosses** — Face new boss challenges every week with unique goals
- **Daily Challenges** — Complete 3 auto-generated challenges for bonus rewards
- **Shadow Level** — Track your consistency; skip tasks and shadow builds up
- **Focus Timer** — Built-in Pomodoro timer with session tracking
- **Habit Tracker** — Build lasting habits with streak tracking and XP rewards
- **Mood Tracker** — Record your daily mood with emoji-based entries
- **Achievements** — Unlock 15+ achievements across 4 rarity tiers
- **Statistics Dashboard** — Track your progress with charts and insights
- **Home Screen Widget** — Glance at your status without opening the app
- **Theme Customization** — Choose from 8 accent colors and 3 background levels
- **Bilingual** — Full English and Persian (Farsi) support

**Your data stays on your device.** No internet required. No ads. No tracking.

**Discipline is the bridge between goals and accomplishment.**

## Full Description (FA)

**پروتکل فینیکس** — از خاکستر تنبلی برخیز.

کارهای روزانه‌ات رو به یک ماجراجویی حماسی RPG تبدیل کن. کارها رو تکمیل کن تا XP بگیری، از آغازگر تا رنک فینیکس بالا برو، و هر هفته چالش‌های باس جدید رو قبول کن.

**قابلیت‌های اصلی:**
- **مدیریت وظایف** — ساخت وظایف با سطوح دشواری، دسته‌بندی‌ها و اولویت‌ها
- **سیستم XP و رنکینگ** — کسب امتیاز تجربه و صعود از آغازگر تا فینیکس
- **باس‌های هفتگی** — هر هفته چالش‌های باس جدید با اهداف منحصربفرد
- **چالش‌های روزانه** — تکمیل ۳ چالش خودکار برای پاداش‌های بونوس
- **سطح شدو** — ردیابی پیگیری؛ کارها رو رد کنی شدو بالا میره
- **تایمر تمرکز** — تایمر پومودوروی داخلی با ردیابی جلسات
- **ردیاب عادت** — ساخت عادت‌های ماندگار با ردیابی استریک و پاداش XP
- **ردیاب خلق‌وخو** — ثبت خلق‌وخوی روزانه با ورودی‌های ایموجی
- **اچیومنت‌ها** — باز کردن ۱۵+ اچیومنت در ۴ سطح نادری
- **داشبورد آمار** — ردیابی پیشرفت با نمودارها و بینش‌ها
- **ویجت صفحه اصلی** — مشاهده وضعیت بدون باز کردن اپ
- **سفارشی‌سازی تم** — انتخاب از ۸ رنگ لهجه و ۳ سطح پس‌زمینه
- **دو زبانه** — پشتیبانی کامل از انگلیسی و فارسی

**داده‌هات فقط روی دستگاهت میمونه.** نیازی به اینترنت نیست. بدون تبلیغات. بدون ردیابی.

**انضباط پل بین اهداف و دستیابی به آن‌هاست.**
```

- [ ] **Step 2: Commit**

```bash
git add docs/play-store/listing.md
git commit -m "docs: add Play Store listing content"
```
