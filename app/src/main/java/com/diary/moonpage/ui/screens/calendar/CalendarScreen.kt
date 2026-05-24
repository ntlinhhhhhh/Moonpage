package com.diary.moonpage.ui.screens.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.core.util.MoonIcon
import com.diary.moonpage.ui.screens.calendar.components.*
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.ui.components.feedback.MoonDeleteConfirmDialog
import com.diary.moonpage.ui.components.refresh.MoonPullToRefreshBox
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
import java.time.LocalDate
import java.time.YearMonth

/**
 * Stateful Component
 */
@Composable
fun CalendarRoute(
    createdLogDate: String? = null,
    onLogDateHandled: () -> Unit = {},
    logSavedMessage: String? = null,
    onMessageShown: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyLog: (String) -> Unit,
    onNavigateToShareLog: (String) -> Unit,
    onNavigateToShareCalendar: (String) -> Unit,
    onNavigateToThemeCalendar: () -> Unit = {},
    onNavigateToStreakStats: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(createdLogDate) {
        if (createdLogDate != null) {
            val date = LocalDate.parse(createdLogDate)
            viewModel.refreshLogs()
            viewModel.onEvent(CalendarUiEvent.ForceDateSelected(date))
            // Ensure the calendar jumps to the month of the created log
            viewModel.onEvent(CalendarUiEvent.OnMonthChanged(YearMonth.from(date)))
            onLogDateHandled()
        }
    }

    LaunchedEffect(logSavedMessage) {
        if (!logSavedMessage.isNullOrBlank()) {
            viewModel.showSnackbar(logSavedMessage)
            onMessageShown()
        }
    }

    CalendarScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToDailyLog = onNavigateToDailyLog,
        onNavigateToShareLog = onNavigateToShareLog,
        onNavigateToShareCalendar = onNavigateToShareCalendar,
        onNavigateToThemeCalendar = onNavigateToThemeCalendar,
        onStreakClick = onNavigateToStreakStats,
        showSnackbar = viewModel::showSnackbar,
        onRefresh = viewModel::refreshLogs
    )
}

