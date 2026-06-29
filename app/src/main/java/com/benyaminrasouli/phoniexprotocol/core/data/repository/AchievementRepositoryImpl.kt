package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.AchievementDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserAchievement
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(): Flow<List<Achievement>> = dao.getAllAchievements()

    override fun getUnlockedAchievements(): Flow<List<UserAchievement>> = dao.getUnlockedAchievements()

    override suspend fun isUnlocked(achievementId: String): Boolean = dao.isAchievementUnlocked(achievementId)

    override suspend fun unlock(achievementId: String) {
        if (!dao.isAchievementUnlocked(achievementId)) {
            dao.unlockAchievement(UserAchievement(achievementId = achievementId))
        }
    }

    override suspend fun getUnlockedCount(): Int = dao.getUnlockedCount()
}