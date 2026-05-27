package com.diary.moonpage.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.diary.moonpage.R
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.ui.components.feedback.SnackbarType
import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.core.util.resolveLogDate
import com.diary.moonpage.core.util.MoonIcons
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
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: DailyLogRepository,
    private val momentRepository: com.diary.moonpage.domain.repository.MomentRepository,
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val locationTracker: com.diary.moonpage.core.util.LocationTracker,
    private val weatherRepository: com.diary.moonpage.domain.repository.WeatherRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

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
                loadCustomMoods()
            }
        }
        viewModelScope.launch {
            themeRepository.activeTheme.collect {
                loadCustomMoods()
            }
        }
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(currentStreak = user.currentStreak) }
                }
            }
        }

        viewModelScope.launch {
            try {
                momentRepository.getMyMoments()
                val response = statisticsRepository.getGlobalSummary()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(currentStreak = response.body()?.currentStreak ?: 0) }
                }
            } catch (e: Exception) {
                android.util.Log.e("CalendarVM", "Failed to fetch streak", e)
            }
        }
        
        // Auto background weather prefetch if location permission exists
        viewModelScope.launch {
            try {
                val location = locationTracker.getCurrentLocation()
                if (location != null) {
                    val today = LocalDate.now()
                    val result = weatherRepository.getWeatherConditions(location.latitude, location.longitude, today)
                    result.onSuccess { weatherResult ->
                        weatherRepository.setCachedWeather(today, weatherResult)
                        android.util.Log.d("CalendarVM", "Auto background weather fetched and cached: ${weatherResult.conditions}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CalendarVM", "Auto background weather fetch failed", e)
            }
        }
        
        observeData()
    }

    private suspend fun loadCustomMoods() {
        val activeThemeId = themeRepository.getActiveThemeId()
        if (activeThemeId == null) {
            _uiState.update { it.copy(customMoods = null) }
            return
        }

        val moodEntities = themeRepository.getMoodsForTheme(activeThemeId)
        if (moodEntities.isEmpty()) {
            _uiState.update { it.copy(customMoods = null) }
            return
        }

        val currentTheme = _uiState.value.themeType
        val customMoods = moodEntities.associate { entity ->
            val level = when (entity.baseMoodId) {
                "1" -> 1
                "2" -> 2
                "3" -> 3
                "4" -> 4
                "5" -> 5
                "Awful", "Very Sad" -> 1
                "Bad", "Sad" -> 2
                "Meh", "Neutral" -> 3
                "Good", "Happy" -> 4
                "Rad", "Very Happy" -> 5
                else -> 3
            }

            val color = MoonIcons.parseThemeColor(entity.iconUrl)
                ?: MoonIcons.Moods.getMoodColor(level, currentTheme)

            level to com.diary.moonpage.core.util.MoonIcon(
                color = color,
                name = entity.customName,
                drawableRes = when (level) {
                    1 -> com.diary.moonpage.R.drawable.very_sad
                    2 -> com.diary.moonpage.R.drawable.sad
                    3 -> com.diary.moonpage.R.drawable.neutral
                    4 -> com.diary.moonpage.R.drawable.happy
                    5 -> com.diary.moonpage.R.drawable.very_happy
                    else -> com.diary.moonpage.R.drawable.neutral
                }
            )
        }
        _uiState.update { it.copy(customMoods = customMoods) }
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
                        val momentDate = moment.resolveLogDate() ?: return@forEach
                        if (YearMonth.from(momentDate) == month) {
                            val existingLog = logsMap[momentDate]
                            val momentPhotoUrl = normalizeAppImageUrl(moment.imageUrl) ?: return@forEach
                            
                            if (existingLog != null) {
                                val logPhotos = existingLog.dailyPhotos.orEmpty().mapNotNull(::normalizeAppImageUrl)
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
                    log.copy(dailyPhotos = log.dailyPhotos?.mapNotNull(::normalizeAppImageUrl))
                }

                val periodDates = finalMap.values.filter { it.isMenstruation }.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.toSet()
                val menstruationDays = finalMap.keys.associateWith { date ->
                    if (periodDates.contains(date)) {
                        var day = 1
                        var cursor = date.minusDays(1)
                        while (periodDates.contains(cursor)) {
                            day += 1
                            cursor = cursor.minusDays(1)
                        }
                        day
                    } else 0
                }.filter { it.value > 0 }

                _uiState.update { it.copy(dailyLogs = finalMap, menstruationDays = menstruationDays, currentYearMonth = month, isLoading = false) }
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
                _uiState.update { it.copy(selectedFilters = event.filters, showFilterSheet = false) }
            }
            CalendarUiEvent.OnClearFilters -> {
                _uiState.update { it.copy(selectedFilters = emptyList()) }
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
        _uiState.update { it.copy(isLoading = true) }
        refreshTrigger.update { it + 1 }
        viewModelScope.launch {
            momentRepository.getMyMoments()
        }
    }

    private fun deleteDailyLog(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val dateString = date.toString()
            
            try {
                // 1. Fetch latest moments from server
                val momentsResult = momentRepository.getMyMoments()
                val allMoments = momentsResult.getOrNull()
                
                if (allMoments != null) {
                    // 2. Identify moments for this date
                    val momentsToDelete = allMoments.filter { it.resolveLogDate() == date }
                    
                    // 3. Delete each moment via API
                    for (moment in momentsToDelete) {
                        momentRepository.deleteMoment(moment.id)
                    }
                }

                // 4. Delete the log
                repository.deleteDailyLog(dateString).onSuccess {
                    statisticsRepository.triggerRefresh()
                    _uiState.update { currentState ->
                        val newLogs = currentState.dailyLogs.filterKeys { it != date }
                        currentState.copy(
                            dailyLogs = newLogs,
                            selectedDate = null,
                            isLoading = false,
                            snackbarMessage = UiText.StringResource(R.string.record_deleted_success),
                            snackbarType = SnackbarType.SUCCESS
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            snackbarMessage = exception.message?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.failed_delete_log),
                            snackbarType = SnackbarType.ERROR
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = e.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.failed_delete_log),
                        snackbarType = SnackbarType.ERROR
                    )
                }
            }
        }
    }

    fun showSnackbar(message: UiText, type: SnackbarType = SnackbarType.INFO) {
        _uiState.update { it.copy(snackbarMessage = message, snackbarType = type) }
    }
}


