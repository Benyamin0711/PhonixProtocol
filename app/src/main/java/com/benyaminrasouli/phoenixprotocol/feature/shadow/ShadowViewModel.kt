package com.benyaminrasouli.phoenixprotocol.feature.shadow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.ShadowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShadowViewModel @Inject constructor(
    shadowRepository: ShadowRepository
) : ViewModel() {

    val shadowLevel: StateFlow<Int> = shadowRepository.getShadowLevel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val shadowTier: StateFlow<String> = shadowRepository.getShadowTier()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SAFE")

    val xpPenalty: StateFlow<Int> = shadowRepository.getXpPenaltyPercent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentLogs: StateFlow<List<ShadowLog>> = shadowRepository.getRecentLogs(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
