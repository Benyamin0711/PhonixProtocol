package com.benyaminrasouli.phoniexprotocol.core.domain.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun getActiveTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByType(type: String): Flow<List<Task>>
    fun getPriorityTasks(): Flow<List<Task>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun completeTask(taskId: Long)
    suspend fun skipTask(taskId: Long)
    suspend fun getTaskCount(): Int
    suspend fun getCompletedTaskCount(): Int
    suspend fun getCompletedPriorityTaskCount(): Int
    suspend fun getCompletedHardTaskCount(): Int
    suspend fun getCompletedTaskCountSince(sinceTimestamp: Long): Int
    suspend fun getCompletedPriorityTaskCountSince(sinceTimestamp: Long): Int
    suspend fun getCompletedHardTaskCountSince(sinceTimestamp: Long): Int
}
