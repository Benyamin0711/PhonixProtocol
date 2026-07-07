package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.ShadowLogDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShadowRepositoryImpl @Inject constructor(
    private val shadowLogDao: ShadowLogDao,
    private val userStatsDao: UserStatsDao,
    private val achievementRepository: com.benyaminrasouli.phoenixprotocol.core.domain.repository.AchievementRepository
) : ShadowRepository {

    override fun getShadowLevel(): Flow<Int> {
        return userStatsDao.getStats().map { stats ->
            stats?.shadowLevel ?: 0
        }
    }

    override fun getShadowTier(): Flow<String> {
        return getShadowLevel().map { level ->
            when {
                level < 30 -> "SAFE"
                level < 60 -> "WARNING"
                level < 90 -> "CRITICAL"
                else -> "CORRUPTED"
            }
        }
    }

    override fun getXpPenaltyPercent(): Flow<Int> {
        return getShadowLevel().map { level ->
            when {
                level < 30 -> 0
                level < 60 -> 10
                level < 90 -> 25
                else -> 50
            }
        }
    }

    override fun getRecentLogs(limit: Int): Flow<List<ShadowLog>> {
        return shadowLogDao.getRecentLogs(limit)
    }

    override suspend fun logShadowChange(action: String, amount: Int, description: String) {
        shadowLogDao.insert(
            ShadowLog(
                action = action,
                amount = amount,
                description = description
            )
        )
    }

    override suspend fun decreaseShadow(amount: Int) {
        val before = userStatsDao.getStatsOnce()?.shadowLevel ?: 0
        userStatsDao.decreaseShadowAtomic(amount)
        shadowLogDao.insert(
            ShadowLog(
                action = "SHADOW_RECOVERY",
                amount = -amount,
                description = "Shadow decreased by $amount"
            )
        )
        val after = userStatsDao.getStatsOnce()?.shadowLevel ?: 0
        if (before > 30 && after == 0) {
            achievementRepository.unlock("shadow_breaker")
        }
    }
}
