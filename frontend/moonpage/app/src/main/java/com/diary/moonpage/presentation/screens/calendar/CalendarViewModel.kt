package com.diary.moonpage.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.domain.repository.DailyLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

import com.diary.moonpage.core.util.ActivityPreferencesManager

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: DailyLogRepository,
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            activityPreferencesManager.activities.collect { activities ->
                _uiState.update { it.copy(dynamicActivities = activities) }
            }
        }
        viewModelScope.launch {
            themePreferencesManager.themeType.collect { themeType ->
                _uiState.update { it.copy(themeType = themeType) }
            }
        }
        fetchLogsForMonth(_uiState.value.currentYearMonth)
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.OnDateSelected -> {
                _uiState.update { currentState ->
                    val newDate = if (currentState.selectedDate == event.date) null else event.date
                    currentState.copy(selectedDate = newDate)
                }
            }
            is CalendarUiEvent.OnMonthChanged -> {
                _uiState.update { it.copy(currentYearMonth = event.yearMonth) }
                fetchLogsForMonth(event.yearMonth)
            }
            is CalendarUiEvent.OnDeleteLog -> {
                deleteDailyLog(event.date)
            }
            is CalendarUiEvent.OnMonthPickerConfirm -> {
                val newMonth = YearMonth.of(event.year, event.month)
                _uiState.update { it.copy(currentYearMonth = newMonth, showMonthPicker = false) }
                fetchLogsForMonth(newMonth)
            }
            CalendarUiEvent.OnMonthPickerClick -> {
                _uiState.update { it.copy(showMonthPicker = true) }
            }
            CalendarUiEvent.OnMonthPickerDismiss -> {
                _uiState.update { it.copy(showMonthPicker = false) }
            }
            CalendarUiEvent.OnFilterClick -> {
                _uiState.update { it.copy(showFilterSheet = true) }
            }
            CalendarUiEvent.OnFilterDismiss -> {
                _uiState.update { it.copy(showFilterSheet = false) }
            }
            CalendarUiEvent.OnShareClick -> {
                _uiState.update { it.copy(showShareSheet = true) }
            }
            CalendarUiEvent.OnShareDismiss -> {
                _uiState.update { it.copy(showShareSheet = false) }
            }
            is CalendarUiEvent.OnShareModeSelected -> {
                _uiState.update { it.copy(showShareSheet = false) }
            }
            is CalendarUiEvent.OnFilterMoodToggled -> {
                _uiState.update { currentState ->
                    val newFilters = currentState.filterMoodIds.toMutableSet()
                    if (newFilters.contains(event.moodId)) newFilters.remove(event.moodId)
                    else newFilters.add(event.moodId)
                    currentState.copy(filterMoodIds = newFilters)
                }
            }
            is CalendarUiEvent.OnFilterActivityToggled -> {
                _uiState.update { currentState ->
                    val newFilters = currentState.filterActivityIds.toMutableSet()
                    if (newFilters.contains(event.activityId)) newFilters.remove(event.activityId)
                    else newFilters.add(event.activityId)
                    currentState.copy(filterActivityIds = newFilters)
                }
            }
            CalendarUiEvent.OnClearFilters -> {
                _uiState.update { it.copy(filterMoodIds = emptySet(), filterActivityIds = emptySet()) }
            }
            CalendarUiEvent.DismissMessage -> {
                _uiState.update { it.copy(snackbarMessage = null) }
            }
            else -> {}
        }
    }

    fun refreshLogs() {
        fetchLogsForMonth(_uiState.value.currentYearMonth)
    }

    private fun fetchLogsForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val yearMonthStr = "${yearMonth.year}-${yearMonth.monthValue.toString().padStart(2, '0')}"

            repository.getDailyLogsByMonth(yearMonthStr).collect { logs ->
                val logsMap = logs.associateBy { LocalDate.parse(it.date) }
                _uiState.update { currentState ->
                    // Instead of putAll which keeps old entries, we want to update the month's data
                    // However, to keep it simple and reactive, we'll just use the new logs from the repository
                    // If the repository is the source of truth, this map should be correct.
                    currentState.copy(dailyLogs = logsMap, isLoading = false)
                }
            }
        }
    }

    private fun deleteDailyLog(date: LocalDate) {
        viewModelScope.launch {
            repository.deleteDailyLog(date.toString()).onSuccess {
                // Remove the deleted log from the local state immediately for instant feedback
                _uiState.update { currentState ->
                    val newLogs = currentState.dailyLogs.filterKeys { it != date }
                    currentState.copy(dailyLogs = newLogs, selectedDate = null, snackbarMessage = "Record deleted successfully!")
                }
                refreshLogs()
            }.onFailure { exception ->
                _uiState.update { it.copy(snackbarMessage = exception.message ?: "Failed to delete log") }
            }
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }
}
