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

    override suspend fun getTaskById(taskId: Long): Task? {
        return dao.getTaskById(taskId)
    }

    override suspend fun completeTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "COMPLETED", System.currentTimeMillis())
    }

    override suspend fun skipTask(taskId: Long) {
        dao.updateTaskStatus(taskId, "SKIPPED", null)
    }
}
