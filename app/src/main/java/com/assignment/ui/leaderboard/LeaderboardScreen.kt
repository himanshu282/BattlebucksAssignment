package com.assignment.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assignment.ui.components.LeaderboardHeroSection
import com.assignment.ui.components.LeaderboardItem
import com.assignment.ui.components.currentHeight
import com.assignment.ui.components.nestedScrollConnection
import com.assignment.ui.components.rememberCollapsingHeroScrollState
import com.himanshu.assignment.ui.theme.LeaderboardBackground
import com.himanshu.assignment.ui.theme.LeaderboardBackgroundGradientEnd
import com.himanshu.assignment.ui.theme.LeaderboardSurface
import com.himanshu.assignment.ui.theme.LeaderboardTextPrimary
import com.himanshu.assignment.ui.theme.LeaderboardTextSecondary

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = viewModel(factory = LeaderboardViewModelFactory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val collapsingHeroState = rememberCollapsingHeroScrollState()
    val nestedScrollConnection = collapsingHeroState.nestedScrollConnection(listState)
    val heroHeight = collapsingHeroState.currentHeight()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                LeaderboardBackgroundGradientEnd,
                LeaderboardBackground,
                LeaderboardSurface
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .clip(RectangleShape)
            ) {
                LeaderboardHeroSection(
                    currentUserEntry = uiState.currentUserEntry,
                    currentUserId = uiState.currentUserId,
                    collapseFraction = collapsingHeroState.collapseFraction,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LeaderboardSurface)
                    .nestedScroll(nestedScrollConnection),
                state = listState,
                contentPadding = navigationBarPadding
            ) {
                stickyHeader(key = "leaderboard_header") {
                    LeaderboardListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LeaderboardSurface)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(
                    items = uiState.entries,
                    key = { it.playerId },
                    contentType = { LEADERBOARD_ITEM_CONTENT_TYPE }
                ) { entry ->
                    LeaderboardItem(entry = entry)
                }

                item(key = "list_footer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LeaderboardListHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Leaderboard",
            color = LeaderboardTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Leaderboard info",
            tint = LeaderboardTextSecondary,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
        )
    }
}

private const val LEADERBOARD_ITEM_CONTENT_TYPE = "leaderboard_item"
