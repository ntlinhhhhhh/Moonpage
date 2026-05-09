package com.diary.moonpage.presentation.components.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TopCircularIcon() {
    val bgColor = MaterialTheme.colorScheme.surface
    val iconColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.08f))
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector =  Icons.Outlined.LockReset,
            contentDescription = "Reset Icon",
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
    }
}