/**
 * Stateless Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onEvent: (CalendarUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyLog: (String) -> Unit,
    onNavigateToShareLog: (String) -> Unit,
    onNavigateToShareCalendar: (String) -> Unit,
    onNavigateToThemeCalendar: () -> Unit,
    onStreakClick: () -> Unit,
    showSnackbar: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var dateToDelete by remember { mutableStateOf<LocalDate?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val futureDateError = stringResource(R.string.future_date_error)

    val initialPage = 500 * 12
    val baseYearMonth = remember { YearMonth.from(LocalDate.now().withDayOfMonth(1)) }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { initialPage * 2 })

    // Sync Pager with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        val offset = pagerState.currentPage - initialPage
        val targetMonth = baseYearMonth.plusMonths(offset.toLong())
        if (targetMonth != uiState.currentYearMonth) {
            onEvent(CalendarUiEvent.OnMonthChanged(targetMonth))
        }
    }

    // Sync ViewModel with Pager (e.g. from Picker)
    LaunchedEffect(uiState.currentYearMonth) {
        val targetOffset = java.time.temporal.ChronoUnit.MONTHS.between(baseYearMonth, uiState.currentYearMonth).toInt()
        val targetPage = initialPage + targetOffset
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(CalendarUiEvent.DismissMessage)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        MoonPullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                CalendarTopBar(
                    viewMode = uiState.viewMode,
                    onFilterClick = { onEvent(CalendarUiEvent.OnFilterClick) },
                    onClearFilters = { onEvent(CalendarUiEvent.OnClearFilters) },
                    onToggleViewMode = { onEvent(CalendarUiEvent.ToggleViewMode) },
                    onThemeClick = onNavigateToThemeCalendar,
                    onStreakClick = onStreakClick,
                    streakCount = uiState.currentStreak,
                    isFilterActive = uiState.selectedFilters.isNotEmpty(),
                    modifier = Modifier.statusBarsPadding()
                )

                AnimatedContent(
                    targetState = uiState.viewMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                    },
                    label = "ViewModeTransition"
                ) { targetMode ->
                    when (targetMode) {
                        CalendarViewMode.CALENDAR -> {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.Top,
                                beyondViewportPageCount = 1
                            ) { page ->
                            val offset = page - initialPage
                            val pageYearMonth = baseYearMonth.plusMonths(offset.toLong())
                            val currentLanguage = com.diary.moonpage.core.theme.LocalLocale.current
                            val currentMonthName = if (currentLanguage == "vi") {
                                "Tháng ${pageYearMonth.monthValue} ${pageYearMonth.year}"
                            } else {
                                pageYearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"))
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))

                                CalendarMonthHeader(
                                    currentMonthName = currentMonthName,
                                    themeType = uiState.themeType,
                                    onMonthClick = { onEvent(CalendarUiEvent.OnMonthPickerClick) },
                                    onShareClick = { onNavigateToShareCalendar(uiState.currentYearMonth.toString()) }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CalendarHeader(themeType = uiState.themeType)

                                CalendarGrid(
                                    pageYearMonth = pageYearMonth,
                                    selectedDate = uiState.selectedDate,
                                    dailyLogs = uiState.dailyLogs,
                                    selectedFilters = uiState.selectedFilters,
                                    dynamicActivities = uiState.dynamicActivities,
                                    themeType = uiState.themeType,
                                    customMoods = uiState.customMoods,
                                    onDateSelected = { date ->
                                        if (date.isAfter(LocalDate.now())) {
                                            showSnackbar(futureDateError)
                                        } else {
                                            onEvent(CalendarUiEvent.OnDateSelected(date))
                                            if (uiState.dailyLogs[date] == null) {
                                                onNavigateToDailyLog(date.toString())
                                            }
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                CalendarSelectedLogDetail(
                                    selectedDate = uiState.selectedDate,
                                    dailyLogs = uiState.dailyLogs,
                                    dynamicActivities = uiState.dynamicActivities,
                                    themeType = uiState.themeType,
                                    customMoods = uiState.customMoods,
                                    onEditLog = { date -> onNavigateToDailyLog(date.toString()) },
                                    onDeleteLog = { date -> 
                                        dateToDelete = date
                                        showDeleteConfirmDialog = true
                                    },
                                    onShareClick = { 
                                        uiState.selectedDate?.let { date ->
                                            onNavigateToShareLog(date.toString())
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(100.dp))
                            }
                            }
                        }
                        CalendarViewMode.TIMELINE -> {
                            TimelineView(
                                dailyLogs = uiState.dailyLogs,
                                selectedFilters = uiState.selectedFilters,
                                dynamicActivities = uiState.dynamicActivities,
                                themeType = uiState.themeType,
                                customMoods = uiState.customMoods,
                                onEditLog = { date -> onNavigateToDailyLog(date.toString()) },
                                onDeleteLog = { date ->
                                    dateToDelete = date
                                    showDeleteConfirmDialog = true
                                },
                                onShareLog = { date -> onNavigateToShareLog(date.toString()) },
                                onAddLog = { date -> onNavigateToDailyLog(date.toString()) }
                            )
                        }
                    }
                }
            }
        }
        
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MoonSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
        }
    }

    if (uiState.showMonthPicker) {
        MonthYearPickerDialog(
            currentYearMonth = uiState.currentYearMonth,
            onConfirm = { year, month -> onEvent(CalendarUiEvent.OnMonthPickerConfirm(year, month)) },
            onDismiss = { onEvent(CalendarUiEvent.OnMonthPickerDismiss) }
        )
    }


    if (uiState.showFilterSheet) {
        val isActuallyDark = com.diary.moonpage.core.theme.MoonTheme.customColors.isDark
        val sheetBgColor = if (isActuallyDark) com.diary.moonpage.core.theme.MoonBgDark else Color.White
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { onEvent(CalendarUiEvent.OnFilterDismiss) },
            sheetState = sheetState,
            containerColor = sheetBgColor,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                val handleColor = if (isActuallyDark) Color(0xFF424242) else Color(0xFFE0E0E0)
                BottomSheetDefaults.DragHandle(color = handleColor)
            }
        ) {
            FilterScreen(
                currentFilters = uiState.selectedFilters,
                dynamicActivities = uiState.dynamicActivities,
                themeType = uiState.themeType,
                onDismiss = { onEvent(CalendarUiEvent.OnFilterDismiss) },
                onSeeResults = { filters ->
                    onEvent(CalendarUiEvent.ApplyFilter(filters))
                }
            )
        }
    }

    if (showDeleteConfirmDialog && dateToDelete != null) {
        MoonDeleteConfirmDialog(
            title = stringResource(R.string.delete_log_title),
            message = stringResource(R.string.delete_log_confirmation),
            onConfirm = {
                onEvent(CalendarUiEvent.OnDeleteLog(dateToDelete!!))
                showDeleteConfirmDialog = false
                dateToDelete = null
            },
            onDismiss = {
                showDeleteConfirmDialog = false
                dateToDelete = null
            }
        )
    }
}

@Composable
fun CalendarMonthHeader(
    currentMonthName: String,
    themeType: com.diary.moonpage.core.theme.MoonThemeType,
    onMonthClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val headerColor = MaterialTheme.colorScheme.onSurface

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
                color = headerColor
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = headerColor
            )
        }
        IconButton(
            onClick = onShareClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Rounded.IosShare,
                contentDescription = stringResource(R.string.share),
                tint = headerColor
            )
        }
    }
}

@Composable
fun CalendarSelectedLogDetail(
    selectedDate: LocalDate?,
    dailyLogs: Map<LocalDate, DailyLog>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: com.diary.moonpage.core.theme.MoonThemeType,
    customMoods: Map<Int, MoonIcon>? = null,
    onEditLog: (LocalDate) -> Unit,
    onDeleteLog: (LocalDate) -> Unit,
    onShareClick: () -> Unit
) {
    val date = selectedDate ?: return
    val selectedLog = dailyLogs[date] ?: return
    val mv = MoonIcons.Moods.getMoodVisual(selectedLog.baseMoodId, themeType, customMoods)
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
            Spacer(modifier = Modifier.width(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Rounded.IosShare, contentDescription = stringResource(R.string.share), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { onEditLog(date) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { onDeleteLog(date) }) {
                    Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.logCardBg
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            DayDetailArea(
                date = date,
                moodIcon = mv.vector,
                moodDrawable = mv.drawableRes,
                moodColor = mv.color,
                moodLabel = mv.name,
                noteSnippet = selectedLog.note,
                activityNames = activityNames,
                dailyPhotos = selectedLog.dailyPhotos ?: emptyList(),
                sleepHours = selectedLog.sleepHours,
                isMenstruation = selectedLog.isMenstruation,
                steps = selectedLog.steps,
                musicRecord = selectedLog.musicRecord,
                weather = selectedLog.weather,
                temperature = selectedLog.temperature
            )
        }
    }
}

@Composable
fun TimelineView(
    dailyLogs: Map<LocalDate, DailyLog>,
    selectedFilters: List<FilterItem>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: com.diary.moonpage.core.theme.MoonThemeType,
    customMoods: Map<Int, MoonIcon>? = null,
    onEditLog: (LocalDate) -> Unit,
    onDeleteLog: (LocalDate) -> Unit,
    onShareLog: (LocalDate) -> Unit,
    onAddLog: (LocalDate) -> Unit
) {
    val filteredLogs = remember(dailyLogs, selectedFilters) {
        dailyLogs.values.filter { log ->
            if (selectedFilters.isEmpty()) true else {
                selectedFilters.any { filter ->
                    when (filter) {
                        is FilterItem.Mood -> log.baseMoodId == filter.id
                        is FilterItem.Activity -> log.activityIds?.contains(filter.id) == true
                        is FilterItem.Special -> {
                            when (filter.id) {
                                "music" -> log.activityIds?.any { it.contains("music", ignoreCase = true) } == true
                                "sleep" -> (log.sleepHours ?: 0.0) > 0.0
                                "sleep_long" -> (log.sleepHours ?: 0.0) in 6.0..8.0
                                "menstruation" -> log.isMenstruation
                                else -> false
                            }
                        }
                    }
                }
            }
        }.sortedByDescending { it.date }
    }

    if (filteredLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (selectedFilters.isNotEmpty()) Icons.Rounded.SearchOff else Icons.Rounded.EditNote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (selectedFilters.isNotEmpty()) {
                        stringResource(R.string.calendar_no_matching_entries)
                    } else {
                        stringResource(R.string.calendar_no_entries_yet)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (selectedFilters.isEmpty()) {
                    Button(
                        onClick = { onAddLog(LocalDate.now()) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.calendar_write_first_entry))
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredLogs,
                key = { it.id }
            ) { log ->
                val date = LocalDate.parse(log.date)
                val mv = MoonIcons.Moods.getMoodVisual(log.baseMoodId, themeType, customMoods)
                val activityNames = log.activityIds?.mapNotNull { id ->
                    dynamicActivities.find { it.id == id }?.name
                } ?: emptyList<String>()

                TimelineItem(
                    log = log,
                    date = date,
                    mv = mv,
                    activityNames = activityNames,
                    themeType = themeType,
                    onEdit = { onEditLog(date) },
                    onDelete = { onDeleteLog(date) },
                    onShare = { onShareLog(date) }
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    log: DailyLog,
    date: LocalDate,
    mv: MoonIcon,
    activityNames: List<String>,
    themeType: com.diary.moonpage.core.theme.MoonThemeType,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dayOfWeek = date.format(java.time.format.DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
            val dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
            
            Text(
                text = "$dayOfWeek, $dateStr",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.IosShare, contentDescription = stringResource(R.string.share), tint = cs.onSurface.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit), tint = cs.onSurface.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), tint = cs.onSurface.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.logCardBg
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            DayDetailArea(
                date = date,
                moodIcon = mv.vector,
                moodDrawable = mv.drawableRes,
                moodColor = mv.color,
                moodLabel = mv.name,
                noteSnippet = log.note,
                activityNames = activityNames,
                dailyPhotos = log.dailyPhotos ?: emptyList(),
                sleepHours = log.sleepHours,
                isMenstruation = log.isMenstruation,
                steps = log.steps,
                musicRecord = log.musicRecord,
                weather = log.weather,
                temperature = log.temperature
            )
        }
    }
}
