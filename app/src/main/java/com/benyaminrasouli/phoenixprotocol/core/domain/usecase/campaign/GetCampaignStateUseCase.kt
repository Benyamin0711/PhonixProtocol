package com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Campaign
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyMission
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Prayer
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class CampaignState(
    val campaign: Campaign?,
    val currentDay: CampaignDay?,
    val allDays: List<CampaignDay>,
    val missions: List<DailyMission>,
    val prayers: List<Prayer>,
    val totalXp: Int = 0,
    val level: Int = 1,
    val rank: String = "D — Recovering",
    val xpProgress: Float = 0f,
    val discipline: Int = 0,
    val isAsh: Boolean = false
)

class GetCampaignStateUseCase @Inject constructor(
    private val repository: CampaignRepository
) {
    operator fun invoke(): Flow<CampaignState> {
        return combine(
            repository.getCampaign(),
            repository.getAllDays(),
            repository.getMissions(),
            repository.getPrayers()
        ) { campaign, allDays, missions, prayers ->
            val currentDayNumber = campaign?.currentDay ?: 1
            val currentDay = allDays.find { it.dayNumber == currentDayNumber }

            val totalXp = calculateTotalXp(allDays, missions, prayers)
            val level = calculateLevel(totalXp)
            val rank = getRank(totalXp)
            val xpProgress = (totalXp % 120).toFloat() / 120f
            val discipline = calculateDiscipline(currentDay, missions, prayers)
            val isAsh = currentDay?.isAsh == true

            CampaignState(
                campaign = campaign,
                currentDay = currentDay,
                allDays = allDays,
                missions = missions,
                prayers = prayers,
                totalXp = totalXp,
                level = level,
                rank = rank,
                xpProgress = xpProgress,
                discipline = discipline,
                isAsh = isAsh
            )
        }
    }

    private fun calculateTotalXp(
        allDays: List<CampaignDay>,
        missions: List<DailyMission>,
        prayers: List<Prayer>
    ): Int {
        var total = 0
        allDays.forEach { day ->
            val completedMissions = day.completedMissions.split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }
            completedMissions.forEach { index ->
                if (index in missions.indices) {
                    total += missions[index].xp
                }
            }
            val completedPrayers = day.completedPrayers.split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }
            completedPrayers.forEach { index ->
                if (index in prayers.indices) {
                    total += prayers[index].xp
                }
            }
        }
        return total
    }

    private fun calculateLevel(xp: Int): Int = xp / 120 + 1

    private fun getRank(xp: Int): String = when {
        xp < 150 -> "D — Recovering"
        xp < 450 -> "C — Rising"
        xp < 900 -> "B — Forged"
        xp < 1500 -> "A — Dangerous"
        else -> "S — Phoenix"
    }

    private fun calculateDiscipline(
        day: CampaignDay?,
        missions: List<DailyMission>,
        prayers: List<Prayer>
    ): Int {
        if (day == null) return 0
        val done = day.completedMissions.split(",").filter { it.isNotBlank() }.size +
                   day.completedPrayers.split(",").filter { it.isNotBlank() }.size
        val all = missions.size + prayers.size
        return if (all > 0) (done * 100 / all) else 0
    }
}
