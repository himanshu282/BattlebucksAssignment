package com.assignment.ui.leaderboard

import com.assignment.domain.LeaderboardEntry

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUserEntry: LeaderboardEntry? = null,
    val currentUserId: String = ""
) {
    companion object {
        fun from(entries: List<LeaderboardEntry>, currentUserId: String): LeaderboardUiState {
            val currentUserEntry = entries.firstOrNull { it.playerId == currentUserId }
            return LeaderboardUiState(
                entries = entries,
                currentUserEntry = currentUserEntry,
                currentUserId = currentUserId
            )
        }
    }
}
