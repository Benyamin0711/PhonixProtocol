package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Achievement
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(userAchievement: UserAchievement)

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM user_achievements")
    fun getUnlockedAchievements(): Flow<List<UserAchievement>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_achievements WHERE achievementId = :achievementId)")
    suspend fun isAchievementUnlocked(achievementId: String): Boolean

    @Query("SELECT COUNT(*) FROM user_achievements")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievementsOnce(): List<Achievement>

    @Query("SELECT * FROM user_achievements")
    suspend fun getAllUnlockedOnce(): List<UserAchievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievement(userAchievement: UserAchievement)

    @Query("DELETE FROM achievements")
    suspend fun deleteAll()

    @Query("DELETE FROM user_achievements")
    suspend fun deleteAllUserAchievements()
}
