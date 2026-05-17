package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.ui.screens.stats.components.*

@Composable
fun StatsAnnualBeansDetailRoute(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val viewContext = LocalView.current
    
    StatsAnnualBeansDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onShareRecap = {
            viewModel.clearCaptureError()
            com.diary.moonpage.core.util.ComposeCaptureUtils.captureComposable(
                view = viewContext,
                content = {
                    com.diary.moonpage.core.theme.MoonPageTheme {
                        YearlyRecapCard(
                            year = uiState.selectedYear,
                            totalLogs = uiState.stats?.totalLogs ?: 0,
                            totalPhotos = uiState.stats?.totalPhotos ?: 0,
                            yearlyMoodGrid = uiState.stats?.yearlyMoodGrid ?: emptyList(),
                            themeType = uiState.themeType,
                            bestActivities = uiState.stats?.bestActivities ?: emptyList(),
                            averageDistance = uiState.stats?.averageDistance ?: 0.0,
                            averageSteps = uiState.stats?.averageSteps?.toInt() ?: 0,
                            longestStreak = uiState.stats?.longestStreak ?: 0
                        )
                    }
                },
                width = 1080,
                onBitmapCaptured = { bitmap -> viewModel.shareRecapCard(context, bitmap) },
                onFailure = {}
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAnnualBeansDetailScreen(
    uiState: StatisticsUiState,
    onBack: () -> Unit,
    onShareRecap: () -> Unit
) {
    val stats = uiState.stats

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Year in Beans", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
            StatsCard(
                title = "Your Recap",
                actionText = "Share",
                onActionClick = onShareRecap
            ) {
                YearlyRecapCard(
                    year = uiState.selectedYear,
                    totalLogs = stats?.totalLogs ?: 0,
                    totalPhotos = stats?.totalPhotos ?: 0,
                    yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                    themeType = uiState.themeType,
                    bestActivities = stats?.bestActivities ?: emptyList(),
                    averageDistance = stats?.averageDistance ?: 0.0,
                    averageSteps = stats?.averageSteps?.toInt() ?: 0,
                    longestStreak = stats?.longestStreak ?: 0,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            StatsCard(title = "Complete Year Grid") {
                YearlyGridChart(
                    yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                    year = uiState.selectedYear,
                    menstruationDates = if (uiState.gender != "Male" && uiState.gender != "Nam") stats?.menstruationData ?: emptyList() else emptyList(),
                    themeType = uiState.themeType
                )
            }
        }
    }
}
