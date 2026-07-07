package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.ShadowLogDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val dao: UserStatsDao,
    private val shadowLogDao: ShadowLogDao
) : StatsRepository {

    override suspend fun initStats(userId: Long) {
        val existing = dao.getStatsOnce()
        if (existing == null) {
            dao.insertStats(UserStats(userId = userId))
        }
    }

    override fun getStats(): Flow<UserStats?> {
        return dao.getStats()
    }

    override suspend fun getStatsOnce(): UserStats? {
        return dao.getStatsOnce()
    }

    override suspend fun addXp(amount: Int) {
        val current = dao.getStatsOnce() ?: return

        val penaltyPercent = when {
            current.shadowLevel < 30 -> 0
            current.shadowLevel < 60 -> 10
            current.shadowLevel < 90 -> 25
            else -> 50
        }
        val actualXp = amount * (100 - penaltyPercent) / 100
        if (actualXp <= 0) return

        dao.addXpAtomic(actualXp)

        val fresh = dao.getStatsOnce() ?: return
        val newLevel = calculateLevel(fresh.xp)
        val newRank = com.benyaminrasouli.phoenixprotocol.core.domain.model.Rank.forLevel(newLevel)
        if (fresh.level != newLevel || fresh.rank != newRank.name) {
            dao.updateStats(fresh.copy(level = newLevel, rank = newRank.name))
        }
    }

    override suspend fun increasePhoenixEnergy(amount: Int) {
        dao.increaseEnergyAtomic(amount)
    }

    override suspend fun decreasePhoenixEnergy(amount: Int) {
        dao.decreaseEnergyAtomic(amount)
    }

    override suspend fun increaseShadowLevel(amount: Int) {
        dao.increaseShadowAtomic(amount)
        val action = when (amount) {
            1 -> "SKIP"
            2 -> "STREAK_BREAK"
            else -> "SHADOW_DAMAGE"
        }
        val description = when (action) {
            "SKIP" -> "Task skipped"
            "STREAK_BREAK" -> "Streak broken"
            else -> "Shadow damage: -$amount"
        }
        shadowLogDao.insert(
            ShadowLog(
                action = action,
                amount = amount,
                description = description
            )
        )
    }

    override suspend fun decreaseShadowLevel(amount: Int) {
        dao.decreaseShadowAtomic(amount)
    }

    override suspend fun incrementCompletedTasks() {
        dao.incrementCompletedTasksAtomic()
    }

    override suspend fun clearStats() {
        dao.deleteAllStats()
    }

    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var requiredXp = 0
        while (requiredXp <= xp && level < 1000) {
            level++
            requiredXp += (level * 100) + (level * level * 10)
        }
        return (level - 1).coerceAtLeast(1)
    }
}
