package com.benyaminrasouli.phoenixprotocol.feature.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.ActiveBoss
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.CompleteBossUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetActiveBossUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.SpawnWeeklyBossUseCase
import android.util.Log
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.UpdateBossProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
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
            while (true) {
                delay(60_000)
                try {
                    updateBossProgressUseCase()
                    spawnWeeklyBossUseCase()
                } catch (e: Exception) {
                    Log.e("BossViewModel", "Error updating boss progress", e)
                }
            }
        }
    }

    fun updateProgress() {
        viewModelScope.launch {
            updateBossProgressUseCase()
        }
    }

    fun completeBoss(bossId: Long) {
        viewModelScope.launch {
            try {
                completeBossUseCase(bossId)
            } catch (e: Exception) {
                Log.e("BossViewModel", "Error completing boss", e)
            }
        }
    }
}
