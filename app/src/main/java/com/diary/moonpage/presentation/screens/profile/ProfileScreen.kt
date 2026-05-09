package com.diary.moonpage.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.presentation.components.profile.*
import com.diary.moonpage.presentation.components.core.layout.SectionTitle
import com.diary.moonpage.presentation.theme.*

/**
 * Stateful Component for Profile Screen
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAccount: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToThemeCalendar: () -> Unit,
    onNavigateToWidgets: () -> Unit,
    onNavigateToInviteFriend: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadMyThemes()
    }

    if (uiState.isLoading && uiState.user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        ProfileScreenContent(
            userId = uiState.user?.id?.take(8) ?: "",
            userName = uiState.user?.name ?: "User",
            avatarUrl = uiState.user?.avatarUrl,
            recordedDays = "8",
            photoCount = uiState.myThemes.size.toString(),
            onNotificationClick = onNavigateToNotifications,
            onSettingsClick = onNavigateToSettings,
            onAccountClick = onNavigateToAccount,
            onPhotosClick = onNavigateToPhotos,
            onThemeCalendarClick = onNavigateToThemeCalendar,
            onWidgetsClick = onNavigateToWidgets,
            onInviteFriendClick = onNavigateToInviteFriend
        )
    }
}

/**
 * Stateless Content for Profile Screen
 */
@Composable
fun ProfileScreenContent(
    userId: String,
    userName: String,
    avatarUrl: String?,
    recordedDays: String,
    photoCount: String,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onPhotosClick: () -> Unit,
    onThemeCalendarClick: () -> Unit,
    onWidgetsClick: () -> Unit,
    onInviteFriendClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            ProfileHeader(
                title = "My Info",
                onNotificationClick = onNotificationClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SectionTitle("Account")
            UserInfoCard(
                userId = if (userId.isNotEmpty()) "#$userId" else "",
                userName = userName,
                avatarUrl = avatarUrl,
                onClick = onAccountClick
            )

            SectionTitle("My records")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(title = "Recorded days", value = recordedDays, modifier = Modifier.weight(1f))
                ActionCard(title = "My Photos", value = photoCount, modifier = Modifier.weight(1f), onClick = onPhotosClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(title = "Theme Calendar", icon = Icons.Rounded.CalendarMonth, onClick = onThemeCalendarClick)

            SectionTitle("More")

            ProfileMenuItem(title = "Widgets", icon = Icons.Rounded.Widgets, onClick = onWidgetsClick)

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(title = "Invite a Friend", icon = Icons.Rounded.PersonAdd, onClick = onInviteFriendClick)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MoonPageTheme {
        ProfileScreenContent(
            userId = "0320",
            userName = "Moon User",
            avatarUrl = null,
            recordedDays = "8",
            photoCount = "3",
            onNotificationClick = {},
            onSettingsClick = {},
            onAccountClick = {},
            onPhotosClick = {},
            onThemeCalendarClick = {},
            onWidgetsClick = {},
            onInviteFriendClick = {}
        )
    }
}
