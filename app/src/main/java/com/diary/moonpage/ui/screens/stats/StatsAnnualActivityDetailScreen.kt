package com.diary.moonpage.ui.screens.stats

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StatsAnnualActivityDetailRoute(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsActivityDetailScreen(
        activities = uiState.frequentlyRecorded,
        onNavigateBack = onBack
    )
}
