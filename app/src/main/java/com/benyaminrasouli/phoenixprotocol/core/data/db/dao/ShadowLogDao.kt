package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.ShadowLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ShadowLogDao {
    @Insert
    suspend fun insert(log: ShadowLog)

    @Query("SELECT * FROM shadow_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 20): Flow<List<ShadowLog>>

    @Query("SELECT * FROM shadow_log")
    suspend fun getAllLogsOnce(): List<ShadowLog>

    @Query("DELETE FROM shadow_log")
    suspend fun deleteAll()
}
