package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.BossRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class CompleteBossUseCase @Inject constructor(
    private val bossRepository: BossRepository,
    private val statsRepository: StatsRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(bossId: Long) {
        val boss = bossRepository.getActiveBoss() ?: return
        if (boss.id != bossId) return

        bossRepository.updateBossStatus(bossId, "COMPLETED")

        statsRepository.addXp(boss.rewardXp)

        val completedCount = bossRepository.getCompletedBossCount()
        if (completedCount >= 5) {
            achievementRepository.unlock("boss_slayer")
        }
    }
}
