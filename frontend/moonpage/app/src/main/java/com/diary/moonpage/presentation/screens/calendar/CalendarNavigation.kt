package com.diary.moonpage.presentation.screens.calendar

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.diary.moonpage.presentation.navigation.Screen

fun NavController.navigateToCalendar(navOptions: NavOptions? = null) {
    this.navigate(Screen.Calendar.route, navOptions)
}

fun NavGraphBuilder.calendarScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyLog: (String) -> Unit,
    onNavigateToThemeCalendar: () -> Unit
) {
    composable(route = Screen.Calendar.route) { backStackEntry ->
        val savedStateHandle = backStackEntry.savedStateHandle
        val createdLogDate by savedStateHandle.getStateFlow<String?>("created_log_date", null).collectAsState()
        val logSavedMessage by savedStateHandle.getStateFlow<String?>("logSavedMessage", null).collectAsState()

        CalendarScreen(
            createdLogDate = createdLogDate,
            onLogDateHandled = { savedStateHandle.set("created_log_date", null) },
            logSavedMessage = logSavedMessage,
            onMessageShown = { savedStateHandle.set("logSavedMessage", null) },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDailyLog = onNavigateToDailyLog,
            onNavigateToThemeCalendar = onNavigateToThemeCalendar
        )
    }
}
