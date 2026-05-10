package com.diary.moonpage.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.domain.model.DailyLog
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.core.util.PkceUtil
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
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager,
    private val tokenManager: com.diary.moonpage.core.util.TokenManager,
    val healthConnectManager: com.diary.moonpage.core.util.HealthConnectManager
) : ViewModel() {

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
            }
        }
        viewModelScope.launch {
            tokenManager.getSpotifyToken().collect { token ->
                _uiState.update { it.copy(isSpotifyLinked = token != null) }
            }
        }
    }

    fun setInitialDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
        fetchLogForDate(date)
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
                _uiState.update { it.copy(dailyPhotos = event.photos) }
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
                _uiState.update { it.copy(
                    existingLog = log,
                    selectedMood = log.baseMoodId,
                    selectedActivities = log.activityIds ?: emptyList(),
                    noteText = log.note ?: "",
                    sleepHours = log.sleepHours?.toFloat() ?: 0f,
                    sleepBedTime = log.sleepBedTime?.let { LocalTime.parse(it, formatter) } ?: LocalTime.of(0, 0),
                    sleepWakeTime = log.sleepWakeTime?.let { LocalTime.parse(it, formatter) } ?: LocalTime.of(7, 0),
                    isMenstruation = log.isMenstruation,
                    menstruationPhase = log.menstruationPhase,
                    dailyPhotos = log.dailyPhotos ?: emptyList(),
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
            
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            
            repository.createDailyLog(
                baseMoodId = state.selectedMood,
                date = state.date.toString(),
                note = state.noteText.takeIf { it.isNotBlank() },
                sleepHours = state.sleepHours.toDouble(),
                isMenstruation = state.isMenstruation,
                menstruationPhase = state.menstruationPhase,
                activityIds = state.selectedActivities,
                dailyPhotos = null,
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
                _uiEffect.emit(DailyLogUiEffect.SaveSuccess(msg))
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = error.message ?: "Failed to save log") }
            }
        }
    }
}
