package com.diary.moonpage.presentation.screens.calendar

import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.model.Activity
import com.diary.moonpage.presentation.theme.MoonThemeType
import java.time.LocalDate
import java.time.YearMonth

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
    val filterMoodIds: Set<Int> = emptySet(),
    val filterActivityIds: Set<String> = emptySet(),
    val themeType: MoonThemeType = MoonThemeType.DEFAULT
)
