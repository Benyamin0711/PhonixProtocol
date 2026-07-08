package com.benyaminrasouli.phoenixprotocol.feature.home

import androidx.lifecycle.ViewModel
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    userRepository: UserRepository,
    statsRepository: StatsRepository
) : ViewModel() {

    val userProfile: Flow<UserProfile?> = userRepository.getProfile()
    val userStats: Flow<UserStats?> = statsRepository.getStats()
}
