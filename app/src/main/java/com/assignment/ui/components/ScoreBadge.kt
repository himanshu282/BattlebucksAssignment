package com.assignment.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assignment.ui.theme.LeaderboardGold
import com.assignment.ui.theme.LeaderboardGoldDark
import com.assignment.ui.theme.LeaderboardOrangeLight
import com.assignment.ui.theme.LeaderboardScoreBadgeBackground
import com.assignment.ui.theme.LeaderboardTextPrimary

@Composable
fun ScoreBadge(
    score: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val scoreColor by animateColorAsState(
        targetValue = if (isHighlighted) LeaderboardOrangeLight else LeaderboardTextPrimary,
        animationSpec = tween(durationMillis = 400),
        label = "scoreBadgeColor"
    )

    Row(
        modifier = modifier
            .background(
                color = LeaderboardScoreBadgeBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = LeaderboardGoldDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = LeaderboardGold,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = score.toString(),
            color = scoreColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
