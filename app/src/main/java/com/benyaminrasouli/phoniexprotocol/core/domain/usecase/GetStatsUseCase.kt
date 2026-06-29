package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(): Flow<UserStats?> {
        return statsRepository.getStats()
    }
}
