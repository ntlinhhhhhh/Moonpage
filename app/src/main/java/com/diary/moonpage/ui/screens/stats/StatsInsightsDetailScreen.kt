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
fun StatsInsightsDetailRoute(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsInsightsDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onIconClick = viewModel::onIconClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsInsightsDetailScreen(
    uiState: StatisticsUiState,
    onNavigateBack: () -> Unit,
    onIconClick: (String?) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val backText = stringResource(R.string.back)
    val deepDiveActivities = uiState.currentData.stats?.performedActivities.orEmpty().ifEmpty {
        (uiState.currentData.bestActivities + uiState.currentData.worstActivities)
            .distinctBy { it.activityId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_insights_title), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                // Best & Worst section — now uses correlation-based data
                StatsCard(title = stringResource(R.string.best_worst)) {
                    BestAndWorstView(
                        bestLegacy = uiState.currentData.bestActivities,
                        worstLegacy = uiState.currentData.worstActivities
                    )
                }

                // Icon Deep Dive — now uses fully computed data
                StatsCard(title = stringResource(R.string.icon_deep_dive)) {
                    IconDeepDiveView(
                        deepDive = uiState.currentData.iconDeepDive,
                        allActivities = deepDiveActivities,
                        selectedIconId = uiState.selectedIconId,
                        themeType = uiState.themeType,
                        customMoods = uiState.customMoods,
                        onIconClick = onIconClick
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
