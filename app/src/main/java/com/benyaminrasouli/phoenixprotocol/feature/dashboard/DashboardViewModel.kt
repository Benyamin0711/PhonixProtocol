package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.CampaignDay
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.campaign.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val missions: List<MissionItem> = emptyList(),
    val prayers: List<PrayerItem> = emptyList(),
    val note: String = ""
)

data class MissionItem(
    val name: String,
    val xp: Int,
    val isCompleted: Boolean
)

data class PrayerItem(
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

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var saveNoteJob: Job? = null

    init {
        viewModelScope.launch {
            getCampaignStateUseCase().collect { campaignState ->
                _state.value = DashboardState(
                    currentDay = campaignState.campaign?.currentDay ?: 1,
                    totalXp = campaignState.totalXp,
                    level = campaignState.level,
                    rank = campaignState.rank,
                    xpProgress = campaignState.xpProgress,
                    discipline = campaignState.discipline,
                    isAsh = campaignState.isAsh,
                    currentDayData = campaignState.currentDay,
                    allDays = campaignState.allDays,
                    missions = campaignState.missions.mapIndexed { index, m ->
                        MissionItem(
                            name = m.name,
                            xp = m.xp,
                            isCompleted = campaignState.currentDay?.completedMissions
                                ?.split(",")
                                ?.filter { it.isNotBlank() }
                                ?.map { it.toInt() }
                                ?.contains(index) == true
                        )
                    },
                    prayers = campaignState.prayers.mapIndexed { index, p ->
                        PrayerItem(
                            name = p.name,
                            xp = p.xp,
                            isCompleted = campaignState.currentDay?.completedPrayers
                                ?.split(",")
                                ?.filter { it.isNotBlank() }
                                ?.map { it.toInt() }
                                ?.contains(index) == true
                        )
                    },
                    note = campaignState.currentDay?.note ?: ""
                )
            }
        }
    }

    fun toggleMission(index: Int) {
        viewModelScope.launch {
            toggleMissionUseCase(_state.value.currentDay, index)
        }
    }

    fun togglePrayer(index: Int) {
        viewModelScope.launch {
            togglePrayerUseCase(_state.value.currentDay, index)
        }
    }

    fun saveNote(note: String) {
        saveNoteJob?.cancel()
        saveNoteJob = viewModelScope.launch {
            delay(500)
            saveDailyNoteUseCase(_state.value.currentDay, note)
        }
    }

    fun nextDay() {
        viewModelScope.launch {
            val next = _state.value.currentDay + 1
            if (next <= 60) navigateDayUseCase(next)
        }
    }

    fun prevDay() {
        viewModelScope.launch {
            val prev = _state.value.currentDay - 1
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
            recordRelapseUseCase(_state.value.currentDay)
        }
    }

    fun recover() {
        viewModelScope.launch {
            recoverFromAshUseCase(_state.value.currentDay)
        }
    }

    fun resetDay() {
        viewModelScope.launch {
            resetDayUseCase(_state.value.currentDay)
        }
    }
}
