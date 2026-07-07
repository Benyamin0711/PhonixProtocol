package com.benyaminrasouli.phoenixprotocol.core.domain.repository

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    fun getCampaign(): Flow<Campaign?>
    suspend fun getCampaignOnce(): Campaign?
    suspend fun updateCampaign(campaign: Campaign)
    suspend fun getDay(day: Int): CampaignDay?
    fun getAllDays(): Flow<List<CampaignDay>>
    suspend fun saveDay(day: CampaignDay)
    fun getMissions(): Flow<List<DailyMission>>
    fun getPrayers(): Flow<List<Prayer>>
}
