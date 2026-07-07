package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: UserStats)

    @Update
    suspend fun updateStats(stats: UserStats)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getStats(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStatsOnce(): UserStats?

    @Query("DELETE FROM user_stats")
    suspend fun deleteAllStats()

    @Query("UPDATE user_stats SET xp = xp + :amount WHERE id = 1")
    suspend fun addXpAtomic(amount: Int)

    @Query("UPDATE user_stats SET phoenixEnergy = MIN(phoenixEnergy + :amount, 100) WHERE id = 1")
    suspend fun increaseEnergyAtomic(amount: Int)

    @Query("UPDATE user_stats SET phoenixEnergy = MAX(phoenixEnergy - :amount, 0) WHERE id = 1")
    suspend fun decreaseEnergyAtomic(amount: Int)

    @Query("UPDATE user_stats SET shadowLevel = shadowLevel + :amount WHERE id = 1")
    suspend fun increaseShadowAtomic(amount: Int)

    @Query("UPDATE user_stats SET shadowLevel = MAX(shadowLevel - :amount, 0) WHERE id = 1")
    suspend fun decreaseShadowAtomic(amount: Int)

    @Query("UPDATE user_stats SET completedTasks = completedTasks + 1 WHERE id = 1")
    suspend fun incrementCompletedTasksAtomic()
}
