package com.diary.moonpage.ui.components.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    width: Dp = 4.dp
): Modifier {
    return drawScrollbarInternal(
        isScrollInProgress = state.isScrollInProgress,
        firstVisibleItemIndex = state.firstVisibleItemIndex,
        firstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset,
        totalItemsCount = state.layoutInfo.totalItemsCount,
        visibleItemsCount = state.layoutInfo.visibleItemsInfo.size,
        viewportStartOffset = state.layoutInfo.viewportStartOffset.toFloat(),
        viewportEndOffset = state.layoutInfo.viewportEndOffset.toFloat(),
        color = color,
        width = width
    )
}

@Composable
fun Modifier.drawVerticalScrollbar(
    state: LazyGridState,
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    width: Dp = 4.dp
): Modifier {
    return drawScrollbarInternal(
        isScrollInProgress = state.isScrollInProgress,
        firstVisibleItemIndex = state.firstVisibleItemIndex,
        firstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset,
        totalItemsCount = state.layoutInfo.totalItemsCount,
        visibleItemsCount = state.layoutInfo.visibleItemsInfo.size,
        viewportStartOffset = state.layoutInfo.viewportStartOffset.toFloat(),
        viewportEndOffset = state.layoutInfo.viewportEndOffset.toFloat(),
        color = color,
        width = width
    )
}

@Composable
private fun Modifier.drawScrollbarInternal(
    isScrollInProgress: Boolean,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    totalItemsCount: Int,
    visibleItemsCount: Int,
    viewportStartOffset: Float,
    viewportEndOffset: Float,
    color: Color,
    width: Dp
): Modifier {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isScrollInProgress, firstVisibleItemIndex, firstVisibleItemScrollOffset) {
        isVisible = true
        delay(1500)
        isVisible = false
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = if (isVisible) 150 else 500),
        label = "scrollbarAlpha"
    )

    return this.drawWithContent {
        drawContent()

        if (visibleItemsCount > 0 && totalItemsCount > visibleItemsCount && alpha > 0f) {
            val viewportHeight = viewportEndOffset - viewportStartOffset
            
            val totalItems = totalItemsCount.toFloat()
            val visibleItems = visibleItemsCount.toFloat()
            
            val scrollbarHeight = (visibleItems / totalItems) * viewportHeight
            val scrollbarOffset = (firstVisibleItemIndex.toFloat() / totalItems) * viewportHeight

            drawRoundRect(
                color = color.copy(alpha = color.alpha * alpha),
                topLeft = Offset(this.size.width - width.toPx() - 4.dp.toPx(), scrollbarOffset + viewportStartOffset),
                size = Size(width.toPx(), scrollbarHeight.coerceAtLeast(24.dp.toPx())),
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
            )
        }
    }
}
