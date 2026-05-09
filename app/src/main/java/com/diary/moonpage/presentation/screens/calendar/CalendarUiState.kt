package com.diary.moonpage.presentation.screens.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.Activity
import com.diary.moonpage.core.theme.MoonThemeType
import java.time.LocalDate
import java.time.YearMonth

sealed class FilterItem {
    data class Mood(val id: Int) : FilterItem()
    data class Activity(val id: String, val name: String) : FilterItem()
    data class Special(val id: String, val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) : FilterItem()
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = LocalDate.now(),
    val dailyLogs: Map<LocalDate, DailyLog> = emptyMap(),
    val dynamicActivities: List<Activity> = emptyList(),
    val snackbarMessage: String? = null,
    val showMonthPicker: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showShareSheet: Boolean = false,
    val selectedFilter: FilterItem? = null,
    val themeType: MoonThemeType = MoonThemeType.DEFAULT
)
