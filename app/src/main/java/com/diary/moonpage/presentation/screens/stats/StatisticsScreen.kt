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
        onTabChange = viewModel::setMonthly,
        onIconClick = viewModel::onIconClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenContent(
    uiState: StatisticsUiState,
    onMonthChange: (Int, Int) -> Unit,
    onTabChange: (Boolean) -> Unit,
    onIconClick: (String) -> Unit
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
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CenterAlignedTopAppBar(
                        title = { Text("Report", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val isMale = uiState.gender == "Male" || uiState.gender == "Nam"
                    
                    if (uiState.isMonthly) {
                        // --- MONTHLY VIEW ---
                        
                        // 1. Mood Flow & Menstruation
                        StatsCard(title = "Mood Flow" + if (!isMale) " & Cycle" else "") {
                            MoodFlowChart(
                                moodFlow = stats?.moodFlow ?: emptyList(), 
                                year = uiState.selectedYear, 
                                month = uiState.selectedMonth, 
                                isMonthly = true, 
                                themeType = uiState.themeType,
                                menstruationDates = if (!isMale) stats?.menstruationData ?: emptyList() else emptyList()
                            )
                        }

                        // 2. Sleep Analysis
                        StatsCard(title = "Sleep Analysis") {
                            SleepAnalysisChart(stats?.sleepData ?: emptyList())
                        }

                        // 3. Mood by Sleep
                        StatsCard(title = "Mood by Sleep") {
                            MoodBySleepChart(stats?.moodBySleep ?: emptyList(), uiState.themeType)
                        }

                        // 4. Mood Distribution
                        StatsCard(title = "Mood Distribution") {
                            MoodDistributionView(stats?.moodDistribution ?: emptyList(), themeType = uiState.themeType)
                        }
                        
                        // 5. Activity Stats (Frequently Recorded)
                        StatsCard(title = "Activity Stats") {
                            FrequentlyRecordedView(
                                activities = frequentlyRecorded,
                                onIconClick = onIconClick
                            )
                        }


                        // 6. Best & Worst
                        StatsCard(title = "Best & Worst") {
                            BestAndWorstView(bestActivities, worstActivities)
                        }

                        // 7. Icon Deep Dive
                        StatsCard(title = "Icon Deep Dive") {
                            IconDeepDiveView(
                                activityId = uiState.selectedIconId,
                                allActivities = stats?.bestActivities ?: emptyList(),
                                themeType = uiState.themeType
                            )
                        }

                    } else {
                        // --- ANNUAL VIEW ---
                        
                        // 1. Yearly Grid (Mood Mandala)
                        StatsCard(title = "Mood Mandala") {
                            YearlyGridChart(
                                yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                                menstruationDates = if (!isMale) stats?.menstruationData ?: emptyList() else emptyList(),
                                themeType = uiState.themeType
                            )
                        }

                        // 2. Monthly Mood Average
                        StatsCard(title = "Monthly Average") {
                            MonthlyMoodAverageChart(
                                yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(), 
                                year = uiState.selectedYear,
                                themeType = uiState.themeType
                            )
                        }

                        // 3. Yearly Trends (Cycle & Sleep)
                        if (!isMale) {
                            StatsCard(title = "Yearly Cycle Trends") {
                                Text("Average Cycle: 28 days", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Average Period: 5 days", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        StatsCard(title = "Yearly Sleep Trends") {
                            Text("Avg Sleep: 7.2h", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Sleep Quality: Good", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // 4. Yearly Top Activities
                        StatsCard(title = "Yearly Top Activities") {
                            FrequentlyRecordedView(
                                activities = frequentlyRecorded,
                                onIconClick = onIconClick
                            )
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
            onDismiss = { showDatePicker = false },
            showMonth = uiState.isMonthly
        )
    }
}
