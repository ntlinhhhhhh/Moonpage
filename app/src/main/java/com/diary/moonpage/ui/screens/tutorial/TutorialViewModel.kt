package com.diary.moonpage.ui.screens.tutorial

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
            if (!completed && !_state.value.isVisible) {
                _state.update { it.copy(isVisible = true, step = TutorialStep.HighlightCurrentDay) }
            }
        }
    }

    fun completeStep(step: TutorialStep) {
        if (_state.value.isVisible && _state.value.step == step) {
            next()
        }
    }

    fun skipStep() {
        next()
    }

    fun next() {
        val nextStep = when (_state.value.step) {
            TutorialStep.HighlightCurrentDay -> TutorialStep.HighlightMoodSelection
            TutorialStep.HighlightMoodSelection -> TutorialStep.HighlightActivityCategory
            TutorialStep.HighlightActivityCategory -> TutorialStep.HighlightActivitySelection
            TutorialStep.HighlightActivitySelection -> TutorialStep.HighlightSleepSelection
            TutorialStep.HighlightSleepSelection -> TutorialStep.HighlightDoneButton
            TutorialStep.HighlightDoneButton -> TutorialStep.HighlightStreak
            TutorialStep.HighlightStreak -> TutorialStep.HighlightStatsTab
            TutorialStep.HighlightStatsTab -> TutorialStep.HighlightYearlyReport
            TutorialStep.HighlightYearlyReport -> TutorialStep.HighlightMoodDetailChart
            TutorialStep.HighlightMoodDetailChart -> TutorialStep.HighlightMoodDetailBackButton
            TutorialStep.HighlightMoodDetailBackButton -> TutorialStep.HighlightMomentTab
            TutorialStep.HighlightMomentTab -> TutorialStep.HighlightCameraCapture
            TutorialStep.HighlightCameraCapture -> TutorialStep.HighlightMomentHistoryButton
            TutorialStep.HighlightMomentHistoryButton -> TutorialStep.HighlightStoreTab
            TutorialStep.HighlightStoreTab -> TutorialStep.HighlightStoreThemes
            TutorialStep.HighlightStoreThemes -> TutorialStep.HighlightThemeDetailApply
            TutorialStep.HighlightThemeDetailApply -> TutorialStep.HighlightThemeDetailBackButton
            TutorialStep.HighlightThemeDetailBackButton -> TutorialStep.HighlightProfileTab
            TutorialStep.HighlightProfileTab -> TutorialStep.HighlightProfileSettings
            TutorialStep.HighlightProfileSettings -> TutorialStep.HighlightAccountInfo
            TutorialStep.HighlightAccountInfo -> TutorialStep.HighlightAccountBackButton
            TutorialStep.HighlightAccountBackButton -> null
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
