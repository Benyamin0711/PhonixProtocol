package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import kotlinx.coroutines.flow.Flow

interface ShadowRepository {
    fun getShadowLevel(): Flow<Int>
    fun getShadowTier(): Flow<String>
    fun getXpPenaltyPercent(): Flow<Int>
    fun getRecentLogs(limit: Int): Flow<List<ShadowLog>>
    suspend fun logShadowChange(action: String, amount: Int, description: String)
    suspend fun decreaseShadow(amount: Int)
}
