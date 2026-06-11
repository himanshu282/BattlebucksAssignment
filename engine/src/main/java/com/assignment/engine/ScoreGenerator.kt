package com.assignment.engine

import kotlinx.coroutines.flow.Flow

interface ScoreGenerator {
    fun scoreUpdates(): Flow<ScoreEvent>
}
