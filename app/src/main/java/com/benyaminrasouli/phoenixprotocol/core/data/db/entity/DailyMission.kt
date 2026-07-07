package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_missions")
data class DailyMission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val xp: Int,
    val isActive: Boolean = true
)
