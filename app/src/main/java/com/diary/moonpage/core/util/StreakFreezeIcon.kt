package com.diary.moonpage.core.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val StreakFreezeIcon: ImageVector
    get() {
        if (_streakFreezeIcon != null) {
            return _streakFreezeIcon!!
        }
        _streakFreezeIcon = ImageVector.Builder(
            name = "StreakFreezeIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF2196F3)),
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                strokeLineWidth = 0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2f)
                lineTo(4.5f, 6.5f)
                verticalLineTo(15.5f)
                lineTo(12f, 20f)
                lineTo(19.5f, 15.5f)
                verticalLineTo(6.5f)
                lineTo(12f, 2f)
                close()
                moveTo(12f, 17f)
                curveTo(10.34f, 17f, 9f, 15.66f, 9f, 14f)
                curveTo(9f, 12.34f, 12f, 8f, 12f, 8f)
                curveTo(12f, 8f, 15f, 12.34f, 15f, 14f)
                curveTo(15f, 15.66f, 13.66f, 17f, 12f, 17f)
                close()
            }
        }.build()
        return _streakFreezeIcon!!
    }

private var _streakFreezeIcon: ImageVector? = null
