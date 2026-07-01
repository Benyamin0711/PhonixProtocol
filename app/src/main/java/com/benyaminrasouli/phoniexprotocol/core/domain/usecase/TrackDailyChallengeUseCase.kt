package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.DailyChallengeRepository
import javax.inject.Inject

class TrackDailyChallengeUseCase @Inject constructor(
    private val challengeRepository: DailyChallengeRepository
) {
    suspend operator fun invoke(challenges: List<DailyChallenge>, completedTaskType: String? = null, earnedXp: Int = 0) {
        for (challenge in challenges) {
            if (challenge.completed) continue

            val shouldIncrement = when (challenge.type) {
                "COMPLETE_TASKS" -> completedTaskType != null
                "COMPLETE_HARD" -> completedTaskType == "HARD"
                "COMPLETE_PRIORITY" -> completedTaskType == "PRIORITY"
                "EARN_XP" -> earnedXp > 0
                "COMPLETE_CATEGORY" -> completedTaskType != null
                else -> false
            }

            if (shouldIncrement) {
                val increment = if (challenge.type == "EARN_XP") earnedXp else 1
                val newCurrent = challenge.current + increment
                challengeRepository.updateProgress(challenge.id, newCurrent)
                if (newCurrent >= challenge.target) {
                    challengeRepository.markCompleted(challenge.id)
                }
            }
        }
    }
}
