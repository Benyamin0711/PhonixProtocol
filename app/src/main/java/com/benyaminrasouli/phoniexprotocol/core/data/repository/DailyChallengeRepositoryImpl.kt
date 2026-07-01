package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.DailyChallengeDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.DailyChallengeRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class DailyChallengeRepositoryImpl @Inject constructor(
    private val dao: DailyChallengeDao
) : DailyChallengeRepository {

    private fun todayDate(): String = LocalDate.now().toString()

    override fun getTodayChallenges(): Flow<List<DailyChallenge>> {
        return dao.getChallengesByDate(todayDate())
    }

    override suspend fun getTodayChallengesOnce(): List<DailyChallenge> {
        return dao.getChallengesByDateOnce(todayDate())
    }

    override suspend fun insertChallenge(challenge: DailyChallenge): Long {
        return dao.insertChallenge(challenge)
    }

    override suspend fun updateProgress(id: Long, current: Int) {
        dao.updateChallengeProgress(id, current)
    }

    override suspend fun markCompleted(id: Long) {
        dao.markCompleted(id)
    }

    override suspend fun markClaimed(id: Long) {
        dao.markClaimed(id)
    }

    override suspend fun getCompletedCountToday(): Int {
        return dao.getCompletedCountByDate(todayDate())
    }

    override suspend fun getTotalCountToday(): Int {
        return dao.getTotalCountByDate(todayDate())
    }
}
