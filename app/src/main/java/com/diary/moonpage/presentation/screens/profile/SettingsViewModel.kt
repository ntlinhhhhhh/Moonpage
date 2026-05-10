package com.diary.moonpage.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.SettingsPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager
) : ViewModel() {

    val language: StateFlow<String> = settingsPreferencesManager.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val isPasscodeEnabled: StateFlow<Boolean> = settingsPreferencesManager.isPasscodeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBiometricEnabled: StateFlow<Boolean> = settingsPreferencesManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            settingsPreferencesManager.setLanguage(lang)
        }
    }

    fun togglePasscode(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesManager.setPasscodeEnabled(enabled)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesManager.setBiometricEnabled(enabled)
        }
    }
}
