package com.diary.moonpage.ui.screens.stats

import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse

import com.diary.moonpage.core.theme.MoonThemeType

data class StatsData(
    val stats: StatisticsResponse? = null,
    val frequentlyRecorded: List<BestActivityDto> = emptyList(),
    val filteredActivities: List<BestActivityDto> = emptyList(),
    val activityFilter: Set<String> = emptySet(),
    val availableActivityCategories: Set<String> = emptySet(),
    val activityCategoriesById: Map<String, String> = emptyMap(),
    val bestActivities: List<BestActivityDto> = emptyList(),
    val worstActivities: List<BestActivityDto> = emptyList(),
    val bestCorrelations: List<ActivityCorrelation> = emptyList(),
    val worstCorrelations: List<ActivityCorrelation> = emptyList(),
    val iconDeepDive: IconDeepDiveResult? = null,
    val averageWakeUpTime: String? = null
)

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val monthlyData: StatsData = StatsData(),
    val annualData: StatsData = StatsData(),
    val sortOrder: SortOrder = SortOrder.MOST_RECORDED,
    val selectedYear: Int = java.time.LocalDate.now().year,
    val selectedMonth: Int = java.time.LocalDate.now().monthValue,
    val isMonthly: Boolean = true,
    val themeType: MoonThemeType = MoonThemeType.DEFAULT,
    val customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null,
    val gender: String? = null,
    val selectedIconId: String? = null,
    val isCapturing: Boolean = false,
    val captureError: String? = null
) {
    val currentData: StatsData get() = if (isMonthly) monthlyData else annualData
}

enum class SortOrder {
    MOST_RECORDED, LEAST_RECORDED
}
