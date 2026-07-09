package com.benyaminrasouli.phoenixprotocol.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GoalType {
    COMPLETE_TASKS,
    STREAK_DAYS,
    PRIORITY_TASKS,
    XP_EARNED,
    DIFFICULTY_TASKS
}
