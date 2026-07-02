package com.benyaminrasouli.phoenixprotocol.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.domain.model.LeaderboardEntry
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class LeaderboardTab { ALL_TIME, WEEKLY }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LeaderboardTab.ALL_TIME)
    val selectedTab: StateFlow<LeaderboardTab> = _selectedTab

    val leaderboard: StateFlow<List<LeaderboardEntry>> = _selectedTab
        .flatMapLatest { tab ->
            when (tab) {
                LeaderboardTab.ALL_TIME -> leaderboardRepository.getAllTimeLeaderboard()
                LeaderboardTab.WEEKLY -> leaderboardRepository.getWeeklyLeaderboard()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: LeaderboardTab) {
        _selectedTab.value = tab
    }
}
