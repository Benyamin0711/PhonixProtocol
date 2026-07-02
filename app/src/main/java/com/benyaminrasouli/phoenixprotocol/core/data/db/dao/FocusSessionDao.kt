package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): FocusSession?

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM focus_sessions WHERE completed = 1")
    suspend fun getTotalFocusTimeSeconds(): Int

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1")
    suspend fun getCompletedSessionCount(): Int

    @Query("SELECT DATE(startedAt/1000, 'unixepoch', 'localtime') as day, SUM(durationSeconds)/60 as total FROM focus_sessions WHERE completed = 1 AND startedAt > :since GROUP BY day ORDER BY day")
    suspend fun getFocusMinutesByDay(since: Long): List<DayTotal>

    @Query("SELECT * FROM focus_sessions")
    suspend fun getAllSessionsOnce(): List<FocusSession>

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()
}
