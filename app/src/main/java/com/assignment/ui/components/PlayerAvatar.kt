package com.assignment.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.himanshu.assignment.R

private val PlayerAvatarDrawables = listOf(
    R.drawable.ic_avatar_man,
    R.drawable.ic_avatar_hacker,
    R.drawable.ic_avatar_cat,
    R.drawable.ic_panda
)

@DrawableRes
fun avatarDrawableForPlayer(playerId: String): Int {
    val playerNumber = playerId.removePrefix("player_").toIntOrNull() ?: 1
    val index = (playerNumber - 1) % PlayerAvatarDrawables.size
    return PlayerAvatarDrawables[index]
}

@Composable
fun PlayerAvatar(
    playerId: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val avatarRes = remember(playerId) { avatarDrawableForPlayer(playerId) }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        HoodedFigureIcon(
            avatarRes = avatarRes,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HeroAvatar(
    playerId: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val avatarRes = remember(playerId) { avatarDrawableForPlayer(playerId) }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val shieldWidth = size.toPx() * 0.85f
            val shieldHeight = size.toPx() * 0.95f
            val left = (this.size.width - shieldWidth) / 2f
            val top = this.size.height * 0.08f

            drawRect(
                color = Color(0xFFE85D04),
                topLeft = Offset(left, top),
                size = Size(shieldWidth, shieldHeight * 0.15f)
            )
            drawRect(
                color = Color(0xFFB91C1C),
                topLeft = Offset(left, top + shieldHeight * 0.12f),
                size = Size(shieldWidth, shieldHeight * 0.75f)
            )
            drawRect(
                color = Color(0xFF7F1D1D),
                topLeft = Offset(left + shieldWidth * 0.1f, top + shieldHeight * 0.78f),
                size = Size(shieldWidth * 0.8f, shieldHeight * 0.18f)
            )
        }

        HoodedFigureIcon(
            avatarRes = avatarRes,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun HoodedFigureIcon(
    @DrawableRes avatarRes: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(avatarRes),
        contentDescription = "Player avatar",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
