package com.diary.moonpage.presentation.screens.stats

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.presentation.components.stats.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSleepDetailScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = uiState.stats
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep & Health", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
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
                        totalSteps = s.totalSteps ?: 0,
                        totalCalories = s.totalCalories ?: 0,
                        totalDistance = s.totalDistance ?: 0.0
                    )
                }

                // Section 2: Sleep Analysis Chart
                StatsCard(title = "Sleep Analysis") {
                    SleepAnalysisChart(
                        sleepData = stats?.sleepAnalysis ?: emptyList(),
                        themeType = uiState.themeType
                    )
                }

                // Section 3: Mood by Sleep Correlation
                StatsCard(title = "Moods by Sleep") {
                    SleepMoodCorrelationChart(
                        sleepData = stats?.sleepAnalysis ?: emptyList(),
                        themeType = uiState.themeType
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
