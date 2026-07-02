package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    suspend fun insertSession(session: FocusSession): Long
    suspend fun updateSession(session: FocusSession)
    fun getAllSessions(): Flow<List<FocusSession>>
    suspend fun getSessionById(sessionId: Long): FocusSession?
    suspend fun getTotalFocusTimeSeconds(): Int
    suspend fun getCompletedSessionCount(): Int
}
