package com.benyaminrasouli.phoenixprotocol.feature.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.ClaimChallengeRewardUseCase
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GenerateDailyChallengesUseCase
import android.util.Log
import com.benyaminrasouli.phoenixprotocol.core.domain.usecase.GetDailyChallengesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    private val getDailyChallengesUseCase: GetDailyChallengesUseCase,
    private val claimChallengeRewardUseCase: ClaimChallengeRewardUseCase,
    private val generateDailyChallengesUseCase: GenerateDailyChallengesUseCase
) : ViewModel() {

    private val _challenges = MutableStateFlow<List<DailyChallenge>>(emptyList())
    val challenges: StateFlow<List<DailyChallenge>> = _challenges.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                generateDailyChallengesUseCase()
            } catch (e: Exception) {
                Log.e("DailyChallengeVM", "Error generating challenges", e)
            }
            try {
                getDailyChallengesUseCase().collect { _challenges.value = it }
            } catch (e: Exception) {
                Log.e("DailyChallengeVM", "Error loading challenges", e)
            }
        }
    }

    fun claimReward(challenge: DailyChallenge) {
        viewModelScope.launch {
            try {
                claimChallengeRewardUseCase(challenge)
            } catch (e: Exception) {
                Log.e("DailyChallengeVM", "Error claiming reward", e)
            }
        }
    }
}
