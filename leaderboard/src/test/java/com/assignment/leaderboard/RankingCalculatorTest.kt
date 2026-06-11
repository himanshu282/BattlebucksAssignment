package com.assignment.leaderboard

import com.assignment.engine.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RankingCalculatorTest {

    private lateinit var calculator: RankingCalculator

    @Before
    fun setUp() {
        calculator = RankingCalculator()
    }

    @Test
    fun `empty leaderboard returns empty list`() {
        val result = calculator.calculate(players = emptyList(), scores = emptyMap())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single player receives rank 1`() {
        val players = listOf(Player(id = "p1", username = "Alice"))
        val scores = mapOf("p1" to 42)

        val result = calculator.calculate(players, scores)

        assertEquals(1, result.size)
        assertEquals(1, result[0].rank)
        assertEquals("p1", result[0].playerId)
        assertEquals("Alice", result[0].username)
        assertEquals(42, result[0].score)
    }

    @Test
    fun `duplicate scores receive same rank`() {
        val players = listOf(
            Player(id = "p1", username = "Alice"),
            Player(id = "p2", username = "Bob")
        )
        val scores = mapOf("p1" to 100, "p2" to 100)

        val result = calculator.calculate(players, scores)

        assertEquals(2, result.size)
        assertEquals(1, result[0].rank)
        assertEquals(1, result[1].rank)
    }

    @Test
    fun `rank skips after tied scores`() {
        val players = listOf(
            Player(id = "p1", username = "Alice"),
            Player(id = "p2", username = "Bob"),
            Player(id = "p3", username = "Charlie")
        )
        val scores = mapOf("p1" to 100, "p2" to 100, "p3" to 90)

        val result = calculator.calculate(players, scores)

        assertEquals(3, result.size)
        assertEquals(1, result[0].rank)
        assertEquals(1, result[1].rank)
        assertEquals(3, result[2].rank)
    }

    @Test
    fun `tied scores are ordered alphabetically by username`() {
        val players = listOf(
            Player(id = "p1", username = "Zara"),
            Player(id = "p2", username = "Alice")
        )
        val scores = mapOf("p1" to 50, "p2" to 50)

        val result = calculator.calculate(players, scores)

        assertEquals(listOf("Alice", "Zara"), result.map { it.username })
        assertEquals(listOf(1, 1), result.map { it.rank })
    }

    @Test
    fun `entries are sorted by score descending`() {
        val players = listOf(
            Player(id = "p1", username = "Alice"),
            Player(id = "p2", username = "Bob"),
            Player(id = "p3", username = "Charlie"),
            Player(id = "p4", username = "Diana")
        )
        val scores = mapOf("p1" to 50, "p2" to 120, "p3" to 120, "p4" to 75)

        val result = calculator.calculate(players, scores)

        assertEquals(listOf(120, 120, 75, 50), result.map { it.score })
        assertEquals(listOf(1, 1, 3, 4), result.map { it.rank })
    }
}
