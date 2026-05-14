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
                    val best = stats.bestActivities.sortedByDescending { it.averageMoodScore }.take(3)
                    val worst = stats.bestActivities.sortedBy { it.averageMoodScore }.take(3)
                    
                    _uiState.update { it.copy(
                        stats = stats, 
                        frequentlyRecorded = freq,
                        bestActivities = best,
                        worstActivities = worst,
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
}
