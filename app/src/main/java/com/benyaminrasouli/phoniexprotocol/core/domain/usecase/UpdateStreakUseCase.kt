package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class UpdateStreakUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(taskCompletedToday: Boolean) {
        val stats = statsRepository.getStatsOnce() ?: return

        if (!taskCompletedToday && stats.currentStreak > 0) {
            // Streak broken — increase shadow level by 2
            statsRepository.increaseShadowLevel(2)
        }
    }
}
