package com.diary.moonpage.presentation.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.presentation.components.calendar.MonthYearPickerDialog
import com.diary.moonpage.presentation.components.stats.*
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
    var showDatePicker by remember { mutableStateOf(false) }

    val stats = uiState.stats
    val frequentlyRecorded = uiState.frequentlyRecorded
    val bestActivities = uiState.bestActivities
    val worstActivities = uiState.worstActivities

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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
                        containerColor = Color.White
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
                        .clip(MaterialTheme.shapes.small)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val date = LocalDate.of(uiState.selectedYear, uiState.selectedMonth, 1)
                    Text(
                        text = if (uiState.isMonthly) date.format(DateTimeFormatter.ofPattern("MMM yyyy")) else uiState.selectedYear.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        containerColor = Color(0xFFF7F7F2)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4CAF50))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isMonthly) {
                        // Monthly Sections
                        StatsCard(title = "Mood Flow") {
                            MoodFlowChart(stats?.moodFlow ?: emptyList(), uiState.selectedYear, uiState.selectedMonth, isMonthly = true)
                        }

                        StatsCard(title = "Mood Bar") {
                            MoodDistributionView(stats?.moodDistribution ?: emptyList())
                        }

                        StatsCard(title = "Frequently Recorded") {
                            FrequentlyRecordedView(frequentlyRecorded)
                        }

                        StatsCard(title = "Best & Worst") {
                            BestAndWorstView(bestActivities, worstActivities)
                        }
                    } else {
                        // Annual Sections
                        StatsCard(title = "Mood Flow") {
                            // In annual view, mood flow could be aggregated by month
                            MoodFlowChart(emptyList(), uiState.selectedYear, uiState.selectedMonth, isMonthly = false)
                        }

                        StatsCard(title = "Mood Bar") {
                            MoodDistributionView(stats?.moodDistribution ?: emptyList())
                        }

                        StatsCard(title = "Year in Beans") {
                            // Placeholder data for Year in Beans
                            YearInBeansView(uiState.selectedYear, emptyMap())
                        }

                        StatsCard(title = "Frequently Recorded") {
                            FrequentlyRecordedView(frequentlyRecorded)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        MonthYearPickerDialog(
            currentYearMonth = YearMonth.of(uiState.selectedYear, uiState.selectedMonth),
            onConfirm = { year, month ->
                onMonthChange(year, month)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
