package com.benyaminrasouli.phoenixprotocol.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.TaskRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetFocusStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsState(
    val stats: UserStats? = null,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val achievementsUnlocked: Int = 0,
    val completionRate: Float = 0f,
    val totalFocusSeconds: Long = 0,
    val completedFocusSessions: Int = 0
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val taskRepository: TaskRepository,
    private val achievementRepository: AchievementRepository,
    private val getFocusStatsUseCase: GetFocusStatsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            statsRepository.getStats().collect { stats ->
                val total = taskRepository.getTaskCount()
                val completed = taskRepository.getCompletedTaskCount()
                val achievements = achievementRepository.getUnlockedCount()
                val focusStats = getFocusStatsUseCase()
                _state.update {
                    it.copy(
                        stats = stats,
                        totalTasks = total,
                        completedTasks = completed,
                        achievementsUnlocked = achievements,
                        completionRate = if (total > 0) completed.toFloat() / total else 0f,
                        totalFocusSeconds = focusStats.totalFocusTimeSeconds,
                        completedFocusSessions = focusStats.completedSessionCount
                    )
                }
            }
        }
    }
}
