package com.diary.moonpage.presentation.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.presentation.components.calendar.MonthYearPickerBottomSheet
import com.diary.moonpage.presentation.components.stats.*
import com.diary.moonpage.presentation.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Stateful Component
 */
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsScreenContent(
        uiState = uiState,
        onMonthChange = viewModel::onMonthSelected,
        onTabChange = viewModel::setMonthly
    )
}

/**
 * Stateless Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenContent(
    uiState: StatisticsUiState,
    onMonthChange: (Int, Int) -> Unit,
    onTabChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }

    val stats = uiState.stats

    // Use pre-calculated lists from ViewModel for better performance
    val frequentlyRecorded = uiState.frequentlyRecorded
    val bestActivities = uiState.bestActivities
    val worstActivities = uiState.worstActivities

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Report",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colorScheme.background
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabItem("Monthly", uiState.isMonthly, onClick = { onTabChange(true) })
                    TabItem("Annual", !uiState.isMonthly, onClick = { onTabChange(false) })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val date = LocalDate.of(uiState.selectedYear, uiState.selectedMonth, 1)
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onBackground
                    )
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        containerColor = if (isSystemInDarkTheme()) colorScheme.background else Color(0xFFF7F7F2)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mood Flow
                    StatsCard(
                        title = "Mood Flow",
                        hint = if (stats?.moodFlow.isNullOrEmpty()) "Please add an entry." else null
                    ) {
                        MoodFlowChart(stats?.moodFlow ?: emptyList(), uiState.selectedYear, uiState.selectedMonth)
                    }

                    // Mood Bar
                    StatsCard(
                        title = "Mood Bar",
                        hint = if (stats?.moodDistribution.isNullOrEmpty()) "Please add an entry." else null
                    ) {
                        MoodDistributionView(stats?.moodDistribution ?: emptyList())
                    }

                    // Frequently Recorded
                    StatsCard(
                        title = "Frequently Recorded",
                        hint = if (frequentlyRecorded.isEmpty()) "Please record an icon." else null
                    ) {
                        FrequentlyRecordedView(frequentlyRecorded)
                    }

                    // Best & Worst
                    StatsCard(
                        title = "Best & Worst",
                        hint = if (stats?.bestActivities.isNullOrEmpty()) "You need an icon that has been recorded 3 times or more." else null
                    ) {
                        BestAndWorstView(bestActivities, worstActivities)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        MonthYearPickerBottomSheet(
            currentYearMonth = YearMonth.of(uiState.selectedYear, uiState.selectedMonth),
            onConfirm = { year, month ->
                onMonthChange(year, month)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
