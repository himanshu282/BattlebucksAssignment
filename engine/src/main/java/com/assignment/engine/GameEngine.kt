package com.assignment.engine

import kotlinx.coroutines.flow.Flow

/**
 * Entry point for the score generator / game engine module.
 */
class GameEngine private constructor(
    private val scoreGenerator: ScoreGenerator
) : ScoreGenerator {

    override fun scoreUpdates(): Flow<ScoreEvent> = scoreGenerator.scoreUpdates()

    companion object {
        fun create(players: List<Player>, seed: Long): GameEngine {
            return GameEngine(
                scoreGenerator = RandomScoreGenerator(players = players, seed = seed)
            )
        }
    }
}
