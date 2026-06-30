package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    suspend fun initStats(userId: Long)
    fun getStats(): Flow<UserStats?>
    suspend fun getStatsOnce(): UserStats?
    suspend fun addXp(amount: Int)
    suspend fun increasePhoenixEnergy(amount: Int)
    suspend fun decreasePhoenixEnergy(amount: Int)
    suspend fun increaseShadowLevel(amount: Int)
    suspend fun incrementCompletedTasks()
    suspend fun clearStats()
}
