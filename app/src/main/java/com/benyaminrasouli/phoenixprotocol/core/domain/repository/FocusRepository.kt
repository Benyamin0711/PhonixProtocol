package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    suspend fun insertSession(session: FocusSession): Long
    suspend fun updateSession(session: FocusSession)
    fun getAllSessions(): Flow<List<FocusSession>>
    suspend fun getSessionById(sessionId: Long): FocusSession?
    suspend fun getTotalFocusTimeSeconds(): Long
    suspend fun getCompletedSessionCount(): Int
}
