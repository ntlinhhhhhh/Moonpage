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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
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
        onIconClick = viewModel::onIconClick,
        onShareRecap = viewModel::shareRecapCard,
        onClearCaptureError = viewModel::clearCaptureError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenContent(
    uiState: StatisticsUiState,
    onMonthChange: (Int, Int) -> Unit,
    onTabChange: (Boolean) -> Unit,
    onIconClick: (String?) -> Unit,
    onShareRecap: (android.content.Context, android.graphics.Bitmap) -> Unit,
    onClearCaptureError: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewContext = androidx.compose.ui.platform.LocalView.current

    val stats = uiState.stats
    val frequentlyRecorded = uiState.frequentlyRecorded
    val bestActivities = uiState.bestActivities
    val worstActivities = uiState.worstActivities

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.report), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabItem(stringResource(R.string.monthly), uiState.isMonthly, onClick = { onTabChange(true) })
                        TabItem(stringResource(R.string.annual), !uiState.isMonthly, onClick = { onTabChange(false) })
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
                        val dateText = if (uiState.isMonthly) {
                            val yearMonth = YearMonth.of(uiState.selectedYear, uiState.selectedMonth)
                            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
                            yearMonth.format(formatter)
                        } else {
                            uiState.selectedYear.toString()
                        }
                        Text(
                            text = dateText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                        
                        // 0.1 Sleep & Steps Summary
                        stats?.let { s ->
                            SleepSummaryView(
                                averageSleepHours = s.averageSleepHours ?: 0.0,
                                averageSleepStartTime = s.averageSleepStartTime,
                                averageWakeUpTime = uiState.averageWakeUpTime,
                                totalSteps = s.totalSteps ?: 0,
                                totalCalories = s.totalCalories ?: 0,
                                totalDistance = s.totalDistance ?: 0.0
                            )
                        }
                        
                        // 1. Mood Flow & Menstruation
                        StatsCard(title = stringResource(R.string.mood_flow) + if (!isMale) " & Cycle" else "") {
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
                        StatsCard(title = stringResource(R.string.sleep_analysis)) {
                            SleepAnalysisChart(stats?.sleepAnalysis ?: emptyList(), themeType = uiState.themeType)
                        }

                        // 3. Moods by Sleep
                        StatsCard(title = stringResource(R.string.moods_by_sleep)) {
                            SleepMoodCorrelationChart(stats?.sleepAnalysis ?: emptyList(), uiState.themeType)
                        }

                        // 4. Mood Bar
                        StatsCard(title = stringResource(R.string.mood_bar)) {
                            MoodDistributionView(stats?.moodDistribution ?: emptyList(), themeType = uiState.themeType)
                        }
                        
                        // 5. Activity Stats (Frequently Recorded)
                        StatsCard(title = stringResource(R.string.activity_stats)) {
                            FrequentlyRecordedView(
                                activities = frequentlyRecorded,
                                onIconClick = onIconClick
                            )
                        }


                        // 6. Best & Worst
                        StatsCard(title = stringResource(R.string.best_worst)) {
                            BestAndWorstView(bestActivities, worstActivities)
                        }

                        // 7. Icon Deep Dive
                        StatsCard(title = stringResource(R.string.icon_deep_dive)) {
                            IconDeepDiveView(
                                activityId = uiState.selectedIconId,
                                allActivities = stats?.bestActivities ?: emptyList(),
                                themeType = uiState.themeType
                            )
                        }

                        // 8. Music Summary
                        StatsCard(title = stringResource(R.string.top_music)) {
                            MusicSummaryView(stats?.musicSummary ?: emptyList())
                        }

                    } else {
                        // --- ANNUAL VIEW ---
                        
                        // 0. Shareable Yearly Recap Card
                        StatsCard(
                            title = stringResource(R.string.look_back, uiState.selectedYear.toString()),
                            actionText = "Share",
                            onActionClick = {
                                // Trigger capture and share
                                onClearCaptureError()
                                val view = viewContext
                                com.diary.moonpage.core.util.ComposeCaptureUtils.captureComposable(
                                    view = view,
                                    content = {
                                        com.diary.moonpage.core.theme.MoonPageTheme {
                                            YearlyRecapCard(
                                                year = uiState.selectedYear,
                                                totalLogs = stats?.totalLogs ?: 0,
                                                totalPhotos = stats?.totalPhotos ?: 0,
                                                yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                                                themeType = uiState.themeType,
                                                bestActivities = stats?.bestActivities ?: emptyList(),
                                                totalDistance = stats?.totalDistance ?: 0.0,
                                                totalSteps = stats?.totalSteps ?: 0,
                                                longestStreak = stats?.longestStreak ?: 0
                                            )
                                        }
                                    },
                                    width = 1080,
                                    onBitmapCaptured = { bitmap ->
                                        onShareRecap(context, bitmap)
                                    },
                                    onFailure = { error ->
                                        // Handle failure if needed
                                    }
                                )
                            }
                        ) {
                            YearlyRecapCard(
                                year = uiState.selectedYear,
                                totalLogs = stats?.totalLogs ?: 0,
                                totalPhotos = stats?.totalPhotos ?: 0,
                                yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                                themeType = uiState.themeType,
                                bestActivities = stats?.bestActivities ?: emptyList(),
                                totalDistance = stats?.totalDistance ?: 0.0,
                                totalSteps = stats?.totalSteps ?: 0,
                                longestStreak = stats?.longestStreak ?: 0
                            )
                        }

                        // 1. Mood Flow (Yearly)
                        StatsCard(title = stringResource(R.string.mood_flow)) {
                            MoodFlowChart(
                                moodFlow = stats?.moodFlow ?: emptyList(), 
                                year = uiState.selectedYear, 
                                month = uiState.selectedMonth, 
                                isMonthly = false, 
                                themeType = uiState.themeType
                            )
                        }

                        // 2. Mood Bar (Yearly)
                        StatsCard(title = stringResource(R.string.mood_bar)) {
                            MoodDistributionView(
                                distribution = stats?.moodDistribution ?: emptyList(), 
                                themeType = uiState.themeType
                            )
                        }

                        // 3. Year in Beans
                        StatsCard(title = stringResource(R.string.year_in_beans)) {
                            Column {
                                Text(
                                    text = buildAnnotatedString {
                                        append(stringResource(R.string.look_back, "").replace("%1${'$'}s", "").trim() + " ")
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            append("${uiState.selectedYear}.")
                                        }
                                    },
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                YearlyGridChart(
                                    yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                                    year = uiState.selectedYear,
                                    menstruationDates = if (!isMale) stats?.menstruationData ?: emptyList() else emptyList(),
                                    themeType = uiState.themeType
                                )
                            }
                        }

                        // 4. Monthly Mood Average
                        StatsCard(title = stringResource(R.string.monthly_average)) {
                            MonthlyMoodAverageChart(
                                yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(), 
                                year = uiState.selectedYear,
                                themeType = uiState.themeType
                            )
                        }

                        // 5. Yearly Top Activities
                        StatsCard(title = stringResource(R.string.yearly_top_activities)) {
                            FrequentlyRecordedView(
                                activities = frequentlyRecorded,
                                onIconClick = onIconClick
                            )
                        }

                        // 6. Trends
                        if (!isMale) {
                            StatsCard(title = stringResource(R.string.yearly_cycle_trends)) {
                                Text(stringResource(R.string.avg_cycle, 28), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(stringResource(R.string.avg_period, 5), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        StatsCard(title = stringResource(R.string.yearly_sleep_trends)) {
                            Text(stringResource(R.string.avg_sleep, 7.2f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.sleep_quality, stringResource(R.string.good)), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
