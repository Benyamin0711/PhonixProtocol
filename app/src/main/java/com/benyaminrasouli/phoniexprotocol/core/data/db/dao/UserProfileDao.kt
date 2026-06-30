package com.benyaminrasouli.phoniexprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getProfileOnce(): UserProfile?

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAllProfiles()
}
