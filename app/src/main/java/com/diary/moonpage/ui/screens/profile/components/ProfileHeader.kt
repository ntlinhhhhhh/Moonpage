package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileHeader(
    title: String,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val textColor = colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 20.sp
            )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = textColor.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
