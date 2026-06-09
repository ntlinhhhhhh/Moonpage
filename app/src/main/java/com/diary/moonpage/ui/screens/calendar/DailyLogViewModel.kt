package com.diary.moonpage.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.diary.moonpage.R
import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.core.util.DailyLogPhotoManager
import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.LOCAL_DAILY_LOG_PHOTO_PREFIX
import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.core.util.resolveLogDate
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.core.util.PkceUtil
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.core.util.LocationTracker
import com.diary.moonpage.domain.repository.WeatherRepository
import com.diary.moonpage.widget.glance.MoonpageWidgets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okio.buffer
import okio.sink
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

import com.diary.moonpage.core.util.SpeechToTextManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.diary.moonpage.core.di.ApplicationScope
import kotlinx.coroutines.Job

import com.diary.moonpage.domain.repository.ThemeRepository
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.domain.repository.UserRepository
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.domain.repository.StatisticsRepository
import com.diary.moonpage.domain.repository.MomentRepository
import com.diary.moonpage.domain.usecase.notification.CheckAndTriggerNotificationsUseCase
import com.diary.moonpage.core.util.HealthConnectManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import com.diary.moonpage.ui.components.feedback.SnackbarType
import com.diary.moonpage.core.util.UiText

import com.diary.moonpage.data.remote.api.SpotifyApi

