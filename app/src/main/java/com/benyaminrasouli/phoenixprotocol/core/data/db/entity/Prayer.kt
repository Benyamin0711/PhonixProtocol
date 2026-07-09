package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import kotlinx.serialization.Serializable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayers")
data class Prayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val xp: Int,
    val isActive: Boolean = true
)

