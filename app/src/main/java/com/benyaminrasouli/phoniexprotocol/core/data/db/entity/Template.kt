package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val difficulty: String,
    val category: String,
    val recurrence: String,
    val taskType: String,
    val isPriority: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
