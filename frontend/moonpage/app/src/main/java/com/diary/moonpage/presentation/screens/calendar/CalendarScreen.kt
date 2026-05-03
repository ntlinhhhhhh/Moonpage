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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.presentation.components.calendar.*
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

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(CalendarUiEvent.DismissMessage)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDailyLog(LocalDate.now().toString()) },
                containerColor = MoonIcons.Moods.getMoodColor(1),
                shape = CircleShape
            ) {
                Image(
                    painter = painterResource(id = MoonIcons.Moods.Good.drawableRes!!),
                    contentDescription = "New Log",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { CalendarSnackbarHost(snackbarHostState = snackbarHostState) }
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
                    onThemeClick = onNavigateToThemeCalendar
                )

                Spacer(modifier = Modifier.height(8.dp))

                CalendarMonthHeader(
                    currentMonthName = currentMonthName,
                    onMonthClick = { onEvent(CalendarUiEvent.OnMonthPickerClick) },
                    onShareClick = { /* TODO */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CalendarHeader()

                CalendarPager(
                    currentYearMonth = uiState.currentYearMonth,
                    selectedDate = uiState.selectedDate,
                    dailyLogs = uiState.dailyLogs,
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
                    onEditLog = { date -> onNavigateToDailyLog(date.toString()) },
                    onDeleteLog = { date -> onEvent(CalendarUiEvent.OnDeleteLog(date)) },
                    onShareClick = { /* TODO */ }
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (uiState.showMonthPicker) {
        MonthYearPickerBottomSheet(
            currentYearMonth = uiState.currentYearMonth,
            onConfirm = { year, month -> onEvent(CalendarUiEvent.OnMonthPickerConfirm(year, month)) },
            onDismiss = { onEvent(CalendarUiEvent.OnMonthPickerDismiss) }
        )
    }

    if (uiState.showFilterSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { onEvent(CalendarUiEvent.OnFilterDismiss) },
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            FilterScreen(
                onDismiss = { onEvent(CalendarUiEvent.OnFilterDismiss) },
                onSeeResults = {
                    onEvent(CalendarUiEvent.OnFilterDismiss)
                }
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
    selectedDate: LocalDate,
    dailyLogs: Map<LocalDate, com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto>,
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

                                val mv = if (logForDay != null) moodVisualFor(logForDay.baseMoodId) else null
                                val isLoggedToday = logForDay?.date == today.toString()

                                DayItem(
                                    day = day,
                                    isSelected = isSelected,
                                    moodColor = mv?.color,
                                    moodIcon = mv?.icon,
                                    moodDrawable = mv?.drawableRes,
                                    isToday = isToday,
                                    isDimmed = logForDay != null && !isLoggedToday,
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
    selectedDate: LocalDate,
    dailyLogs: Map<LocalDate, com.diary.moonpage.data.remote.dto.calendar.DailyLogResponseDto>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    onEditLog: (LocalDate) -> Unit,
    onDeleteLog: (LocalDate) -> Unit,
    onShareClick: () -> Unit
) {
    val selectedLog = dailyLogs[selectedDate] ?: return
    val mv = moodVisualFor(selectedLog.baseMoodId)
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
                IconButton(onClick = { onEditLog(selectedDate) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { onDeleteLog(selectedDate) }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            DayDetailArea(
                date = selectedDate,
                moodIcon = mv.icon,
                moodDrawable = mv.drawableRes,
                moodColor = mv.color,
                moodLabel = mv.label,
                noteSnippet = selectedLog.note,
                activityNames = activityNames
            )
        }
    }
}

@Composable
fun CalendarSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) { data ->
        val isError = data.visuals.message.contains("future", ignoreCase = true) ||
                data.visuals.message.contains("Failed", ignoreCase = true) ||
                data.visuals.message.contains("Please select", ignoreCase = true)
        val isSuccess = data.visuals.message.contains("success", ignoreCase = true) ||
                data.visuals.message.contains("deleted", ignoreCase = true) ||
                data.visuals.message.contains("recorded", ignoreCase = true) ||
                data.visuals.message.contains("updated", ignoreCase = true) ||
                data.visuals.message.contains("edited", ignoreCase = true)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isError -> Icons.Rounded.Error
                    isSuccess -> Icons.Rounded.CheckCircle
                    else -> Icons.Rounded.Info
                },
                contentDescription = null,
                tint = when {
                    isError -> Color(0xFFE57373)
                    isSuccess -> Color(0xFF81C784)
                    else -> Color(0xFFFFB74D)
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.visuals.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class MoodVisual(val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null, val drawableRes: Int? = null, val label: String)

private fun moodVisualFor(baseMoodId: Int?): MoodVisual {
    val moodIcon = when (baseMoodId) {
        1 -> MoonIcons.Moods.Happy
        2 -> MoonIcons.Moods.Good
        3 -> MoonIcons.Moods.Neutral
        4 -> MoonIcons.Moods.Sad
        5 -> MoonIcons.Moods.Angry
        else -> MoonIcons.Moods.Good
    }
    return MoodVisual(moodIcon.color, moodIcon.vector, moodIcon.drawableRes, moodIcon.name)
}
