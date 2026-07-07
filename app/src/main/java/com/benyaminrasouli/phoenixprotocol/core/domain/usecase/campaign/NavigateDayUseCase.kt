package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class NavigateDayUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(newDay: Int): Int {
        val campaign = repository.getCampaignOnce() ?: return 1
        val clampedDay = newDay.coerceIn(1, 60)
        repository.updateCampaign(campaign.copy(currentDay = clampedDay))
        return clampedDay
    }
}
