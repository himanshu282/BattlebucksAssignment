package com.assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.assignment.ui.leaderboard.LeaderboardScreen
import com.assignment.ui.theme.BattleBucksAssignmentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BattleBucksAssignmentTheme {
                LeaderboardScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
