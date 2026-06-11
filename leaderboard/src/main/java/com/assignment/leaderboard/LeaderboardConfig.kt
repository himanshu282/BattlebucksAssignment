package com.assignment.leaderboard

import com.assignment.engine.Player

data class LeaderboardConfig(
    val players: List<Player>,
    val currentUserId: String,
    val sessionSeed: Long
)
