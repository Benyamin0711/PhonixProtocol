package com.benyaminrasouli.phoniexprotocol.core.domain.model

object Slogans {
    private val dailySlogans = listOf(
        "Discipline is winning today.",
        "Shadow is getting louder. Move.",
        "Phoenix Energy rising.",
        "One action changes the board.",
        "The system remembers consistency.",
        "You are not your excuses.",
        "Rise through resistance.",
        "The protocol does not sleep.",
        "Consistency is power.",
        "Every task is a vote for your future self.",
        "Shadow feeds on inaction.",
        "Your rank is earned, not given.",
        "The Phoenix burns weakness.",
        "Today you choose strength.",
        "The system watches. The system remembers."
    )

    fun getDailySlogan(index: Int): String {
        return dailySlogans[index % dailySlogans.size]
    }

    val sloganCount: Int get() = dailySlogans.size
}
