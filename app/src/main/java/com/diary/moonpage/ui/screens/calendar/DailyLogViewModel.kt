package com.diary.moonpage.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.core.util.PkceUtil
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.core.util.LocationTracker
import com.diary.moonpage.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: DailyLogRepository,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository,
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val tokenManager: com.diary.moonpage.core.util.TokenManager,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository,
    private val spotifyApi: com.diary.moonpage.data.remote.api.SpotifyApi,
    private val momentRepository: com.diary.moonpage.domain.repository.MomentRepository,
    private val checkAndTriggerNotificationsUseCase: com.diary.moonpage.domain.usecase.notification.CheckAndTriggerNotificationsUseCase,
    @com.diary.moonpage.core.di.ApplicationScope private val applicationScope: kotlinx.coroutines.CoroutineScope,
    val healthConnectManager: com.diary.moonpage.core.util.HealthConnectManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val BASE_URL = "https://hieu-wikipedia.io.vn/"

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DailyLogUiEffect>()
    val uiEffect: SharedFlow<DailyLogUiEffect> = _uiEffect.asSharedFlow()

    private val currentDate = MutableStateFlow<LocalDate?>(null)

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
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            currentDate.filterNotNull().flatMapLatest { date ->
                combine(
                    repository.getDailyLogByDateFlow(date.toString()),
                    momentRepository.moments
                ) { log, moments ->
                    val momentPhotos = moments.filter { it.capturedAt.startsWith(date.toString()) }
                        .map { if (it.imageUrl.startsWith("http")) it.imageUrl else BASE_URL + it.imageUrl.trimStart('/') }
                    Pair(log, momentPhotos)
                }
            }.collect { (log, momentPhotos) ->
                _uiState.update { currentState ->
                    val updatedState = if (log != null) {
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        val bedTime = log.sleepStartTime?.let { try { LocalTime.parse(it, formatter) } catch(e: Exception) { LocalTime.of(0, 0) } } ?: LocalTime.of(0, 0)
                        val wakeTime = log.wakeupTime?.let { try { LocalTime.parse(it, formatter) } catch(e: Exception) { bedTime.plusMinutes(((log.sleepHours ?: 8.0) * 60).toLong()) } } 
                            ?: bedTime.plusMinutes(((log.sleepHours ?: 8.0) * 60).toLong())
                        val calculatedHours = log.sleepHours?.toFloat() ?: 0f
                        val logPhotos = log.dailyPhotos?.map { if (it.startsWith("http")) it else BASE_URL + it.trimStart('/') } ?: emptyList()

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
                                dailyPhotos = logPhotos,
                                momentPhotos = momentPhotos,
                                musicTitle = log.musicRecord,
                                steps = log.steps ?: 0,
                                calories = log.calories ?: 0,
                                distance = log.distance ?: 0.0,
                                isInitialized = true,
                                isLoading = false
                            )
                        } else {
                            // Only update metadata and photos, keep user's current edits
                            currentState.copy(
                                existingLog = log,
                                dailyPhotos = logPhotos,
                                momentPhotos = momentPhotos,
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
                                isInitialized = true,
                                isLoading = false
                            )
                        } else {
                            currentState.copy(
                                momentPhotos = momentPhotos,
                                isLoading = false
                            )
                        }
                    }
                    updatedState
                }
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
                        "Awful" -> 1
                        "Bad" -> 2
                        "Meh" -> 3
                        "Good" -> 4
                        "Rad" -> 5
                        else -> 3
                    }
                    
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
        if (currentDate.value == date) return
        _uiState.update { it.copy(date = date, isLoading = true) }
        currentDate.value = date
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
                _uiState.update { it.copy(dailyPhotos = combined) }
            }
            is DailyLogUiEvent.OnPhotoRemoved -> {
                _uiState.update { it.copy(
                    dailyPhotos = it.dailyPhotos.filter { uri -> uri != event.photoUri }
                ) }
            }
            is DailyLogUiEvent.OnPhotoZoom -> {
                _uiState.update { it.copy(zoomImageUrl = event.imageUrl) }
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
                                val msg = "Health Connect is not available (Status: $status)."
                                _uiEffect.emit(DailyLogUiEffect.ShowSnackBar(msg))
                            }
                            _uiState.update { it.copy(isImportingHealth = false) }
                        } else if (healthConnectManager.hasAllPermissions()) {
                            val data = healthConnectManager.readHealthData(_uiState.value.date)
                            if (data.steps == 0 && data.calories == 0 && data.distance == 0.0 && data.sleepHours == 0.0) {
                                _uiEffect.emit(DailyLogUiEffect.ShowSnackBar("No health data found for this day in Health Connect. Make sure Google Fit is syncing."))
                            } else {
                                val msg = buildString {
                                    append("Imported:")
                                    if (data.steps > 0) append(" ${data.steps} steps")
                                    if (data.calories > 0) append("${if (this.length > 9) "," else ""} ${data.calories} kcal")
                                    if (data.distance > 0.0) append("${if (this.length > 9) "," else ""} ${String.format(java.util.Locale.ENGLISH, "%.1f", data.distance)} km")
                                    if (data.sleepHours > 0) append("${if (this.length > 9) "," else ""} ${String.format(java.util.Locale.ENGLISH, "%.1f", data.sleepHours)}h sleep")
                                }
                                _uiEffect.emit(DailyLogUiEffect.ShowSnackBar(msg))
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
                        _uiEffect.emit(DailyLogUiEffect.ShowSnackBar("Import failed: ${e.localizedMessage ?: "Unknown error"}"))
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

    private fun autoFetchWeather(date: LocalDate) {
        viewModelScope.launch {
            android.util.Log.d("WeatherFetch", "Triggered for date: $date")
            
            // Check if we already have weather for THIS specific date to avoid double notification
            val currentState = _uiState.value
            if (currentState.suggestedWeather != null && 
                currentState.suggestedWeather?.cityName == "Detected" &&
                currentState.date == date) {
                android.util.Log.d("WeatherFetch", "Skipping: already fetched for this date.")
                return@launch
            }

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
                _uiEffect.emit(DailyLogUiEffect.ShowSnackBar("Updating weather conditions..."))
                
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
                            condition = weatherNames.firstOrNull() ?: "Unknown",
                            description = "Weather auto-filled",
                            temp = temp,
                            cityName = "Detected",
                            iconUrl = ""
                        )
                    ) }
                    
                    if (addedCount > 0) {
                        delay(300)
                        val tempText = String.format(java.util.Locale.ENGLISH, "%.1f°C", temp)
                        _uiEffect.emit(DailyLogUiEffect.ShowSnackBar("Weather data fetched: ${weatherNames.joinToString(", ")} ($tempText)"))
                    } else {
                        android.util.Log.d("WeatherFetch", "No new activities added.")
                    }
                }.onFailure { e ->
                    android.util.Log.e("WeatherFetch", "API Error", e)
                    _uiEffect.emit(DailyLogUiEffect.ShowSnackBar("Could not update weather data automatically."))
                }
            } else {
                android.util.Log.w("WeatherFetch", "Location is NULL - check permissions and GPS.")
            }
        }
    }

    private fun saveDailyLog() {
        val state = _uiState.value
        if (state.selectedMood == null || state.selectedMood == 0) {
            viewModelScope.launch {
                _uiEffect.emit(DailyLogUiEffect.ShowSnackBar("Please select a mood first!"))
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Process local photos
            val photoFiles = state.dailyPhotos.filter { !it.startsWith("http") }.mapNotNull { uriString ->
                try {
                    val uri = android.net.Uri.parse(uriString)
                    com.diary.moonpage.core.util.ImageUtils.compressAndCropSquare(context, uri)
                } catch (e: Exception) {
                    null
                }
            }

            // 2. Download existing HTTP photos to include them in the upload,
            // so the backend doesn't overwrite them.
            val existingPhotoUrls = state.dailyPhotos.filter { it.startsWith("http") }
            val existingPhotoFiles = mutableListOf<java.io.File>()
            if (existingPhotoUrls.isNotEmpty()) {
                val client = okhttp3.OkHttpClient()
                for (url in existingPhotoUrls) {
                    try {
                        val request = okhttp3.Request.Builder().url(url).build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful && response.body != null) {
                            val tempFile = java.io.File(context.cacheDir, "retained_photo_${UUID.randomUUID()}.jpg")
                            val sink = tempFile.sink().buffer()
                            sink.writeAll(response.body!!.source())
                            sink.close()
                            existingPhotoFiles.add(tempFile)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DailyLogVM", "Failed to download retained photo: $url", e)
                    }
                }
            }

            val allPhotoFiles = existingPhotoFiles + photoFiles
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            
            repository.createDailyLog(
                baseMoodId = state.selectedMood,
                date = state.date.toString(),
                note = state.noteText.takeIf { it.isNotBlank() },
                sleepHours = state.sleepHours.toDouble(),
                sleepStartTime = state.sleepBedTime.format(timeFormatter),
                isMenstruation = state.isMenstruation,
                menstruationPhase = state.menstruationPhase,
                activityIds = state.selectedActivities,
                dailyPhotos = allPhotoFiles.takeIf { it.isNotEmpty() },
                steps = state.steps,
                musicRecord = state.musicTitle,
                calories = state.calories,
                distance = state.distance,
                wakeupTime = state.sleepWakeTime.format(timeFormatter),
                weather = state.suggestedWeather?.condition,
                temperature = state.suggestedWeather?.temp
            ).onSuccess {
                val msg = if (state.existingLog != null) "Record updated successfully!" else "Record created successfully!"
                statisticsRepository.triggerRefresh()

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

                _uiEffect.emit(DailyLogUiEffect.SaveSuccess(state.date.toString(), msg))
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                existingPhotoFiles.forEach { it.delete() }
                _uiEffect.emit(DailyLogUiEffect.ShowSnackBar(error.message ?: "Failed to save log"))
            }
        }
    }
}
