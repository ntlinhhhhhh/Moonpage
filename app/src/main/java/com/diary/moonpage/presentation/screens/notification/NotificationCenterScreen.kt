package com.diary.moonpage.presentation.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.data.remote.dto.notification.NotificationDto
import com.diary.moonpage.data.remote.dto.notification.NotificationType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notification Center", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    if (uiState.notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.deleteAll() }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (uiState.isLoading && uiState.notifications.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.notifications.isEmpty()) {
                EmptyNotifications()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = { viewModel.markAsRead(notification.id) },
                            onDelete = { viewModel.deleteNotification(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotifications() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "All caught up!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Check back later for updates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun NotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = getNotificationStyle(notification.type)
    
    // Unified design: no elevation difference to avoid "2 boxes" feel
    // Use a subtler but full-box color change for read status
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (notification.isRead) 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else 
            MoonTheme.customColors.logItemBg.copy(alpha = 0.8f),
        border = BorderStroke(
            width = 1.dp,
            color = if (notification.isRead) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Section - Integrated smoothly
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (notification.isRead) color.copy(alpha = 0.1f) else color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (notification.isRead) color.copy(alpha = 0.6f) else color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                            color = if (notification.isRead) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    // Blue dot only for unread
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notification.isRead) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun getNotificationStyle(type: String?): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.REMINDER -> Icons.Rounded.Notifications to Color(0xFF5C6BC0)
        NotificationType.STREAK -> Icons.Rounded.EmojiEvents to Color(0xFFFFD54F)
        NotificationType.MEMORY_LANE -> Icons.Rounded.Schedule to Color(0xFFAB47BC)
        NotificationType.MOOD_TREND -> Icons.Rounded.Favorite to Color(0xFFEC407A)
        NotificationType.MONTHLY_REPORT -> Icons.Rounded.BarChart to Color(0xFF66BB6A)
        NotificationType.WEATHER -> Icons.Rounded.WbSunny to Color(0xFFFFB74D)
        else -> Icons.Rounded.NotificationsNone to Color(0xFF78909C)
    }
}

private fun formatNotificationTime(createdAt: String): String {
    return try {
        val date = ZonedDateTime.parse(createdAt)
        val now = ZonedDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(date, now)
        val hours = ChronoUnit.HOURS.between(date, now)
        val days = ChronoUnit.DAYS.between(date, now)

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes m ago"
            hours < 24 -> "$hours h ago"
            days < 7 -> "$days d ago"
            else -> date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    } catch (e: Exception) {
        createdAt
    }
}
