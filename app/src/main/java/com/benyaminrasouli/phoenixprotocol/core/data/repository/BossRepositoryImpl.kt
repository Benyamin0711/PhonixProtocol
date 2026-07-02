package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.BossDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.BossRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BossRepositoryImpl @Inject constructor(
    private val dao: BossDao
) : BossRepository {

    override suspend fun getActiveBoss(): Boss? = dao.getActiveBoss()

    override fun getActiveBossFlow(): Flow<Boss?> = dao.getActiveBossFlow()

    override fun getAllBosses(): Flow<List<Boss>> = dao.getAllBosses()

    override fun getCompletedBosses(): Flow<List<Boss>> = dao.getCompletedBosses()

    override suspend fun insertBoss(boss: Boss): Long = dao.insertBoss(boss)

    override suspend fun updateBossStatus(bossId: Long, status: String) {
        dao.updateBossStatus(bossId, status)
    }

    override suspend fun getCompletedBossCount(): Int = dao.getCompletedBossCount()

    override suspend fun getLastBoss(): Boss? = dao.getLastBoss()
}
