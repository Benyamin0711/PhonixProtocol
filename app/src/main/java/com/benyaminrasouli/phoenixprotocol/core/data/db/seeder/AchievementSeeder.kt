package com.benyaminrasouli.phoenixprotocol.core.data.db.seeder

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Achievement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementSeeder @Inject constructor(
    private val achievementDao: AchievementDao
) {
    suspend fun seed() {
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
            Achievement("challenge_master", "Challenge Master", "Complete 100 total challenges", "military_tech", "LEGENDARY", "challenges_100"),
            Achievement("perfect_day", "Perfect Day", "Complete all challenges in a single day", "star", "EPIC", "perfect_day")
        )
        achievements.forEach { achievementDao.insertAchievement(it) }
    }
}