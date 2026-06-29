package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createProfile(profile: UserProfile): Long
    fun getProfile(): Flow<UserProfile?>
    suspend fun getProfileOnce(): UserProfile?
}
