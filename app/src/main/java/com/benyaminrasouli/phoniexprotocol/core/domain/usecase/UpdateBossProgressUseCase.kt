package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.domain.model.BossGoal
import com.benyaminrasouli.phoniexprotocol.core.domain.model.GoalType
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateBossProgressUseCase @Inject constructor(
    private val bossRepository: BossRepository,
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke() {
        val boss = bossRepository.getActiveBoss() ?: return
        if (boss.status != "ACTIVE") return

        val goals = BossGoal.fromJson(boss.goals)
        var updated = false

        goals.forEach { goal ->
            when (goal.type) {
                GoalType.COMPLETE_TASKS -> {
                    val count = taskRepository.getCompletedTaskCountSince(boss.createdAt)
                    if (count > goal.current) {
                        goal.current = count
                        updated = true
                    }
                }
                GoalType.STREAK_DAYS -> {
                    val stats = statsRepository.getStatsOnce()
                    if (stats != null && stats.currentStreak > goal.current) {
                        goal.current = stats.currentStreak
                        updated = true
                    }
                }
                GoalType.PRIORITY_TASKS -> {
                    val count = taskRepository.getCompletedPriorityTaskCountSince(boss.createdAt)
                    if (count > goal.current) {
                        goal.current = count
                        updated = true
                    }
                }
                GoalType.XP_EARNED -> {
                    val stats = statsRepository.getStatsOnce()
                    if (stats != null && stats.xp > goal.current) {
                        goal.current = stats.xp
                        updated = true
                    }
                }
                GoalType.DIFFICULTY_TASKS -> {
                    val count = taskRepository.getCompletedHardTaskCountSince(boss.createdAt)
                    if (count > goal.current) {
                        goal.current = count
                        updated = true
                    }
                }
            }
        }

        if (updated) {
            val updatedBoss = boss.copy(goals = BossGoal.toJson(goals))
            bossRepository.insertBoss(updatedBoss)
        }
    }
}
