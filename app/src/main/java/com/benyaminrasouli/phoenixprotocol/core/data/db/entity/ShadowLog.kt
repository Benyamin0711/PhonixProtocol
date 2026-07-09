package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import kotlinx.serialization.Serializable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shadow_log")
data class ShadowLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val amount: Int,
    val description: String
)

