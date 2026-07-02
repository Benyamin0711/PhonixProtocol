package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Boss
import kotlinx.coroutines.flow.Flow

interface BossRepository {
    suspend fun getActiveBoss(): Boss?
    fun getActiveBossFlow(): Flow<Boss?>
    fun getAllBosses(): Flow<List<Boss>>
    fun getCompletedBosses(): Flow<List<Boss>>
    suspend fun insertBoss(boss: Boss): Long
    suspend fun updateBossStatus(bossId: Long, status: String)
    suspend fun getCompletedBossCount(): Int
    suspend fun getLastBoss(): Boss?
}
