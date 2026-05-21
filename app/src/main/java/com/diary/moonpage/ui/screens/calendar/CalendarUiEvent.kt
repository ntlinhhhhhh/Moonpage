package com.diary.moonpage.ui.screens.calendar

import java.time.LocalDate
import java.time.YearMonth

/**
 * Events: UI -> VM
 */
sealed class CalendarUiEvent {
    data class OnDateSelected(val date: LocalDate) : CalendarUiEvent()
    data class ForceDateSelected(val date: LocalDate) : CalendarUiEvent()
    data class OnMonthChanged(val yearMonth: YearMonth) : CalendarUiEvent()
    data class OnDeleteLog(val date: LocalDate) : CalendarUiEvent()
    data class OnMonthPickerConfirm(val year: Int, val month: Int) : CalendarUiEvent()
    object OnMonthPickerClick : CalendarUiEvent()
    object OnMonthPickerDismiss : CalendarUiEvent()
    object OnFilterClick : CalendarUiEvent()
    object OnFilterDismiss : CalendarUiEvent()
    object OnShareDismiss : CalendarUiEvent()
    data class ApplyFilter(val filters: List<FilterItem>) : CalendarUiEvent()
    object OnClearFilters : CalendarUiEvent()
    object DismissMessage : CalendarUiEvent()
    object OnSettingsClick : CalendarUiEvent()
    object OnThemeClick : CalendarUiEvent()
    object ToggleViewMode : CalendarUiEvent()
}

/**
 * Effects: VM -> UI (One-time events)
 */
sealed class CalendarUiEffect {
    data class ShowSnackBar(val message: String) : CalendarUiEffect()
    data class NavigateToDailyLog(val date: String) : CalendarUiEffect()
    object NavigateToSettings : CalendarUiEffect()
    object NavigateToThemeCalendar : CalendarUiEffect()
}
