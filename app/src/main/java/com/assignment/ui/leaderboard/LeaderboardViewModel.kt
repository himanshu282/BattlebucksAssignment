package com.assignment.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.leaderboard.LeaderboardUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LeaderboardViewModel(
    private val leaderboardUseCase: LeaderboardUseCase,
    private val currentUserId: String
) : ViewModel() {

    val uiState: StateFlow<LeaderboardUiState> = leaderboardUseCase.leaderboard
        .map { entries -> LeaderboardUiState.from(entries, currentUserId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = LeaderboardUiState.from(
                entries = leaderboardUseCase.leaderboard.value,
                currentUserId = currentUserId
            )
        )

    init {
        leaderboardUseCase.start(viewModelScope)
    }
}
