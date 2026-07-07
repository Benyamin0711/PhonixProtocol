package com.benyaminrasouli.phoenixprotocol.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.benyaminrasouli.phoenixprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.BossDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DailyChallengeDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.FocusSessionDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CampaignDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.TemplateDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.ShadowLogDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Achievement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN taskType TEXT NOT NULL DEFAULT 'CUSTOM'")
            db.execSQL("ALTER TABLE tasks ADD COLUMN isPriority INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS achievements (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, icon TEXT NOT NULL, rarity TEXT NOT NULL, condition TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS user_achievements (achievementId TEXT NOT NULL PRIMARY KEY, unlockedAt INTEGER NOT NULL)")
            // Seed achievements for existing users
            val achievements = arrayOf(
                arrayOf("first_blood", "First Blood", "Complete your first task", "emoji_events", "COMMON", "complete_1"),
                arrayOf("7day_warrior", "7-Day Warrior", "Maintain a 7-day streak", "local_fire_department", "COMMON", "streak_7"),
                arrayOf("30day_legend", "30-Day Legend", "Maintain a 30-day streak", "military_tech", "RARE", "streak_30"),
                arrayOf("no_excuses", "No Excuses", "Complete 10 tasks", "task_alt", "COMMON", "tasks_10"),
                arrayOf("discipline_above_mood", "Discipline Above Mood", "Complete task when Shadow > 50", "psychology", "RARE", "shadow_task"),
                arrayOf("phoenix_rising", "Phoenix Rising", "Recover from 3+ day absence", "trending_up", "EPIC", "recovery"),
                arrayOf("boss_slayer", "Boss Slayer", "Complete 5 boss missions", "castle", "EPIC", "boss_5"),
                arrayOf("shadow_breaker", "Shadow Breaker", "Reduce Shadow to 0 after being > 30", "dark_mode", "RARE", "shadow_break"),
                arrayOf("elite_consistency", "Elite Consistency", "90% completion rate for 7 days", "star", "LEGENDARY", "consistency_90"),
                arrayOf("unbroken", "Unbroken", "100-day streak", "whatshot", "LEGENDARY", "streak_100"),
                arrayOf("life_master", "Life Master", "Complete task in all 8 life areas", "public", "LEGENDARY", "all_areas"),
                arrayOf("night_owl", "Night Owl", "Complete task after midnight", "bedtime", "COMMON", "night"),
                arrayOf("early_bird", "Early Bird", "Complete task before 6 AM", "wb_sunny", "COMMON", "morning"),
                arrayOf("speed_demon", "Speed Demon", "Complete 5 tasks in one day", "bolt", "RARE", "speed_5"),
                arrayOf("marathon_runner", "Marathon Runner", "Complete 50 tasks total", "directions_run", "RARE", "tasks_50"),
                arrayOf("daily_warrior", "Daily Warrior", "Complete all challenges 7 days in a row", "emoji_events", "RARE", "daily_7"),
                arrayOf("challenge_master", "Challenge Master", "Complete 100 total challenges", "military_tech", "EPIC", "challenges_100"),
                arrayOf("perfect_day", "Perfect Day", "Complete all challenges in a single day", "star", "COMMON", "perfect_day")
            )
            achievements.forEach { a ->
                db.execSQL(
                    "INSERT OR IGNORE INTO achievements (id, title, description, icon, rarity, condition) VALUES (?, ?, ?, ?, ?, ?)",
                    a
                )
            }
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS bosses (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, level INTEGER NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, goals TEXT NOT NULL, deadline INTEGER NOT NULL, status TEXT NOT NULL DEFAULT 'ACTIVE', rewardXp INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE bosses ADD COLUMN streakAtSpawn INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE bosses ADD COLUMN xpAtSpawn INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS daily_challenges (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, type TEXT NOT NULL, target INTEGER NOT NULL, current INTEGER NOT NULL DEFAULT 0, rewardXp INTEGER NOT NULL, rewardEnergy INTEGER NOT NULL, completed INTEGER NOT NULL DEFAULT 0, claimed INTEGER NOT NULL DEFAULT 0, date TEXT NOT NULL, createdAt INTEGER NOT NULL)")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS templates (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, difficulty TEXT NOT NULL, category TEXT NOT NULL, recurrence TEXT NOT NULL, taskType TEXT NOT NULL, isPriority INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL)")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, color TEXT NOT NULL DEFAULT '#FF6B35', isDefault INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("ALTER TABLE tasks ADD COLUMN categoryId INTEGER")
            val predefinedCategories = arrayOf(
                arrayOf("Work", "#4A90D9", "1"),
                arrayOf("Health", "#4CAF50", "1"),
                arrayOf("Learning", "#9C27B0", "1"),
                arrayOf("Personal", "#FF6B35", "1"),
                arrayOf("Finance", "#FFC107", "1"),
                arrayOf("Social", "#E91E63", "1")
            )
            predefinedCategories.forEach { c ->
                db.execSQL(
                    "INSERT INTO categories (name, color, isDefault) VALUES (?, ?, ?)",
                    c
                )
            }
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Recreate categories table with UNIQUE constraint on name column
            db.execSQL("CREATE TABLE IF NOT EXISTS categories_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL UNIQUE, color TEXT NOT NULL DEFAULT '#FF6B35', isDefault INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("INSERT INTO categories_new SELECT * FROM categories")
            db.execSQL("DROP TABLE categories")
            db.execSQL("ALTER TABLE categories_new RENAME TO categories")
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS focus_sessions " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "taskId INTEGER, " +
                "startedAt INTEGER NOT NULL, " +
                "endedAt INTEGER, " +
                "durationSeconds INTEGER NOT NULL, " +
                "completed INTEGER NOT NULL DEFAULT 0, " +
                "mode TEXT NOT NULL DEFAULT 'POMODORO')"
            )
        }
    }

    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS shadow_log (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, action TEXT NOT NULL, amount INTEGER NOT NULL, description TEXT NOT NULL)")
        }
    }

    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS habits " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT NOT NULL DEFAULT '', " +
                "target INTEGER NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "categoryId INTEGER, " +
                "color TEXT NOT NULL DEFAULT '#FF6B35', " +
                "reminderEnabled INTEGER NOT NULL DEFAULT 0, " +
                "reminderHour INTEGER NOT NULL DEFAULT 9, " +
                "reminderMinute INTEGER NOT NULL DEFAULT 0, " +
                "createdAt INTEGER NOT NULL, " +
                "isActive INTEGER NOT NULL DEFAULT 1)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS habit_logs " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "habitId INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "value INTEGER NOT NULL, " +
                "createdAt INTEGER NOT NULL)"
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_logs_habitId_date ON habit_logs (habitId, date)")
        }
    }

    private val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Campaigns table
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS campaigns " +
                "(id INTEGER PRIMARY KEY NOT NULL, " +
                "currentDay INTEGER NOT NULL DEFAULT 1, " +
                "totalXp INTEGER NOT NULL DEFAULT 0, " +
                "startDate INTEGER NOT NULL)"
            )
            db.execSQL("INSERT INTO campaigns (id, currentDay, totalXp, startDate) VALUES (1, 1, 0, ${System.currentTimeMillis()})")

            // Campaign days table
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS campaign_days " +
                "(dayNumber INTEGER PRIMARY KEY NOT NULL, " +
                "completedMissions TEXT NOT NULL DEFAULT '', " +
                "completedPrayers TEXT NOT NULL DEFAULT '', " +
                "note TEXT NOT NULL DEFAULT '', " +
                "relapseCount INTEGER NOT NULL DEFAULT 0, " +
                "isAsh INTEGER NOT NULL DEFAULT 0)"
            )

            // Daily missions table
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS daily_missions " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "xp INTEGER NOT NULL, " +
                "isActive INTEGER NOT NULL DEFAULT 1)"
            )
            val missions = arrayOf(
                arrayOf("3 saat mothale'e daneshgah", "30"),
                arrayOf("hal tamrin riazi", "20"),
                arrayOf("hal tamrin fizik", "20"),
                arrayOf("kodnevisi HabitAway", "25"),
                arrayOf("baghesh / tamrin", "20"),
                arrayOf("bedoon porn", "30"),
                arrayOf("gitar", "10"),
                arrayOf("mothale'e azad", "10"),
                arrayOf("journal shab", "10"),
                arrayOf("scroll control shode", "15"),
                arrayOf("khabe muntazam", "15"),
                arrayOf("marrat sazie mahal", "5")
            )
            missions.forEach { m ->
                db.execSQL("INSERT INTO daily_missions (name, xp, isActive) VALUES (?, ?, 1)", m)
            }

            // Prayers table
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS prayers " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "xp INTEGER NOT NULL, " +
                "isActive INTEGER NOT NULL DEFAULT 1)"
            )
            val prayers = arrayOf(
                arrayOf("namaz sobh", "8"),
                arrayOf("namaz zohr", "5"),
                arrayOf("namaz asr", "5"),
                arrayOf("namaz maghrib", "5"),
                arrayOf("namaz esha", "5")
            )
            prayers.forEach { p ->
                db.execSQL("INSERT INTO prayers (name, xp, isActive) VALUES (?, ?, 1)", p)
            }

            // Academic subjects table
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS academic_subjects " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "progress REAL NOT NULL DEFAULT 0, " +
                "riskLevel TEXT NOT NULL DEFAULT 'NORMAL', " +
                "isActive INTEGER NOT NULL DEFAULT 1)"
            )
            val subjects = arrayOf(
                arrayOf("riazi 1", "18", "BOSS_FIGHT"),
                arrayOf("fizik", "15", "HIGH_RISK"),
                arrayOf("barnameh nevisi", "35", "MAIN_SKILL")
            )
            subjects.forEach { s ->
                db.execSQL("INSERT INTO academic_subjects (name, progress, riskLevel, isActive) VALUES (?, ?, ?, 1)", s)
            }
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhoenixDatabase {
        return Room.databaseBuilder(
            context,
            PhoenixDatabase::class.java,
            "phoenix_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val achievements = listOf(
                            Achievement("first_blood", "First Blood", "Complete your first task", "emoji_events", "COMMON", "complete_1"),
                            Achievement("7day_warrior", "7-Day Warrior", "Maintain a 7-day streak", "local_fire_department", "COMMON", "streak_7"),
                            Achievement("30day_legend", "30-Day Legend", "Maintain a 30-day streak", "military_tech", "RARE", "streak_30"),
                            Achievement("no_excuses", "No Excuses", "Complete 10 tasks", "task_alt", "COMMON", "tasks_10"),
                            Achievement("discipline_above_mood", "Discipline Above Mood", "Complete task when Shadow > 50", "psychology", "RARE", "shadow_task"),
                            Achievement("phoenix_rising", "Phoenix Rising", "Recover from 3+ day absence", "trending_up", "EPIC", "recovery"),
                            Achievement("boss_slayer", "Boss Slayer", "Complete 5 boss missions", "castle", "EPIC", "boss_5"),
                            Achievement("shadow_breaker", "Shadow Breaker", "Reduce Shadow to 0 after being > 30", "dark_mode", "RARE", "shadow_break"),
                            Achievement("elite_consistency", "Elite Consistency", "90% completion rate for 7 days", "star", "LEGENDARY", "consistency_90"),
                            Achievement("unbroken", "Unbroken", "100-day streak", "whatshot", "LEGENDARY", "streak_100"),
                            Achievement("life_master", "Life Master", "Complete task in all 8 life areas", "public", "LEGENDARY", "all_areas"),
                            Achievement("night_owl", "Night Owl", "Complete task after midnight", "bedtime", "COMMON", "night"),
                            Achievement("early_bird", "Early Bird", "Complete task before 6 AM", "wb_sunny", "COMMON", "morning"),
                            Achievement("speed_demon", "Speed Demon", "Complete 5 tasks in one day", "bolt", "RARE", "speed_5"),
                            Achievement("marathon_runner", "Marathon Runner", "Complete 50 tasks total", "directions_run", "RARE", "tasks_50"),
                            Achievement("daily_warrior", "Daily Warrior", "Complete all challenges 7 days in a row", "emoji_events", "RARE", "daily_7"),
                            Achievement("challenge_master", "Challenge Master", "Complete 100 total challenges", "military_tech", "EPIC", "challenges_100"),
                            Achievement("perfect_day", "Perfect Day", "Complete all challenges in a single day", "star", "COMMON", "perfect_day")
                        )
                        achievements.forEach { a ->
                            db.execSQL(
                                "INSERT OR REPLACE INTO achievements (id, title, description, icon, rarity, condition) VALUES (?, ?, ?, ?, ?, ?)",
                                arrayOf(a.id, a.title, a.description, a.icon, a.rarity, a.condition)
                            )
                        }
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideUserProfileDao(db: PhoenixDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideTaskDao(db: PhoenixDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideUserStatsDao(db: PhoenixDatabase): UserStatsDao = db.userStatsDao()

    @Provides
    fun provideAchievementDao(db: PhoenixDatabase): AchievementDao = db.achievementDao()

    @Provides
    fun provideBossDao(db: PhoenixDatabase): BossDao = db.bossDao()

    @Provides
    fun provideDailyChallengeDao(db: PhoenixDatabase): DailyChallengeDao = db.dailyChallengeDao()

    @Provides
    fun provideTemplateDao(db: PhoenixDatabase): TemplateDao = db.templateDao()

    @Provides
    fun provideCategoryDao(db: PhoenixDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideFocusSessionDao(db: PhoenixDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    fun provideShadowLogDao(db: PhoenixDatabase): ShadowLogDao = db.shadowLogDao()

    @Provides
    fun provideCampaignDao(db: PhoenixDatabase): CampaignDao = db.campaignDao()

    private val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS mood_entries " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "moodLevel INTEGER NOT NULL, " +
                "note TEXT NOT NULL DEFAULT '', " +
                "tags TEXT NOT NULL DEFAULT '', " +
                "timestamp INTEGER NOT NULL)"
            )
        }
    }
}
