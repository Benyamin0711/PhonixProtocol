<div align="center">

# PHOENIX PROTOCOL | پروتکل فینیکس

### Rise from the ashes of procrastination.
### از خاکستر تنبلی برخیز.

**A gamified productivity app that turns your daily tasks into an epic RPG quest.**
**یک اپلیکیشن بهره‌وری گیمیفاید که کارهای روزانه‌ات رو تبدیل به یک ماجراجویی حماسی RPG می‌کنه.**

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green?logo=android)
![License](https://img.shields.io/badge/License-MIT-orange)

</div>

---

## What is Phoenix Protocol? | پروتکل فینیکس چیه؟

**EN:** Phoenix Protocol is not your average to-do list. It's a **gamified life system** where every task you complete earns XP, every day you stay consistent builds your streak, and every week a new **Boss** appears that you must defeat through discipline and focus. Miss a task? Your **Shadow Level** rises. Recover from failure? You earn an epic achievement. This is productivity as an RPG — designed to keep you motivated through game mechanics that actually work.

**فا:** پروتکل فینیکس یه لیست کار معمولی نیست. یه **سیستم زندگی گیمیفایده** که هر کاری که تکمیل کنی XP می‌گیری، هر روز که پیگیر باشی استریکت بالا میره، و هر هفته یه **باس جدید** ظاهر می‌شه که باید با انضباط و تمرکز شکستش بدی. یه کار رو از دست بدی؟ **سطح شدوت** بالا میره. از شکست برگردی؟ یه اچیومنت حماسی می‌گیری. این بهره‌وری به شکل RPG طراحی شده تا با مکانیک‌های بازی انگیزه‌ات حفظ بشه.

---

## Core Mechanics | مکانیک‌های اصلی

### XP & Ranking System | سیستم XP و رنکینگ
**EN:** Complete tasks to earn XP and level up through 10 ranks:

**فا:** کارها رو تکمیل کن تا XP بگیری و از ۱۰ رنک رد بشی:

| Rank / رنک | Level / سطح |
|------|-------|
| Initiate / آغازگر | 1 |
| Survivor / بازمانده | 5 |
| Hunter / شکارچی | 10 |
| Warrior / جنگجو | 15 |
| Elite / نخبه | 20 |
| Commander / فرمانده | 30 |
| Phantom / شبح | 40 |
| Titan / تایتان | 50 |
| Ascendant / صعودکننده | 65 |
| **Phoenix / فینیکس** | **80** |

### Phoenix Energy | انرژی فینیکس
**EN:** Your energy bar depletes as you take on tasks and recovers over time. Manage it wisely — burnout is real, even for a phoenix.

**فا:** نوار انرژیت با انجام کارها کم میشه و با مرور زمان برمیگرده. عاقلانه مدیریتش کن — فرسودگی واقعیه، حتی برای یه فینیکس.

### Shadow Level | سطح شدو
**EN:** Skip tasks or break your streak? Shadow builds up. High shadow means higher penalties. Break through the darkness to earn the **Shadow Breaker** achievement.

**فا:** کارها رو رد کنی یا استریکت رو بشکنی؟ شدو جمع میشه. شدوی بالا یعنی جریمه‌های سنگین‌تر. از تاریکی رد شو تا اچیومنت **شکستن شدو** رو بگیری.

### Weekly Bosses | باس‌های هفتگی
**EN:** Every week, a new **Boss** spawns with unique goals and a deadline. Defeat it by completing the required tasks before time runs out. Track your boss history and prove you're a true slayer.

**فا:** هر هفته یه **باس جدید** با اهداف و ضرب‌الأجل منحصربفرد ظاهر میشه. با تکمیل کارهای لازم قبل از اتمام زمان شکستش بده. تاریخچه باس‌هات رو دنبال کن و ثابت کن یه شکست‌دهنده واقعی هستی.

### Daily Challenges | چالش‌های روزانه
**EN:** Three auto-generated challenges every day. Complete them for bonus XP and energy rewards. Consistency is the name of the game.

**فا:** هر روز سه چالش خودکار. تکمیلشون کن تا XP و انرژی بونوس بگیری. پیگیری کلید موفقیته.

### Focus Timer | تایمر تمرکز
**EN:** Built-in Pomodoro-style focus timer with session tracking. Stay locked in and earn focus XP.

**فا:** تایمر تمرکز پومودورویی داخلی با ردیابی جلسات. متمرکز بمون و XP تمرکز بگیر.

---

## Features | قابلیت‌ها

- **Task Management / مدیریت وظایف** — Create tasks with difficulty levels, categories, priorities, and recurrence
- **Task Templates / قالب وظایف** — Save and reuse task templates for repetitive workflows
- **Categories / دسته‌بندی‌ها** — Organize tasks into life areas
- **Achievements / اچیومنت‌ها** — 15+ achievements across 4 rarity tiers (Common, Rare, Epic, Legendary)
- **Statistics Dashboard / داشبورد آمار** — Track your XP, streaks, completion rate, and progress
- **Home Screen Widget / ویجت صفحه اصلی** — Glance at your status without opening the app
- **Onboarding / ورود به اپ** — Personalized setup with identity path selection
- **Daily Motivational Slogans / شعارهای انگیزشی روزانه** — Rotating quotes to keep you going
- **Dark Theme / تم تاریک** — Full dark mode with Phoenix Orange accent
- **Multi-language Support / پشتیبانی چندزبانه** — English and Persian (Farsi)

---

## Architecture | معماری

**EN:** Built with **Clean Architecture** and **MVVM** pattern:

**فا:** ساخته شده با **معماری پاک** و الگوی **MVVM**:

```
com.benyaminrasouli.phoenixprotocol/
├── core/
│   ├── data/          # Room DB, DAOs, Repositories, DataStore
│   ├── domain/        # Use cases, domain models
│   ├── navigation/    # Nav graph & screen routes
│   ├── notification/  # Notification helpers
│   ├── service/       # Foreground focus timer service
│   ├── ui/            # Reusable composables
│   ├── util/          # Locale helpers
│   └── work/          # WorkManager workers
├── di/                # Hilt modules
├── feature/           # Feature screens
├── ui/theme/          # Material3 theme
└── widget/            # Home screen widget
```

---

## Tech Stack | تکنولوژی‌ها

| Layer / لایه | Technology / تکنولوژی |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Database / دیتابیس | Room (10 entities, 9 DAOs) |
| Preferences / تنظیمات | DataStore |
| Background / پس‌زمینه | WorkManager |
| Navigation / ناوبری | Navigation Compose |
| Serialization / سریال‌سازی | Kotlinx Serialization + Gson |
| Language / زبان | Kotlin |

---

## Getting Started | شروع کار

1. Clone the repo / ریپو رو کلون کن
   ```bash
   git clone https://github.com/BenyaminRasouli/phoenixprotocol.git
   ```

2. Open in Android Studio (Ladybug or newer) / در Android Studio باز کن (Ladybug یا جدیدتر)

3. Sync Gradle and run / Gradle رو هماهنگ کن و اجرا کن (minSdk 24)

---

## Screens | صفحات

| Screen / صفحه | Description / توضیحات |
|--------|-------------|
| Splash / اسپلش | App intro with Phoenix branding / معرفی اولیه اپ |
| Onboarding / ورود | User setup / تنظیمات اولیه کاربر |
| Dashboard / داشبورد | Main hub / مرکز اصلی — وضعیت، وظایف، باس، چالش‌ها |
| Task List / لیست وظایف | All tasks with filters / همه وظایف با فیلتر |
| Create Task / ساخت وظیفه | New task creation / ساخت وظیفه جدید |
| Boss Detail / جزئیات باس | Active boss fight progress / پیشرفت نبرد باس فعال |
| Boss History / تاریخچه باس | Past boss战绩 / تاریخچه باس‌های قبلی |
| Daily Challenges / چالش‌های روزانه | Today's challenges / چالش‌ها و پاداش‌های امروز |
| Focus Timer / تایمر تمرکز | Pomodoro timer / تایمر پومودورو |
| Statistics / آمار | XP, streaks, completion stats / آمار XP، استریک، تکمیل |
| Achievements / اچیومنت‌ها | Badge collection / مجموعه نشان‌ها |
| Profile / پروفایل | User info / اطلاعات کاربر |
| Templates / قالب‌ها | Reusable task templates / قالب‌های قابل استفاده مجدد |
| Categories / دسته‌بندی‌ها | Life area organization / سازماندهی حوزه‌های زندگی |
| Settings / تنظیمات | App preferences / تنظیمات اپلیکیشن |
| About / درباره | App info / اطلاعات اپلیکیشن |
| Support / پشتیبانی | Help & feedback / راهنما و بازخورد |

---

## Achievements | اچیومنت‌ها

| Achievement / اچیومنت | Rarity / نادری | Condition / شرط |
|-------------|--------|-----------|
| First Blood / اولین خون | Common / معمولی | Complete your first task / تکمیل اولین وظیفه |
| 7-Day Warrior / جنگجوی ۷ روزه | Common / معمولی | Maintain a 7-day streak / حفظ استریک ۷ روزه |
| No Excuses / بدون بهانه | Common / معمولی | Complete 10 tasks / تکمیل ۱۰ وظیفه |
| Night Owl / جغد شب | Common / معمولی | Complete task after midnight / تکمیل وظیفه بعد از نیمه‌شب |
| Early Bird / پرنده صبح‌گاهی | Common / معمولی | Complete task before 6 AM / تکمیل وظیفه قبل از ۶ صبح |
| 30-Day Legend / افسانه ۳۰ روزه | Rare / نادر | Maintain a 30-day streak / حفظ استریک ۳۰ روزه |
| Shadow Breaker / شکستن شدو | Rare / نادر | Reduce Shadow to 0 after being > 30 / کاهش شدو به ۰ بعد از بالای ۳۰ بودن |
| Speed Demon / شیطان سرعت | Rare / نادر | Complete 5 tasks in one day / تکمیل ۵ وظیفه در یک روز |
| Marathon Runner / دونده ماراتن | Rare / نادر | Complete 50 tasks total / تکمیل ۵۰ وظیفه در مجموع |
| Phoenix Rising / برخاست فینیکس | Epic / حماسی | Recover from 3+ day absence / بازیابی از غیبت ۳+ روزه |
| Boss Slayer / شکست‌دهنده باس | Epic / حماسی | Complete 5 boss missions / تکمیل ۵ مأموریت باس |
| Discipline Above Mood / انضباط بالاتر از حال | Rare / نادر | Complete task when Shadow > 50 / تکمیل وظیفه وقتی شدو بالای ۵۰ باشه |
| Elite Consistency / ثبات نخبگان | Legendary / افسانه‌ای | 90% completion rate for 7 days / نرخ تکمیل ۹۰٪ به مدت ۷ روز |
| Unbroken / شکست‌ناپذیر | Legendary / افسانه‌ای | 100-day streak / استریک ۱۰۰ روزه |
| Life Master / استاد زندگی | Legendary / افسانه‌ای | Complete task in all 8 life areas / تکمیل وظیفه در تمام ۸ حوزه زندگی |

---

<div align="center">

**Built with fire by [Benyamin Rasouli](https://github.com/BenyaminRasouli)**
**ساخته شده با آتش توسط بنیامین رسولی**

*Discipline is the bridge between goals and accomplishment.*
*انضباط پل بین اهداف و دستیابی به آن‌هاست.*

</div>
