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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.ui.screens.stats.components.*
import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep

@Composable
fun StatsMoodDetailRoute(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StatsMoodDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsMoodDetailScreen(
    uiState: StatisticsUiState,
    onNavigateBack: () -> Unit
) {
    val stats = uiState.stats
    val isMale = uiState.gender == "Male" || uiState.gender == "Nam"
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Analysis", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.tutorialTarget(TutorialStep.HighlightMoodDetailBackButton)
                    ) {
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
                val title = "Mood Flow" + if (!isMale) " & Cycle" else ""
                Box(modifier = Modifier.fillMaxWidth().tutorialTarget(TutorialStep.HighlightMoodDetailChart)) {
                    StatsCard(title = title) {
                        MoodFlowChart(
                            moodFlow = stats?.moodFlow ?: emptyList(),
                            year = uiState.selectedYear,
                            month = uiState.selectedMonth,
                            isMonthly = uiState.isMonthly,
                            themeType = uiState.themeType,
                            menstruationDates = if (!isMale) stats?.menstruationData ?: emptyList() else emptyList()
                        )
                    }
                }

                StatsCard(title = "Mood Bar") {
                    MoodDistributionView(
                        distribution = stats?.moodDistribution ?: emptyList(),
                        themeType = uiState.themeType
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
