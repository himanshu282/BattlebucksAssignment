package com.assignment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assignment.domain.LeaderboardEntry
import com.assignment.ui.leaderboard.toOrdinalRank
import com.himanshu.assignment.ui.theme.LeaderboardBackgroundGradientEnd
import com.himanshu.assignment.ui.theme.LeaderboardGold
import com.himanshu.assignment.ui.theme.LeaderboardLegendsWatermark
import com.himanshu.assignment.ui.theme.LeaderboardOrange
import com.himanshu.assignment.ui.theme.LeaderboardOrangeLight
import com.himanshu.assignment.ui.theme.LeaderboardPillBackground
import com.himanshu.assignment.ui.theme.LeaderboardPillBorder
import com.himanshu.assignment.ui.theme.LeaderboardShieldRed
import com.himanshu.assignment.ui.theme.LeaderboardTextPrimary
import com.himanshu.assignment.ui.theme.LeaderboardTextSecondary

@Composable
fun LeaderboardHeroSection(
    currentUserEntry: LeaderboardEntry?,
    currentUserId: String,
    collapseFraction: Float,
    modifier: Modifier = Modifier
) {
    val expandedAlpha = (1f - collapseFraction * 1.2f).coerceIn(0f, 1f)
    val collapsedAlpha = ((collapseFraction - 0.35f) / 0.65f).coerceIn(0f, 1f)
    val avatarScale = (1f - collapseFraction * 0.85f).coerceIn(0f, 1f)
    val watermarkAlpha = (1f - collapseFraction * 2f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LeaderboardOrange,
                            LeaderboardOrangeLight.copy(alpha = 0.85f),
                            LeaderboardBackgroundGradientEnd.copy(alpha = 0.9f - collapseFraction * 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(1f - collapseFraction * 0.7f)
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LeaderboardOrange.copy(alpha = 0.35f),
                                LeaderboardBackgroundGradientEnd.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height * 0.35f),
                            radius = size.width * 0.85f
                        )
                    )
                }
        )

        Text(
            text = "LEGENDS",
            color = LeaderboardLegendsWatermark,
            fontSize = 52.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .alpha(watermarkAlpha)
                .graphicsLayer {
                    translationY = -collapseFraction * 30.dp.toPx()
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SeasonSelector()

            if (expandedAlpha > 0.05f) {
                Text(
                    text = "Season ends in 60 days.",
                    color = LeaderboardTextSecondary.copy(alpha = expandedAlpha),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .alpha(expandedAlpha)
                )
            }

            if (expandedAlpha > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ExpandedHeroContent(
                        currentUserEntry = currentUserEntry,
                        currentUserId = currentUserId,
                        alpha = expandedAlpha,
                        avatarScale = avatarScale,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else if (collapsedAlpha > 0f) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (collapsedAlpha > 0f) {
                CollapsedUserBar(
                    currentUserEntry = currentUserEntry,
                    currentUserId = currentUserId,
                    alpha = collapsedAlpha
                )
            }
        }
    }
}

@Composable
private fun ExpandedHeroContent(
    currentUserEntry: LeaderboardEntry?,
    currentUserId: String,
    alpha: Float,
    avatarScale: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = -(1f - alpha) * 24.dp.toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeroAvatar(
            playerId = currentUserId,
            modifier = Modifier.graphicsLayer {
                scaleX = avatarScale
                scaleY = avatarScale
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        UserStatsPills(
            currentUserEntry = currentUserEntry,
            arrangement = Arrangement.spacedBy(12.dp)
        )
    }
}

@Composable
private fun CollapsedUserBar(
    currentUserEntry: LeaderboardEntry?,
    currentUserId: String,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .graphicsLayer { this.alpha = alpha },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlayerAvatar(
            playerId = currentUserId,
            size = 32.dp
        )

        StatPill(
            text = currentUserEntry?.rank?.toOrdinalRank() ?: "—",
            borderColor = LeaderboardShieldRed.copy(alpha = 0.8f)
        )

        StatPill(
            text = currentUserEntry?.score?.toString() ?: "0",
            showTrophy = true,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun SeasonSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Back",
            tint = LeaderboardTextPrimary,
            modifier = Modifier.size(22.dp)
        )
        Row(
            modifier = Modifier.padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "GENESIS SEASON",
                color = LeaderboardTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select season",
                tint = LeaderboardTextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun UserStatsPills(
    currentUserEntry: LeaderboardEntry?,
    arrangement: Arrangement.Horizontal
) {
    Row(
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatPill(
            text = currentUserEntry?.rank?.toOrdinalRank() ?: "—"
        )
        StatPill(
            text = currentUserEntry?.score?.toString() ?: "0",
            showTrophy = true
        )
    }
}

@Composable
private fun StatPill(
    text: String,
    modifier: Modifier = Modifier,
    showTrophy: Boolean = false,
    borderColor: Color = LeaderboardPillBorder
) {
    Row(
        modifier = modifier
            .background(
                color = LeaderboardPillBackground,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showTrophy) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = LeaderboardGold,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            color = LeaderboardTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
