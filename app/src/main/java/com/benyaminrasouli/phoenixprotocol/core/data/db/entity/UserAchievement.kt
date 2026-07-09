package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import kotlinx.serialization.Serializable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_achievements")
data class UserAchievement(
    @PrimaryKey
    val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

