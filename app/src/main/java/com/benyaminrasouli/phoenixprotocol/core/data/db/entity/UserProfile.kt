package com.benyaminrasouli.phoenixprotocol.core.data.db.entity

import kotlinx.serialization.Serializable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val username: String,
    val birthYear: Int?,
    val identityPath: String,
    val createdAt: Long = System.currentTimeMillis()
)

