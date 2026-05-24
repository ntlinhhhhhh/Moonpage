package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.ui.screens.stats.components.*

@Composable
fun StatsSleepDetailRoute(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StatsSleepDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSleepDetailScreen(
    uiState: StatisticsUiState,
    onNavigateBack: () -> Unit
) {
    val stats = uiState.stats
    val scrollState = rememberScrollState()
    val backText = stringResource(R.string.back)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_sleep_health), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = backText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Summary stats
                stats?.let { s ->
                    SleepSummaryView(
                        averageSleepHours = s.averageSleepHours ?: 0.0,
                        averageSleepStartTime = s.averageSleepStartTime,
                        averageWakeUpTime = uiState.averageWakeUpTime,
                        avgSteps = s.averageSteps?.toInt() ?: 0,
                        avgCalories = s.averageCalories?.toInt() ?: 0,
                        avgDistance = s.averageDistance ?: 0.0
                    )
                }

                // Section 2: Sleep Analysis Chart
                StatsCard(title = stringResource(R.string.sleep_analysis)) {
                    SleepAnalysisChart(
                        sleepData = stats?.sleepAnalysis ?: emptyList(),
                        year = uiState.selectedYear,
                        month = uiState.selectedMonth,
                        isMonthly = true,
                        themeType = uiState.themeType
                    )
                }

                // Section 3: Mood by Sleep Correlation
                StatsCard(title = stringResource(R.string.moods_by_sleep)) {
                    SleepMoodCorrelationChart(
                        sleepData = stats?.sleepAnalysis ?: emptyList(),
                        themeType = uiState.themeType,
                        customMoods = uiState.customMoods
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
