package com.benyaminrasouli.phoenixprotocol.core.util

object ShadowCalculator {

    fun shadowTier(level: Int): String = when {
        level < 30 -> "SAFE"
        level < 60 -> "WARNING"
        level < 90 -> "CRITICAL"
        else -> "CORRUPTED"
    }

    fun xpPenaltyPercent(level: Int): Int = when {
        level < 30 -> 0
        level < 60 -> 10
        level < 90 -> 25
        else -> 50
    }
}
