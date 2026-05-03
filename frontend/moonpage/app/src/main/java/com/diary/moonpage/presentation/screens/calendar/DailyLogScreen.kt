package com.diary.moonpage.presentation.screens.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.util.MoonIcon
import com.diary.moonpage.core.util.MoonIcons
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.launch

/**
 * Stateful Component
 */
@Composable
fun DailyLogScreen(
    dateString: String,
    onNavigateBack: () -> Unit,
    onDone: (String) -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dateString) {
        viewModel.setInitialDate(LocalDate.parse(dateString))
    }

    LaunchedEffect(Unit) {
        viewModel.setOnSaveSuccess(onDone)
    }

    DailyLogScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        checkLogExists = viewModel::checkLogExists,
        setPendingDate = viewModel::setPendingDate
    )
}

/**
 * Stateless Component
 */
@Composable
fun DailyLogScreenContent(
    uiState: DailyLogUiState,
    onEvent: (DailyLogUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    checkLogExists: (LocalDate, (Boolean) -> Unit) -> Unit,
    setPendingDate: (LocalDate) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val hasChanges = remember(uiState) {
        uiState.selectedMood != null || uiState.selectedActivities.isNotEmpty() || uiState.noteText.isNotBlank()
    }

    BackHandler(enabled = hasChanges) { onEvent(DailyLogUiEvent.OnExitClick) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(DailyLogUiEvent.DismissMessage)
        }
    }

    Scaffold(
        topBar = {
            DailyLogTopBar(
                date = uiState.date,
                onBackClick = { if (hasChanges) onEvent(DailyLogUiEvent.OnExitClick) else onNavigateBack() },
                onDateClick = { onEvent(DailyLogUiEvent.OnDatePickerClick) }
            )
        },
        bottomBar = {
            DailyLogBottomBar(
                isLoading = uiState.isLoading,
                onSaveClick = { onEvent(DailyLogUiEvent.OnSaveClick) }
            )
        },
        containerColor = colorScheme.background,
        snackbarHost = { CalendarSnackbarHost(snackbarHostState = snackbarHostState) }
    ) { paddingValues ->
        DailyLogMainContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onEvent = onEvent
        )
    }

    if (uiState.showExitDialog) {
        DailyLogExitDialog(
            onDismiss = { onEvent(DailyLogUiEvent.OnDismissExitDialog) },
            onExit = onNavigateBack
        )
    }

    if (uiState.showOverwriteDialog) {
        DailyLogOverwriteDialog(
            onDismiss = { onEvent(DailyLogUiEvent.OnDismissOverwriteDialog) },
            onConfirm = { onEvent(DailyLogUiEvent.OnConfirmOverwrite) }
        )
    }

    if (uiState.showDatePicker) {
        DailyLogDatePickerDialog(
            initialDate = uiState.date,
            onDateSelected = { date ->
                onEvent(DailyLogUiEvent.OnDatePickerDismiss)
                if (date.isAfter(LocalDate.now())) {
                    onEvent(DailyLogUiEvent.DismissMessage)
                } else {
                    checkLogExists(date) { exists ->
                        if (exists) {
                            setPendingDate(date)
                        } else {
                            onEvent(DailyLogUiEvent.OnDateChanged(date))
                        }
                    }
                }
            },
            onDismiss = { onEvent(DailyLogUiEvent.OnDatePickerDismiss) }
        )
    }
}

@Composable
private fun DailyLogTopBar(
    date: LocalDate,
    onBackClick: () -> Unit,
    onDateClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back", tint = colorScheme.onBackground)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onDateClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = colorScheme.onBackground)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = colorScheme.onBackground)
        }
    }
}

