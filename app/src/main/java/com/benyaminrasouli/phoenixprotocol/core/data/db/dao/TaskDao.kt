package com.benyaminrasouli.phoenixprotocol.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benyaminrasouli.phoenixprotocol.core.data.db.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE taskType = :type AND status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getTasksByType(type: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isPriority = 1 AND status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getPriorityTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED'")
    suspend fun getCompletedTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND isPriority = 1")
    suspend fun getCompletedPriorityTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND (difficulty = 'HARD' OR difficulty = 'EXTREME')")
    suspend fun getCompletedHardTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND completedAt >= :sinceTimestamp")
    suspend fun getCompletedTaskCountSince(sinceTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND isPriority = 1 AND completedAt >= :sinceTimestamp")
    suspend fun getCompletedPriorityTaskCountSince(sinceTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND (difficulty = 'HARD' OR difficulty = 'EXTREME') AND completedAt >= :sinceTimestamp")
    suspend fun getCompletedHardTaskCountSince(sinceTimestamp: Long): Int
}
