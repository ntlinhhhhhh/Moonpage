package com.diary.moonpage.presentation.screens.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.ColorFilter
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost
import com.diary.moonpage.presentation.theme.MoonTheme
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
    onNavigateToMusic: () -> Unit,
    onNavigateToMenstrualCycle: () -> Unit,
    onNavigateToDailyPhoto: () -> Unit,
    onDone: (String) -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dateString) {
        viewModel.setInitialDate(LocalDate.parse(dateString))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            if (effect is DailyLogUiEffect.SaveSuccess) {
                onDone(effect.message)
            }
        }
    }

    DailyLogScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToMusic = onNavigateToMusic,
        onNavigateToMenstrualCycle = onNavigateToMenstrualCycle,
        onNavigateToDailyPhoto = onNavigateToDailyPhoto,
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
    onNavigateToMusic: () -> Unit,
    onNavigateToMenstrualCycle: () -> Unit,
    onNavigateToDailyPhoto: () -> Unit,
    checkLogExists: (LocalDate, (Boolean) -> Unit) -> Unit,
    setPendingDate: (LocalDate) -> Unit
) {
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
                onSaveClick = { onEvent(DailyLogUiEvent.OnSaveClick) },
                themeType = uiState.themeType,
                selectedMood = uiState.selectedMood
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        DailyLogMainContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onEvent = onEvent,
            onNavigateToMusic = onNavigateToMusic,
            onNavigateToMenstrualCycle = onNavigateToMenstrualCycle,
            onNavigateToDailyPhoto = onNavigateToDailyPhoto
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
                if (!date.isAfter(LocalDate.now())) {
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onDateClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH)
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DailyLogBottomBar(
    isLoading: Boolean,
    onSaveClick: () -> Unit,
    themeType: com.diary.moonpage.presentation.theme.MoonThemeType,
    selectedMood: Int?
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val buttonColor = if (selectedMood != null) {
            MoonIcons.Moods.getMoodColor(selectedMood, themeType)
        } else {
            MaterialTheme.colorScheme.primary
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = if (selectedMood != null) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Text("Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DailyLogMainContent(
    modifier: Modifier = Modifier,
    uiState: DailyLogUiState,
    onEvent: (DailyLogUiEvent) -> Unit,
    onNavigateToMusic: () -> Unit,
    onNavigateToMenstrualCycle: () -> Unit,
    onNavigateToDailyPhoto: () -> Unit
) {
    val themeType = uiState.themeType

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DailyMoodSection(
                selectedMood = uiState.selectedMood,
                themeType = themeType,
                onMoodSelected = { onEvent(DailyLogUiEvent.OnMoodSelected(it)) }
            )
        }

        item {
            DailyMusicSection(musicTitle = uiState.musicTitle, onMusicClick = onNavigateToMusic)
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
            DailySleepSection(sleepHours = uiState.sleepHours)
        }

        item {
            DailyMenstruationSection(
                isMenstruation = uiState.isMenstruation,
                onToggle = { onEvent(DailyLogUiEvent.OnMenstruationToggled(it)) },
                onMenstrualClick = onNavigateToMenstrualCycle
            )
        }

        item {
            DailyNoteSection(
                noteText = uiState.noteText,
                onNoteChanged = { onEvent(DailyLogUiEvent.OnNoteChanged(it)) }
            )
        }

        item {
            DailyPhotoSection(photos = uiState.dailyPhotos, onPhotoClick = onNavigateToDailyPhoto)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun DailyMoodSection(
    selectedMood: Int?,
    themeType: com.diary.moonpage.presentation.theme.MoonThemeType,
    onMoodSelected: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("How was your day?", color = MoonTheme.customColors.logCardOnBg, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (1..5).forEach { id ->
                    val isSelected = selectedMood == id
                    val moodColor = MoonIcons.Moods.getMoodColor(id, themeType)
                    val moodVisual = MoonIcons.Moods.getMoodVisual(id, themeType)
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(
                                if (isSelected) moodColor
                                else moodColor.copy(alpha = 0.2f)
                            )
                            .clickable { onMoodSelected(id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (moodVisual.drawableRes != null) {
                            Image(
                                painter = painterResource(id = moodVisual.drawableRes),
                                contentDescription = null,
                                modifier = Modifier.size(if (isSelected) 32.dp else 28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyMusicSection(musicTitle: String?, onMusicClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Music", fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg, fontSize = 14.sp)
                Text("Link account", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(color = MoonTheme.customColors.logItemBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable { onMusicClick() }) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = MoonTheme.customColors.logCardOnBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(musicTitle ?: "Add a song", fontSize = 12.sp, color = MoonTheme.customColors.logCardOnBg)
                }
            }
        }
    }
}

@Composable
fun DailyActivitySection(
    title: String,
    items: List<DailyActivity>,
    selectedIds: List<String>,
    onItemClick: (String) -> Unit
) {
    var isCollapsed by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (isCollapsed) -90f else 0f, label = "")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isCollapsed = !isCollapsed },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    color = MoonTheme.customColors.logCardOnBg,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.6f),
                    modifier = Modifier.rotate(rotation).size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = !isCollapsed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    DailyLogGrid(items = items, selectedIds = selectedIds, onItemClick = onItemClick)
                }
            }
        }
    }
}

@Composable
fun DailyLogGrid(
    items: List<DailyActivity>,
    selectedIds: List<String>,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { item ->
                    val isSelected = selectedIds.contains(item.id)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(68.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MoonTheme.customColors.logItemSelect else MoonTheme.customColors.logItemBg)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple()
                                ) { onItemClick(item.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.icon.drawableRes != null) {
                                Image(
                                    painter = painterResource(id = item.icon.drawableRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp),
                                    colorFilter = if (isSelected) null else ColorFilter.tint(MoonTheme.customColors.logItemIconUnselected.copy(alpha = 0.4f))
                                )
                            } else if (item.icon.vector != null) {
                                Icon(
                                    item.icon.vector,
                                    contentDescription = null,
                                    tint = if (isSelected) item.icon.color else MoonTheme.customColors.logItemIconUnselected.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item.label,
                            color = MoonTheme.customColors.logCardOnBg.copy(alpha = if (isSelected) 1f else 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            maxLines = 1,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.width(68.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySleepSection(sleepHours: Float) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Sleep", fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
                Text("Import", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = MoonTheme.customColors.logItemBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Bedtime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MoonTheme.customColors.logCardOnBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record your sleep", fontSize = 14.sp, color = MoonTheme.customColors.logCardOnBg)
                }
            }
        }
    }
}

@Composable
private fun DailyMenstruationSection(isMenstruation: Boolean, onToggle: (Boolean) -> Unit, onMenstrualClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth().clickable { onMenstrualClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Menstruation", fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("5/${6+i}", fontSize = 10.sp, color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(if (i == 2 && isMenstruation) MoonTheme.customColors.logItemAccent else MoonTheme.customColors.logItemBg)
                                .clickable { if (i == 2) onToggle(!isMenstruation) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = if (i == 2 && isMenstruation) MoonTheme.customColors.logCardOnBg else MoonTheme.customColors.logCardOnBg.copy(alpha = 0.4f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WaterDrop, contentDescription = null, modifier = Modifier.size(14.dp), tint = MoonTheme.customColors.logCardOnBg)
                Spacer(modifier = Modifier.width(4.dp))
                Text("On day 60 of cycle", fontSize = 12.sp, color = MoonTheme.customColors.logCardOnBg)
            }
        }
    }
}

@Composable
private fun DailyNoteSection(noteText: String, onNoteChanged: (String) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's note", fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = MoonTheme.customColors.logItemBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = noteText, onValueChange = onNoteChanged, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    placeholder = { Text("Write here...", color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.5f), fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = MoonTheme.customColors.logCardOnBg,
                        focusedTextColor = MoonTheme.customColors.logCardOnBg,
                        unfocusedTextColor = MoonTheme.customColors.logCardOnBg
                    ),
                    shape = RoundedCornerShape(12.dp), maxLines = 5
                )
            }
        }
    }
}

