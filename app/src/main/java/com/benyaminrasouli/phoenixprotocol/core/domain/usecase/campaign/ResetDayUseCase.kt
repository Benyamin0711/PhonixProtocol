package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class ResetDayUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int) {
        repository.saveDay(CampaignDay(dayNumber = dayNumber))
    }
}
