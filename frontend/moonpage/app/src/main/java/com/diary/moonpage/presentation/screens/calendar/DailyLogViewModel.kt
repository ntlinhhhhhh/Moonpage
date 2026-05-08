package com.diary.moonpage.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.domain.repository.DailyLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import java.io.File

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: DailyLogRepository,
    private val activityPreferencesManager: ActivityPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DailyLogUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

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
    }

    fun onEvent(event: DailyLogUiEvent) {
        when (event) {
            is DailyLogUiEvent.OnMoodSelected -> {
                _uiState.update { it.copy(selectedMood = event.moodId) }
            }
            is DailyLogUiEvent.OnActivityToggled -> {
                _uiState.update { state ->
                    val newList = if (state.selectedActivities.contains(event.activityId)) {
                        state.selectedActivities - event.activityId
                    } else {
                        state.selectedActivities + event.activityId
                    }
                    state.copy(selectedActivities = newList)
                }
            }
            is DailyLogUiEvent.OnNoteChanged -> {
                _uiState.update { it.copy(noteText = event.note) }
            }
            is DailyLogUiEvent.OnSleepChanged -> {
                _uiState.update { it.copy(sleepHours = event.hours) }
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
            DailyLogUiEvent.OnSaveClick -> {
                saveDailyLog()
            }
            DailyLogUiEvent.OnExitClick -> {
                _uiState.update { it.copy(showExitDialog = true) }
            }
            DailyLogUiEvent.OnDismissExitDialog -> {
                _uiState.update { it.copy(showExitDialog = false) }
            }
            DailyLogUiEvent.OnDismissOverwriteDialog -> {
                _uiState.update { it.copy(showOverwriteDialog = false) }
            }
            DailyLogUiEvent.OnConfirmOverwrite -> {
                _uiState.value.pendingDate?.let { date ->
                    _uiState.update { it.copy(date = date, showOverwriteDialog = false) }
                    fetchLogForDate(date)
                }
            }
            DailyLogUiEvent.OnDatePickerClick -> {
                _uiState.update { it.copy(showDatePicker = true) }
            }
            DailyLogUiEvent.OnDatePickerDismiss -> {
                _uiState.update { it.copy(showDatePicker = false) }
            }
            DailyLogUiEvent.DismissMessage -> {
                _uiState.update { it.copy(snackbarMessage = null) }
            }
        }
    }

    fun setInitialDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
        fetchLogForDate(date)
    }

    private fun fetchLogForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getDailyLogByDate(date.toString()).onSuccess { log ->
                _uiState.update { it.copy(
                    existingLog = log,
                    selectedMood = log.baseMoodId,
                    selectedActivities = log.activityIds ?: emptyList(),
                    noteText = log.note ?: "",
                    sleepHours = log.sleepHours?.toFloat() ?: 7f,
                    isMenstruation = log.isMenstruation,
                    menstruationPhase = log.menstruationPhase,
                    dailyPhotos = log.dailyPhotos ?: emptyList(),
                    isLoading = false
                ) }
            }.onFailure {
                _uiState.update { it.copy(
                    existingLog = null,
                    selectedMood = null,
                    selectedActivities = emptyList(),
                    noteText = "",
                    sleepHours = 7f,
                    isMenstruation = false,
                    menstruationPhase = null,
                    dailyPhotos = emptyList(),
                    musicTitle = null,
                    isLoading = false
                ) }
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

    private fun saveDailyLog() {
        val state = _uiState.value
        if (state.selectedMood == null) {
            _uiState.update { it.copy(snackbarMessage = "Please select a mood first!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.createDailyLog(
                baseMoodId = state.selectedMood,
                date = state.date.toString(),
                note = state.noteText.takeIf { it.isNotBlank() },
                sleepHours = state.sleepHours.toDouble(),
                isMenstruation = state.isMenstruation,
                menstruationPhase = state.menstruationPhase,
                activityIds = state.selectedActivities,
                dailyPhotos = state.dailyPhotos.takeIf { it.isNotEmpty() } as List<File>?
            ).onSuccess {
                val msg = if (state.existingLog != null) "Record updated successfully!" else "Record created successfully!"
                _uiEffect.send(DailyLogUiEffect.SaveSuccess(msg))
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = error.message ?: "Failed to save log") }
            }
        }
    }
}