import com.diary.moonpage.core.util.LanguagePreferencesManager
import com.diary.moonpage.service.DailyLogUploadWorker
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: DailyLogRepository,
    private val themeRepository: ThemeRepository,
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val themePreferencesManager: ThemePreferencesManager,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val statisticsRepository: StatisticsRepository,
    private val spotifyApi: SpotifyApi,
    private val momentRepository: MomentRepository,
    private val checkAndTriggerNotificationsUseCase: CheckAndTriggerNotificationsUseCase,
    private val dailyLogPhotoManager: DailyLogPhotoManager,
    @ApplicationScope private val applicationScope: CoroutineScope,
    val healthConnectManager: HealthConnectManager,
    @ApplicationContext private val context: Context,
    private val speechToTextManager: SpeechToTextManager,
    private val languagePreferencesManager: LanguagePreferencesManager,
    private val settingsPreferencesManager: com.diary.moonpage.core.util.SettingsPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DailyLogUiEffect>()
    val uiEffect: SharedFlow<DailyLogUiEffect> = _uiEffect.asSharedFlow()

    private val currentDate = MutableStateFlow<LocalDate?>(null)
    private var listeningJob: Job? = null
    private val cachingPhotoKeys = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    init {
        viewModelScope.launch {
            activityPreferencesManager.enabledCategories.collect { categories ->
                _uiState.update { it.copy(enabledCategories = categories.toList()) }
            }
        }
        viewModelScope.launch {
            activityPreferencesManager.activities.collect { activities ->
                _uiState.update { it.copy(dynamicActivities = activities) }
            }
        }
        viewModelScope.launch {
            themePreferencesManager.themeType.collect { themeType ->
                _uiState.update { it.copy(themeType = themeType) }
                loadCustomMoods()
            }
        }
        viewModelScope.launch {
            themeRepository.activeTheme.collect {
                loadCustomMoods()
            }
        }
        viewModelScope.launch {
            tokenManager.getSpotifyToken().collect { token ->
                _uiState.update { it.copy(isSpotifyLinked = token != null) }
            }
        }
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { it.copy(gender = user?.gender) }
            }
        }
        viewModelScope.launch {
            settingsPreferencesManager.isMusicEnabled.collect { isEnabled ->
                _uiState.update { it.copy(isMusicSectionEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            settingsPreferencesManager.isSleepEnabled.collect { isEnabled ->
                _uiState.update { it.copy(isSleepSectionEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            settingsPreferencesManager.isStepsEnabled.collect { isEnabled ->
                _uiState.update { it.copy(isStepsSectionEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            settingsPreferencesManager.isMenstruationEnabled.collect { isEnabled ->
                _uiState.update { it.copy(isMenstruationSectionEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            dailyLogPhotoManager.getLocalPaths().collect { paths ->
                _uiState.update { it.copy(dailyPhotoLocalPaths = paths) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            dailyLogPhotoManager.cleanupOrphans()
        }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            currentDate.filterNotNull().flatMapLatest { date ->
                combine(
                    repository.getDailyLogByDateFlow(date.toString()),
                    momentRepository.moments,
                    repository.getAllDailyLogsFlow()
                ) { log, moments, allLogs ->
                    val momentPhotos = moments.filter { it.resolveLogDate() == date }
                        .mapNotNull { normalizeAppImageUrl(it.imageUrl) }
                    Triple(log, momentPhotos, allLogs)
                }
            }.collect { (log, momentPhotos, allLogs) ->
                val normalizedLogPhotos = log?.dailyPhotos.orEmpty().mapNotNull(::normalizeAppImageUrl)
                cacheDailyPhotos(normalizedLogPhotos)
                _uiState.update { currentState ->
                    val menstruationDay = calculateMenstruationDay(log, allLogs)
                    val updatedState = if (log != null) {
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        val bedTime = log.sleepStartTime?.let { try { LocalTime.parse(it, formatter) } catch(e: Exception) { LocalTime.of(0, 0) } } ?: LocalTime.of(0, 0)
                        val wakeTime = log.wakeupTime?.let { try { LocalTime.parse(it, formatter) } catch(e: Exception) { bedTime.plusMinutes(((log.sleepHours ?: 8.0) * 60).toLong()) } } 
                            ?: bedTime.plusMinutes(((log.sleepHours ?: 8.0) * 60).toLong())
                        val calculatedHours = log.sleepHours?.toFloat() ?: 0f
                        val logPhotos = normalizedLogPhotos

                        if (!currentState.isInitialized || currentState.date != log.date.let { LocalDate.parse(it) }) {
                            currentState.copy(
                                existingLog = log,
                                date = LocalDate.parse(log.date),
                                selectedMood = log.baseMoodId,
                                selectedActivities = log.activityIds ?: emptyList(),
                                noteText = log.note ?: "",
                                sleepHours = calculatedHours,
                                sleepBedTime = bedTime,
                                sleepWakeTime = wakeTime,
                                isMenstruation = log.isMenstruation,
                                menstruationPhase = log.menstruationPhase,
                                menstruationDay = menstruationDay,
                                dailyPhotos = logPhotos,
                                momentPhotos = momentPhotos,
                                musicTitle = log.musicTitle ?: log.musicRecord,
                                artistName = log.artistName,
                                albumArtUrl = log.albumArtUrl,
                                steps = log.steps ?: 0,
                                calories = log.calories ?: 0,
                                distance = log.distance ?: 0.0,
                                isInitialized = true,
                                isLoading = false
                            )
                        } else {
                            val nextDailyPhotos = if (currentState.hasUnsavedPhotoChanges()) {
                                currentState.dailyPhotos
                            } else {
                                logPhotos
                            }

                            // Only update remote-backed metadata; keep in-progress user edits.
                            currentState.copy(
                                existingLog = log,
                                dailyPhotos = nextDailyPhotos,
                                momentPhotos = momentPhotos,
                                menstruationDay = menstruationDay,
                                isLoading = false
                            )
                        }
                    } else {
                        // Log is null (new log or deleted)
                        if (!currentState.isInitialized) {
                            currentState.copy(
                                existingLog = null,
                                dailyPhotos = emptyList(),
                                momentPhotos = momentPhotos,
                                menstruationDay = null,
                                isInitialized = true,
                                isLoading = false
                            )
                        } else {
                            currentState.copy(
                                momentPhotos = momentPhotos,
                                menstruationDay = null,
                                isLoading = false
                            )
                        }
                    }
                    updatedState
                }
            }
        }
    }

    private fun DailyLogUiState.hasUnsavedPhotoChanges(): Boolean {
        val existingPhotos = existingLog?.dailyPhotos
            .orEmpty()
            .mapNotNull(::normalizeAppImageUrl)
        return dailyPhotos != existingPhotos
    }

    private fun calculateMenstruationDay(currentLog: DailyLog?, allLogs: List<DailyLog>): Int? {
        if (currentLog?.isMenstruation != true) return null
        val currentDate = runCatching { LocalDate.parse(currentLog.date) }.getOrNull() ?: return null
        val periodDates = allLogs.asSequence()
            .filter { it.isMenstruation }
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .toSet()

        var day = 1
        var cursor = currentDate.minusDays(1)
        while (periodDates.contains(cursor)) {
            day += 1
            cursor = cursor.minusDays(1)
        }
        return day
    }

    fun fetchExternalData() {
        fetchWeather()
        fetchRecentSpotifyTracks()
        onEvent(DailyLogUiEvent.OnImportSteps)
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                val location = locationTracker.getCurrentLocation()
                if (location != null) {
                    autoFetchWeather(_uiState.value.date)
                }
            } catch (e: Exception) {
                // Silently fail weather suggestions
            }
        }
    }

    private fun suggestWeatherActivity(condition: String) {
        val weatherActivityId = when (condition.lowercase()) {
            "clear" -> "cce4f580-0871-4c2e-8e07-39ff2967dea7" // Sunny
            "clouds" -> "5aca98d5-e8d8-489e-a5f1-cf06125abb04" // Cloudy
            "rain", "drizzle", "thunderstorm" -> "65784609-4593-494f-997d-6e2fb83dc74f" // Rainy
            "snow" -> "34673dbc-618f-45d2-a739-26a51064a83b" // Snowy
            else -> null
        }
        
        if (weatherActivityId != null && !_uiState.value.selectedActivities.contains(weatherActivityId)) {
            // We don't auto-toggle yet
        }
    }

    private suspend fun getValidSpotifyToken(): String? {
        val currentToken = tokenManager.getSpotifyToken().first() ?: return null
        val expiresAt = tokenManager.getSpotifyExpiresAt()
        
        if (System.currentTimeMillis() > (expiresAt - 5 * 60 * 1000L)) {
            val refreshToken = tokenManager.getSpotifyRefreshToken().first()
            if (refreshToken != null) {
                try {
                    val response = spotifyApi.refreshToken(
                        clientId = com.diary.moonpage.data.remote.api.SpotifyApi.CLIENT_ID,
                        refreshToken = refreshToken
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val newToken = "Bearer ${body.accessToken}"
                        tokenManager.saveSpotifyToken(
                            token = newToken,
                            refreshToken = body.refreshToken ?: refreshToken,
                            expiresIn = body.expiresIn
                        )
                        return newToken
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SpotifyAuth", "Refresh failed", e)
                }
            }
        }
        return currentToken
    }

    private fun fetchRecentSpotifyTracks() {
        viewModelScope.launch {
            getValidSpotifyToken()?.let { token ->
                try {
                    val userResponse = spotifyApi.getCurrentUser(token)
                    val tracks = if (userResponse.isSuccessful && userResponse.body()?.product == "premium") {
                        val recentResponse = spotifyApi.getRecentlyPlayedTracks(token)
                        if (recentResponse.isSuccessful) {
                            recentResponse.body()?.items?.map { it.track } ?: emptyList()
                        } else emptyList()
                    } else {
                        val topResponse = spotifyApi.getTopTracks(token)
                        if (topResponse.isSuccessful) {
                            topResponse.body()?.items ?: emptyList()
                        } else emptyList()
                    }

                    _uiState.update { it.copy(recentTracks = tracks) }

                    if (_uiState.value.musicTitle.isNullOrBlank() && tracks.isNotEmpty()) {
                        val lastTrack = tracks.first()
                        _uiState.update { it.copy(
                            musicTitle = lastTrack.name,
                            artistName = lastTrack.artists.firstOrNull()?.name,
                            albumArtUrl = lastTrack.album.images.firstOrNull()?.url
                        ) }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SpotifyFetch", "Failed to fetch tracks", e)
                }
            }
        }
    }

    private suspend fun loadCustomMoods() {
        val activeThemeId = themeRepository.getActiveThemeId()
        if (activeThemeId != null) {
            val moodEntities = themeRepository.getMoodsForTheme(activeThemeId)
            if (moodEntities.isNotEmpty()) {
                val currentTheme = _uiState.value.themeType
                val customMoods = moodEntities.associate { entity ->
                    val level = when (entity.baseMoodId) {
                        "1" -> 1
                        "2" -> 2
                        "3" -> 3
                        "4" -> 4
                        "5" -> 5
                        "Awful" -> 1
                        "Bad" -> 2
                        "Meh" -> 3
                        "Good" -> 4
                        "Rad" -> 5
                        "Very Sad" -> 1
                        "Sad" -> 2
                        "Neutral" -> 3
                        "Happy" -> 4
                        "Very Happy" -> 5
                        else -> 3
                    }
                    
                    val parsedColor = MoonIcons.parseThemeColor(entity.iconUrl)
                    val color = parsedColor ?: MoonIcons.Moods.getMoodColor(level, currentTheme)

                    level to com.diary.moonpage.core.util.MoonIcon(
                        color = color,
                        name = entity.customName,
                        imageUrl = if (parsedColor == null && entity.iconUrl.isNotBlank()) entity.iconUrl else null,
                        drawableRes = if (parsedColor != null || entity.iconUrl.isBlank()) {
                            when (level) {
                                1 -> com.diary.moonpage.R.drawable.very_sad
                                2 -> com.diary.moonpage.R.drawable.sad
                                3 -> com.diary.moonpage.R.drawable.neutral
                                4 -> com.diary.moonpage.R.drawable.happy
                                5 -> com.diary.moonpage.R.drawable.very_happy
                                else -> com.diary.moonpage.R.drawable.neutral
                            }
                        } else null
                    )
                }
                _uiState.update { it.copy(customMoods = customMoods) }
            } else {
                _uiState.update { it.copy(customMoods = null) }
            }
        } else {
            _uiState.update { it.copy(customMoods = null) }
        }
    }

    fun setInitialDate(date: LocalDate) {
        if (currentDate.value == date) return
        _uiState.update { it.copy(date = date, isLoading = true) }
        currentDate.value = date
        viewModelScope.launch {
            momentRepository.getMyMoments()
        }
        if (date == LocalDate.now()) {
            fetchExternalData()
        }
    }

    fun onEvent(event: DailyLogUiEvent) {
        when (event) {
            is DailyLogUiEvent.OnMoodSelected -> {
                _uiState.update { it.copy(selectedMood = event.moodId) }
            }
            is DailyLogUiEvent.OnCategoryToggle -> {
                val current = _uiState.value.expandedCategories.toMutableSet()
                if (current.contains(event.category)) {
                    current.remove(event.category)
                } else {
                    current.add(event.category)
                }
                _uiState.update { it.copy(expandedCategories = current) }
            }
            is DailyLogUiEvent.OnActivityCategoryCollapseToggle -> {
                val current = _uiState.value.collapsedActivityCategories.toMutableSet()
                if (current.contains(event.category)) {
                    current.remove(event.category)
                } else {
                    current.add(event.category)
                }
                _uiState.update { it.copy(collapsedActivityCategories = current) }
            }
            is DailyLogUiEvent.OnActivityToggled -> {
                val current = _uiState.value.selectedActivities.toMutableList()
                if (current.contains(event.activityId)) {
                    current.remove(event.activityId)
                } else {
                    current.add(event.activityId)
                }
                _uiState.update { it.copy(selectedActivities = current.toList()) }
            }
            is DailyLogUiEvent.OnNoteChanged -> {
                _uiState.update { it.copy(noteText = event.note) }
            }
            is DailyLogUiEvent.OnDateChanged -> {
                _uiState.update { it.copy(date = event.date, isInitialized = false) }
                currentDate.value = event.date
            }
            is DailyLogUiEvent.OnMenstruationToggled -> {
                _uiState.update { it.copy(isMenstruation = event.isMenstruation) }
            }
            is DailyLogUiEvent.OnPhotosChanged -> {
                val current = _uiState.value.dailyPhotos
                val combined = (current + event.photos).distinct().take(10)
                val added = combined.filter { it in event.photos && it !in current }
                _uiState.update { it.copy(dailyPhotos = combined) }
                cacheDailyPhotos(added)
            }
            is DailyLogUiEvent.OnPhotoRemoved -> {
                _uiState.update { it.copy(
                    dailyPhotos = it.dailyPhotos.filter { uri -> uri != event.photoUri },
                    zoomImageUrl = it.zoomImageUrl.takeUnless { zoom -> zoom == event.photoUri }
                ) }
                viewModelScope.launch(Dispatchers.IO) {
                    dailyLogPhotoManager.removePath(
                        event.photoUri,
                        deleteFile = !isRemotePhoto(event.photoUri)
                    )
                }
            }
            is DailyLogUiEvent.OnPhotoZoom -> {
                _uiState.update { it.copy(zoomImageUrl = event.imageUrl) }
            }
            is DailyLogUiEvent.OnMusicChanged -> {
                _uiState.update {
                    it.copy(
                        musicTitle = event.musicTitle,
                        artistName = if (event.musicTitle.isNullOrBlank()) null else it.artistName,
                        albumArtUrl = if (event.musicTitle.isNullOrBlank()) null else it.albumArtUrl
                    )
                }
            }
            is DailyLogUiEvent.OnMusicSelected -> {
                _uiState.update { it.copy(
                    musicTitle = event.title,
                    artistName = event.artist,
                    albumArtUrl = event.imageUrl
                ) }
            }
            is DailyLogUiEvent.OnSleepChanged -> {
                _uiState.update { it.copy(sleepHours = event.hours) }
            }
            DailyLogUiEvent.OnImportClick -> {
                if (_uiState.value.isImportingHealth) return
                
                _uiState.update { it.copy(isImportingHealth = true) }
                viewModelScope.launch {
                    try {
                        val status = healthConnectManager.getSdkStatus()
                        if (status != androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE) {
                            if (status == androidx.health.connect.client.HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED || 
                                status == androidx.health.connect.client.HealthConnectClient.SDK_UNAVAILABLE) {
                                val providerPackageName = "com.google.android.apps.healthdata"
                                _uiEffect.emit(DailyLogUiEffect.NavigateToPlayStore(providerPackageName))
                            } else {
                                _uiEffect.emit(
                                    DailyLogUiEffect.ShowSnackBar(
                                        UiText.StringResource(R.string.health_connect_unavailable_status, status),
                                        SnackbarType.ERROR
                                    )
                                )
                            }
                            _uiState.update { it.copy(isImportingHealth = false) }
                        } else if (healthConnectManager.hasAllPermissions()) {
                            val data = healthConnectManager.readHealthData(_uiState.value.date)
                            if (data.steps == 0 && data.calories == 0 && data.distance == 0.0 && data.sleepHours == 0.0) {
                                _uiEffect.emit(
                                    DailyLogUiEffect.ShowSnackBar(
                                        UiText.StringResource(R.string.no_health_data_found),
                                        SnackbarType.INFO
                                    )
                                )
                            } else {
                                _uiEffect.emit(
                                    DailyLogUiEffect.ShowSnackBar(
                                        UiText.StringResource(R.string.health_import_success),
                                        SnackbarType.SUCCESS
                                    )
                                )
                            }
                            _uiState.update { state -> state.copy(
                                steps = data.steps,
                                calories = data.calories,
                                distance = data.distance,
                                sleepHours = if (data.sleepHours > 0) data.sleepHours.toFloat() else state.sleepHours,
                                sleepBedTime = data.sleepStartTime?.let { timeStr -> try { java.time.LocalTime.parse(timeStr) } catch(e: Exception) { state.sleepBedTime } } ?: state.sleepBedTime,
                                sleepWakeTime = data.sleepWakeTime?.let { timeStr -> try { java.time.LocalTime.parse(timeStr) } catch(e: Exception) { state.sleepWakeTime } } ?: state.sleepWakeTime,
                                isImportingHealth = false
                            ) }
                        } else {
                            // Leave isImportingHealth = true, it will be reset by OnHealthPermissionResult
                            _uiEffect.emit(DailyLogUiEffect.LaunchHealthPermissions(healthConnectManager.permissions))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DailyLogVM", "Import failed", e)
                        val message = e.localizedMessage?.let {
                            UiText.StringResource(R.string.import_failed, it)
                        } ?: UiText.StringResource(R.string.import_failed_unknown)
                        _uiEffect.emit(DailyLogUiEffect.ShowSnackBar(message, SnackbarType.ERROR))
                        _uiState.update { it.copy(isImportingHealth = false) }
                    }
                }
            }
            is DailyLogUiEvent.OnHealthPermissionResult -> {
                _uiState.update { it.copy(isImportingHealth = false) }
                if (event.isGranted) {
                    onEvent(DailyLogUiEvent.OnImportSteps)
                }
            }
            DailyLogUiEvent.OnImportSteps -> {
                if (_uiState.value.isImportingHealth) return
                
                viewModelScope.launch {
                    _uiState.update { it.copy(isImportingHealth = true) }
                    try {
                        val status = healthConnectManager.getSdkStatus()
                        if (status == androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE) {
                            if (healthConnectManager.hasAllPermissions()) {
                                val data = healthConnectManager.readHealthData(_uiState.value.date)
                                _uiState.update { state -> state.copy(
                                    steps = data.steps,
                                    calories = data.calories,
                                    distance = data.distance,
                                    sleepHours = if (data.sleepHours > 0) data.sleepHours.toFloat() else state.sleepHours,
                                    sleepBedTime = data.sleepStartTime?.let { timeStr -> try { java.time.LocalTime.parse(timeStr) } catch(e: Exception) { state.sleepBedTime } } ?: state.sleepBedTime,
                                    sleepWakeTime = data.sleepWakeTime?.let { timeStr -> try { java.time.LocalTime.parse(timeStr) } catch(e: Exception) { state.sleepWakeTime } } ?: state.sleepWakeTime,
                                ) }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DailyLogVM", "Auto-import failed", e)
                        // Don't show snackbar for auto-import to avoid annoyance
                    } finally {
                        _uiState.update { it.copy(isImportingHealth = false) }
                    }
                }
            }
            DailyLogUiEvent.OnLinkMusicAccount -> {
                _uiState.update { it.copy(showSpotifyAuthDialog = true) }
            }
            DailyLogUiEvent.OnSpotifyAuthConfirm -> {
                _uiState.update { it.copy(showSpotifyAuthDialog = false) }
            }
            DailyLogUiEvent.OnSpotifyAuthDismiss -> {
                _uiState.update { it.copy(showSpotifyAuthDialog = false) }
            }
            DailyLogUiEvent.OnSleepRecordClick -> {
                _uiState.update { it.copy(showSleepDialog = true) }
            }
            DailyLogUiEvent.OnSleepDialogDismiss -> {
                _uiState.update { it.copy(showSleepDialog = false) }
            }
            is DailyLogUiEvent.OnSleepTimeConfirmed -> {
                val bedMin = event.bedTime.hour * 60 + event.bedTime.minute
                val wakeMin = event.wakeTime.hour * 60 + event.wakeTime.minute
                val diffMin = if (wakeMin >= bedMin) wakeMin - bedMin else (24 * 60 - bedMin) + wakeMin
                val hours = diffMin / 60f
                _uiState.update { it.copy(
                    sleepHours = hours,
                    sleepBedTime = event.bedTime,
                    sleepWakeTime = event.wakeTime,
                    showSleepDialog = false
                ) }
            }
            DailyLogUiEvent.OnSaveClick -> {
                saveDailyLog()
            }
            DailyLogUiEvent.OnExitClick -> {
                _uiState.update { it.copy(showExitDialog = true) }
            }
            DailyLogUiEvent.OnDismissExitDialog -> {
                _uiState.update { it.copy(showExitDialog = false) }
            }
            DailyLogUiEvent.OnDatePickerClick -> {
                _uiState.update { it.copy(showDatePicker = true) }
            }
            DailyLogUiEvent.OnDatePickerDismiss -> {
                _uiState.update { it.copy(showDatePicker = false) }
            }
            DailyLogUiEvent.OnConfirmOverwrite -> {
                val pending = _uiState.value.pendingDate
                if (pending != null) {
                    _uiState.update { it.copy(date = pending, showOverwriteDialog = false, isInitialized = false) }
                    currentDate.value = pending
                }
            }
            DailyLogUiEvent.OnDismissOverwriteDialog -> {
                _uiState.update { it.copy(showOverwriteDialog = false) }
            }
            DailyLogUiEvent.DismissMessage -> {
                _uiState.update { it.copy(snackbarMessage = null) }
            }
            DailyLogUiEvent.OnLocationPermissionGranted -> {
                autoFetchWeather(_uiState.value.date)
            }
            is DailyLogUiEvent.OnToggleListening -> {
                toggleListening(event.cursorIndex)
            }
        }
    }

    private fun toggleListening(cursorIndex: Int) {
        if (_uiState.value.isListening) {
            listeningJob?.cancel()
            _uiState.update { state ->
                val newText = state.partialNoteText
                    .takeIf { it.isNotEmpty() }
                    ?.let { partial -> insertSpeechText(state.noteText, state.speechInsertIndex, partial) }
                    ?: state.noteText

                state.copy(
                    noteText = newText,
                    partialNoteText = "",
                    speechInsertIndex = null,
                    isListening = false
                )
            }
        } else {
            listeningJob?.cancel()
            val insertIndex = cursorIndex.coerceIn(0, _uiState.value.noteText.length)
            _uiState.update {
                it.copy(
                    partialNoteText = "",
                    speechInsertIndex = insertIndex
                )
            }
            listeningJob = viewModelScope.launch {
                val currentLang = languagePreferencesManager.languageCode.first()
                val systemLocales = context.resources.configuration.locales
                val isSystemVi = (0 until systemLocales.size()).any { systemLocales.get(it).language == "vi" }
                
                // Prioritize app setting, fallback to system locale
                val sttLang = if (currentLang == "vi" || isSystemVi) "vi-VN" else "en-US"
                
                speechToTextManager.startListening(sttLang).collect { state ->
                    when (state) {
                        is SpeechToTextManager.SpeechState.Listening -> {
                            _uiState.update {
                                it.copy(
                                    isListening = true,
                                    speechInsertIndex = it.speechInsertIndex ?: insertIndex
                                )
                            }
                        }
                        is SpeechToTextManager.SpeechState.Partial -> {
                            _uiState.update { it.copy(partialNoteText = state.text) }
                        }
                        is SpeechToTextManager.SpeechState.Success -> {
                            _uiState.update {
                                val newText = state.text
                                    .takeIf { it.isNotEmpty() }
                                    ?.let { text -> insertSpeechText(it.noteText, it.speechInsertIndex, text) }
                                    ?: it.noteText

                                it.copy(
                                    noteText = newText,
                                    partialNoteText = "", 
                                    speechInsertIndex = null,
                                    isListening = false
                                ) 
                            }
                        }
                        is SpeechToTextManager.SpeechState.Error -> {
                            _uiState.update {
                                it.copy(
                                    isListening = false,
                                    partialNoteText = "",
                                    speechInsertIndex = null
                                )
                            }
                            _uiEffect.emit(
                                DailyLogUiEffect.ShowSnackBar(
                                    UiText.DynamicString(state.message),
                                    SnackbarType.ERROR
                                )
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun insertSpeechText(baseText: String, insertIndex: Int?, speechText: String): String {
        val safeIndex = (insertIndex ?: baseText.length).coerceIn(0, baseText.length)
        return baseText.substring(0, safeIndex) + speechText + baseText.substring(safeIndex)
    }

    fun checkLogExists(date: LocalDate, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.getDailyLogByDate(date.toString()).onSuccess {
                onResult(true)
            }.onFailure {
                onResult(false)
            }
        }
    }

    fun setPendingDate(date: LocalDate) {
        _uiState.update { it.copy(pendingDate = date, showOverwriteDialog = true) }
    }

    suspend fun getSpotifyAuthUrl(): String {
        val verifier = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.generateCodeChallenge(verifier)
        val state = UUID.randomUUID().toString()
        tokenManager.saveSpotifyAuthData(verifier, state)
        return com.diary.moonpage.data.remote.api.SpotifyApi.getAuthUrl(challenge, state)
    }

    private fun autoFetchWeather(date: LocalDate) {
        viewModelScope.launch {
            android.util.Log.d("WeatherFetch", "Triggered for date: $date")
            
            // Check if we already have weather for THIS specific date to avoid double notification
            val currentState = _uiState.value
            if (currentState.isFetchingWeather ||
                (currentState.suggestedWeather != null &&
                currentState.suggestedWeather?.cityName == "Detected" &&
                currentState.date == date)) {
                android.util.Log.d("WeatherFetch", "Skipping: already fetched for this date.")
                return@launch
            }

            _uiState.update { it.copy(isFetchingWeather = true) }
            try {
                val location = try {
                locationTracker.getCurrentLocation()
            } catch (e: Exception) {
                android.util.Log.e("WeatherFetch", "Location error", e)
                null
            }

            if (location != null) {
                android.util.Log.d("WeatherFetch", "Location found: ${location.latitude}, ${location.longitude}")
                
                // Ensure UI is ready
                delay(500)
                
                val cached = weatherRepository.getCachedWeather(date)
                val weatherResult = if (cached != null) {
                    Result.success(cached)
                } else {
                    val res = weatherRepository.getWeatherConditions(location.latitude, location.longitude, date)
                    res.onSuccess { weatherRepository.setCachedWeather(date, it) }
                    res
                }
                
                weatherResult.onSuccess { result ->
                    val weatherNames = result.conditions
                    val temp = result.averageTemp
                    
                    android.util.Log.d("WeatherFetch", "Success: $weatherNames, Temp: $temp°C")
                    
                    // Ensure activities are loaded
                    if (_uiState.value.dynamicActivities.isEmpty()) {
                        android.util.Log.d("WeatherFetch", "Waiting for dynamicActivities...")
                        activityPreferencesManager.activities.first { it.isNotEmpty() }
                    }

                    val currentActivities = _uiState.value.selectedActivities.toMutableList()
                    var addedCount = 0
                    weatherNames.forEach { weatherName ->
                        val weatherActivity = _uiState.value.dynamicActivities.find {
                            it.name.equals(weatherName, ignoreCase = true)
                        }
                        weatherActivity?.let { activity ->
                            if (!currentActivities.contains(activity.id)) {
                                currentActivities.add(activity.id)
                                addedCount++
                                android.util.Log.d("WeatherFetch", "Auto-selected activity: ${activity.name}")
                            }
                        }
                    }
                    
                    _uiState.update { it.copy(
                        selectedActivities = currentActivities.toList(),
                        suggestedWeather = com.diary.moonpage.domain.repository.WeatherData(
                            condition = weatherNames.firstOrNull() ?: context.getString(R.string.unknown),
                            description = context.getString(R.string.weather_auto_filled),
                            temp = temp,
                            cityName = context.getString(R.string.detected),
                            iconUrl = ""
                        )
                    ) }
                    
                    if (addedCount == 0) {
                        android.util.Log.d("WeatherFetch", "No new activities added.")
                    }
                }.onFailure { e ->
                    android.util.Log.e("WeatherFetch", "API Error", e)
                }
                } else {
                    android.util.Log.d("WeatherFetch", "Weather auto-fill skipped: location unavailable or permission not granted.")
                }
            } finally {
                _uiState.update { it.copy(isFetchingWeather = false) }
            }
        }
    }

    private fun saveDailyLog() {
        val state = _uiState.value
        val selectedMood = state.selectedMood
        if (selectedMood == null || selectedMood == 0) {
            viewModelScope.launch {
                _uiEffect.emit(
                    DailyLogUiEffect.ShowSnackBar(
                        UiText.StringResource(R.string.select_mood_first),
                        SnackbarType.WARNING
                    )
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val (uploadState, photoPathByKey) = prepareDailyLogPhotosForSave(state)
                val optimisticLog = uploadState.toOptimisticDailyLog(selectedMood, timeFormatter)
                repository.cacheDailyLog(optimisticLog)
                enqueueDailyLogUpload(uploadState, selectedMood, photoPathByKey, timeFormatter)

                statisticsRepository.triggerRefresh()
                MoonpageWidgets.refreshAll(context)

                _uiState.update {
                    it.copy(
                        existingLog = optimisticLog,
                        dailyPhotos = uploadState.dailyPhotos,
                        dailyPhotoLocalPaths = it.dailyPhotoLocalPaths + photoPathByKey,
                        isInitialized = true,
                        isLoading = false
                    )
                }

                val messageResId = if (state.existingLog != null) {
                    R.string.record_updated_success
                } else {
                    R.string.record_created_success
                }
                statisticsRepository.triggerRefresh()
                MoonpageWidgets.refreshAll(context)

                // Cleanup temporary retained files
                existingPhotoFiles.forEach { it.delete() }

                // Trigger notification evaluation using ApplicationScope to survive VM clearing
                applicationScope.launch {
                    try {
                        checkAndTriggerNotificationsUseCase()
                    } catch (e: Exception) {
                        android.util.Log.e("DailyLogVM", "Notification check failed", e)
                    }
                }

                _uiEffect.emit(DailyLogUiEffect.SaveSuccess(state.date.toString(), messageResId))
            } catch (error: CancellationException) {
                _uiState.update { it.copy(isLoading = false) }
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.emit(
                    DailyLogUiEffect.ShowSnackBar(
                        error.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.failed_to_save_log),
                        SnackbarType.ERROR
                    )
                )
            }
        }
    }

    private suspend fun prepareDailyLogPhotosForSave(
        state: DailyLogUiState
    ): Pair<DailyLogUiState, Map<String, String>> {
        val preparedPhotos = mutableListOf<String>()
        val photoPathByKey = mutableMapOf<String, String>()

        state.dailyPhotos.take(10).forEach { photo ->
            if (isRemotePhoto(photo)) {
                preparedPhotos += photo
                dailyLogPhotoManager.getLocalPath(photo)?.let { path ->
                    photoPathByKey[photo] = path
                }
                return@forEach
            }

            val localFile = resolveDailyLogPhotoFile(photo)
                ?: throw IllegalStateException(context.getString(R.string.failed_to_process_image))
            val stableKey = if (isLocalDailyPhotoKey(photo)) photo else createLocalDailyPhotoKey()

            if (stableKey != photo) {
                dailyLogPhotoManager.migratePath(photo, stableKey)
            }

            val localPath = dailyLogPhotoManager.getLocalPath(stableKey)
                ?: dailyLogPhotoManager.saveLocalPhoto(stableKey, localFile).absolutePath

            preparedPhotos += stableKey
            photoPathByKey[stableKey] = localPath
        }

        return state.copy(dailyPhotos = preparedPhotos.distinct()) to photoPathByKey
    }

    private fun DailyLogUiState.toOptimisticDailyLog(
        selectedMood: Int,
        timeFormatter: DateTimeFormatter
    ): DailyLog {
        return DailyLog(
            id = existingLog?.id ?: "local_${date}",
            baseMoodId = selectedMood,
            date = date.toString(),
            note = noteText.takeIf { it.isNotBlank() },
            sleepHours = sleepHours.toDouble(),
            sleepStartTime = sleepBedTime.format(timeFormatter),
            isMenstruation = isMenstruation,
            menstruationPhase = menstruationPhase,
            steps = steps,
            musicRecord = musicTitle,
            musicTitle = musicTitle,
            artistName = artistName,
            albumArtUrl = albumArtUrl,
            dailyPhotos = dailyPhotos,
            activityIds = selectedActivities,
            createdAt = existingLog?.createdAt,
            calories = calories,
            distance = distance,
            wakeupTime = sleepWakeTime.format(timeFormatter),
            weather = suggestedWeather?.condition,
            temperature = suggestedWeather?.temp
        )
    }

    private fun enqueueDailyLogUpload(
        state: DailyLogUiState,
        selectedMood: Int,
        photoPathByKey: Map<String, String>,
        timeFormatter: DateTimeFormatter
    ) {
        val data = Data.Builder()
            .putString(DailyLogUploadWorker.KEY_DATE, state.date.toString())
            .putInt(DailyLogUploadWorker.KEY_BASE_MOOD_ID, selectedMood)
            .putDouble(DailyLogUploadWorker.KEY_SLEEP_HOURS, state.sleepHours.toDouble())
            .putString(DailyLogUploadWorker.KEY_SLEEP_START_TIME, state.sleepBedTime.format(timeFormatter))
            .putBoolean(DailyLogUploadWorker.KEY_IS_MENSTRUATION, state.isMenstruation)
            .putStringArray(DailyLogUploadWorker.KEY_ACTIVITY_IDS, state.selectedActivities.toTypedArray())
            .putStringArray(DailyLogUploadWorker.KEY_PHOTO_KEYS, state.dailyPhotos.toTypedArray())
            .putStringArray(DailyLogUploadWorker.KEY_PHOTO_PATHS, state.dailyPhotos.map { photoPathByKey[it].orEmpty() }.toTypedArray())
            .putInt(DailyLogUploadWorker.KEY_STEPS, state.steps)
            .putInt(DailyLogUploadWorker.KEY_CALORIES, state.calories)
            .putDouble(DailyLogUploadWorker.KEY_DISTANCE, state.distance)
            .putString(DailyLogUploadWorker.KEY_WAKEUP_TIME, state.sleepWakeTime.format(timeFormatter))
            .apply {
                state.noteText.takeIf { it.isNotBlank() }?.let { putString(DailyLogUploadWorker.KEY_NOTE, it) }
                state.menstruationPhase?.let { putString(DailyLogUploadWorker.KEY_MENSTRUATION_PHASE, it) }
                state.musicTitle?.takeIf { it.isNotBlank() }?.let { putString(DailyLogUploadWorker.KEY_MUSIC_TITLE, it) }
                state.artistName?.takeIf { it.isNotBlank() }?.let { putString(DailyLogUploadWorker.KEY_ARTIST_NAME, it) }
                state.albumArtUrl?.takeIf { it.isNotBlank() }?.let { putString(DailyLogUploadWorker.KEY_ALBUM_ART_URL, it) }
                state.suggestedWeather?.condition?.takeIf { it.isNotBlank() }?.let { putString(DailyLogUploadWorker.KEY_WEATHER, it) }
                state.suggestedWeather?.temp?.let { putDouble(DailyLogUploadWorker.KEY_TEMPERATURE, it) }
            }
            .build()

        val request = DailyLogUploadWorker.buildRequest(data)
        WorkManager.getInstance(context).enqueueUniqueWork(
            DailyLogUploadWorker.UNIQUE_WORK_PREFIX + state.date,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun cacheDailyPhotos(photoKeys: List<String>) {
        val keysToCache = photoKeys
            .distinct()
            .filter { key ->
                val cachedPath = _uiState.value.dailyPhotoLocalPaths[key]
                val isAlreadyCached = cachedPath != null && File(cachedPath).exists()
                !isAlreadyCached && cachingPhotoKeys.add(key)
            }
        if (keysToCache.isEmpty()) return

        applicationScope.launch(Dispatchers.IO) {
            try {
                keysToCache.forEach { key ->
                    try {
                        resolveDailyLogPhotoFile(key)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        android.util.Log.w("DailyLogVM", "Skipped daily log photo cache: $key", e)
                    }
                }
            } finally {
                keysToCache.forEach(cachingPhotoKeys::remove)
            }
        }
    }

    private suspend fun resolveDailyLogPhotoFile(photoKey: String): File? {
        dailyLogPhotoManager.getLocalPath(photoKey)?.let { path ->
            val file = File(path)
            if (file.exists()) return file
        }

        return if (isRemotePhoto(photoKey)) {
            downloadAndCacheRemotePhoto(photoKey)
        } else {
            cacheLocalDailyPhoto(photoKey)
        }
    }

    private suspend fun cacheLocalDailyPhoto(photoKey: String): File? {
        if (isLocalDailyPhotoKey(photoKey)) return null

        val compressedFile = ImageUtils.compressAndCropSquare(
            context = context,
            uri = android.net.Uri.parse(photoKey)
        ) ?: return null

        val cachedFile = dailyLogPhotoManager.saveLocalPhoto(photoKey, compressedFile)
        if (compressedFile.absolutePath != cachedFile.absolutePath) {
            compressedFile.delete()
        }
        return cachedFile
    }

    private suspend fun downloadAndCacheRemotePhoto(url: String): File? {
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()
        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) return@use null

                val tempFile = File(context.cacheDir, "retained_photo_${UUID.randomUUID()}.jpg")
                tempFile.sink().buffer().use { sink ->
                    sink.writeAll(body.source())
                }
                val cachedFile = dailyLogPhotoManager.savePhoto(url, tempFile)
                tempFile.delete()
                cachedFile
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.w("DailyLogVM", "Failed to cache retained photo: $url", e)
            null
        }
    }

    private fun isRemotePhoto(photo: String): Boolean {
        return photo.startsWith("http", ignoreCase = true)
    }

    private fun isLocalDailyPhotoKey(photo: String): Boolean {
        return photo.startsWith(LOCAL_DAILY_LOG_PHOTO_PREFIX)
    }

    private fun createLocalDailyPhotoKey(): String {
        return "$LOCAL_DAILY_LOG_PHOTO_PREFIX${UUID.randomUUID()}"
    }
}
