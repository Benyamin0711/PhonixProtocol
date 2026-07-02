package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createProfile(profile: UserProfile): Long
    suspend fun updateProfile(profile: UserProfile)
    fun getProfile(): Flow<UserProfile?>
    suspend fun getProfileOnce(): UserProfile?
    suspend fun clearProfile()
}
