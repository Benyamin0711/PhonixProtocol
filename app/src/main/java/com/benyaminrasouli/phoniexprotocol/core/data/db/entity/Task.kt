package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val difficulty: String,
    val category: String,
    val xpValue: Int,
    val recurrence: String,
    val status: String,
    val taskType: String = "CUSTOM",
    val isPriority: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
