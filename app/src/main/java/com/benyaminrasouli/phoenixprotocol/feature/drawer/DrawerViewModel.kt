package com.benyaminrasouli.phoenixprotocol.feature.drawer

import androidx.lifecycle.ViewModel
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    userRepository: UserRepository,
    statsRepository: StatsRepository
) : ViewModel() {

    val profile: Flow<UserProfile?> = userRepository.getProfile()
    val stats: Flow<UserStats?> = statsRepository.getStats()
}
