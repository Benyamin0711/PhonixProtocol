package com.benyaminrasouli.phoniexprotocol.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.AchievementRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
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
    val completionRate: Float = 0f
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val taskRepository: TaskRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            statsRepository.getStats().collect { stats ->
                val total = taskRepository.getTaskCount()
                val completed = taskRepository.getCompletedTaskCount()
                val achievements = achievementRepository.getUnlockedCount()
                _state.update {
                    it.copy(
                        stats = stats,
                        totalTasks = total,
                        completedTasks = completed,
                        achievementsUnlocked = achievements,
                        completionRate = if (total > 0) completed.toFloat() / total else 0f
                    )
                }
            }
        }
    }
}
