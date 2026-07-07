package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class TogglePrayerUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int, prayerIndex: Int) {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        val current = day.completedPrayers.split(",").filter { it.isNotBlank() }.map { it.toInt() }
        val newPrayers = if (prayerIndex in current) {
            current.filter { it != prayerIndex }
        } else {
            current + prayerIndex
        }
        repository.saveDay(day.copy(completedPrayers = newPrayers.joinToString(",")))
    }
}
