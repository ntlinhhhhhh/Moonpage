package com.diary.moonpage.ui.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.ui.components.refresh.MoonPullToRefreshBox
import com.diary.moonpage.ui.screens.calendar.components.MonthYearPickerDialog
import com.diary.moonpage.ui.screens.stats.components.*
import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep
import com.diary.moonpage.core.util.MoonIcons
import androidx.compose.ui.res.painterResource
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsRoute(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateToMoodDetail: () -> Unit = {},
    onNavigateToSleepDetail: () -> Unit = {},
    onNavigateToActivityDetail: () -> Unit = {},
    onNavigateToInsightsDetail: () -> Unit = {},
    onNavigateToMusicDetail: () -> Unit = {},
    onNavigateToAnnualMoodDetail: () -> Unit = {},
    onNavigateToAnnualSleepDetail: () -> Unit = {},
    onNavigateToAnnualActivityDetail: () -> Unit = {},
    onNavigateToAnnualBeansDetail: () -> Unit = {},
    onNavigateToAnnualMusicDetail: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsScreen(
        uiState = uiState,
        onMonthChange = viewModel::onMonthSelected,
        onTabChange = viewModel::setMonthly,
        onIconClick = viewModel::onIconClick,
        onShareRecap = viewModel::shareRecapCard,
        onClearCaptureError = viewModel::clearCaptureError,
        onRefresh = viewModel::loadStatistics,
        onNavigateToMoodDetail = onNavigateToMoodDetail,
        onNavigateToSleepDetail = onNavigateToSleepDetail,
        onNavigateToActivityDetail = onNavigateToActivityDetail,
        onNavigateToInsightsDetail = onNavigateToInsightsDetail,
        onNavigateToMusicDetail = onNavigateToMusicDetail,
        onNavigateToAnnualMoodDetail = onNavigateToAnnualMoodDetail,
        onNavigateToAnnualSleepDetail = onNavigateToAnnualSleepDetail,
        onNavigateToAnnualActivityDetail = onNavigateToAnnualActivityDetail,
        onNavigateToAnnualBeansDetail = onNavigateToAnnualBeansDetail,
        onNavigateToAnnualMusicDetail = onNavigateToAnnualMusicDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
    onMonthChange: (Int, Int) -> Unit,
    onTabChange: (Boolean) -> Unit,
    onIconClick: (String?) -> Unit,
    onShareRecap: (android.content.Context, android.graphics.Bitmap) -> Unit,
    onClearCaptureError: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToMoodDetail: () -> Unit = {},
    onNavigateToSleepDetail: () -> Unit = {},
    onNavigateToActivityDetail: () -> Unit = {},
    onNavigateToInsightsDetail: () -> Unit = {},
    onNavigateToMusicDetail: () -> Unit = {},
    onNavigateToAnnualMoodDetail: () -> Unit = {},
    onNavigateToAnnualSleepDetail: () -> Unit = {},
    onNavigateToAnnualActivityDetail: () -> Unit = {},
    onNavigateToAnnualBeansDetail: () -> Unit = {},
    onNavigateToAnnualMusicDetail: () -> Unit = {}
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
        MoonPullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
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
                        Box(modifier = Modifier.fillMaxWidth().tutorialTarget(TutorialStep.HighlightYearlyReport)) {
                            MoodOverviewCard(
                                stats = stats,
                                themeType = uiState.themeType,
                                customMoods = uiState.customMoods,
                                isMonthly = true,
                                year = uiState.selectedYear,
                                month = uiState.selectedMonth,
                                onClick = onNavigateToMoodDetail
                            )
                        }

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
                        // --- ANNUAL DASHBOARD VIEW ---
                        
                        // 1. Annual Mood Overview Card
                        MoodOverviewCard(
                            stats = stats,
                            themeType = uiState.themeType,
                            customMoods = uiState.customMoods,
                            isMonthly = false,
                            year = uiState.selectedYear,
                            onClick = onNavigateToAnnualMoodDetail
                        )

                        // 2. Annual Sleep & Physical Row
                        SleepPhysicalRow(
                            stats = stats,
                            onClick = onNavigateToAnnualSleepDetail
                        )

                        // 3. Annual Activity & Habits Card
                        ActivityHabitsCard(
                            frequentlyRecorded = frequentlyRecorded,
                            onClick = onNavigateToAnnualActivityDetail
                        )

                        // 3.5. Annual Insights & Deep Dive Card (bấm → Insights Detail)
                        InsightsTeaserCard(
                            bestActivities = bestActivities,
                            onClick = onNavigateToInsightsDetail
                        )

                        // 4. Year in Moonpage (Overview) Card
                        val annualDominantMoodId = remember(stats) {
                            val dist = stats?.moodDistribution ?: emptyList()
                            val fromDist = dist.maxByOrNull { it.percentage }?.baseMoodId
                            if (fromDist != null && fromDist != 0) fromDist
                            else {
                                val flow = stats?.moodFlow ?: emptyList()
                                if (flow.isNotEmpty()) flow.groupBy { it.moodId.toInt() }.maxByOrNull { it.value.size }?.key ?: 3 else 3
                            }
                        }
                        val annualMoodVisual = MoonIcons.Moods.getMoodVisual(annualDominantMoodId, uiState.themeType, uiState.customMoods)

                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onNavigateToAnnualBeansDetail() },
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                                .background(annualMoodVisual.color.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (annualMoodVisual.drawableRes != null) {
                                                Image(
                                                    painter = painterResource(id = annualMoodVisual.drawableRes),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(22.dp),
                                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(annualMoodVisual.color)
                                                )
                                            } else {
                                                Icon(Icons.Rounded.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(stringResource(R.string.year_in_beans), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                                    }
                                    Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.stats_emotional_journey),
                                    fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp, fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // 5. Annual Top Music Card
                        TopMusicCard(
                            musicSummary = stats?.musicSummary,
                            onClick = onNavigateToAnnualMusicDetail
                        )
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
