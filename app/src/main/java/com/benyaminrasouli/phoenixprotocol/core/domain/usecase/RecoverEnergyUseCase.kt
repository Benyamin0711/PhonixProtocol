package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class RecoverEnergyUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke() {
        val stats = statsRepository.getStatsOnce() ?: return
        if (stats.phoenixEnergy < 100) {
            statsRepository.increasePhoenixEnergy(1)
        }
    }
}
