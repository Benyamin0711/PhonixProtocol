package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoniexprotocol.core.data.datastore.SettingsDataStore
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.UserRepository
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
