package com.diary.moonpage.presentation.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.presentation.components.stats.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAnnualMoodDetailScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = uiState.stats

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Annual Mood", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatsCard(title = "Mood Flow") {
                MoodFlowChart(
                    moodFlow = stats?.moodFlow ?: emptyList(),
                    year = uiState.selectedYear,
                    month = uiState.selectedMonth,
                    isMonthly = false,
                    themeType = uiState.themeType
                )
            }
            StatsCard(title = "Mood Distribution") {
                MoodDistributionView(
                    distribution = stats?.moodDistribution ?: emptyList(),
                    themeType = uiState.themeType
                )
            }
            StatsCard(title = "Monthly Average") {
                MonthlyMoodAverageChart(
                    yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                    year = uiState.selectedYear,
                    themeType = uiState.themeType
                )
            }
        }
    }
}
