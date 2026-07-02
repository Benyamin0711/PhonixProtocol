package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Boss
import kotlinx.coroutines.flow.Flow

@Dao
interface BossDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoss(boss: Boss): Long

    @Query("SELECT * FROM bosses WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveBoss(): Boss?

    @Query("SELECT * FROM bosses WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveBossFlow(): Flow<Boss?>

    @Query("SELECT * FROM bosses ORDER BY createdAt DESC")
    fun getAllBosses(): Flow<List<Boss>>

    @Query("SELECT * FROM bosses WHERE status = 'COMPLETED' ORDER BY createdAt DESC")
    fun getCompletedBosses(): Flow<List<Boss>>

    @Query("UPDATE bosses SET status = :status WHERE id = :bossId")
    suspend fun updateBossStatus(bossId: Long, status: String)

    @Query("SELECT COUNT(*) FROM bosses WHERE status = 'COMPLETED'")
    suspend fun getCompletedBossCount(): Int

    @Query("SELECT * FROM bosses ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastBoss(): Boss?
}
