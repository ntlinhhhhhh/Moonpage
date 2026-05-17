package com.diary.moonpage.ui.screens.stats

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
import com.diary.moonpage.ui.screens.stats.components.*

@Composable
fun StatsAnnualSleepDetailRoute(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StatsAnnualSleepDetailScreen(
        uiState = uiState,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAnnualSleepDetailScreen(
    uiState: StatisticsUiState,
    onBack: () -> Unit
) {
    val stats = uiState.stats

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Annual Sleep & Health", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
            SleepSummaryView(
                averageSleepHours = stats?.averageSleepHours ?: 0.0,
                averageSleepStartTime = stats?.averageSleepStartTime,
                averageWakeUpTime = uiState.averageWakeUpTime,
                totalSteps = stats?.totalSteps ?: 0,
                totalCalories = stats?.totalCalories ?: 0,
                totalDistance = stats?.totalDistance ?: 0.0
            )
            
            StatsCard(title = "Sleep Trends") {
                Text(
                    "Annual sleep trends analysis is in development...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            if (uiState.gender != "Male" && uiState.gender != "Nam") {
                StatsCard(title = "Cycle Trends") {
                    Text(
                        "Annual cycle trends analysis is in development...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
