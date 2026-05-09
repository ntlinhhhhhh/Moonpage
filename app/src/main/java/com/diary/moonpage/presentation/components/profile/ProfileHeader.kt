package com.diary.moonpage.presentation.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(
    title: String,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 20.sp
                )
            )
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = textColor.copy(alpha = 0.6f))
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = textColor.copy(alpha = 0.6f))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}
