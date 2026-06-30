package com.benyaminrasouli.phoniexprotocol.feature.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.ActiveBoss
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.CompleteBossUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.GetActiveBossUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.SpawnWeeklyBossUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.UpdateBossProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BossViewModel @Inject constructor(
    private val getActiveBossUseCase: GetActiveBossUseCase,
    private val spawnWeeklyBossUseCase: SpawnWeeklyBossUseCase,
    private val updateBossProgressUseCase: UpdateBossProgressUseCase,
    private val completeBossUseCase: CompleteBossUseCase
) : ViewModel() {

    val activeBoss: StateFlow<ActiveBoss?> = getActiveBossUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            spawnWeeklyBossUseCase()
        }
    }

    fun updateProgress() {
        viewModelScope.launch {
            updateBossProgressUseCase()
        }
    }

    fun completeBoss(bossId: Long) {
        viewModelScope.launch {
            completeBossUseCase(bossId)
        }
    }
}
