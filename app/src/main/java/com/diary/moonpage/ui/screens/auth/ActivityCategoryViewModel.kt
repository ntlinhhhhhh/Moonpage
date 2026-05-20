package com.diary.moonpage.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.core.util.OnboardingPrefsManager
import com.diary.moonpage.core.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityCategoryViewModel @Inject constructor(
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val onboardingPrefsManager: OnboardingPrefsManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityCategoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            activityPreferencesManager.enabledCategories.collect { saved ->
                _uiState.update { it.copy(enabledCategories = saved, isLoading = false) }
            }
        }
    }

    fun toggle(categoryKey: String) {
        _uiState.update { currentState ->
            val current = currentState.enabledCategories.toMutableSet()
            if (current.contains(categoryKey)) current.remove(categoryKey) else current.add(categoryKey)
            currentState.copy(enabledCategories = current)
        }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            activityPreferencesManager.saveEnabledCategories(_uiState.value.enabledCategories)
            markOnboardingComplete()
            _uiState.update { it.copy(isLoading = false) }
            onDone()
        }
    }

    fun saveDefaults(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            activityPreferencesManager.saveEnabledCategories(ActivityPreferencesManager.DEFAULT_ENABLED)
            markOnboardingComplete()
            _uiState.update { it.copy(isLoading = false) }
            onDone()
        }
    }

    private suspend fun markOnboardingComplete() {
        val userId = tokenManager.getUserId() ?: return
        if (userId.isNotBlank()) {
            onboardingPrefsManager.setOnboardingCompleted(userId)
        }
    }
}
