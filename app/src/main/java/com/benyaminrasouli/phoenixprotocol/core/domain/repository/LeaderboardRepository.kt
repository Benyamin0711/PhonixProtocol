package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.domain.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getAllTimeLeaderboard(): Flow<List<LeaderboardEntry>>
    fun getWeeklyLeaderboard(): Flow<List<LeaderboardEntry>>
}
