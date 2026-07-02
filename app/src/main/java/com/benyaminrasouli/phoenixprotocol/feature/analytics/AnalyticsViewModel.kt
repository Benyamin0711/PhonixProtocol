package com.benyaminrasouli.phoenixprotocol.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.CategoryCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayCount
import com.benyaminrasouli.phoenixprotocol.core.data.db.dao.DayTotal
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsState(
    val weeklyTaskCompletion: List<DayCount> = emptyList(),
    val xpTrend: List<DayTotal> = emptyList(),
    val categoryBreakdown: List<CategoryCount> = emptyList(),
    val weeklyFocusMinutes: List<DayTotal> = emptyList()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    weeklyTaskCompletion = analyticsRepository.getWeeklyTaskCompletion(),
                    xpTrend = analyticsRepository.getXpTrend(),
                    categoryBreakdown = analyticsRepository.getCategoryBreakdown(),
                    weeklyFocusMinutes = analyticsRepository.getWeeklyFocusMinutes()
                )
            }
        }
    }
}
