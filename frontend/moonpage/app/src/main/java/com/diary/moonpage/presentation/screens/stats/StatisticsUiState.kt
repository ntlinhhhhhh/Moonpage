package com.diary.moonpage.presentation.screens.stats

import com.diary.moonpage.data.remote.dto.stats.StatisticsResponse

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: StatisticsResponse? = null,
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 4,
    val isMonthly: Boolean = true
)
