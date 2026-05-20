package com.diary.moonpage.ui.components.refresh

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/**
 * Lightweight pull-to-refresh wrapper for scrollable screens.
 *
 * It listens to unconsumed downward drag from child scrollables, so normal list
 * scrolling keeps priority and refresh only starts when the content is at the top.
 */
@Composable
fun MoonPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    var pullDistance by remember { mutableFloatStateOf(0f) }
    val refreshThreshold = 120f
    val indicatorOffset by animateFloatAsState(
        targetValue = when {
            isRefreshing -> 58f
            pullDistance > 0f -> (pullDistance * 0.45f).coerceAtMost(58f)
            else -> 0f
        },
        label = "moonPullRefreshIndicator"
    )

    val nestedScrollConnection = remember(enabled, isRefreshing) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!enabled || isRefreshing || source != NestedScrollSource.UserInput) return Offset.Zero
                if (available.y > 0f) {
                    pullDistance = (pullDistance + available.y).coerceAtMost(refreshThreshold * 1.6f)
                }
                return Offset.Zero
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!enabled || isRefreshing || source != NestedScrollSource.UserInput) return Offset.Zero
                if (available.y < 0f && pullDistance > 0f) {
                    pullDistance = (pullDistance + available.y).coerceAtLeast(0f)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (enabled && !isRefreshing && pullDistance >= refreshThreshold) {
                    onRefresh()
                }
                pullDistance = 0f
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        content()
        if (isRefreshing || pullDistance > 0f) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = indicatorOffset.dp)
                    .size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}
