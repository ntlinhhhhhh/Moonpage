package com.diary.moonpage.presentation.components.moment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class MomentTag(
    val id: String,
    val icon: ImageVector?,
    val text: String,
    val containerColor: Color? = null,
    val contentColor: Color = Color.White
)
