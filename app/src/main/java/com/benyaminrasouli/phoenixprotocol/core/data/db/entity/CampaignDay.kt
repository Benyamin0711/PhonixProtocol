package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaign_days")
data class CampaignDay(
    @PrimaryKey val dayNumber: Int,
    val completedMissions: String = "",
    val completedPrayers: String = "",
    val note: String = "",
    val relapseCount: Int = 0,
    val isAsh: Boolean = false
)
