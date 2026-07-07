package com.benyaminrasouli.phoenixprotocol.core.data.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CampaignDao
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CampaignRepositoryImpl @Inject constructor(
    private val dao: CampaignDao
) : CampaignRepository {

    override fun getCampaign(): Flow<Campaign?> = dao.getCampaign()
    override suspend fun getCampaignOnce(): Campaign? = dao.getCampaignOnce()
    override suspend fun updateCampaign(campaign: Campaign) {
        dao.insertCampaign(campaign)
    }
    override suspend fun getDay(day: Int): CampaignDay? = dao.getDay(day)
    override fun getAllDays(): Flow<List<CampaignDay>> = dao.getAllDays()
    override suspend fun saveDay(day: CampaignDay) = dao.insertDay(day)
    override fun getMissions(): Flow<List<DailyMission>> = dao.getMissions()
    override fun getPrayers(): Flow<List<Prayer>> = dao.getPrayers()
}
