package com.benyaminrasouli.phoenixprotocol.core.domain.model

data class LeaderboardEntry(
    val id: Long,
    val name: String,
    val avatarColor: String,
    val xp: Int,
    val level: Int,
    val rank: Rank,
    val isCurrentUser: Boolean = false
)
