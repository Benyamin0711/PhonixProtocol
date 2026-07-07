package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import javax.inject.Inject

class ToggleMissionUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    suspend operator fun invoke(dayNumber: Int, missionIndex: Int) {
        val day = repository.getDay(dayNumber) ?: CampaignDay(dayNumber = dayNumber)
        val current = day.completedMissions.split(",").filter { it.isNotBlank() }.map { it.toInt() }
        val newMissions = if (missionIndex in current) {
            current.filter { it != missionIndex }
        } else {
            current + missionIndex
        }
        repository.saveDay(day.copy(completedMissions = newMissions.joinToString(",")))
    }
}
