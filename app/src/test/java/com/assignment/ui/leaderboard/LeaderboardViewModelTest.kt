package com.assignment.ui.leaderboard

import com.assignment.leaderboard.RankingCalculator
import com.assignment.leaderboard.LeaderboardUseCase
import com.assignment.engine.FakeScoreGenerator
import com.assignment.engine.Player
import com.assignment.engine.ScoreEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val players = listOf(
        Player(id = "p1", username = "Alice"),
        Player(id = "p2", username = "Bob")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState exposes current user entry`() = runTest(testDispatcher) {
        val useCase = LeaderboardUseCase(
            scoreGenerator = FakeScoreGenerator(
                flowOf(ScoreEvent(playerId = "p2", scoreIncrement = 42))
            ),
            players = players,
            rankingCalculator = RankingCalculator(),
            collectionDispatcher = StandardTestDispatcher(testScheduler)
        )
        val viewModel = LeaderboardViewModel(
            leaderboardUseCase = useCase,
            currentUserId = "p2"
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals("p2", state.currentUserEntry?.playerId)
        assertEquals("Bob", state.currentUserEntry?.username)
        assertEquals(42, state.currentUserEntry?.score)
        assertEquals(2, state.entries.size)
        assertTrue(state.entries.any { it.playerId == "p2" })

        collectJob.cancel()
    }
}
