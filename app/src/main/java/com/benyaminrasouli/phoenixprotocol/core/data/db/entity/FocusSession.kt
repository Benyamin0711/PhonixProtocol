package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import kotlinx.serialization.Serializable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationSeconds: Int,
    val completed: Boolean = false,
    val mode: String = "POMODORO"
)

