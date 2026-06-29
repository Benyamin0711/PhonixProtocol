package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserStatsDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val dao: UserStatsDao
) : StatsRepository {

    override suspend fun initStats(userId: Long) {
        dao.insertStats(UserStats(userId = userId))
    }

    override fun getStats(): Flow<UserStats?> {
        return dao.getStats()
    }

    override suspend fun getStatsOnce(): UserStats? {
        return dao.getStatsOnce()
    }

    override suspend fun addXp(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        val newXp = current.xp + amount
        val newLevel = calculateLevel(newXp)
        val newRank = com.benyaminrasouli.phoniexprotocol.core.domain.model.Rank.forLevel(newLevel)
        dao.updateStats(current.copy(
            xp = newXp,
            level = newLevel,
            rank = newRank.name
        ))
    }

    override suspend fun increasePhoenixEnergy(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            phoenixEnergy = (current.phoenixEnergy + amount).coerceAtMost(100)
        ))
    }

    override suspend fun decreasePhoenixEnergy(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            phoenixEnergy = (current.phoenixEnergy - amount).coerceAtLeast(0)
        ))
    }

    override suspend fun increaseShadowLevel(amount: Int) {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            shadowLevel = current.shadowLevel + amount
        ))
    }

    override suspend fun incrementCompletedTasks() {
        val current = dao.getStatsOnce() ?: return
        dao.updateStats(current.copy(
            completedTasks = current.completedTasks + 1
        ))
    }

    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var requiredXp = 0
        while (requiredXp <= xp) {
            level++
            requiredXp += (level * 100) + (level * level * 10)
        }
        return (level - 1).coerceAtLeast(1)
    }
}
