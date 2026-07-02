package com.benyaminrasouli.phoniexprotocol.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.BossDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.CategoryDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.DailyChallengeDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TemplateDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Category
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats

@Database(
    entities = [
        UserProfile::class,
        Task::class,
        UserStats::class,
        Achievement::class,
        UserAchievement::class,
        Boss::class,
        DailyChallenge::class,
        Template::class,
        Category::class
    ],
    version = 8,
    exportSchema = false
)
abstract class PhoenixDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun bossDao(): BossDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun templateDao(): TemplateDao
    abstract fun categoryDao(): CategoryDao
}
