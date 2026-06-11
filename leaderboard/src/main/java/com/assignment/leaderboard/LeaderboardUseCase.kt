package com.assignment.leaderboard

import com.assignment.engine.Player
import com.assignment.engine.ScoreEvent
import com.assignment.engine.ScoreGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class LeaderboardUseCase(
    private val scoreGenerator: ScoreGenerator,
    private val players: List<Player>,
    private val rankingCalculator: RankingCalculator,
    private val collectionDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val scores: MutableMap<String, Int> = players.associate { it.id to 0 }.toMutableMap()

    private val _leaderboard = MutableStateFlow(calculateLeaderboard())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val isStarted = AtomicBoolean(false)

    fun start(scope: CoroutineScope) {
        if (!isStarted.compareAndSet(false, true)) return

        scope.launch(collectionDispatcher) {
            scoreGenerator.scoreUpdates().collect { event ->
                applyScoreEvent(event)
            }
        }
    }

    private fun applyScoreEvent(event: ScoreEvent) {
        val currentScore = scores[event.playerId] ?: return
        scores[event.playerId] = currentScore + event.scoreIncrement
        publishLeaderboardIfChanged()
    }

    private fun publishLeaderboardIfChanged() {
        _leaderboard.update { current ->
            val updated = calculateLeaderboard()
            if (updated == current) current else updated
        }
    }

    private fun calculateLeaderboard(): List<LeaderboardEntry> {
        return rankingCalculator.calculate(players, scores)
    }
}
