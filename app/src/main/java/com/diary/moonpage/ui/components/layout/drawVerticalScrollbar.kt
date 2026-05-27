package com.diary.moonpage.ui.components.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    color: Color = Color.Gray,
    width: Dp = 4.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbar_alpha"
    )

    drawWithContent {
        drawContent()

        val visibleItemsInfo = state.layoutInfo.visibleItemsInfo
        val totalItemsCount = state.layoutInfo.totalItemsCount

        if (visibleItemsInfo.isNotEmpty() && totalItemsCount > visibleItemsInfo.size) {
            val scrollbarHeight = (visibleItemsInfo.size.toFloat() / totalItemsCount) * this.size.height
            val firstVisibleItemIndex = visibleItemsInfo.first().index
            val scrollbarOffsetY = (firstVisibleItemIndex.toFloat() / totalItemsCount) * this.size.height

            drawRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

fun Modifier.drawVerticalScrollbar(
    state: LazyGridState,
    color: Color = Color.Gray,
    width: Dp = 4.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbar_alpha"
    )

    drawWithContent {
        drawContent()

        val visibleItemsInfo = state.layoutInfo.visibleItemsInfo
        val totalItemsCount = state.layoutInfo.totalItemsCount

        if (visibleItemsInfo.isNotEmpty() && totalItemsCount > visibleItemsInfo.size) {
            val scrollbarHeight = (visibleItemsInfo.size.toFloat() / totalItemsCount) * this.size.height
            val firstVisibleItemIndex = visibleItemsInfo.first().index
            val scrollbarOffsetY = (firstVisibleItemIndex.toFloat() / totalItemsCount) * this.size.height

            drawRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}
