package com.diary.moonpage.presentation.screens.calendar

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
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
import com.diary.moonpage.core.theme.MoonTheme
import com.diary.moonpage.core.theme.MoonThemeType
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.launch
import com.diary.moonpage.core.theme.LocalLocale

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
    val scope = rememberCoroutineScope()

    LaunchedEffect(dateString) {
        viewModel.setInitialDate(LocalDate.parse(dateString))
    }

    // Permission Launcher for Health Connect
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthConnectManager.requestPermissionsContract()
    ) { granted ->
        // Notify VM of the result so it can reset loading state and proceed if granted
        viewModel.onEvent(DailyLogUiEvent.OnHealthPermissionResult(
            granted.containsAll(viewModel.healthConnectManager.permissions)
        ))
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DailyLogUiEffect.SaveSuccess -> onDone(effect.message)
                is DailyLogUiEffect.LaunchHealthPermissions -> {
                    // Safe launch of permission activity with a small delay to avoid transition conflicts on MIUI
                    scope.launch {
                        kotlinx.coroutines.delay(100)
                        permissionLauncher.launch(effect.permissions)
                    }
                }
                is DailyLogUiEffect.ShowSnackBar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
                else -> {}
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onEvent(DailyLogUiEvent.OnPhotosChanged(uris.map { it.toString() }))
        }
    }

    DailyLogScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToMusic = onNavigateToMusic,
        onNavigateToMenstrualCycle = onNavigateToMenstrualCycle,
        onNavigateToDailyPhoto = {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onImportSteps = { viewModel.onEvent(DailyLogUiEvent.OnImportClick) },
        onLinkMusicAccount = { viewModel.onEvent(DailyLogUiEvent.OnLinkMusicAccount) },
        checkLogExists = { date, callback -> viewModel.checkLogExists(date, callback) },
        setPendingDate = { date -> viewModel.setPendingDate(date) },
        getSpotifyAuthUrl = { viewModel.getSpotifyAuthUrl() },
        scope = scope,
        snackbarHostState = snackbarHostState
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
    onImportSteps: () -> Unit,
    onLinkMusicAccount: () -> Unit,
    checkLogExists: (LocalDate, (Boolean) -> Unit) -> Unit,
    setPendingDate: (LocalDate) -> Unit,
    getSpotifyAuthUrl: suspend () -> String,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val isChanged = remember<Boolean>(uiState) {
        val existing = uiState.existingLog
        if (existing == null) {
            // New Log: Enabled if mood or activities are selected
            (uiState.selectedMood != null && uiState.selectedMood != 0) || uiState.selectedActivities.isNotEmpty()
        } else {
            // Edit Log: Enabled if any field differs from the existing record
            val moodChanged = uiState.selectedMood != existing.baseMoodId
            
            // Compare activities as sets to be order-insensitive
            val currentActivities = uiState.selectedActivities.toSet()
            val existingActivities = (existing.activityIds ?: emptyList<String>()).toSet()
            val activitiesChanged = currentActivities != existingActivities
            
            val noteChanged = uiState.noteText != (existing.note ?: "")
            val sleepChanged = kotlin.math.abs(uiState.sleepHours - (existing.sleepHours?.toFloat() ?: 0f)) > 0.1f
            val menstruationChanged = uiState.isMenstruation != existing.isMenstruation
            
            // For photos, we strip the BASE_URL prefix to compare correctly with stored paths
            val baseUrl = "https://hieu-wikipedia.io.vn/"
            val currentPhotosNormalized = uiState.dailyPhotos.map { it.removePrefix(baseUrl).trimStart('/') }.toSet()
            val existingPhotos = (existing.dailyPhotos ?: emptyList<String>()).map { it.trimStart('/') }.toSet()
            val photosChanged = currentPhotosNormalized != existingPhotos
            
            val musicChanged = uiState.musicTitle != existing.musicRecord
            
            moodChanged || activitiesChanged || noteChanged || sleepChanged || menstruationChanged || photosChanged || musicChanged
        }
    }

    BackHandler(enabled = isChanged) { onEvent(DailyLogUiEvent.OnExitClick) }

    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        topBar = {
            DailyLogTopBar(
                date = uiState.date,
                onBackClick = { if (isChanged) onEvent(DailyLogUiEvent.OnExitClick) else onNavigateBack() },
                onDateClick = { onEvent(DailyLogUiEvent.OnDatePickerClick) }
            )
        },
        bottomBar = {
            DailyLogBottomBar(
                isLoading = uiState.isLoading,
                onSaveClick = { onEvent(DailyLogUiEvent.OnSaveClick) },
                enabled = isChanged
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            DailyLogMainContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onEvent = onEvent,
                onNavigateToMusic = onNavigateToMusic,
                onNavigateToMenstrualCycle = onNavigateToMenstrualCycle,
                onNavigateToDailyPhoto = onNavigateToDailyPhoto,
                onImportSteps = onImportSteps,
                onLinkMusicAccount = onLinkMusicAccount
            )
            
            MoonSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
        }
    }

    if (uiState.showSpotifyAuthDialog) {
        SpotifyAuthDialog(
            onDismiss = { onEvent(DailyLogUiEvent.OnSpotifyAuthDismiss) },
            onConfirm = { 
                onEvent(DailyLogUiEvent.OnSpotifyAuthDismiss)
                scope.launch {
                    val authUrl = getSpotifyAuthUrl()
                    uriHandler.openUri(authUrl)
                }
            }
        )
    }

    if (uiState.showExitDialog) {
        DailyLogExitDialog(
            onDismiss = { onEvent(DailyLogUiEvent.OnDismissExitDialog) },
            onExit = {
                onEvent(DailyLogUiEvent.OnDismissExitDialog)
                scope.launch {
                    kotlinx.coroutines.delay(50)
                    onNavigateBack()
                }
            }
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

    if (uiState.showSleepDialog) {
        SleepRecordDialog(
            initialBedTime = uiState.sleepBedTime,
            initialWakeTime = uiState.sleepWakeTime,
            onDismiss = { onEvent(DailyLogUiEvent.OnSleepDialogDismiss) },
            onConfirm = { bed, wake ->
                onEvent(DailyLogUiEvent.OnSleepTimeConfirmed(bed, wake))
            }
        )
    }
}

@Composable
private fun DailyLogTopBar(
    date: LocalDate,
    onBackClick: () -> Unit,
    onDateClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.close), tint = MaterialTheme.colorScheme.onSurface)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDateClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val currentLanguage = LocalLocale.current
                val formatter = remember(currentLanguage) {
                    if (currentLanguage == "vi") {
                        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("vi"))
                    } else {
                        DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH)
                    }
                }
                Text(
                    text = date.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun DailyLogBottomBar(
    isLoading: Boolean,
    onSaveClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled && !isLoading,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    stringResource(R.string.done), 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = if (enabled && !isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
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
    onNavigateToDailyPhoto: () -> Unit,
    onImportSteps: () -> Unit,
    onLinkMusicAccount: () -> Unit
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            DailyMoodSection(
                selectedMood = uiState.selectedMood,
                themeType = themeType,
                customMoods = uiState.customMoods,
                suggestedWeather = uiState.suggestedWeather,
                onMoodSelected = { onMoodId ->
                    if (onMoodId != 0) {
                        onEvent(DailyLogUiEvent.OnMoodSelected(onMoodId))
                    }
                }
            )
        }

        item {
            DailyMusicSection(
                musicTitle = uiState.musicTitle, 
                artistName = uiState.artistName,
                albumArtUrl = uiState.albumArtUrl,
                isLinked = uiState.isSpotifyLinked,
                recentTracks = uiState.recentTracks,
                onMusicClick = onNavigateToMusic,
                onLinkAccount = onLinkMusicAccount,
                onTrackSelected = { track ->
                    onEvent(DailyLogUiEvent.OnMusicSelected(
                        title = track.name,
                        artist = track.artists.firstOrNull()?.name ?: "Unknown",
                        imageUrl = track.album.images.firstOrNull()?.url ?: ""
                    ))
                }
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
            DailyHealthSection(
                steps = uiState.steps,
                calories = uiState.calories,
                distance = uiState.distance,
                isImporting = uiState.isImportingHealth,
                onImportClick = onImportSteps
            )
        }

        item {
            DailySleepSection(
                sleepHours = uiState.sleepHours,
                bedTime = uiState.sleepBedTime,
                wakeTime = uiState.sleepWakeTime,
                isImporting = uiState.isImportingHealth,
                onSleepClick = { onEvent(DailyLogUiEvent.OnSleepRecordClick) },
                onImportClick = onImportSteps
            )
        }

        val isMale = uiState.gender == "Male" || uiState.gender == "Nam"
        if (!isMale) {
            item {
                DailyMenstruationSection(
                    isMenstruation = uiState.isMenstruation,
                    onToggle = { onEvent(DailyLogUiEvent.OnMenstruationToggled(it)) },
                    onMenstrualClick = onNavigateToMenstrualCycle
                )
            }
        }

        item {
            DailyNoteSection(
                noteText = uiState.noteText,
                onNoteChanged = { onEvent(DailyLogUiEvent.OnNoteChanged(it)) }
            )
        }

        item {
            DailyPhotoSection(
                photos = uiState.dailyPhotos, 
                onPhotoClick = onNavigateToDailyPhoto,
                onPhotoRemove = { onEvent(DailyLogUiEvent.OnPhotoRemoved(it)) }
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun DailyMoodSection(
    selectedMood: Int?,
    themeType: MoonThemeType,
    customMoods: Map<Int, MoonIcon>? = null,
    suggestedWeather: com.diary.moonpage.domain.repository.WeatherData? = null,
    onMoodSelected: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            if (suggestedWeather != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    coil.compose.AsyncImage(
                        model = suggestedWeather.iconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${suggestedWeather.cityName}: ${suggestedWeather.temp.toInt()}°C, ${suggestedWeather.condition}",
                        fontSize = 13.sp,
                        color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.7f)
                    )
                }
            }

            Text(stringResource(R.string.select_mood), color = MoonTheme.customColors.logCardOnBg, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (5 downTo 1).forEach { id ->
                    val isSelected = selectedMood == id
                    val moodVisual = MoonIcons.Moods.getMoodVisual(id, themeType, customMoods)
                    val moodColor = moodVisual.color
                    Box(
                        modifier = Modifier.size(54.dp).clip(CircleShape)
                            .background(
                                if (isSelected) moodColor
                                else moodColor.copy(alpha = 0.2f)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onMoodSelected(id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (moodVisual.drawableRes != null) {
                            Image(
                                painter = painterResource(id = moodVisual.drawableRes),
                                contentDescription = moodVisual.name,
                                modifier = Modifier.size(if (isSelected) 38.dp else 32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyMusicSection(
    musicTitle: String?, 
    artistName: String?,
    albumArtUrl: String?,
    isLinked: Boolean,
    recentTracks: List<com.diary.moonpage.data.remote.api.SpotifyTrack> = emptyList(),
    onMusicClick: () -> Unit, 
    onLinkAccount: () -> Unit,
    onTrackSelected: (com.diary.moonpage.data.remote.api.SpotifyTrack) -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Music", fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg, fontSize = 16.sp)
                if (!isLinked) {
                    Text(
                        "Link account", 
                        fontSize = 11.sp, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onLinkAccount() }
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = MoonTheme.customColors.successColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Spotify Linked", fontSize = 11.sp, color = MoonTheme.customColors.successColor)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            Surface(
                color = MoonTheme.customColors.logItemBg, 
                shape = RoundedCornerShape(12.dp), 
                modifier = Modifier.fillMaxWidth().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onMusicClick() }
            ) {
                if (musicTitle != null) {
                    Row(
                        modifier = Modifier.padding(10.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        coil.compose.AsyncImage(
                            model = albumArtUrl,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(musicTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg, maxLines = 1)
                            Text(artistName ?: "Unknown Artist", fontSize = 12.sp, color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.6f), maxLines = 1)
                        }
                        Icon(Icons.Rounded.MusicNote, null, tint = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.4f))
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(12.dp), 
                        horizontalArrangement = Arrangement.Center, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, modifier = Modifier.size(20.dp), tint = MoonTheme.customColors.logCardOnBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add a song", fontSize = 14.sp, color = MoonTheme.customColors.logCardOnBg)
                    }
                }
            }

            if (musicTitle == null && recentTracks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recently Played", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 8.dp))
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recentTracks) { track ->
                        Card(
                            modifier = Modifier.width(120.dp).clickable { onTrackSelected(track) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logItemBg)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                coil.compose.AsyncImage(
                                    model = track.album.images.firstOrNull()?.url,
                                    contentDescription = null,
                                    modifier = Modifier.size(104.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(track.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = MoonTheme.customColors.logCardOnBg)
                                Text(track.artists.firstOrNull()?.name ?: "", fontSize = 10.sp, maxLines = 1, color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.6f))
                            }
                        }
                    }
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
                    fontSize = 16.sp
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
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MoonTheme.customColors.logItemSelect else MoonTheme.customColors.logItemBg)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onItemClick(item.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.icon.drawableRes != null) {
                                Image(
                                    painter = painterResource(id = item.icon.drawableRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp),
                                    colorFilter = if (isSelected) {
                                        if (MoonTheme.customColors.logItemIconSelected != Color.Unspecified) ColorFilter.tint(MoonTheme.customColors.logItemIconSelected) else null
                                    } else {
                                        ColorFilter.tint(MoonTheme.customColors.logItemIconUnselected)
                                    }
                                )
                            } else if (item.icon.vector != null) {
                                Icon(
                                    item.icon.vector,
                                    contentDescription = null,
                                    tint = if (isSelected) {
                                        if (MoonTheme.customColors.logItemIconSelected != Color.Unspecified) MoonTheme.customColors.logItemIconSelected else item.icon.color
                                    } else {
                                        MoonTheme.customColors.logItemIconUnselected
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item.label,
                            color = MoonTheme.customColors.logCardOnBg.copy(alpha = if (isSelected) 1f else 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
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
private fun DailyHealthSection(steps: Int, calories: Int, distance: Double, isImporting: Boolean, onImportClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Health & Steps", fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg, fontSize = 16.sp)
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        "Import", 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onImportClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HealthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "Steps",
                    value = steps.toString(),
                    icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    color = Color(0xFF66BB6A)
                )
                HealthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "Calories",
                    value = "$calories kcal",
                    icon = Icons.Rounded.LocalFireDepartment,
                    color = Color(0xFFEF5350)
                )
                HealthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "Distance",
                    value = String.format(java.util.Locale.getDefault(), "%.1f km", distance),
                    icon = Icons.Rounded.Route,
                    color = Color(0xFF42A5F5)
                )
            }
        }
    }
}

@Composable
private fun DailySleepSection(
    sleepHours: Float,
    bedTime: LocalTime,
    wakeTime: LocalTime,
    isImporting: Boolean,
    onSleepClick: () -> Unit,
    onImportClick: () -> Unit
) {
    val currentLanguage = LocalLocale.current
    val fmt = remember(currentLanguage) {
        if (currentLanguage == "vi") {
            DateTimeFormatter.ofPattern("HH:mm", Locale("vi"))
        } else {
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        }
    }
    val hrs = sleepHours.toInt()
    val mins = ((sleepHours - hrs) * 60).toInt()
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {      
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.sleep_analysis), fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        "Import", 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onImportClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = if (sleepHours > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MoonTheme.customColors.logItemBg, 
                shape = RoundedCornerShape(12.dp), 
                modifier = Modifier.fillMaxWidth().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onSleepClick() }
            ) {
                if (sleepHours <= 0) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.Nightlight, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.sleep_question), color = MoonTheme.customColors.logCardOnBg, fontSize = 14.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Nightlight, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp).weight(0.12f)
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.25f)) {
                            Text(bedTime.format(fmt), fontWeight = FontWeight.Bold, color = if (sleepHours > 0) MaterialTheme.colorScheme.primary else MoonTheme.customColors.logCardOnBg, fontSize = 14.sp)
                            Text(if (currentLanguage == "vi") "Đi ngủ" else "Went to bed", color = if (sleepHours > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MoonTheme.customColors.logCardOnBg.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.26f)) {
                            Text(
                                if (mins == 0) "${hrs}h" else "${hrs}h ${mins}m", 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                            Text(if (currentLanguage == "vi") "Đã ngủ" else "Asleep", color = if (sleepHours > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MoonTheme.customColors.logCardOnBg.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.25f)) {
                            Text(wakeTime.format(fmt), fontWeight = FontWeight.Bold, color = if (sleepHours > 0) MaterialTheme.colorScheme.primary else MoonTheme.customColors.logCardOnBg, fontSize = 14.sp)
                            Text(if (currentLanguage == "vi") "Thức dậy" else "Woke up", color = if (sleepHours > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MoonTheme.customColors.logCardOnBg.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                        
                        Icon(
                            Icons.Rounded.AlarmOn, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp).weight(0.12f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthStatItem(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        color = MoonTheme.customColors.logItemBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MoonTheme.customColors.logCardOnBg)
            Text(label, fontSize = 10.sp, color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun DailyMenstruationSection(isMenstruation: Boolean, onToggle: (Boolean) -> Unit, onMenstrualClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), 
        modifier = Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onMenstrualClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.period_cycle), fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val formatter = DateTimeFormatter.ofPattern("M/d")
                        val date = LocalDate.now().minusDays(2L - i.toLong())
                        Text(date.format(formatter), fontSize = 10.sp, color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(
                                    if (i == 2 && isMenstruation) Color(0xFFFFEBEE)
                                    else MoonTheme.customColors.logItemBg
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { if (i == 2) onToggle(!isMenstruation) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = if (i == 2 && isMenstruation) Color(0xFFEF5350)
                                       else MoonTheme.customColors.logCardOnBg.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WaterDrop, contentDescription = null, modifier = Modifier.size(14.dp), tint = MoonTheme.customColors.logCardOnBg)
                Spacer(modifier = Modifier.width(4.dp))
                val currentLanguage = LocalLocale.current
                Text(
                    if (currentLanguage == "vi") "Đã bật theo dõi kinh nguyệt" else "Menstrual tracking enabled", 
                    fontSize = 12.sp, 
                    color = MoonTheme.customColors.logCardOnBg
                )
            }
        }
    }
}

@Composable
private fun DailyNoteSection(noteText: String, onNoteChanged: (String) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {      
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.note), fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = MoonTheme.customColors.logItemBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                val currentLanguage = LocalLocale.current
                OutlinedTextField(
                    value = noteText, onValueChange = onNoteChanged, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    placeholder = { Text(if (currentLanguage == "vi") "Viết tại đây..." else "Write here...", color = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.5f), fontSize = 14.sp) },
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
private fun DailyPhotoSection(
    photos: List<String>, 
    onPhotoClick: () -> Unit,
    onPhotoRemove: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg), modifier = Modifier.fillMaxWidth()) {      
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.daily_photo), fontWeight = FontWeight.Bold, color = MoonTheme.customColors.logCardOnBg)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (photos.isEmpty()) {
                    Surface(
                        color = MoonTheme.customColors.logItemBg, 
                        shape = RoundedCornerShape(12.dp), 
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPhotoClick() }
                    ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.4f)) 
                        Spacer(modifier = Modifier.height(12.dp))
                        val currentLanguage = LocalLocale.current
                        Text(if (currentLanguage == "vi") "Chọn ảnh" else "Select photos", color = MoonTheme.customColors.logCardOnBg, fontSize = 14.sp)
                    }
                }
            } else {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos.take(10)) { photoUri ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onPhotoClick() }
                            ) {
                                coil.compose.AsyncImage(
                                    model = photoUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onPhotoRemove(photoUri) },
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.5f)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                    if (photos.size < 10) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MoonTheme.customColors.logItemBg.copy(alpha = 0.5f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onPhotoClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = MoonTheme.customColors.logCardOnBg.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyLogExitDialog(onDismiss: () -> Unit, onExit: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(20.dp), color = MoonTheme.customColors.popupBgColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.discard_confirmation), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MoonTheme.customColors.cancelBtnBgColor, contentColor = MoonTheme.customColors.cancelBtnTextColor), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold) }
                    Button(onClick = onExit, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.discard), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun DailyLogOverwriteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MoonTheme.customColors.popupBgColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                val currentLanguage = LocalLocale.current
                Text(text = if (currentLanguage == "vi") "Dữ liệu đã tồn tại" else "Data already exists", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)  
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = if (currentLanguage == "vi") "Đã có bản ghi cho ngày này. Bạn có muốn ghi đè không?" else "There is already a record for this day. Do you want to overwrite it?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MoonTheme.customColors.cancelBtnBgColor, contentColor = MoonTheme.customColors.cancelBtnTextColor), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold) }
                    Button(onClick = { onConfirm() }, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError), shape = RoundedCornerShape(12.dp)) { Text(if (currentLanguage == "vi") "Ghi đè" else "Overwrite", fontWeight = FontWeight.Bold) }
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
    val currentLanguage = LocalLocale.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), color = MoonTheme.customColors.popupBgColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (currentLanguage == "vi") "Bản ghi này cho ngày nào?" else "Which day is this record for?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 16.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) {
                    val currentPageMonth = baseYearMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
                    val monthText = if (currentLanguage == "vi") {
                        "Tháng ${currentPageMonth.monthValue} ${currentPageMonth.year}"
                    } else {
                        currentPageMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))
                    }
                    Text(
                        text = monthText, 
                        style = MaterialTheme.typography.titleSmall, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
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
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = MoonTheme.customColors.cancelBtnBgColor, contentColor = MoonTheme.customColors.cancelBtnTextColor), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold) }
                    Button(onClick = { onDateSelected(selectedDateInPicker) }, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun SpotifyAuthDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MoonTheme.customColors.popupBgColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color(0xFF1DB954), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                val currentLanguage = LocalLocale.current
                Text(text = if (currentLanguage == "vi") "Kết nối Spotify" else "Connect to Spotify", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = if (currentLanguage == "vi") "MoonPage muốn truy cập tài khoản Spotify của bạn để tìm kiếm và thêm nhạc vào nhật ký." else "MoonPage wants to access your Spotify account to search and add music to your logs.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MoonTheme.customColors.cancelBtnBgColor, contentColor = MoonTheme.customColors.cancelBtnTextColor), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold) }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954), contentColor = Color.White), shape = RoundedCornerShape(12.dp)) { Text(if (currentLanguage == "vi") "Cho phép truy cập" else "Allow Access", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
