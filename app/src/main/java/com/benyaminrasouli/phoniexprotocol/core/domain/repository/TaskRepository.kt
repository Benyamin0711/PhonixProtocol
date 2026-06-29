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
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun completeTask(taskId: Long)
    suspend fun skipTask(taskId: Long)
}
