package com.assignment.domain

import com.assignment.engine.FakeScoreGenerator
import com.assignment.engine.Player
import com.assignment.engine.ScoreEvent
import com.assignment.engine.ScoreGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardUseCaseTest {

    private val players = listOf(
        Player(id = "p1", username = "Alice"),
        Player(id = "p2", username = "Bob")
    )

    @Test
    fun `initial leaderboard has zero scores`() = runTest {
        val useCase = createUseCase(flowOf(), this)

        val leaderboard = useCase.leaderboard.value

        assertEquals(2, leaderboard.size)
        assertTrue(leaderboard.all { it.score == 0 })
    }

    @Test
    fun `accumulates score events and recalculates ranks`() = runTest {
        val useCase = createUseCase(
            events = flowOf(
                ScoreEvent(playerId = "p1", scoreIncrement = 30),
                ScoreEvent(playerId = "p2", scoreIncrement = 50),
                ScoreEvent(playerId = "p1", scoreIncrement = 25)
            ),
            testScope = this
        )

        useCase.start(this)
        advanceUntilIdle()

        val leaderboard = useCase.leaderboard.value

        assertEquals("Alice", leaderboard[0].username)
        assertEquals(55, leaderboard[0].score)
        assertEquals(1, leaderboard[0].rank)
        assertEquals("Bob", leaderboard[1].username)
        assertEquals(50, leaderboard[1].score)
        assertEquals(2, leaderboard[1].rank)
    }

    @Test
    fun `ignores events for unknown players`() = runTest {
        val useCase = createUseCase(
            events = flowOf(ScoreEvent(playerId = "unknown", scoreIncrement = 100)),
            testScope = this
        )

        useCase.start(this)
        advanceUntilIdle()

        assertTrue(useCase.leaderboard.value.all { it.score == 0 })
    }

    @Test
    fun `start is idempotent and uses a single collector`() = runTest {
        var collectorCount = 0
        val generator = object : ScoreGenerator {
            override fun scoreUpdates(): Flow<ScoreEvent> = flow {
                collectorCount++
                emit(ScoreEvent(playerId = "p1", scoreIncrement = 10))
            }
        }
        val useCase = LeaderboardUseCase(
            scoreGenerator = generator,
            players = players,
            rankingCalculator = RankingCalculator(),
            collectionDispatcher = StandardTestDispatcher(testScheduler)
        )

        useCase.start(this)
        useCase.start(this)
        advanceUntilIdle()

        assertEquals(1, collectorCount)
        assertEquals(10, useCase.leaderboard.value.first { it.playerId == "p1" }.score)
    }

    @Test
    fun `publishes updated scores after multiple events`() = runTest {
        val useCase = createUseCase(
            events = flowOf(
                ScoreEvent(playerId = "p1", scoreIncrement = 10),
                ScoreEvent(playerId = "p1", scoreIncrement = 5)
            ),
            testScope = this
        )

        useCase.start(this)
        advanceUntilIdle()

        assertEquals(
            15,
            useCase.leaderboard.value.first { it.playerId == "p1" }.score
        )
    }

    private fun createUseCase(
        events: Flow<ScoreEvent>,
        testScope: kotlinx.coroutines.test.TestScope
    ): LeaderboardUseCase {
        return LeaderboardUseCase(
            scoreGenerator = FakeScoreGenerator(events),
            players = players,
            rankingCalculator = RankingCalculator(),
            collectionDispatcher = StandardTestDispatcher(testScope.testScheduler)
        )
    }
}
