package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.domain.repository.FocusRepository
import javax.inject.Inject

data class FocusStats(
    val totalFocusTimeSeconds: Int,
    val completedSessionCount: Int
)

class GetFocusStatsUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(): FocusStats {
        return FocusStats(
            totalFocusTimeSeconds = repository.getTotalFocusTimeSeconds(),
            completedSessionCount = repository.getCompletedSessionCount()
        )
    }
}
