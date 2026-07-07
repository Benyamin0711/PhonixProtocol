package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class RecoverFromAshUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int) {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        repository.saveDay(day.copy(isAsh = false, relapseCount = 0))
    }
}
