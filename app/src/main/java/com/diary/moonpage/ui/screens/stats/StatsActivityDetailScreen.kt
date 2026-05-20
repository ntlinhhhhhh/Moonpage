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
fun StatsActivityDetailRoute(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StatsActivityDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onIconClick = viewModel::onIconClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsActivityDetailScreen(
    uiState: StatisticsUiState,
    onNavigateBack: () -> Unit,
    onIconClick: (String?) -> Unit
) {
    val scrollState = rememberScrollState()
    val backText = stringResource(R.string.back)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_activities_habits), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                StatsCard(title = stringResource(R.string.activity_stats)) {
                    FrequentlyRecordedView(
                        activities = uiState.frequentlyRecorded,
                        onIconClick = onIconClick
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
