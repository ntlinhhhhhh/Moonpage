package com.diary.moonpage.presentation.screens.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.domain.model.DailyLog
import androidx.compose.ui.platform.LocalView
import com.diary.moonpage.core.util.ComposeCaptureUtils
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.presentation.components.calendar.*
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Stateful Component
 */
@Composable
fun CalendarScreen(
    createdLogDate: String? = null,
    onLogDateHandled: () -> Unit = {},
    logSavedMessage: String? = null,
    onMessageShown: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyLog: (String) -> Unit,
    onNavigateToThemeCalendar: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(createdLogDate) {
        if (createdLogDate != null) {
            viewModel.refreshLogs()
            viewModel.onEvent(CalendarUiEvent.OnDateSelected(LocalDate.parse(createdLogDate)))
            onLogDateHandled()
        }
    }

    LaunchedEffect(logSavedMessage) {
        if (!logSavedMessage.isNullOrBlank()) {
            viewModel.showSnackbar(logSavedMessage)
            onMessageShown()
        }
    }

    CalendarScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToDailyLog = onNavigateToDailyLog,
        onNavigateToThemeCalendar = onNavigateToThemeCalendar,
        showSnackbar = viewModel::showSnackbar
    )
}

/**
 * Stateless Component
 */
@Composable
fun CalendarScreenContent(
    uiState: CalendarUiState,
    onEvent: (CalendarUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyLog: (String) -> Unit,
    onNavigateToThemeCalendar: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") }
    val currentMonthName = remember(uiState.currentYearMonth) { uiState.currentYearMonth.format(monthFormatter) }
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(CalendarUiEvent.DismissMessage)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState, topPadding = 110.dp) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                CalendarTopBar(
                    onFilterClick = { onEvent(CalendarUiEvent.OnFilterClick) },
                    onSettingsClick = onNavigateToSettings,
                    onThemeClick = onNavigateToThemeCalendar,
                    isFilterActive = uiState.filterMoodIds.isNotEmpty() || uiState.filterActivityIds.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                CalendarMonthHeader(
                    currentMonthName = currentMonthName,
                    onMonthClick = { onEvent(CalendarUiEvent.OnMonthPickerClick) },
                    onShareClick = { onEvent(CalendarUiEvent.OnShareClick) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CalendarHeader()

                CalendarPager(
                    currentYearMonth = uiState.currentYearMonth,
                    selectedDate = uiState.selectedDate,
                    dailyLogs = uiState.dailyLogs,
                    filterMoodIds = uiState.filterMoodIds,
                    filterActivityIds = uiState.filterActivityIds,
                    dynamicActivities = uiState.dynamicActivities,
                    themeType = uiState.themeType,
                    onDateSelected = { date ->
                        if (date.isAfter(LocalDate.now())) {
                            showSnackbar("You cannot record for a future date!")
                        } else {
                            onEvent(CalendarUiEvent.OnDateSelected(date))
                            if (uiState.dailyLogs[date] == null) {
                                onNavigateToDailyLog(date.toString())
                            }
                        }
                    },
                    onMonthChanged = { newMonth -> onEvent(CalendarUiEvent.OnMonthChanged(newMonth)) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                CalendarSelectedLogDetail(
                    selectedDate = uiState.selectedDate,
                    dailyLogs = uiState.dailyLogs,
                    dynamicActivities = uiState.dynamicActivities,
                    themeType = uiState.themeType,
                    onEditLog = { date -> onNavigateToDailyLog(date.toString()) },
                    onDeleteLog = { date -> onEvent(CalendarUiEvent.OnDeleteLog(date)) },
                    onShareClick = { onEvent(CalendarUiEvent.OnShareClick) }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (uiState.showMonthPicker) {
        MonthYearPickerDialog(
            currentYearMonth = uiState.currentYearMonth,
            onConfirm = { year, month -> onEvent(CalendarUiEvent.OnMonthPickerConfirm(year, month)) },
            onDismiss = { onEvent(CalendarUiEvent.OnMonthPickerDismiss) }
        )
    }

    if (uiState.showShareSheet) {
        ShareModeBottomSheet(
            onDismiss = { onEvent(CalendarUiEvent.OnShareDismiss) },
            onModeSelected = { isSquare ->
                coroutineScope.launch {
                    val width = 1080
                    val height = if (isSquare) 1080 else 1920

                    ComposeCaptureUtils.captureComposable(
                        view = view,
                        content = {
                            // Wrap in theme to ensure correct colors
                            Surface(color = MaterialTheme.colorScheme.background) {
                                ShareCalendarCard(
                                    yearMonth = uiState.currentYearMonth,
                                    dailyLogs = uiState.dailyLogs,
                                    isSquare = isSquare
                                )
                            }
                        },
                        width = width,
                        height = height,
                        onBitmapCaptured = { bitmap ->
                            ImageUtils.shareImage(view.context, bitmap, "My Mood Calendar")
                        }
                    )
                    onEvent(CalendarUiEvent.OnShareDismiss)
                }
            }
        )
    }

    if (uiState.showFilterSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { onEvent(CalendarUiEvent.OnFilterDismiss) },
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            FilterBottomSheet(
                selectedMoodIds = uiState.filterMoodIds,
                selectedActivityIds = uiState.filterActivityIds,
                dynamicActivities = uiState.dynamicActivities,
                themeType = uiState.themeType,
                onMoodToggled = { onEvent(CalendarUiEvent.OnFilterMoodToggled(it)) },
                onActivityToggled = { onEvent(CalendarUiEvent.OnFilterActivityToggled(it)) },
                onClearAll = { onEvent(CalendarUiEvent.OnClearFilters) },
                onDismiss = { onEvent(CalendarUiEvent.OnFilterDismiss) }
            )
        }
    }
}

@Composable
fun CalendarMonthHeader(
    currentMonthName: String,
    onMonthClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onMonthClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = currentMonthName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onShareClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Rounded.IosShare,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CalendarPager(
    currentYearMonth: java.time.YearMonth,
    selectedDate: LocalDate?,
    dailyLogs: Map<LocalDate, DailyLog>,
    filterMoodIds: Set<Int>,
    filterActivityIds: Set<String>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: com.diary.moonpage.presentation.theme.MoonThemeType,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (java.time.YearMonth) -> Unit
) {
    val baseYearMonth = remember { currentYearMonth }
    val initialPage = 500 * 12
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { initialPage * 2 }
    )

    LaunchedEffect(pagerState.currentPage) {
        val offset = pagerState.currentPage - initialPage
        val targetMonth = baseYearMonth.plusMonths(offset.toLong())
        if (targetMonth != currentYearMonth) {
            onMonthChanged(targetMonth)
        }
    }

    LaunchedEffect(currentYearMonth) {
        val targetOffset = java.time.temporal.ChronoUnit.MONTHS.between(
            baseYearMonth,
            currentYearMonth
        ).toInt()
        val targetPage = initialPage + targetOffset
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val offset = page - initialPage
        val pageYearMonth = baseYearMonth.plusMonths(offset.toLong())
        val daysInMonth = pageYearMonth.lengthOfMonth()
        val firstDayOfMonth = pageYearMonth.atDay(1)
        val firstDayOffset = if (firstDayOfMonth.dayOfWeek == java.time.DayOfWeek.SUNDAY) 0 else firstDayOfMonth.dayOfWeek.value
        val today = LocalDate.now()

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val totalCells = firstDayOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (colIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + colIndex
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellIndex in firstDayOffset until totalCells) {
                                val day = cellIndex - firstDayOffset + 1
                                val date = pageYearMonth.atDay(day)
                                val isSelected = date == selectedDate
                                val isToday = date == today
                                val logForDay = dailyLogs[date]

                                val matchesMood = filterMoodIds.isEmpty() || (logForDay != null && filterMoodIds.contains(logForDay.baseMoodId))
                                val matchesActivity = filterActivityIds.isEmpty() || (logForDay != null && logForDay.activityIds?.any { filterActivityIds.contains(it) } == true)
                                
                                val isFiltered = filterMoodIds.isNotEmpty() || filterActivityIds.isNotEmpty()
                                val isMatch = matchesMood && matchesActivity
                                val isDimmed = isFiltered && !isMatch

                                // Determine what to show in the circle
                                var moodColor: Color? = null
                                var moodIcon: ImageVector? = null
                                var moodDrawable: Int? = null

                                if (logForDay != null) {
                                    val mv = MoonIcons.Moods.getMoodVisual(logForDay.baseMoodId, themeType)
                                    moodColor = mv.color
                                    
                                    if (filterActivityIds.isNotEmpty()) {
                                        // Show activity icon if filtered by activities
                                        val firstMatchedActivityId = logForDay.activityIds?.find { filterActivityIds.contains(it) }
                                        val activity = dynamicActivities.find { it.id == firstMatchedActivityId }
                                        if (activity != null) {
                                            val activityIcon = MoonIcons.getIconForActivity(activity.name)
                                            moodDrawable = activityIcon.drawableRes
                                            moodIcon = activityIcon.vector
                                        } else {
                                            moodDrawable = mv.drawableRes
                                        }
                                    } else {
                                        moodDrawable = mv.drawableRes
                                    }
                                }

                                DayItem(
                                    day = day,
                                    isSelected = isSelected,
                                    moodColor = moodColor,
                                    moodIcon = moodIcon,
                                    moodDrawable = moodDrawable,
                                    isToday = isToday,
                                    isDimmed = isDimmed,
                                    themeType = themeType,
                                    onClick = { onDateSelected(date) }
                                )
                            } else {
                                DayItem(day = null, isSelected = false, moodColor = null, onClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSelectedLogDetail(
    selectedDate: LocalDate?,
    dailyLogs: Map<LocalDate, DailyLog>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: com.diary.moonpage.presentation.theme.MoonThemeType,
    onEditLog: (LocalDate) -> Unit,
    onDeleteLog: (LocalDate) -> Unit,
    onShareClick: () -> Unit
) {
    val date = selectedDate ?: return
    val selectedLog = dailyLogs[date] ?: return
    val mv = com.diary.moonpage.core.util.MoonIcons.Moods.getMoodVisual(selectedLog.baseMoodId, themeType)
    val activityNames = selectedLog.activityIds?.mapNotNull { id ->
        dynamicActivities.find { it.id == id }?.name
    } ?: emptyList()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                tint = Color(0xFF81C784),
                modifier = Modifier.size(28.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Rounded.IosShare, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { onEditLog(date) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))   
                }
                IconButton(onClick = { onDeleteLog(date) }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Reduced shadow
        ) {
            DayDetailArea(
                date = date,
                moodIcon = mv.vector,
                moodDrawable = mv.drawableRes,
                moodColor = mv.color,
                moodLabel = mv.name,
                noteSnippet = selectedLog.note,
                activityNames = activityNames
            )
        }
    }
}
