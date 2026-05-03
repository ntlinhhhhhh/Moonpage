package com.diary.moonpage.presentation.screens.calendar

import java.time.LocalDate
import java.time.YearMonth

sealed interface CalendarUiEvent {
    data class OnDateSelected(val date: LocalDate) : CalendarUiEvent
    data class OnMonthChanged(val yearMonth: YearMonth) : CalendarUiEvent
    data class OnDeleteLog(val date: LocalDate) : CalendarUiEvent
    data class OnMonthPickerConfirm(val year: Int, val month: Int) : CalendarUiEvent
    object OnMonthPickerClick : CalendarUiEvent
    object OnMonthPickerDismiss : CalendarUiEvent
    object OnFilterClick : CalendarUiEvent
    object OnFilterDismiss : CalendarUiEvent
    object DismissMessage : CalendarUiEvent
}
