package com.assignment.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LeaderboardColorScheme = darkColorScheme(
    primary = LeaderboardOrange,
    onPrimary = LeaderboardTextPrimary,
    secondary = LeaderboardGold,
    onSecondary = LeaderboardBackground,
    background = LeaderboardBackground,
    onBackground = LeaderboardTextPrimary,
    surface = LeaderboardSurface,
    onSurface = LeaderboardTextPrimary,
    surfaceVariant = LeaderboardRowBackground,
    onSurfaceVariant = LeaderboardTextSecondary,
    outline = LeaderboardPillBorder
)

@Composable
fun BattleBucksAssignmentTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LeaderboardColorScheme,
        typography = Typography,
        content = content
    )
}
