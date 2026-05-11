package com.diary.moonpage.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.core.util.PkceUtil
import com.diary.moonpage.core.util.MoonIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: DailyLogRepository,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository,
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val tokenManager: com.diary.moonpage.core.util.TokenManager,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository,
    private val weatherRepository: com.diary.moonpage.domain.repository.WeatherRepository,
    private val spotifyApi: com.diary.moonpage.data.remote.api.SpotifyApi,
    val healthConnectManager: com.diary.moonpage.core.util.HealthConnectManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val BASE_URL = "https://hieu-wikipedia.io.vn/"

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DailyLogUiEffect>()
    val uiEffect: SharedFlow<DailyLogUiEffect> = _uiEffect.asSharedFlow()

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
            tokenManager.getSpotifyToken().collect { token ->
                _uiState.update { it.copy(isSpotifyLinked = token != null) }
            }
        }
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { it.copy(gender = user?.gender) }
            }
        }
    }

    fun fetchExternalData() {
        fetchWeather()
        fetchRecentSpotifyTracks()
        onEvent(DailyLogUiEvent.OnImportSteps)
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            viewModelScope.launch {
                                weatherRepository.getCurrentWeather(location.latitude, location.longitude).onSuccess { data ->
                                    _uiState.update { it.copy(suggestedWeather = data) }
                                    // Auto-select if no activities selected yet or if weather is prominent
                                    suggestWeatherActivity(data.condition)
                                }
                            }
                        }
                    }
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
            // We don't auto-toggle yet, but we could highlight it in the UI
        }
    }

    private fun fetchRecentSpotifyTracks() {
        viewModelScope.launch {
            tokenManager.getSpotifyToken().firstOrNull()?.let { token ->
                try {
                    val response = spotifyApi.getRecentlyPlayedTracks(token)
                    if (response.isSuccessful) {
                        val tracks = response.body()?.items?.map { it.track } ?: emptyList()
                        _uiState.update { it.copy(recentTracks = tracks) }
                        
                        // Auto-fill if empty
                        if (_uiState.value.musicTitle.isNullOrBlank() && tracks.isNotEmpty()) {
                            val lastTrack = tracks.first()
                            _uiState.update { it.copy(
                                musicTitle = lastTrack.name,
                                artistName = lastTrack.artists.firstOrNull()?.name,
                                albumArtUrl = lastTrack.album.images.firstOrNull()?.url
                            ) }
                        }
                    }
                } catch (e: Exception) {}
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
                        "Awful" -> 1
                        "Bad" -> 2
                        "Meh" -> 3
                        "Good" -> 4
                        "Rad" -> 5
                        else -> 3
                    }
                    
                    // Fallback to local mood color if API hex is invalid or bad
                    val color = try {
                        if (!entity.iconUrl.isNullOrBlank() && (entity.iconUrl.startsWith("#") || entity.iconUrl.length == 6 || entity.iconUrl.length == 8)) {
                            val colorStr = if (entity.iconUrl.startsWith("#")) entity.iconUrl else "#${entity.iconUrl}"
                            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorStr))
                        } else {
                            MoonIcons.Moods.getMoodColor(level, currentTheme)
                        }
                    } catch (e: Exception) {
                        MoonIcons.Moods.getMoodColor(level, currentTheme)
                    }

                    level to com.diary.moonpage.core.util.MoonIcon(
                        color = color,
                        name = entity.customName,
                        drawableRes = when (level) {
                            1 -> com.diary.moonpage.R.drawable.very_sad
                            2 -> com.diary.moonpage.R.drawable.sad
                            3 -> com.diary.moonpage.R.drawable.neutral
                            4 -> com.diary.moonpage.R.drawable.happy
                            5 -> com.diary.moonpage.R.drawable.very_happy
                            else -> com.diary.moonpage.R.drawable.neutral
                        }
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
        if (_uiState.value.isInitialized && _uiState.value.date == date) return
        _uiState.update { it.copy(date = date, isInitialized = true) }
        fetchLogForDate(date)
        if (date == LocalDate.now()) {
            fetchExternalData()
        }
    }

    fun onEvent(event: DailyLogUiEvent) {
        when (event) {
            is DailyLogUiEvent.OnMoodSelected -> {
                _uiState.update { it.copy(selectedMood = event.moodId) }
            }
            is DailyLogUiEvent.OnActivityToggled -> {
                val current = _uiState.value.selectedActivities.toMutableList()
                if (current.contains(event.activityId)) {
                    current.remove(event.activityId)
                } else {
                    current.add(event.activityId)
                }
                _uiState.update { it.copy(selectedActivities = current) }
            }
            is DailyLogUiEvent.OnNoteChanged -> {
                _uiState.update { it.copy(noteText = event.note) }
            }
            is DailyLogUiEvent.OnDateChanged -> {
                _uiState.update { it.copy(date = event.date) }
                fetchLogForDate(event.date)
            }
            is DailyLogUiEvent.OnMenstruationToggled -> {
                _uiState.update { it.copy(isMenstruation = event.isMenstruation) }
            }
            is DailyLogUiEvent.OnPhotosChanged -> {
                // event.photos is List<String> (URIs)
                val current = _uiState.value.dailyPhotos
                val combined = (current + event.photos).distinct().take(3)
                _uiState.update { it.copy(dailyPhotos = combined) }
            }
            is DailyLogUiEvent.OnPhotoRemoved -> {
                _uiState.update { it.copy(
                    dailyPhotos = it.dailyPhotos.filter { uri -> uri != event.photoUri }
                ) }
            }
            is DailyLogUiEvent.OnMusicChanged -> {
                _uiState.update { it.copy(musicTitle = event.musicTitle) }
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
            DailyLogUiEvent.OnImportSteps -> {
                viewModelScope.launch {
                    if (healthConnectManager.hasAllPermissions()) {
                        try {
                            val data = healthConnectManager.readHealthData(_uiState.value.date)
                            _uiState.update { it.copy(
                                steps = data.steps,
                                calories = data.calories,
                                distance = data.distance,
                                snackbarMessage = "Health data imported: ${data.steps} steps!"
                            ) }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(snackbarMessage = "Failed to read health data: ${e.message}") }
                        }
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
                    _uiState.update { it.copy(date = pending, showOverwriteDialog = false) }
                    fetchLogForDate(pending)
                }
            }
            DailyLogUiEvent.OnDismissOverwriteDialog -> {
                _uiState.update { it.copy(showOverwriteDialog = false) }
            }
            DailyLogUiEvent.DismissMessage -> {
                _uiState.update { it.copy(snackbarMessage = null) }
            }
        }
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

    private fun fetchLogForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getDailyLogByDate(date.toString()).onSuccess { log ->
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val bedTime = log.sleepBedTime?.let { try { LocalTime.parse(it, formatter) } catch(e: Exception) { LocalTime.of(0, 0) } } ?: LocalTime.of(0, 0)
                val wakeTime = log.sleepWakeTime?.let { try { LocalTime.parse(it, formatter) } catch(e: Exception) { LocalTime.of(7, 0) } } ?: LocalTime.of(7, 0)
                
                val calculatedHours = if ((log.sleepHours ?: 0.0) <= 0.0) {
                    if (log.sleepBedTime != null && log.sleepWakeTime != null) {
                        val bedMin = bedTime.hour * 60 + bedTime.minute
                        val wakeMin = wakeTime.hour * 60 + wakeTime.minute
                        val diffMin = if (wakeMin >= bedMin) wakeMin - bedMin else (24 * 60 - bedMin) + wakeMin
                        diffMin / 60f
                    } else 0f
                } else {
                    log.sleepHours?.toFloat() ?: 0f
                }

                _uiState.update { it.copy(
                    existingLog = log,
                    selectedMood = log.baseMoodId,
                    selectedActivities = log.activityIds ?: emptyList(),
                    noteText = log.note ?: "",
                    sleepHours = calculatedHours,
                    sleepBedTime = bedTime,
                    sleepWakeTime = wakeTime,
                    isMenstruation = log.isMenstruation,
                    menstruationPhase = log.menstruationPhase,
                    dailyPhotos = log.dailyPhotos?.map { if (it.startsWith("http")) it else BASE_URL + it.trimStart('/') } ?: emptyList(),
                    musicTitle = log.songTitle,
                    artistName = log.artistName,
                    albumArtUrl = log.albumArtUrl,
                    steps = log.steps ?: 0,
                    calories = log.calories ?: 0,
                    distance = log.distance ?: 0.0,
                    isLoading = false
                ) }
            }.onFailure {
                _uiState.update { it.copy(
                    existingLog = null,
                    selectedMood = null,
                    selectedActivities = emptyList(),
                    noteText = "",
                    sleepHours = 0f,
                    sleepBedTime = LocalTime.of(0, 0),
                    sleepWakeTime = LocalTime.of(7, 0),
                    isMenstruation = false,
                    menstruationPhase = null,
                    dailyPhotos = emptyList(),
                    musicTitle = null,
                    artistName = null,
                    albumArtUrl = null,
                    steps = 0,
                    calories = 0,
                    distance = 0.0,
                    isLoading = false
                ) }
            }
        }
    }

    private fun saveDailyLog() {
        val state = _uiState.value
        if (state.selectedMood == null) {
            _uiState.update { it.copy(snackbarMessage = "Please select a mood first!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Only upload photos that are local (don't start with http)
            val photoFiles = state.dailyPhotos.filter { !it.startsWith("http") }.mapNotNull { uriString ->
                try {
                    val uri = android.net.Uri.parse(uriString)
                    com.diary.moonpage.core.util.ImageUtils.compressAndCropSquare(context, uri)
                } catch (e: Exception) {
                    null
                }
            }
            
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            
            repository.createDailyLog(
                baseMoodId = state.selectedMood,
                date = state.date.toString(),
                note = state.noteText.takeIf { it.isNotBlank() },
                sleepHours = state.sleepHours.toDouble(),
                isMenstruation = state.isMenstruation,
                menstruationPhase = state.menstruationPhase,
                activityIds = state.selectedActivities,
                dailyPhotos = photoFiles.takeIf { it.isNotEmpty() },
                songTitle = state.musicTitle,
                artistName = state.artistName,
                albumArtUrl = state.albumArtUrl,
                sleepBedTime = state.sleepBedTime.format(timeFormatter),
                sleepWakeTime = state.sleepWakeTime.format(timeFormatter),
                steps = state.steps,
                calories = state.calories,
                distance = state.distance
            ).onSuccess {
                val msg = if (state.existingLog != null) "Record updated successfully!" else "Record created successfully!"
                statisticsRepository.triggerRefresh()
                _uiEffect.emit(DailyLogUiEffect.SaveSuccess(msg))
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = error.message ?: "Failed to save log") }
            }
        }
    }
}
