package com.benyaminrasouli.phoniexprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val type: String,
    val target: Int,
    val current: Int = 0,
    val rewardXp: Int,
    val rewardEnergy: Int,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)
