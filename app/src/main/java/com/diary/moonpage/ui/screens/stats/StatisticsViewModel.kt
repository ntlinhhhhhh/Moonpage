package com.diary.moonpage.ui.screens.stats

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
                    
                    // Apply initial filtering and sorting
                    val filtered = filterAndSortActivities(
                        stats.bestActivities,
                        _uiState.value.activityFilter,
                        _uiState.value.sortOrder
                    )

                    // Improved Correlation Algorithm for Best/Worst
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
                        filteredActivities = filtered,
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

    fun updateFilter(category: String, isSelected: Boolean) {
        _uiState.update { currentState ->
            val newFilter = if (isSelected) {
                currentState.activityFilter + category
            } else {
                currentState.activityFilter - category
            }
            currentState.copy(
                activityFilter = newFilter,
                filteredActivities = filterAndSortActivities(
                    currentState.stats?.bestActivities ?: emptyList(),
                    newFilter,
                    currentState.sortOrder
                )
            )
        }
    }

    fun toggleSortOrder() {
        _uiState.update { currentState ->
            val newSortOrder = if (currentState.sortOrder == SortOrder.MOST_RECORDED) {
                SortOrder.LEAST_RECORDED
            } else {
                SortOrder.MOST_RECORDED
            }
            currentState.copy(
                sortOrder = newSortOrder,
                filteredActivities = filterAndSortActivities(
                    currentState.stats?.bestActivities ?: emptyList(),
                    currentState.activityFilter,
                    newSortOrder
                )
            )
        }
    }

    private fun filterAndSortActivities(
        activities: List<com.diary.moonpage.data.remote.dto.stats.BestActivityDto>,
        filter: Set<String>,
        sortOrder: SortOrder
    ): List<com.diary.moonpage.data.remote.dto.stats.BestActivityDto> {
        val filtered = if (filter.isEmpty()) activities else activities.filter { filter.contains(it.activityName) }
        return when (sortOrder) {
            SortOrder.MOST_RECORDED -> filtered.sortedByDescending { it.occurrence }
            SortOrder.LEAST_RECORDED -> filtered.sortedBy { it.occurrence }
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

    fun downloadRecapCard(context: android.content.Context, bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                com.diary.moonpage.core.util.ImageUtils.saveBitmapToGallery(context, bitmap)
            } catch (e: Exception) {
                _uiState.update { it.copy(captureError = "Failed to download: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    fun saveRecapToGallery(context: android.content.Context, bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                com.diary.moonpage.core.util.ImageUtils.saveBitmapToGallery(context, bitmap)
                com.diary.moonpage.core.util.ImageUtils.shareImage(context, bitmap, "My Year in Beans")
            } catch (e: Exception) {
                _uiState.update { it.copy(captureError = "Failed to save: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    fun clearCaptureError() {
        _uiState.update { it.copy(captureError = null) }
    }
}
