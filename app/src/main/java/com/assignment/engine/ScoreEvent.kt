package com.assignment.engine

data class ScoreEvent(
    val playerId: String,
    val scoreIncrement: Int
)
