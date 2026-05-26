package com.diary.moonpage.ui.screens.calendar

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.diary.moonpage.ui.navigation.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

fun NavController.navigateToCalendar(navOptions: NavOptions? = null) {
    this.navigate(Screen.Calendar.route, navOptions)
}

fun NavController.navigateToShareCalendar(yearMonth: String) {
    this.navigate("share_calendar_screen/$yearMonth")
}

fun NavGraphBuilder.calendarScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyLog: (String) -> Unit,
    onNavigateToShareLog: (String) -> Unit,
    onNavigateToShareCalendar: (String) -> Unit,
    onNavigateToThemeCalendar: () -> Unit,
    onNavigateToStreakStats: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(route = Screen.Calendar.route) { backStackEntry ->
        val savedStateHandle = backStackEntry.savedStateHandle
        // Use collectAsStateWithLifecycle to ensure we respect the NavBackStackEntry's lifecycle.
        // This prevents IllegalStateException when the entry is being destroyed.
        val createdLogDate by savedStateHandle.getStateFlow<String?>("created_log_date", null).collectAsStateWithLifecycle()
        val logSavedMessageResId by savedStateHandle.getStateFlow<Int?>("logSavedMessageResId", null).collectAsStateWithLifecycle()

        CalendarRoute(
            createdLogDate = createdLogDate,
            onLogDateHandled = { savedStateHandle.set("created_log_date", null) },
            logSavedMessageResId = logSavedMessageResId,
            onMessageShown = { savedStateHandle.set("logSavedMessageResId", null) },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDailyLog = onNavigateToDailyLog,
            onNavigateToShareLog = onNavigateToShareLog,
            onNavigateToShareCalendar = onNavigateToShareCalendar,
            onNavigateToThemeCalendar = onNavigateToThemeCalendar,
            onNavigateToStreakStats = onNavigateToStreakStats
        )
    }

    composable(route = Screen.ShareCalendar.route) { backStackEntry ->
        val yearMonth = backStackEntry.arguments?.getString("yearMonth") ?: ""
        ShareCalendarRoute(
            yearMonthString = yearMonth,
            onNavigateBack = onNavigateBack
        )
    }
}