@Composable
private fun DailyPhotoSection(photos: List<String>, onPhotoClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's photo", fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = MoonTheme.customColors.logItemBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable { onPhotoClick() }) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select up to 3 photos", color = MoonTheme.customColors.logCardOnBg, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun DailyLogExitDialog(onDismiss: () -> Unit, onExit: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Changes have not been saved.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(12.dp)) { Text("Cancel", fontWeight = FontWeight.Bold) }
                    Button(onClick = onExit, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(12.dp)) { Text("Exit", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun DailyLogOverwriteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Data already exists", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "There is already a record for this day. Do you want to overwrite it?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp)) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError), shape = RoundedCornerShape(12.dp)) { Text("Overwrite", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

data class DailyActivity(val id: String, val label: String, val icon: MoonIcon)

@Composable
fun DailyLogDatePickerDialog(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val initialPage = 500 * 12
    val baseYearMonth = YearMonth.from(initialDate)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { initialPage * 2 })
    var selectedDateInPicker by remember { mutableStateOf(initialDate) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Which day is this record for?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val currentPageMonth = baseYearMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
                    Text(text = currentPageMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row {
                        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) { Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) { Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    }
                }
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(260.dp)) { page ->
                    val offset = page - initialPage
                    val pageYearMonth = baseYearMonth.plusMonths(offset.toLong())
                    val daysInMonth = (1..pageYearMonth.lengthOfMonth()).toList()
                    val firstDayOffset = if (pageYearMonth.atDay(1).dayOfWeek == java.time.DayOfWeek.SUNDAY) 0 else pageYearMonth.atDay(1).dayOfWeek.value
                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize(), userScrollEnabled = false) {
                        items(firstDayOffset) { Spacer(Modifier) }
                        items(daysInMonth) { day ->
                            val date = pageYearMonth.atDay(day)
                            val isSelected = date == selectedDateInPicker
                            Box(modifier = Modifier.aspectRatio(1f).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent).clickable(enabled = !date.isAfter(LocalDate.now())) { selectedDateInPicker = date }, contentAlignment = Alignment.Center) {
                                Text(text = day.toString(), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (date.isAfter(LocalDate.now())) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(12.dp)) { Text("Cancel", fontWeight = FontWeight.Bold) }
                    Button(onClick = { onDateSelected(selectedDateInPicker) }, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(12.dp)) { Text("OK", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
