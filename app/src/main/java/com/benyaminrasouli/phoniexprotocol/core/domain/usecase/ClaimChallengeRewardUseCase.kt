package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.DailyChallengeRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import javax.inject.Inject

class ClaimChallengeRewardUseCase @Inject constructor(
    private val challengeRepository: DailyChallengeRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(challenge: DailyChallenge) {
        if (challenge.completed && !challenge.claimed) {
            statsRepository.addXp(challenge.rewardXp)
            statsRepository.increasePhoenixEnergy(challenge.rewardEnergy)
            challengeRepository.markClaimed(challenge.id)
        }
    }
}
