package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserAchievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getUnlockedAchievements(): Flow<List<UserAchievement>>
    suspend fun isUnlocked(achievementId: String): Boolean
    suspend fun unlock(achievementId: String)
    suspend fun getUnlockedCount(): Int
}