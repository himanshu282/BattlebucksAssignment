package com.assignment.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class CollapsingHeroScrollState(
    val maxCollapsePx: Float,
    initialCollapsedPx: Float = 0f
) {
    var collapsedPx by mutableFloatStateOf(initialCollapsedPx)
        private set

    val collapseFraction: Float
        get() = if (maxCollapsePx <= 0f) 1f else (collapsedPx / maxCollapsePx).coerceIn(0f, 1f)

    val isFullyCollapsed: Boolean
        get() = collapsedPx >= maxCollapsePx

    internal fun consumePreScroll(delta: Float, listState: LazyListState): Float {
        if (delta == 0f) return 0f

        return if (delta < 0f) {
            if (isFullyCollapsed) 0f else -collapse(-delta)
        } else {
            expand(delta, listState)
        }
    }

    private fun collapse(scrollUpAmount: Float): Float {
        val newCollapsed = (collapsedPx + scrollUpAmount).coerceAtMost(maxCollapsePx)
        val consumed = newCollapsed - collapsedPx
        collapsedPx = newCollapsed
        return consumed
    }

    private fun expand(scrollDownAmount: Float, listState: LazyListState): Float {
        if (collapsedPx <= 0f) return 0f
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
            return 0f
        }

        val newCollapsed = (collapsedPx - scrollDownAmount).coerceAtLeast(0f)
        val consumed = collapsedPx - newCollapsed
        collapsedPx = newCollapsed
        return consumed
    }
}

@Composable
fun rememberCollapsingHeroScrollState(
    expandedHeight: Dp = HERO_EXPANDED_HEIGHT,
    collapsedHeight: Dp = HERO_COLLAPSED_HEIGHT
): CollapsingHeroScrollState {
    val density = LocalDensity.current
    val maxCollapsePx = with(density) { (expandedHeight - collapsedHeight).toPx() }
    return remember(maxCollapsePx) { CollapsingHeroScrollState(maxCollapsePx) }
}

@Composable
fun CollapsingHeroScrollState.nestedScrollConnection(
    listState: LazyListState
): NestedScrollConnection {
    val state = this
    return remember(state, listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val consumedY = state.consumePreScroll(available.y, listState)
                return Offset(0f, consumedY)
            }
        }
    }
}

fun CollapsingHeroScrollState.currentHeight(
    expandedHeight: Dp = HERO_EXPANDED_HEIGHT,
    collapsedHeight: Dp = HERO_COLLAPSED_HEIGHT
): Dp {
    return expandedHeight - (expandedHeight - collapsedHeight) * collapseFraction
}

val HERO_EXPANDED_HEIGHT = 300.dp
val HERO_COLLAPSED_HEIGHT = 148.dp
