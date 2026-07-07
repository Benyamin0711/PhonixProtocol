package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class RecordRelapseUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int): Boolean {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        val newCount = day.relapseCount + 1
        val isAsh = newCount >= 2
        repository.saveDay(day.copy(relapseCount = newCount, isAsh = isAsh))
        return isAsh
    }
}
