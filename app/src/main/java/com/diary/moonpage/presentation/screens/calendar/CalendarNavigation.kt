package com.diary.moonpage.presentation.screens.calendar

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.diary.moonpage.presentation.navigation.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

fun NavController.navigateToCalendar(navOptions: NavOptions? = null) {
    this.navigate(Screen.Calendar.route, navOptions)
}

fun NavGraphBuilder.calendarScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyLog: (String) -> Unit,
    onNavigateToShareLog: (String) -> Unit,
    onNavigateToThemeCalendar: () -> Unit
) {
    composable(route = Screen.Calendar.route) { backStackEntry ->
        val savedStateHandle = backStackEntry.savedStateHandle
        // Use collectAsStateWithLifecycle to ensure we respect the NavBackStackEntry's lifecycle.
        // This prevents IllegalStateException when the entry is being destroyed.
        val createdLogDate by savedStateHandle.getStateFlow<String?>("created_log_date", null).collectAsStateWithLifecycle()
        val logSavedMessage by savedStateHandle.getStateFlow<String?>("logSavedMessage", null).collectAsStateWithLifecycle()

        CalendarScreen(
            createdLogDate = createdLogDate,
            onLogDateHandled = { savedStateHandle.set("created_log_date", null) },
            logSavedMessage = logSavedMessage,
            onMessageShown = { savedStateHandle.set("logSavedMessage", null) },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDailyLog = onNavigateToDailyLog,
            onNavigateToShareLog = onNavigateToShareLog,
            onNavigateToThemeCalendar = onNavigateToThemeCalendar
        )
    }
}
