package com.assignment.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

/**
 * Cold [Flow] that emits score events indefinitely.
 *
 * Contract: collect from a single coroutine only. [LeaderboardUseCase] owns
 * the sole collector; multiple collectors will each restart the emission loop.
 */
class RandomScoreGenerator(
    private val players: List<Player>,
    private val seed: Long
) : ScoreGenerator {

    init {
        require(players.isNotEmpty()) { "RandomScoreGenerator requires at least one player" }
    }

    override fun scoreUpdates(): Flow<ScoreEvent> = flow {
        val random = Random(seed)
        while (true) {
            delay(random.nextLong(MIN_INTERVAL_MS, MAX_INTERVAL_MS + 1))
            val player = players[random.nextInt(players.size)]
            val increment = random.nextInt(MIN_INCREMENT, MAX_INCREMENT + 1)
            emit(ScoreEvent(playerId = player.id, scoreIncrement = increment))
        }
    }.flowOn(Dispatchers.Default)

    companion object {
        private const val MIN_INTERVAL_MS = 500L
        private const val MAX_INTERVAL_MS = 2000L
        private const val MIN_INCREMENT = 1
        private const val MAX_INCREMENT = 20
    }
}
