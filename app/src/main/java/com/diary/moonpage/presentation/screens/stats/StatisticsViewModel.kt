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
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.getStatisticsSummary(
                    _uiState.value.selectedYear,
                    _uiState.value.selectedMonth,
                    _uiState.value.isMonthly
                )
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    
                    // Enhancement: Ensure we have data for the new charts (Mocking if missing)
                    val enhancedStats = stats.copy(
                        sleepData = stats.sleepData ?: generateMockSleepData(_uiState.value.selectedYear, _uiState.value.selectedMonth),
                        stepsData = stats.stepsData ?: generateMockStepsData(_uiState.value.selectedYear, _uiState.value.selectedMonth),
                        menstruationData = stats.menstruationData ?: if (_uiState.value.gender != "Male") listOf("2026-05-12", "2026-05-13", "2026-05-14", "2026-05-15") else emptyList(),
                        moodBySleep = stats.moodBySleep ?: generateMockMoodBySleep(),
                        yearlyMoodGrid = stats.yearlyMoodGrid ?: generateMockYearlyGrid(_uiState.value.selectedYear)
                    )

                    val freq = enhancedStats.bestActivities.sortedByDescending { it.occurrence }.take(3)
                    val best = enhancedStats.bestActivities.sortedByDescending { it.averageMoodScore }.take(3)
                    val worst = enhancedStats.bestActivities.sortedBy { it.averageMoodScore }.take(3)
                    
                    _uiState.update { it.copy(
                        stats = enhancedStats, 
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

    private fun generateMockSleepData(year: Int, month: Int): List<com.diary.moonpage.data.remote.dto.stats.SleepDataDto> {
        val days = java.time.YearMonth.of(year, month).lengthOfMonth()
        return (1..days).map { d ->
            com.diary.moonpage.data.remote.dto.stats.SleepDataDto(
                date = String.format("%04d-%02d-%02d", year, month, d),
                hours = (6..9).random() + (0..9).random() / 10.0
            )
        }
    }

    private fun generateMockStepsData(year: Int, month: Int): List<com.diary.moonpage.data.remote.dto.stats.StepsDataDto> {
        val days = java.time.YearMonth.of(year, month).lengthOfMonth()
        return (1..days).map { d ->
            com.diary.moonpage.data.remote.dto.stats.StepsDataDto(
                date = String.format("%04d-%02d-%02d", year, month, d),
                count = (2000..12000).random()
            )
        }
    }

    private fun generateMockMoodBySleep(): List<com.diary.moonpage.data.remote.dto.stats.MoodBySleepDto> {
        return listOf(
            com.diary.moonpage.data.remote.dto.stats.MoodBySleepDto("< 5h", listOf(
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Bad", 40, 40),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Low", 30, 30),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Meh", 20, 20),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Good", 10, 10),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Rad", 0, 0)
            )),
            com.diary.moonpage.data.remote.dto.stats.MoodBySleepDto("6-7h", listOf(
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Bad", 10, 10),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Low", 20, 20),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Meh", 40, 40),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Good", 20, 20),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Rad", 10, 10)
            )),
            com.diary.moonpage.data.remote.dto.stats.MoodBySleepDto("8h+", listOf(
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Bad", 5, 5),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Low", 5, 5),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Meh", 10, 10),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Good", 40, 40),
                com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto("Rad", 40, 40)
            ))
        )
    }

    private fun generateMockYearlyGrid(year: Int): List<com.diary.moonpage.data.remote.dto.stats.MoodFlowDto> {
        val list = mutableListOf<com.diary.moonpage.data.remote.dto.stats.MoodFlowDto>()
        for (m in 1..12) {
            val days = java.time.YearMonth.of(year, m).lengthOfMonth()
            for (d in 1..days) {
                list.add(com.diary.moonpage.data.remote.dto.stats.MoodFlowDto(
                    date = String.format("%04d-%02d-%02d", year, m, d),
                    moodId = (1..5).random()
                ))
            }
        }
        return list
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
