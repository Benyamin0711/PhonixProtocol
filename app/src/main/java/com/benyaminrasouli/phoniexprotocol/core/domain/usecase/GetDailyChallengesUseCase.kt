package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.DailyChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyChallengesUseCase @Inject constructor(
    private val repository: DailyChallengeRepository
) {
    operator fun invoke(): Flow<List<DailyChallenge>> {
        return repository.getTodayChallenges()
    }
}
