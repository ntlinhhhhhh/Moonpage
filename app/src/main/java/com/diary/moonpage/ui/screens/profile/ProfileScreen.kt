package com.diary.moonpage.ui.screens.profile

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
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
import com.diary.moonpage.ui.screens.profile.components.*
import com.diary.moonpage.ui.components.layout.SectionTitle
import com.diary.moonpage.ui.components.refresh.MoonPullToRefreshBox
import com.diary.moonpage.core.theme.*
import com.diary.moonpage.core.util.StreakFreezeIcon
import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep

/**
 * Stateful Component for Profile Screen
 */
@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAccount: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToThemeCalendar: () -> Unit,
    onNavigateToWidgets: () -> Unit,
    onNavigateToInviteFriend: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToStreakStats: () -> Unit,
    onNavigateToStore: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadMyThemes()
        viewModel.loadStatistics()
    }

    if (uiState.isLoading && uiState.user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        ProfileScreen(
            userId = uiState.user?.id?.take(8) ?: "",
            userName = uiState.user?.name ?: stringResource(R.string.user_fallback),
            avatarUrl = uiState.user?.avatarUrl,
            localAvatarPath = uiState.localAvatarPath,
            tempAvatarPath = uiState.tempAvatarPath,
            recordedDays = uiState.totalLogs.toString(),
            photoCount = uiState.totalPhotos.toString(),
            currentStreak = uiState.currentStreak.toString(),
            longestStreak = uiState.longestStreak.toString(),
            streakFreezeCount = uiState.streakFreezeCount.toString(),
            onNotificationClick = onNavigateToNotifications,
            onSettingsClick = onNavigateToSettings,
            onAccountClick = onNavigateToAccount,
            onPhotosClick = onNavigateToPhotos,
            onThemeCalendarClick = onNavigateToThemeCalendar,
            onWidgetsClick = onNavigateToWidgets,
            onInviteFriendClick = onNavigateToInviteFriend,
            onStatsClick = onNavigateToStats,
            onStreakClick = onNavigateToStreakStats,
            onStreakFreezesClick = onNavigateToStore,
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::refreshProfile
        )
    }
}

/**
 * Stateless Content for Profile Screen
 */
@Composable
fun ProfileScreen(
    userId: String,
    userName: String,
    avatarUrl: String?,
    localAvatarPath: String? = null,
    tempAvatarPath: String? = null,
    recordedDays: String,
    photoCount: String,
    currentStreak: String,
    longestStreak: String,
    streakFreezeCount: String,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onPhotosClick: () -> Unit,
    onThemeCalendarClick: () -> Unit,
    onWidgetsClick: () -> Unit,
    onInviteFriendClick: () -> Unit,
    onStatsClick: () -> Unit,
    onStreakClick: () -> Unit,
    onStreakFreezesClick: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            ProfileHeader(
                title = stringResource(R.string.my_info),
                onNotificationClick = onNotificationClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        MoonPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionTitle(stringResource(R.string.account))
                Box(modifier = Modifier.fillMaxWidth().tutorialTarget(TutorialStep.HighlightProfileSettings)) {
                    UserInfoCard(
                        userId = if (userId.isNotEmpty()) "#$userId" else "",
                        userName = userName,
                        avatarUrl = avatarUrl,
                        localAvatarPath = localAvatarPath,
                        tempAvatarPath = tempAvatarPath,
                        onClick = onAccountClick
                    )
                }

                SectionTitle(stringResource(R.string.my_records))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        title = stringResource(R.string.report),
                        value = recordedDays,
                        icon = Icons.Rounded.CalendarToday,
                        modifier = Modifier.weight(1f),
                        onClick = onStatsClick
                    )
                    ActionCard(title = stringResource(R.string.my_photos), value = photoCount, modifier = Modifier.weight(1f), onClick = onPhotosClick)
                }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    title = stringResource(R.string.streak),
                    value = currentStreak,
                    icon = Icons.Rounded.Whatshot,
                    modifier = Modifier.weight(1f),
                    onClick = onStreakClick
                )
                ActionCard(
                    title = stringResource(R.string.streak_freezes),
                    value = streakFreezeCount,
                    icon = StreakFreezeIcon,
                    modifier = Modifier.weight(1f),
                    onClick = onStreakFreezesClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(title = stringResource(R.string.theme_calendar), icon = Icons.Rounded.CalendarMonth, onClick = onThemeCalendarClick)

            SectionTitle(stringResource(R.string.more))

            ProfileMenuItem(title = stringResource(R.string.widgets), icon = Icons.Rounded.Widgets, onClick = onWidgetsClick)

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(title = stringResource(R.string.invite_a_friend), icon = Icons.Rounded.PersonAdd, onClick = onInviteFriendClick)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MoonPageTheme {
        ProfileScreen(
            userId = "0320",
            userName = stringResource(R.string.user_fallback),
            avatarUrl = null,
            recordedDays = "8",
            photoCount = "3",
            currentStreak = "5",
            longestStreak = "12",
            streakFreezeCount = "2",
            onNotificationClick = {},
            onSettingsClick = {},
            onAccountClick = {},
            onPhotosClick = {},
            onThemeCalendarClick = {},
            onWidgetsClick = {},
            onInviteFriendClick = {},
            onStatsClick = {},
            onStreakClick = {},
            onStreakFreezesClick = {}
        )
    }
}

