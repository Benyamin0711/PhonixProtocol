package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val id: Long = 1,
    val userId: Long,
    val xp: Int = 0,
    val level: Int = 1,
    val rank: String = "INITIATE",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val phoenixEnergy: Int = 50,
    val shadowLevel: Int = 0,
    val completedTasks: Int = 0
)
