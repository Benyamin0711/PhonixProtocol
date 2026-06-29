package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.UserProfileDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : UserRepository {

    override suspend fun createProfile(profile: UserProfile): Long {
        return dao.insertProfile(profile)
    }

    override fun getProfile(): Flow<UserProfile?> {
        return dao.getProfile()
    }

    override suspend fun getProfileOnce(): UserProfile? {
        return dao.getProfileOnce()
    }
}
