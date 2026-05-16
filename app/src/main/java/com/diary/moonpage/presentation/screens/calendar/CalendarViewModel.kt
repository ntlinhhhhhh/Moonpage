package com.diary.moonpage.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.domain.repository.DailyLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

import com.diary.moonpage.core.util.ActivityPreferencesManager

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: DailyLogRepository,
    private val momentRepository: com.diary.moonpage.domain.repository.MomentRepository,
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository
) : ViewModel() {

    private val BASE_URL = "https://hieu-wikipedia.io.vn/"

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val currentMonth = MutableStateFlow(_uiState.value.currentYearMonth)
    private val refreshTrigger = MutableStateFlow(0)

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
        
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                currentMonth.flatMapLatest { month -> 
                    refreshTrigger.flatMapLatest {
                        val yearMonthStr = "${month.year}-${month.monthValue.toString().padStart(2, '0')}"
                        repository.getDailyLogsByMonth(yearMonthStr)
                    }
                },
                momentRepository.moments,
                currentMonth
            ) { logs, moments, month ->
                val logsMap = logs.associateBy { LocalDate.parse(it.date) }.toMutableMap()
                
                moments.forEach { moment ->
                    try {
                        val momentDate = LocalDate.parse(moment.capturedAt.substring(0, 10))
                        if (YearMonth.from(momentDate) == month) {
                            val existingLog = logsMap[momentDate]
                            val momentPhotoUrl = if (moment.imageUrl.startsWith("http")) moment.imageUrl else BASE_URL + moment.imageUrl.trimStart('/')
                            
                            if (existingLog != null) {
                                val logPhotos = existingLog.dailyPhotos?.map { if (it.startsWith("http")) it else BASE_URL + it.trimStart('/') } ?: emptyList()
                                val combinedPhotos = (logPhotos + momentPhotoUrl).distinct()
                                logsMap[momentDate] = existingLog.copy(dailyPhotos = combinedPhotos)
                            } else {
                                logsMap[momentDate] = com.diary.moonpage.domain.model.DailyLog(
                                    id = "moment_${moment.id}",
                                    baseMoodId = 0,
                                    date = momentDate.toString(),
                                    note = null,
                                    sleepHours = null,
                                    isMenstruation = false,
                                    menstruationPhase = null,
                                    dailyPhotos = listOf(momentPhotoUrl),
                                    activityIds = emptyList()
                                )
                            }
                        }
                    } catch (e: Exception) {}
                }

                val finalMap = logsMap.mapValues { (_, log) ->
                    log.copy(dailyPhotos = log.dailyPhotos?.map { if (it.startsWith("http")) it else BASE_URL + it.trimStart('/') })
                }

                _uiState.update { it.copy(dailyLogs = finalMap, currentYearMonth = month, isLoading = false) }
            }.collect {}
        }
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.OnDateSelected -> {
                _uiState.update { currentState ->
                    val newDate = if (currentState.selectedDate == event.date) null else event.date
                    currentState.copy(selectedDate = newDate)
                }
            }
            is CalendarUiEvent.ForceDateSelected -> {
                _uiState.update { it.copy(selectedDate = event.date) }
            }
            is CalendarUiEvent.OnMonthChanged -> {
                _uiState.update { it.copy(isLoading = true) }
                currentMonth.value = event.yearMonth
            }
            is CalendarUiEvent.OnDeleteLog -> {
                deleteDailyLog(event.date)
            }
            is CalendarUiEvent.OnMonthPickerConfirm -> {
                _uiState.update { it.copy(isLoading = true, showMonthPicker = false) }
                currentMonth.value = YearMonth.of(event.year, event.month)
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
            CalendarUiEvent.OnShareDismiss -> {
                _uiState.update { it.copy(showShareSheet = false) }
            }
            is CalendarUiEvent.ApplyFilter -> {
                _uiState.update { it.copy(selectedFilter = event.filterItem, showFilterSheet = false) }
            }
            CalendarUiEvent.ToggleViewMode -> {
                _uiState.update { currentState ->
                    val newMode = if (currentState.viewMode == CalendarViewMode.CALENDAR) CalendarViewMode.TIMELINE else CalendarViewMode.CALENDAR
                    currentState.copy(viewMode = newMode)
                }
            }
            CalendarUiEvent.DismissMessage -> {
                _uiState.update { it.copy(snackbarMessage = null) }
            }
            else -> {}
        }
    }

    fun refreshLogs() {
        refreshTrigger.update { it + 1 }
    }

    private fun deleteDailyLog(date: LocalDate) {
        viewModelScope.launch {
            repository.deleteDailyLog(date.toString()).onSuccess {
                statisticsRepository.triggerRefresh()
                _uiState.update { currentState ->
                    val newLogs = currentState.dailyLogs.filterKeys { it != date }
                    currentState.copy(dailyLogs = newLogs, selectedDate = null, snackbarMessage = "Record deleted successfully!")
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(snackbarMessage = exception.message ?: "Failed to delete log") }
            }
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }
}
