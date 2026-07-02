package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.FocusSessionDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.FocusSession
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FocusRepositoryImpl @Inject constructor(
    private val dao: FocusSessionDao
) : FocusRepository {

    override suspend fun insertSession(session: FocusSession): Long {
        return dao.insertSession(session)
    }

    override suspend fun updateSession(session: FocusSession) {
        dao.updateSession(session)
    }

    override fun getAllSessions(): Flow<List<FocusSession>> {
        return dao.getAllSessions()
    }

    override suspend fun getSessionById(sessionId: Long): FocusSession? {
        return dao.getSessionById(sessionId)
    }

    override suspend fun getTotalFocusTimeSeconds(): Int {
        return dao.getTotalFocusTimeSeconds()
    }

    override suspend fun getCompletedSessionCount(): Int {
        return dao.getCompletedSessionCount()
    }
}
