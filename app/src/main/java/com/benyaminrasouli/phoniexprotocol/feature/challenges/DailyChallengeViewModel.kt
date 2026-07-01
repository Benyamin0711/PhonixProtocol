package com.benyaminrasouli.phoniexprotocol.feature.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.ClaimChallengeRewardUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.GenerateDailyChallengesUseCase
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.GetDailyChallengesUseCase
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
            generateDailyChallengesUseCase()
            getDailyChallengesUseCase().collect { _challenges.value = it }
        }
    }

    fun claimReward(challenge: DailyChallenge) {
        viewModelScope.launch {
            claimChallengeRewardUseCase(challenge)
        }
    }
}
