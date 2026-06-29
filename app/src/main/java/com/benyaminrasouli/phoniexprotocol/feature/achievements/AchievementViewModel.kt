package com.benyaminrasouli.phoniexprotocol.feature.achievements

import androidx.lifecycle.ViewModel
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.AchievementWithStatus
import com.benyaminrasouli.phoniexprotocol.core.domain.usecase.GetAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    val achievements: Flow<List<AchievementWithStatus>> = getAchievementsUseCase()
}
