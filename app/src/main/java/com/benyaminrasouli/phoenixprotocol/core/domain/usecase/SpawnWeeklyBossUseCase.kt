package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoenixprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoenixprotocol.core.domain.model.GoalType
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class SpawnWeeklyBossUseCase @Inject constructor(
    private val bossRepository: BossRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke() {
        val activeBoss = bossRepository.getActiveBoss()
        if (activeBoss != null) {
            if (System.currentTimeMillis() > activeBoss.deadline) {
                bossRepository.updateBossStatus(activeBoss.id, "FAILED")
            } else {
                return
            }
        }

        val lastBoss = bossRepository.getLastBoss()
        if (lastBoss != null) {
            val timeSinceLastBoss = System.currentTimeMillis() - lastBoss.createdAt
            val oneWeek = 7 * 24 * 60 * 60 * 1000L
            if (timeSinceLastBoss < oneWeek) return
        }

        val completedCount = bossRepository.getCompletedBossCount()
        val level = (completedCount + 1).coerceAtMost(5)

        val goals = generateGoals(level)
        val rewardXp = when (level) {
            1 -> 100
            2 -> 200
            3 -> 350
            4 -> 500
            else -> 700
        }

        val deadline = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)

        val stats = statsRepository.getStatsOnce()
        val streakAtSpawn = stats?.currentStreak ?: 0
        val xpAtSpawn = stats?.xp ?: 0

        val boss = Boss(
            level = level,
            title = "Boss Level $level",
            description = "Complete all goals to defeat the boss!",
            goals = BossGoal.toJson(goals),
            deadline = deadline,
            rewardXp = rewardXp,
            streakAtSpawn = streakAtSpawn,
            xpAtSpawn = xpAtSpawn
        )

        bossRepository.insertBoss(boss)
    }

    private fun generateGoals(level: Int): List<BossGoal> {
        val goalCount = when (level) {
            1 -> 2
            2 -> 2
            3 -> 3
            4 -> 3
            else -> 4
        }

        val availableGoals = listOf(
            GoalType.COMPLETE_TASKS,
            GoalType.STREAK_DAYS,
            GoalType.PRIORITY_TASKS,
            GoalType.XP_EARNED,
            GoalType.DIFFICULTY_TASKS
        )

        val selectedGoals = availableGoals.shuffled().take(goalCount)

        return selectedGoals.map { type ->
            when (type) {
                GoalType.COMPLETE_TASKS -> BossGoal(
                    type = type,
                    target = (5 + level * 2).coerceAtMost(15),
                    description = "Complete ${(5 + level * 2).coerceAtMost(15)} tasks"
                )
                GoalType.STREAK_DAYS -> BossGoal(
                    type = type,
                    target = (3 + level).coerceAtMost(7),
                    description = "Maintain a ${(3 + level).coerceAtMost(7)}-day streak"
                )
                GoalType.PRIORITY_TASKS -> BossGoal(
                    type = type,
                    target = (2 + level).coerceAtMost(5),
                    description = "Complete ${(2 + level).coerceAtMost(5)} priority tasks"
                )
                GoalType.XP_EARNED -> BossGoal(
                    type = type,
                    target = (100 + level * 100).coerceAtMost(500),
                    description = "Earn ${(100 + level * 100).coerceAtMost(500)} XP"
                )
                GoalType.DIFFICULTY_TASKS -> BossGoal(
                    type = type,
                    target = (3 + level).coerceAtMost(8),
                    description = "Complete ${(3 + level).coerceAtMost(8)} HARD/EXTREME tasks"
                )
            }
        }
    }
}
