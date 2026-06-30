package com.benyaminrasouli.phoniexprotocol.core.data.repository

import com.benyaminrasouli.phoniexprotocol.core.data.db.dao.TaskDao
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override suspend fun createTask(task: Task): Long {
        return dao.insertTask(task)
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
    }

    override fun getActiveTasks(): Flow<List<Task>> {
        return dao.getActiveTasks()
    }

    override fun getCompletedTasks(): Flow<List<Task>> {
        return dao.getCompletedTasks()
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks()
    }

    override fun getTasksByType(type: String): Flow<List<Task>> {
        return dao.getTasksByType(type)
    }

    override fun getPriorityTasks(): Flow<List<Task>> {
        return dao.getPriorityTasks()
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        return dao.getTaskById(taskId)
    }

    override suspend fun completeTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "COMPLETED", System.currentTimeMillis())
    }

    override suspend fun skipTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "SKIPPED", null)
    }

    override suspend fun getTaskCount(): Int {
        return dao.getTaskCount()
    }

    override suspend fun getCompletedTaskCount(): Int {
        return dao.getCompletedTaskCount()
    }

    override suspend fun getCompletedPriorityTaskCount(): Int {
        return dao.getCompletedPriorityTaskCount()
    }

    override suspend fun getCompletedHardTaskCount(): Int {
        return dao.getCompletedHardTaskCount()
    }

    override suspend fun getCompletedTaskCountSince(sinceTimestamp: Long): Int {
        return dao.getCompletedTaskCountSince(sinceTimestamp)
    }

    override suspend fun getCompletedPriorityTaskCountSince(sinceTimestamp: Long): Int {
        return dao.getCompletedPriorityTaskCountSince(sinceTimestamp)
    }

    override suspend fun getCompletedHardTaskCountSince(sinceTimestamp: Long): Int {
        return dao.getCompletedHardTaskCountSince(sinceTimestamp)
    }
}
