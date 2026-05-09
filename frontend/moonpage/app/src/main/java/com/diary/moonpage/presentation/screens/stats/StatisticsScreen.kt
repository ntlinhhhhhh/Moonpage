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
            Surface(
                color = Color(0xFFF9FBF9),
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CenterAlignedTopAppBar(
                        title = { Text("Report", fontWeight = FontWeight.Bold, color = Color(0xFF424242)) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                            color = Color(0xFF424242)
                        )
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        containerColor = Color(0xFFF9FBF9)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF66BB6A))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Mood Flow Chart
                    StatsCard(title = "Mood Flow") {
                        MoodFlowChart(stats?.moodFlow ?: emptyList(), uiState.selectedYear, uiState.selectedMonth, isMonthly = uiState.isMonthly)
                    }

                    // Mood Bar Chart
                    StatsCard(title = "Mood Bar") {
                        MoodDistributionView(stats?.moodDistribution ?: emptyList())
                    }

                    if (!uiState.isMonthly) {
                        // Year in Beans (Annual only)
                        StatsCard(title = "Year in Beans") {
                            YearInBeansView(uiState.selectedYear)
                        }
                    }

                    // Frequently Recorded
                    StatsCard(title = "Frequently Recorded", actionText = "More") {
                        FrequentlyRecordedView(frequentlyRecorded)
                    }

                    // Best & Worst
                    StatsCard(title = "Best & Worst", actionText = "More") {
                        BestAndWorstView(bestActivities, worstActivities)
                    }

                    // Premium Section
                    PremiumAnalysisSection()

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
