package com.benyaminrasouli.phoniexprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: DailyChallenge): Long

    @Query("SELECT * FROM daily_challenges WHERE date = :date ORDER BY createdAt ASC")
    fun getChallengesByDate(date: String): Flow<List<DailyChallenge>>

    @Query("SELECT * FROM daily_challenges WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getChallengesByDateOnce(date: String): List<DailyChallenge>

    @Query("UPDATE daily_challenges SET current = :current WHERE id = :id")
    suspend fun updateChallengeProgress(id: Long, current: Int)

    @Query("UPDATE daily_challenges SET completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("UPDATE daily_challenges SET claimed = 1 WHERE id = :id")
    suspend fun markClaimed(id: Long)

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE date = :date AND completed = 1")
    suspend fun getCompletedCountByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE date = :date")
    suspend fun getTotalCountByDate(date: String): Int
}
