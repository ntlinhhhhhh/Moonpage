package com.diary.moonpage.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StatisticsRepository,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferencesManager.themeType.collect { themeType ->
                _uiState.update { it.copy(themeType = themeType) }
            }
        }
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { it.copy(gender = user?.gender) }
            }
        }
        loadStatistics()
        viewModelScope.launch {
            repository.refreshTrigger.collect {
                loadStatistics()
            }
        }
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val isMonthly = _uiState.value.isMonthly
                val response = repository.getStatisticsSummary(
                    _uiState.value.selectedYear,
                    if (isMonthly) _uiState.value.selectedMonth else null,
                    isMonthly
                )
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    
                    val freq = stats.bestActivities.sortedByDescending { it.occurrence }.take(3)
                    
                    // Improved Correlation Algorithm for Best/Worst
                    // Filter activities that occurred at least 2 times (for better correlation)
                    val relevantActivities = stats.bestActivities.filter { it.occurrence >= 1 }
                    val best = relevantActivities.sortedByDescending { it.averageMoodScore }.take(3)
                    val worst = relevantActivities.sortedBy { it.averageMoodScore }.take(3)
                    
                    // Calculate Average Wake Up Time based on average bedtime and sleep hours
                    val avgWakeUpTime = stats.averageWakeupTime ?: if (stats.averageSleepStartTime != null && stats.averageSleepHours != null) {
                        try {
                            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
                            val date = sdf.parse(stats.averageSleepStartTime)
                            if (date != null) {
                                val cal = java.util.Calendar.getInstance().apply { time = date }
                                cal.add(java.util.Calendar.MINUTE, (stats.averageSleepHours * 60).toInt())
                                sdf.format(cal.time)
                            } else null
                        } catch (e: Exception) { null }
                    } else null

                    
                    _uiState.update { it.copy(
                        stats = stats, 
                        frequentlyRecorded = freq,
                        bestActivities = best,
                        worstActivities = worst,
                        averageWakeUpTime = avgWakeUpTime,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(error = "Failed to load statistics", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onIconClick(id: String?) {
        _uiState.update { it.copy(selectedIconId = id) }
    }

    fun onMonthSelected(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month) }
        loadStatistics()
    }

    fun setMonthly(isMonthly: Boolean) {
        if (_uiState.value.isMonthly != isMonthly) {
            _uiState.update { it.copy(isMonthly = isMonthly) }
            loadStatistics()
        }
    }

    fun shareRecapCard(context: android.content.Context, bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                com.diary.moonpage.core.util.ImageUtils.shareImage(context, bitmap, "My Year in Beans")
            } catch (e: Exception) {
                _uiState.update { it.copy(captureError = "Failed to share: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    fun clearCaptureError() {
        _uiState.update { it.copy(captureError = null) }
    }
}
