package com.benyaminrasouli.phoniexprotocol.core.domain.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class BossGoal(
    val type: GoalType,
    val target: Int,
    var current: Int = 0,
    val description: String
) {
    val isCompleted: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)

    companion object {
        private val gson = Gson()

        fun toJson(goals: List<BossGoal>): String {
            return gson.toJson(goals)
        }

        fun fromJson(json: String): List<BossGoal> {
            val type = object : TypeToken<List<BossGoal>>() {}.type
            return gson.fromJson(json, type)
        }
    }
}
