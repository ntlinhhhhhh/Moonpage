package com.diary.moonpage.ui.screens.moment.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DashedDivider(
    color: Color = Color.Gray,
    thickness: Dp = 1.dp,
    dashLength: Dp = 5.dp,
    gapLength: Dp = 5.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(thickness)) {
        val dashLengthPx = dashLength.toPx()
        val gapLengthPx = gapLength.toPx()
        val thicknessPx = thickness.toPx()
        
        drawLine(
            color = color,
            start = Offset(0f, thicknessPx / 2),
            end = Offset(size.width, thicknessPx / 2),
            strokeWidth = thicknessPx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLengthPx, gapLengthPx), 0f)
        )
    }
}
