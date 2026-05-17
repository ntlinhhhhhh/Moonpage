package com.diary.moonpage.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse
import com.diary.moonpage.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

data class AnnualReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedYear: Int = LocalDate.now().year,
    val stats: StatisticsResponse? = null,
    val mappedMoodGrid: Map<Int, Map<Int, MoodFlowDto>> = emptyMap(),
    val themeType: MoonThemeType = MoonThemeType.DEFAULT
)

@HiltViewModel
class AnnualReportViewModel @Inject constructor(
    private val repository: StatisticsRepository,
    private val themePreferencesManager: ThemePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnualReportUiState())
    val uiState: StateFlow<AnnualReportUiState> = _uiState.asStateFlow()

    init {
        // Collect current theme dynamically to ensure real-time updating
        viewModelScope.launch {
            themePreferencesManager.themeType.collect { theme ->
                _uiState.update { it.copy(themeType = theme) }
            }
        }
        
        // Load report for the current year initially
        loadAnnualReport(_uiState.value.selectedYear)
    }

    fun loadAnnualReport(year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedYear = year) }
            try {
                // Fetch data on IO Dispatcher as per guidelines
                val response = withContext(Dispatchers.IO) {
                    repository.getStatisticsSummary(year, null, false)
                }

                if (response.isSuccessful && response.body() != null) {
                    val statsResponse = response.body()!!
                    val flatGridList = statsResponse.yearlyMoodGrid ?: emptyList()

                    // Transform flat list to optimized Month -> Day -> Log map for O(1) rendering lookup
                    val mappedGrid = transformFlatListTo2DMap(flatGridList)

                    _uiState.update {
                        it.copy(
                            stats = statsResponse,
                            mappedMoodGrid = mappedGrid,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load annual report stats: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    /**
     * Transforms flat List<MoodFlowDto> into Map<Month, Map<Day, MoodFlowDto>>
     */
    private fun transformFlatListTo2DMap(flatList: List<MoodFlowDto>): Map<Int, Map<Int, MoodFlowDto>> {
        val outerMap = mutableMapOf<Int, MutableMap<Int, MoodFlowDto>>()
        for (item in flatList) {
            try {
                // Expected format: "yyyy-MM-dd"
                val date = LocalDate.parse(item.date)
                val month = date.monthValue
                val day = date.dayOfMonth

                if (!outerMap.containsKey(month)) {
                    outerMap[month] = mutableMapOf()
                }
                outerMap[month]!![day] = item
            } catch (e: Exception) {
                // Silently bypass any parsing error to avoid UI crash
            }
        }
        return outerMap
    }
}
