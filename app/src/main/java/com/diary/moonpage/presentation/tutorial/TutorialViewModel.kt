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
    PickToday,
    DailyActivities,
    DailySleep,
    DailyNote,
    DailyPhoto,
    Stats,
    Camera,
    Store,
    Profile
}

data class TutorialState(
    val isVisible: Boolean = false,
    val step: TutorialStep = TutorialStep.PickToday
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
            _state.value = TutorialState(isVisible = !completed)
        }
    }

    fun next() {
        val nextStep = when (_state.value.step) {
            TutorialStep.PickToday -> TutorialStep.DailyActivities
            TutorialStep.DailyActivities -> TutorialStep.DailySleep
            TutorialStep.DailySleep -> TutorialStep.DailyNote
            TutorialStep.DailyNote -> TutorialStep.DailyPhoto
            TutorialStep.DailyPhoto -> TutorialStep.Stats
            TutorialStep.Stats -> TutorialStep.Camera
            TutorialStep.Camera -> TutorialStep.Store
            TutorialStep.Store -> TutorialStep.Profile
            TutorialStep.Profile -> null
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
