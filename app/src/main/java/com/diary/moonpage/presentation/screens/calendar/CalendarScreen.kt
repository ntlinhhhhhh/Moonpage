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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.diary.moonpage.core.util.ComposeCaptureUtils
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.presentation.components.calendar.*
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
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
    val coroutineScope = rememberCoroutineScope()
    val view = androidx.compose.ui.platform.LocalView.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var dateToDelete by remember { mutableStateOf<LocalDate?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            CalendarTopBar(
                onFilterClick = { onEvent(CalendarUiEvent.OnFilterClick) },
                onSettingsClick = onNavigateToSettings,
                onThemeClick = onNavigateToThemeCalendar,
                isFilterActive = uiState.selectedFilter != null,
                modifier = Modifier.statusBarsPadding()
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 1
            ) { page ->
                val offset = page - initialPage
                val pageYearMonth = baseYearMonth.plusMonths(offset.toLong())
                val currentMonthName = pageYearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    CalendarMonthHeader(
                        currentMonthName = currentMonthName,
                        onMonthClick = { onEvent(CalendarUiEvent.OnMonthPickerClick) },
                        onShareClick = { onEvent(CalendarUiEvent.OnShareClick) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CalendarHeader()

                    CalendarGrid(
                        pageYearMonth = pageYearMonth,
                        selectedDate = uiState.selectedDate,
                        dailyLogs = uiState.dailyLogs,
                        selectedFilter = uiState.selectedFilter,
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
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CalendarSelectedLogDetail(
                        selectedDate = uiState.selectedDate,
                        dailyLogs = uiState.dailyLogs,
                        dynamicActivities = uiState.dynamicActivities,
                        themeType = uiState.themeType,
                        onEditLog = { date -> onNavigateToDailyLog(date.toString()) },
                        onDeleteLog = { date -> 
                            dateToDelete = date
                            showDeleteConfirmDialog = true
                        },
                        onShareClick = { onEvent(CalendarUiEvent.OnShareClick) }
                    )

                    Spacer(modifier = Modifier.height(100.dp))
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
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.background)
            }
        ) {
            FilterScreen(
                currentFilter = uiState.selectedFilter,
                dynamicActivities = uiState.dynamicActivities,
                themeType = uiState.themeType,
                onDismiss = { onEvent(CalendarUiEvent.OnFilterDismiss) },
                onSeeResults = { filter ->
                    onEvent(CalendarUiEvent.ApplyFilter(filter))
                }
            )
        }
    }

    if (showDeleteConfirmDialog && dateToDelete != null) {
        DeleteConfirmDialog(
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
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = com.diary.moonpage.core.theme.MoonTheme.customColors.popupBgColor,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Delete Moment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Main message
                Text(
                    text = "Are you sure you want to delete this moment?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Warning message
                Text(
                    text = "This action cannot be undone and the record will be lost forever.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Delete button
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            "Delete",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
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
fun CalendarGrid(
    pageYearMonth: java.time.YearMonth,
    selectedDate: LocalDate?,
    dailyLogs: Map<LocalDate, com.diary.moonpage.domain.model.DailyLog>,
    selectedFilter: FilterItem?,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: com.diary.moonpage.core.theme.MoonThemeType,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = pageYearMonth.lengthOfMonth()
    val firstDayOfMonth = pageYearMonth.atDay(1)
    val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
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

                            val isMatch = when (val filter = selectedFilter) {
                                null -> true
                                is FilterItem.Mood -> logForDay?.baseMoodId == filter.id
                                is FilterItem.Activity -> logForDay?.activityIds?.contains(filter.id) == true
                                is FilterItem.Special -> {
                                    when (filter.id) {
                                        "music" -> logForDay?.activityIds?.any { it.contains("music", ignoreCase = true) } == true
                                        "sleep" -> (logForDay?.sleepHours ?: 0.0) > 0.0
                                        "sleep_long" -> (logForDay?.sleepHours ?: 0.0) >= 6.0 && (logForDay?.sleepHours ?: 0.0) <= 8.0
                                        "menstruation" -> logForDay?.isMenstruation == true
                                        else -> false
                                    }
                                }
                            }
                            
                            val isFiltered = selectedFilter != null
                            val isDimmed = isFiltered && !isMatch

                            var moodColor: Color? = null
                            var moodIcon: ImageVector? = null
                            var moodDrawable: Int? = null

                            if (logForDay != null) {
                                val mv = MoonIcons.Moods.getMoodVisual(logForDay.baseMoodId, themeType)
                                moodColor = mv.color
                                
                                if (selectedFilter is FilterItem.Activity) {
                                    val activity = dynamicActivities.find { it.id == selectedFilter.id }
                                    if (activity != null) {
                                        val activityIcon = MoonIcons.getIconForActivity(activity.name)
                                        moodDrawable = activityIcon.drawableRes
                                        moodIcon = activityIcon.vector
                                    } else {
                                        moodDrawable = mv.drawableRes
                                    }
                                } else if (selectedFilter is FilterItem.Special) {
                                    moodIcon = selectedFilter.icon
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
                                isFiltered = isFiltered,
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

@Composable
fun CalendarSelectedLogDetail(
    selectedDate: LocalDate?,
    dailyLogs: Map<LocalDate, DailyLog>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: com.diary.moonpage.core.theme.MoonThemeType,
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
                isMenstruation = selectedLog.isMenstruation
            )
        }
    }
}