@Composable
private fun DailyLogBottomBar(
    isLoading: Boolean,
    onSaveClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box {
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Text("Done", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun DailyLogMainContent(
    modifier: Modifier = Modifier,
    uiState: DailyLogUiState,
    onEvent: (DailyLogUiEvent) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val moods = remember {
        listOf(
            Pair(1, MoonIcons.Moods.Happy),
            Pair(2, MoonIcons.Moods.Good),
            Pair(3, MoonIcons.Moods.Neutral),
            Pair(4, MoonIcons.Moods.Sad),
            Pair(5, MoonIcons.Moods.Angry)
        )
    }

    val activitiesByCategory = remember(uiState.dynamicActivities) {
        uiState.dynamicActivities.groupBy { it.category }.mapValues { entry ->
            entry.value.map { activity ->
                DailyActivity(
                    id = activity.id,
                    label = activity.name,
                    icon = MoonIcons.getIconForActivity(activity.name)
                )
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            DailyMoodSection(
                selectedMood = uiState.selectedMood,
                moods = moods,
                onMoodSelected = { onEvent(DailyLogUiEvent.OnMoodSelected(it)) }
            )
        }

        uiState.enabledCategories.forEach { category ->
            val categoryActivities = activitiesByCategory[category] ?: emptyList()
            if (categoryActivities.isNotEmpty()) {
                item(key = category) {
                    val sectionTitle = if (category == "SelfCare") "Self-Care" else category
                    DailyActivitySection(
                        title = sectionTitle,
                        items = categoryActivities,
                        selectedIds = uiState.selectedActivities,
                        onItemClick = { onEvent(DailyLogUiEvent.OnActivityToggled(it)) }
                    )
                }
            }
        }

        item {
            DailySleepSection(
                sleepHours = uiState.sleepHours,
                onSleepChanged = { onEvent(DailyLogUiEvent.OnSleepChanged(it)) }
            )
        }

        item {
            DailyNoteSection(
                noteText = uiState.noteText,
                onNoteChanged = { onEvent(DailyLogUiEvent.OnNoteChanged(it)) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DailyMoodSection(
    selectedMood: Int?,
    moods: List<Pair<Int, MoonIcon>>,
    onMoodSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(color = colorScheme.surface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("How was your day?", color = colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                moods.forEach { (id, moodIcon) ->
                    val isSelected = selectedMood == id
                    val moodColor = MoonIcons.Moods.getMoodColor(id)
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(
                                if (isSelected) moodColor
                                else moodColor.copy(alpha = 0.2f)
                            )
                            .clickable { onMoodSelected(id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (moodIcon.drawableRes != null) {
                            Image(
                                painter = painterResource(id = moodIcon.drawableRes),
                                contentDescription = null,
                                modifier = Modifier.size(if (isSelected) 32.dp else 28.dp)
                            )
                        } else if (moodIcon.vector != null) {
                            Icon(
                                moodIcon.vector, contentDescription = null,
                                tint = if (isSelected) Color.White else moodColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySleepSection(
    sleepHours: Float,
    onSleepChanged: (Float) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Sleep", color = colorScheme.onBackground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${sleepHours.toInt()}h ${((sleepHours % 1) * 60).toInt()}m", color = colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = colorScheme.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = colorScheme.tertiary, modifier = Modifier.size(28.dp))
                    Slider(
                        value = sleepHours,
                        onValueChange = onSleepChanged,
                        valueRange = 0f..12f,
                        steps = 23,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0h", color = colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
                    Text("6h", color = colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
                    Text("12h", color = colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun DailyNoteSection(
    noteText: String,
    onNoteChanged: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Text("Today's note", color = colorScheme.onBackground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Surface(color = colorScheme.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = noteText, onValueChange = onNoteChanged,
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                placeholder = { Text("Write about your day...", color = colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = colorScheme.primary.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp), maxLines = 8
            )
        }
    }
}

@Composable
private fun DailyLogExitDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Changes have not been saved.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Are you sure you want to exit?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant,
                            contentColor = colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Button(
                        onClick = onExit,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Exit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyLogOverwriteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Data already exists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "There is already a record for this day. Do you want to overwrite it with your current changes?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Cancel", color = colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Overwrite", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class DailyActivity(val id: String, val label: String, val icon: MoonIcon)

@Composable
fun DailyActivitySection(
    title: String,
    items: List<DailyActivity>,
    selectedIds: List<String>,
    onItemClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var isCollapsed by remember { mutableStateOf(false) }
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isCollapsed) -90f else 0f,
        label = "arrowRotation"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { isCollapsed = !isCollapsed }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = colorScheme.onBackground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.rotate(rotation)
            )
        }
        
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                DailyLogGrid(items = items, selectedIds = selectedIds, onItemClick = onItemClick)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogGrid(
    items: List<DailyActivity>,
    selectedIds: List<String>,
    onItemClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            val isSelected = selectedIds.contains(item.id)
            val iconColor = item.icon.color
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(72.dp).clickable { onItemClick(item.id) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) iconColor.copy(alpha = 0.18f)
                            else colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.icon.drawableRes != null) {
                        Image(
                            painter = painterResource(id = item.icon.drawableRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(if (isSelected) 1f else 0.4f)
                        )
                    } else if (item.icon.vector != null) {
                        Icon(
                            item.icon.vector, contentDescription = null,
                            tint = if (isSelected) iconColor else colorScheme.onSurface.copy(alpha = 0.35f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.label,
                    color = if (isSelected) iconColor else colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DailyLogDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val initialPage = 500 * 12
    val baseYearMonth = YearMonth.from(initialDate)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { initialPage * 2 }
    )
    var selectedDateInPicker by remember { mutableStateOf(initialDate) }
    val scope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Which day is this record for?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPageMonth = baseYearMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
                    Text(
                        text = currentPageMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                    Row(modifier = Modifier.padding(end = 4.dp)) {
                        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                            Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = "Prev", tint = colorScheme.primary)
                        }
                        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                            Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "Next", tint = colorScheme.primary)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                ) { page ->
                    val offset = page - initialPage
                    val pageYearMonth = baseYearMonth.plusMonths(offset.toLong())
                    val daysInMonth = (1..pageYearMonth.lengthOfMonth()).toList()
                    val firstDayOfMonth = pageYearMonth.atDay(1)
                    val firstDayOffset = if (firstDayOfMonth.dayOfWeek == java.time.DayOfWeek.SUNDAY) 0 else firstDayOfMonth.dayOfWeek.value
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        userScrollEnabled = false
                    ) {
                        items(firstDayOffset) { Spacer(Modifier) }
                        items(daysInMonth) { day ->
                            val date = pageYearMonth.atDay(day)
                            val isFuture = date.isAfter(LocalDate.now())
                            val isSelected = date == selectedDateInPicker
                            val isToday = date == LocalDate.now()
                            
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> colorScheme.primary
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = !isFuture) { selectedDateInPicker = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = when {
                                        isSelected -> colorScheme.onPrimary
                                        isFuture -> colorScheme.onSurface.copy(alpha = 0.2f)
                                        isToday && !isSelected -> colorScheme.primary
                                        else -> colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant, contentColor = colorScheme.onSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onDateSelected(selectedDateInPicker) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
