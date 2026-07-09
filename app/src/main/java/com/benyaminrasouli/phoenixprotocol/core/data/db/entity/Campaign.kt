package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import kotlinx.serialization.Serializable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey val id: Long = 1,
    val currentDay: Int = 1,
    val totalXp: Int = 0,
    val startDate: Long = System.currentTimeMillis()
)

