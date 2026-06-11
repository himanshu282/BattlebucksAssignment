package com.assignment.engine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RandomScoreGeneratorTest {

    private val players = listOf(
        Player(id = "p1", username = "Alice"),
        Player(id = "p2", username = "Bob")
    )

    @Test
    fun `emits valid score events`() = runTest {
        val generator = RandomScoreGenerator(players = players, seed = 42L)

        val events = generator.scoreUpdates().take(10).toList()

        assertEquals(10, events.size)
        events.forEach { event ->
            assertTrue(event.playerId in players.map { it.id })
            assertTrue(event.scoreIncrement in 1..20)
        }
    }

    @Test
    fun `same seed produces deterministic first event`() = runTest {
        val firstGenerator = RandomScoreGenerator(players = players, seed = 99L)
        val secondGenerator = RandomScoreGenerator(players = players, seed = 99L)

        val firstEvent = firstGenerator.scoreUpdates().take(1).toList().single()
        val secondEvent = secondGenerator.scoreUpdates().take(1).toList().single()

        assertEquals(firstEvent, secondEvent)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires at least one player`() {
        RandomScoreGenerator(players = emptyList(), seed = 1L)
    }
}
