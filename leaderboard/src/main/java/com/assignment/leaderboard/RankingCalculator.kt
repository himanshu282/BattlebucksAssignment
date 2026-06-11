package com.assignment.leaderboard

import com.assignment.engine.Player

class RankingCalculator {

    fun calculate(
        players: List<Player>,
        scores: Map<String, Int>
    ): List<LeaderboardEntry> {
        if (players.isEmpty()) return emptyList()

        val sorted = players
            .map { player -> player to (scores[player.id] ?: 0) }
            .sortedWith(compareByDescending<Pair<Player, Int>> { it.second }.thenBy { it.first.username })

        var currentRank = 1
        var previousScore: Int? = null

        return sorted.mapIndexed { index, (player, score) ->
            if (previousScore != null && score < previousScore) {
                currentRank = index + 1
            }
            previousScore = score

            LeaderboardEntry(
                rank = currentRank,
                playerId = player.id,
                username = player.username,
                score = score
            )
        }
    }
}
