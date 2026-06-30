package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bosses")
data class Boss(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val level: Int,
    val title: String,
    val description: String,
    val goals: String,
    val deadline: Long,
    val status: String = "ACTIVE",
    val rewardXp: Int,
    val createdAt: Long = System.currentTimeMillis()
)
