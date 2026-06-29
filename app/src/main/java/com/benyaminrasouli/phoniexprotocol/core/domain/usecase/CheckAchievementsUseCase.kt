package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import javax.inject.Inject

class CheckAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val taskRepository: TaskRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) {
    suspend operator fun invoke() {
        val stats = statsRepository.getStatsOnce() ?: return
        val taskCount = taskRepository.getCompletedTaskCount()

        // First Blood
        if (taskCount >= 1) achievementRepository.unlock("first_blood")

        // No Excuses
        if (taskCount >= 10) achievementRepository.unlock("no_excuses")

        // Marathon Runner
        if (taskCount >= 50) achievementRepository.unlock("marathon_runner")

        // 7-Day Warrior
        if (stats.currentStreak >= 7) achievementRepository.unlock("7day_warrior")

        // 30-Day Legend
        if (stats.currentStreak >= 30) achievementRepository.unlock("30day_legend")

        // Unbroken
        if (stats.currentStreak >= 100) achievementRepository.unlock("unbroken")

        // Shadow Breaker
        if (stats.shadowLevel > 30) {
            // Will be checked when shadow decreases
        }

        // Speed Demon - check today's completed tasks
        // (simplified: check if completedTasks increased significantly)
    }
}