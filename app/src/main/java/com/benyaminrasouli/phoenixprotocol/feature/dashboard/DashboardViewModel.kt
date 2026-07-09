package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign.*
import com.benyaminrasouli.phoenixprotocol.core.util.parseCsvIndices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class DashboardState(
    val currentDay: Int = 1,
    val totalXp: Int = 0,
    val level: Int = 1,
    val rank: String = "D — Recovering",
    val xpProgress: Float = 0f,
    val discipline: Int = 0,
    val isAsh: Boolean = false,
    val currentDayData: CampaignDay? = null,
    val allDays: List<CampaignDay> = emptyList(),
    val dayActivityMap: Map<Int, Boolean> = emptyMap(),
    val missions: List<CampaignItem> = emptyList(),
    val prayers: List<CampaignItem> = emptyList(),
    val note: String = ""
)

@Immutable
data class CampaignItem(
    val name: String,
    val xp: Int,
    val isCompleted: Boolean
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCampaignStateUseCase: GetCampaignStateUseCase,
    private val toggleMissionUseCase: ToggleMissionUseCase,
    private val togglePrayerUseCase: TogglePrayerUseCase,
    private val saveDailyNoteUseCase: SaveDailyNoteUseCase,
    private val navigateDayUseCase: NavigateDayUseCase,
    private val recordRelapseUseCase: RecordRelapseUseCase,
    private val recoverFromAshUseCase: RecoverFromAshUseCase,
    private val resetDayUseCase: ResetDayUseCase
) : ViewModel() {

    val state: StateFlow<DashboardState> = getCampaignStateUseCase()
        .map { campaignState ->
            DashboardState(
                currentDay = campaignState.campaign?.currentDay ?: 1,
                totalXp = campaignState.totalXp,
                level = campaignState.level,
                rank = campaignState.rank,
                xpProgress = campaignState.xpProgress,
                discipline = campaignState.discipline,
                isAsh = campaignState.isAsh,
                currentDayData = campaignState.currentDay,
                allDays = campaignState.allDays,
                dayActivityMap = campaignState.allDays.associate { 
                    it.dayNumber to (it.completedMissions.isNotBlank() || 
                                   it.completedPrayers.isNotBlank() || 
                                   it.note.isNotBlank() || 
                                   it.relapseCount > 0)
                },
                missions = campaignState.missions.mapIndexed { index, m ->
                    CampaignItem(
                        name = m.name,
                        xp = m.xp,
                        isCompleted = campaignState.currentDay?.completedMissions
                            ?.parseCsvIndices()
                            ?.contains(index) == true
                    )
                },
                prayers = campaignState.prayers.mapIndexed { index, p ->
                    CampaignItem(
                        name = p.name,
                        xp = p.xp,
                        isCompleted = campaignState.currentDay?.completedPrayers
                            ?.parseCsvIndices()
                            ?.contains(index) == true
                    )
                },
                note = campaignState.currentDay?.note ?: ""
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState()
        )

    private var saveNoteJob: Job? = null

    fun toggleMission(index: Int) {
        viewModelScope.launch {
            toggleMissionUseCase(state.value.currentDay, index)
        }
    }

    fun togglePrayer(index: Int) {
        viewModelScope.launch {
            togglePrayerUseCase(state.value.currentDay, index)
        }
    }

    fun saveNote(note: String) {
        saveNoteJob?.cancel()
        saveNoteJob = viewModelScope.launch {
            delay(500)
            saveDailyNoteUseCase(state.value.currentDay, note)
        }
    }

    fun nextDay() {
        viewModelScope.launch {
            val next = state.value.currentDay + 1
            if (next <= 60) navigateDayUseCase(next)
        }
    }

    fun prevDay() {
        viewModelScope.launch {
            val prev = state.value.currentDay - 1
            if (prev >= 1) navigateDayUseCase(prev)
        }
    }

    fun goToDay(day: Int) {
        viewModelScope.launch {
            navigateDayUseCase(day)
        }
    }

    fun recordRelapse() {
        viewModelScope.launch {
            recordRelapseUseCase(state.value.currentDay)
        }
    }

    fun recover() {
        viewModelScope.launch {
            recoverFromAshUseCase(state.value.currentDay)
        }
    }

    fun resetDay() {
        viewModelScope.launch {
            resetDayUseCase(state.value.currentDay)
        }
    }
}
