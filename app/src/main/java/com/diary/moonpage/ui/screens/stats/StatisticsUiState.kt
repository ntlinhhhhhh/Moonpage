package com.diary.moonpage.ui.screens.stats

import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse

import com.diary.moonpage.core.theme.MoonThemeType

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: StatisticsResponse? = null,
    val frequentlyRecorded: List<BestActivityDto> = emptyList(),
    // Legacy lists kept for backward compatibility (use activityCorrelations for new UI)
    val bestActivities: List<BestActivityDto> = emptyList(),
    val worstActivities: List<BestActivityDto> = emptyList(),
    // New computed insights from local data
    val bestCorrelations: List<ActivityCorrelation> = emptyList(),
    val worstCorrelations: List<ActivityCorrelation> = emptyList(),
    val iconDeepDive: IconDeepDiveResult? = null,
    val selectedYear: Int = java.time.LocalDate.now().year,
    val selectedMonth: Int = java.time.LocalDate.now().monthValue,
    val isMonthly: Boolean = true,
    val themeType: MoonThemeType = MoonThemeType.DEFAULT,
    val gender: String? = null,
    val selectedIconId: String? = null,
    val averageWakeUpTime: String? = null,
    val isCapturing: Boolean = false,
    val captureError: String? = null
)
