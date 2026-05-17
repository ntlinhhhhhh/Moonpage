package com.diary.moonpage.presentation.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateToMoodDetail: () -> Unit = {},
    onNavigateToSleepDetail: () -> Unit = {},
    onNavigateToActivityDetail: () -> Unit = {},
    onNavigateToInsightsDetail: () -> Unit = {},
    onNavigateToMusicDetail: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsScreenContent(
        uiState = uiState,
        onMonthChange = viewModel::onMonthSelected,
        onTabChange = viewModel::setMonthly,
        onIconClick = viewModel::onIconClick,
        onShareRecap = viewModel::shareRecapCard,
        onClearCaptureError = viewModel::clearCaptureError,
        onNavigateToMoodDetail = onNavigateToMoodDetail,
        onNavigateToSleepDetail = onNavigateToSleepDetail,
        onNavigateToActivityDetail = onNavigateToActivityDetail,
        onNavigateToInsightsDetail = onNavigateToInsightsDetail,
        onNavigateToMusicDetail = onNavigateToMusicDetail
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
    onClearCaptureError: () -> Unit,
    onNavigateToMoodDetail: () -> Unit = {},
    onNavigateToSleepDetail: () -> Unit = {},
    onNavigateToActivityDetail: () -> Unit = {},
    onNavigateToInsightsDetail: () -> Unit = {},
    onNavigateToMusicDetail: () -> Unit = {}
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
                        title = {
                            Text(
                                stringResource(R.string.report),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isMale = uiState.gender == "Male" || uiState.gender == "Nam"

                    if (uiState.isMonthly) {
                        // --- MONTHLY DASHBOARD VIEW ---

                        // 1. Mood Overview Card (full width, bấm → Mood Detail)
                        MoodOverviewCard(
                            stats = stats,
                            themeType = uiState.themeType,
                            onClick = onNavigateToMoodDetail
                        )

                        // 2. Sleep & Physical Row (2 columns, bấm → Sleep Detail)
                        SleepPhysicalRow(
                            stats = stats,
                            onClick = onNavigateToSleepDetail
                        )

                        // 3. Activity & Habits Card (bấm → Activity Detail)
                        ActivityHabitsCard(
                            frequentlyRecorded = frequentlyRecorded,
                            onClick = onNavigateToActivityDetail
                        )

                        // 4. Insights & Deep Dive Card (bấm → Insights Detail)
                        InsightsTeaserCard(
                            bestActivities = bestActivities,
                            onClick = onNavigateToInsightsDetail
                        )

                        // 5. Top Music Card (bấm → Music Detail)
                        TopMusicCard(
                            musicSummary = stats?.musicSummary,
                            onClick = onNavigateToMusicDetail
                        )

                    } else {
                        // --- ANNUAL VIEW (giữ nguyên layout cũ) ---

                        StatsCard(
                            title = stringResource(R.string.look_back, uiState.selectedYear.toString()),
                            actionText = "Share",
                            onActionClick = {
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
                                    onBitmapCaptured = { bitmap -> onShareRecap(context, bitmap) },
                                    onFailure = {}
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

                        StatsCard(title = stringResource(R.string.mood_flow)) {
                            MoodFlowChart(
                                moodFlow = stats?.moodFlow ?: emptyList(),
                                year = uiState.selectedYear,
                                month = uiState.selectedMonth,
                                isMonthly = false,
                                themeType = uiState.themeType
                            )
                        }

                        StatsCard(title = stringResource(R.string.mood_bar)) {
                            MoodDistributionView(
                                distribution = stats?.moodDistribution ?: emptyList(),
                                themeType = uiState.themeType
                            )
                        }

                        StatsCard(title = stringResource(R.string.year_in_beans)) {
                            Column {
                                Text(
                                    text = buildAnnotatedString {
                                        append(stringResource(R.string.look_back, "").replace("%1\$s", "").trim() + " ")
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
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

                        StatsCard(title = stringResource(R.string.monthly_average)) {
                            MonthlyMoodAverageChart(
                                yearlyMoodGrid = stats?.yearlyMoodGrid ?: emptyList(),
                                year = uiState.selectedYear,
                                themeType = uiState.themeType
                            )
                        }

                        StatsCard(title = stringResource(R.string.yearly_top_activities)) {
                            FrequentlyRecordedView(
                                activities = frequentlyRecorded,
                                onIconClick = onIconClick
                            )
                        }

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
