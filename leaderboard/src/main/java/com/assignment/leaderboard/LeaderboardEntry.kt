package com.assignment.leaderboard

data class LeaderboardEntry(
    val rank: Int,
    val playerId: String,
    val username: String,
    val score: Int
)
