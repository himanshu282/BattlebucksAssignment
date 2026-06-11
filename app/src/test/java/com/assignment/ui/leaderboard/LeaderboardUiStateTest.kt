package com.assignment.ui.leaderboard

import com.assignment.leaderboard.LeaderboardEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardUiStateTest {

    @Test
    fun `from keeps current user in leaderboard entries when ranked in list`() {
        val entries = listOf(
            LeaderboardEntry(rank = 1, playerId = "p1", username = "Alice", score = 100),
            LeaderboardEntry(rank = 7, playerId = "p2", username = "Bob", score = 80)
        )

        val state = LeaderboardUiState.from(entries, currentUserId = "p2")

        assertEquals("p2", state.currentUserEntry?.playerId)
        assertEquals(7, state.currentUserEntry?.rank)
        assertEquals(2, state.entries.size)
        assertTrue(state.entries.any { it.playerId == "p2" })
    }

    @Test
    fun `from returns null current user when not in list`() {
        val entries = listOf(
            LeaderboardEntry(rank = 1, playerId = "p1", username = "Alice", score = 100)
        )

        val state = LeaderboardUiState.from(entries, currentUserId = "missing")

        assertNull(state.currentUserEntry)
        assertEquals(1, state.entries.size)
    }
}
