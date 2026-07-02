package com.benyaminrasouli.phoenixprotocol.core.domain.usecase

import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoenixprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoenixprotocol.core.domain.repository.UserRepository
import javax.inject.Inject

class CreateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) {
    suspend operator fun invoke(profile: UserProfile) {
        val userId = userRepository.createProfile(profile)
        statsRepository.initStats(userId)
        settingsDataStore.setOnboardingComplete(true)
    }
}
