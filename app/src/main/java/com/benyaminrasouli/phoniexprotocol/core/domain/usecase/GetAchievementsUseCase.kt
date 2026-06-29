package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class AchievementWithStatus(
    val achievement: Achievement,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
)

class GetAchievementsUseCase @Inject constructor(
    private val repository: AchievementRepository
) {
    operator fun invoke(): Flow<List<AchievementWithStatus>> {
        return combine(
            repository.getAllAchievements(),
            repository.getUnlockedAchievements()
        ) { achievements, unlocked ->
            val unlockedMap = unlocked.associateBy { it.achievementId }
            achievements.map { achievement ->
                val userAchievement = unlockedMap[achievement.id]
                AchievementWithStatus(
                    achievement = achievement,
                    isUnlocked = userAchievement != null,
                    unlockedAt = userAchievement?.unlockedAt
                )
            }
        }
    }
}