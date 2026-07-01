package com.benyaminrasouli.phoniexprotocol.core.domain.usecase

import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.DailyChallenge
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.DailyChallengeRepository
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.StatsRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

class GenerateDailyChallengesUseCase @Inject constructor(
    private val challengeRepository: DailyChallengeRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke() {
        val today = LocalDate.now().toString()
        val existing = challengeRepository.getTodayChallengesOnce()
        if (existing.isNotEmpty()) return

        val stats = statsRepository.getStatsOnce()
        val level = stats?.level ?: 1
        val challengeCount = Random.nextInt(2, 6)

        val challenges = generateChallenges(level, challengeCount, today)
        challenges.forEach { challengeRepository.insertChallenge(it) }
    }

    private fun generateChallenges(level: Int, count: Int, date: String): List<DailyChallenge> {
        val pool = mutableListOf<DailyChallenge>()

        pool.add(DailyChallenge(
            title = "Complete a task",
            description = "Complete 1 task today",
            type = "COMPLETE_TASKS",
            target = 1,
            rewardXp = 20,
            rewardEnergy = 5,
            date = date
        ))
        pool.add(DailyChallenge(
            title = "Complete 2 tasks",
            description = "Complete 2 tasks today",
            type = "COMPLETE_TASKS",
            target = 2,
            rewardXp = 40,
            rewardEnergy = 8,
            date = date
        ))
        pool.add(DailyChallenge(
            title = "Complete 3 tasks",
            description = "Complete 3 tasks today",
            type = "COMPLETE_TASKS",
            target = 3,
            rewardXp = 60,
            rewardEnergy = 10,
            date = date
        ))
        pool.add(DailyChallenge(
            title = "Hard task",
            description = "Complete a HARD difficulty task",
            type = "COMPLETE_HARD",
            target = 1,
            rewardXp = 80,
            rewardEnergy = 15,
            date = date
        ))
        pool.add(DailyChallenge(
            title = "Priority focus",
            description = "Complete 2 priority tasks",
            type = "COMPLETE_PRIORITY",
            target = 2,
            rewardXp = 70,
            rewardEnergy = 12,
            date = date
        ))

        if (level >= 11) {
            pool.add(DailyChallenge(
                title = "XP grinder",
                description = "Earn 100 XP today",
                type = "EARN_XP",
                target = 100,
                rewardXp = 100,
                rewardEnergy = 20,
                date = date
            ))
            pool.add(DailyChallenge(
                title = "Task marathon",
                description = "Complete 5 tasks today",
                type = "COMPLETE_TASKS",
                target = 5,
                rewardXp = 150,
                rewardEnergy = 20,
                date = date
            ))
        }

        pool.shuffle()
        return pool.take(count)
    }
}
