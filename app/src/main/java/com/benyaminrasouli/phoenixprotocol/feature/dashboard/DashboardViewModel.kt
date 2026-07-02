package com.benyaminrasouli.phoenixprotocol.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.model.Rank
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CompleteTaskUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetProfileUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetStatsUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetSloganUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetTasksUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetDailyChallengesUseCase
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val slogan: String = "",
    val profile: UserProfile? = null,
    val stats: UserStats? = null,
    val activeTasks: List<Task> = emptyList(),
    val challenges: List<DailyChallenge> = emptyList(),
    val rank: Rank = Rank.INITIATE
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getStatsUseCase: GetStatsUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val getSloganUseCase: GetSloganUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val getDailyChallengesUseCase: GetDailyChallengesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(slogan = getSloganUseCase()) }
        }

        viewModelScope.launch {
            combine(
                getProfileUseCase(),
                getStatsUseCase(),
                getTasksUseCase()
            ) { profile, stats, tasks ->
                DashboardState(
                    slogan = _state.value.slogan,
                    profile = profile,
                    stats = stats,
                    activeTasks = tasks,
                    challenges = _state.value.challenges,
                    rank = Rank.forLevel(stats?.level ?: 1)
                )
            }.collect { newState ->
                _state.update { newState }
            }
        }

        viewModelScope.launch {
            getDailyChallengesUseCase().collect { challenges ->
                _state.update { it.copy(challenges = challenges) }
            }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            completeTaskUseCase(task)
        }
    }
}
