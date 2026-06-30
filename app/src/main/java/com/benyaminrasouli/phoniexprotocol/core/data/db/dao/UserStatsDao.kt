package com.benyaminrasouli.phoniexprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.UserStats
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
}
