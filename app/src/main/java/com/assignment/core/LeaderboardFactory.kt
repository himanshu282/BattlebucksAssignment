package com.assignment.core

import com.assignment.domain.LeaderboardUseCase
import com.assignment.domain.RankingCalculator
import com.assignment.engine.Player
import com.assignment.engine.RandomScoreGenerator

object LeaderboardFactory {

    fun defaultConfig(sessionSeed: Long = System.currentTimeMillis()): LeaderboardConfig {
        val players = defaultPlayers()
        return LeaderboardConfig(
            players = players,
            currentUserId = CURRENT_USER_ID,
            sessionSeed = sessionSeed
        )
    }

    fun createUseCase(config: LeaderboardConfig = defaultConfig()): LeaderboardUseCase {
        val scoreGenerator = RandomScoreGenerator(
            players = config.players,
            seed = config.sessionSeed
        )
        return LeaderboardUseCase(
            scoreGenerator = scoreGenerator,
            players = config.players,
            rankingCalculator = RankingCalculator()
        )
    }

    private const val CURRENT_USER_ID = "player_8"

    private fun defaultPlayers(): List<Player> = listOf(
        Player(id = "player_1", username = "Deepender"),
        Player(id = "player_2", username = "Predekin_Singh"),
        Player(id = "player_3", username = "Premjit"),
        Player(id = "player_4", username = "Manya Aggarwal"),
        Player(id = "player_5", username = "Vishal"),
        Player(id = "player_6", username = "Shreyas"),
        Player(id = "player_7", username = "Anwesha"),
        Player(id = CURRENT_USER_ID, username = "Himanshu")
    )
}
