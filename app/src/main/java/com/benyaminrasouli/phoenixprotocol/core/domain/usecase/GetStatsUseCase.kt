package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(): Flow<UserStats?> {
        return statsRepository.getStats()
    }
}
