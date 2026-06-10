package com.assignment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assignment.domain.LeaderboardEntry
import com.himanshu.assignment.ui.theme.LeaderboardGold
import com.himanshu.assignment.ui.theme.LeaderboardRowBackground
import com.himanshu.assignment.ui.theme.LeaderboardTextPrimary
import com.himanshu.assignment.ui.theme.LeaderboardTrendGreen
import kotlinx.coroutines.delay

@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    var previousRank by remember(entry.playerId) { mutableIntStateOf(entry.rank) }
    var previousScore by remember(entry.playerId) { mutableIntStateOf(entry.score) }
    var showRankUp by remember(entry.playerId) { mutableStateOf(false) }
    var isScoreHighlighted by remember(entry.playerId) { mutableStateOf(false) }

    LaunchedEffect(entry.rank) {
        if (entry.rank < previousRank) {
            showRankUp = true
            delay(RANK_UP_INDICATOR_DURATION_MS)
            showRankUp = false
        }
        previousRank = entry.rank
    }

    LaunchedEffect(entry.score) {
        if (entry.score > previousScore) {
            isScoreHighlighted = true
            delay(SCORE_HIGHLIGHT_DURATION_MS)
            isScoreHighlighted = false
        }
        previousScore = entry.score
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LeaderboardRowBackground)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatar(playerId = entry.playerId)

        Text(
            text = "${entry.rank}.",
            color = if (entry.rank <= 3) LeaderboardGold else LeaderboardTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 2.dp)
        )

        Text(
            text = entry.username,
            color = LeaderboardTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (showRankUp) {
            Icon(
                imageVector = Icons.Default.KeyboardDoubleArrowUp,
                contentDescription = "Rank improved",
                tint = LeaderboardTrendGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        ScoreBadge(
            score = entry.score,
            isHighlighted = isScoreHighlighted
        )
    }
}

private const val RANK_UP_INDICATOR_DURATION_MS = 2_000L
private const val SCORE_HIGHLIGHT_DURATION_MS = 400L
