package com.benyaminrasouli.phoniexprotocol.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.PhoenixDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.BossDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.DailyChallengeDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
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
                arrayOf("marathon_runner", "Marathon Runner", "Complete 50 tasks total", "directions_run", "RARE", "tasks_50")
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhoenixDatabase {
        return Room.databaseBuilder(
            context,
            PhoenixDatabase::class.java,
            "phoenix_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
                            Achievement("marathon_runner", "Marathon Runner", "Complete 50 tasks total", "directions_run", "RARE", "tasks_50")
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
}
