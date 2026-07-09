package com.benyaminrasouli.phoenixprotocol.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Category
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.FocusSession
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Template
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserAchievement
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats

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
        Category::class,
        FocusSession::class,
        ShadowLog::class,
        Campaign::class,
        CampaignDay::class,
        DailyMission::class,
        Prayer::class
    ],
    version = 12,
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
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun shadowLogDao(): ShadowLogDao
    abstract fun campaignDao(): CampaignDao
}
