package com.diary.moonpage.presentation.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.OnboardingPrefsManager
import com.diary.moonpage.core.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TutorialStep {
    HighlightCurrentDay,
    HighlightHowWasYourDay,
    HighlightHobbiesButton,
    HighlightFirstHobby,
    HighlightTodayNotes,
    HighlightTodayPhotos,
    HighlightDoneButton
}

data class TutorialState(
    val isVisible: Boolean = false,
    val step: TutorialStep = TutorialStep.HighlightCurrentDay
)

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val onboardingPrefsManager: OnboardingPrefsManager
) : ViewModel() {
    private val _state = MutableStateFlow(TutorialState())
    val state: StateFlow<TutorialState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            val completed = onboardingPrefsManager.checkTutorialCompleted(userId)
            _state.update { current ->
                when {
                    completed -> TutorialState(isVisible = false)
                    current.isVisible -> current
                    else -> TutorialState(isVisible = true, step = TutorialStep.HighlightCurrentDay)
                }
            }
        }
    }

    fun completeStep(step: TutorialStep) {
        if (_state.value.isVisible && _state.value.step == step) {
            next()
        }
    }

    fun skipStep() {
        val nextStep = when (_state.value.step) {
            TutorialStep.HighlightHobbiesButton,
            TutorialStep.HighlightFirstHobby -> TutorialStep.HighlightTodayNotes
            TutorialStep.HighlightTodayNotes -> TutorialStep.HighlightTodayPhotos
            TutorialStep.HighlightTodayPhotos -> TutorialStep.HighlightDoneButton
            else -> return
        }
        _state.update { it.copy(step = nextStep) }
    }

    fun next() {
        val nextStep = when (_state.value.step) {
            TutorialStep.HighlightCurrentDay -> TutorialStep.HighlightHowWasYourDay
            TutorialStep.HighlightHowWasYourDay -> TutorialStep.HighlightHobbiesButton
            TutorialStep.HighlightHobbiesButton -> TutorialStep.HighlightFirstHobby
            TutorialStep.HighlightFirstHobby -> TutorialStep.HighlightTodayNotes
            TutorialStep.HighlightTodayNotes -> TutorialStep.HighlightTodayPhotos
            TutorialStep.HighlightTodayPhotos -> TutorialStep.HighlightDoneButton
            TutorialStep.HighlightDoneButton -> null
        }

        if (nextStep == null) {
            complete()
        } else {
            _state.update { it.copy(step = nextStep) }
        }
    }

    fun complete() {
        viewModelScope.launch {
            tokenManager.getUserId()?.let { onboardingPrefsManager.setTutorialCompleted(it) }
            _state.value = TutorialState(isVisible = false)
        }
    }
}
