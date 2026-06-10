package com.assignment.engine

import kotlinx.coroutines.flow.Flow

class FakeScoreGenerator(
    private val events: Flow<ScoreEvent>
) : ScoreGenerator {
    override fun scoreUpdates(): Flow<ScoreEvent> = events
}
