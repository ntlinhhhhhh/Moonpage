package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.ui.screens.stats.components.*

@Composable
fun StatsAnnualActivityDetailRoute(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StatsAnnualActivityDetailScreen(
        uiState = uiState,
        onNavigateBack = onBack,
        onIconClick = viewModel::onIconClick,
        onFilterChange = viewModel::updateFilter,
        onSortToggle = viewModel::toggleSortOrder
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAnnualActivityDetailScreen(
    uiState: StatisticsUiState,
    onNavigateBack: () -> Unit,
    onIconClick: (String?) -> Unit,
    onFilterChange: (String, Boolean) -> Unit,
    onSortToggle: () -> Unit
) {
    var showFilterModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Annual Activities", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilterToggle(onClick = { showFilterModal = true })
                    SortToggle(currentOrder = uiState.sortOrder, onClick = onSortToggle)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (showFilterModal) {
            ActivityFilterModal(
                categories = uiState.availableActivityCategories,
                selectedFilter = uiState.activityFilter,
                onFilterChange = onFilterChange,
                onDismiss = { showFilterModal = false }
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            RankedActivityList(
                activities = uiState.filteredActivities,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
