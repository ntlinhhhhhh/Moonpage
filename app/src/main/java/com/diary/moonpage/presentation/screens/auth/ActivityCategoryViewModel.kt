package com.diary.moonpage.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ActivityPreferencesManager
import com.diary.moonpage.core.util.OnboardingPrefsManager
import com.diary.moonpage.core.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityCategoryViewModel @Inject constructor(
    private val activityPreferencesManager: ActivityPreferencesManager,
    private val onboardingPrefsManager: OnboardingPrefsManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _enabledCategories = MutableStateFlow<Set<String>>(ActivityPreferencesManager.DEFAULT_ENABLED)
    val enabledCategories = _enabledCategories.asStateFlow()

    init {
        viewModelScope.launch {
            activityPreferencesManager.enabledCategories.collect { saved ->
                _enabledCategories.value = saved
            }
        }
    }

    fun toggle(categoryKey: String) {
        val current = _enabledCategories.value.toMutableSet()
        if (current.contains(categoryKey)) current.remove(categoryKey) else current.add(categoryKey)
        _enabledCategories.value = current
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            activityPreferencesManager.saveEnabledCategories(_enabledCategories.value)
            markOnboardingComplete()
            onDone()
        }
    }

    /** Dùng khi Skip: lưu categories mặc định và đánh dấu onboarding hoàn thành */
    fun saveDefaults(onDone: () -> Unit) {
        viewModelScope.launch {
            activityPreferencesManager.saveEnabledCategories(ActivityPreferencesManager.DEFAULT_ENABLED)
            markOnboardingComplete()
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
