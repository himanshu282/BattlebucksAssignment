package com.assignment.ui.leaderboard

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.assignment.leaderboard.LeaderboardFactory

val LeaderboardViewModelFactory = viewModelFactory {
    initializer {
        val config = LeaderboardFactory.defaultConfig()
        LeaderboardViewModel(
            leaderboardUseCase = LeaderboardFactory.createUseCase(config),
            currentUserId = config.currentUserId
        )
    }
}
