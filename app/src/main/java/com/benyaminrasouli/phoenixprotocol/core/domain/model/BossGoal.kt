package com.benyaminrasouli.phoenixprotocol.core.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BossGoal(
    val type: GoalType,
    val target: Int,
    var current: Int = 0,
    val description: String
) {
    val isCompleted: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun toJson(goals: List<BossGoal>): String {
            return json.encodeToString(goals)
        }

        fun fromJson(jsonString: String): List<BossGoal> {
            return json.decodeFromString(jsonString)
        }
    }
}