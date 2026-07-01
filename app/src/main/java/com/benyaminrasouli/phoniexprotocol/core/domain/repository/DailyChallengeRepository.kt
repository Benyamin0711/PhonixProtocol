package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import kotlinx.coroutines.flow.Flow

interface DailyChallengeRepository {
    fun getTodayChallenges(): Flow<List<DailyChallenge>>
    suspend fun getTodayChallengesOnce(): List<DailyChallenge>
    suspend fun insertChallenge(challenge: DailyChallenge): Long
    suspend fun updateProgress(id: Long, current: Int)
    suspend fun markCompleted(id: Long)
    suspend fun markClaimed(id: Long)
    suspend fun getCompletedCountToday(): Int
    suspend fun getTotalCountToday(): Int
}
