package com.diary.moonpage.presentation.screens.profile

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val themePreferencesManager: ThemePreferencesManager,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsPreferencesManager.language,
        themePreferencesManager.themeType,
        themePreferencesManager.isDarkMode,
        settingsPreferencesManager.isPasscodeEnabled,
        settingsPreferencesManager.isBiometricEnabled,
        _uiState
    ) { args: Array<Any?> ->
        val language = args[0] as String
        val themeType = args[1] as MoonThemeType
        val isDarkMode = args[2] as Boolean?
        val isPasscodeEnabled = args[3] as Boolean
        val isBiometricEnabled = args[4] as Boolean
        val currentState = args[5] as SettingsUiState
        
        currentState.copy(
            language = language,
            themeType = themeType,
            isDarkMode = isDarkMode,
            isPasscodeEnabled = isPasscodeEnabled,
            isBiometricEnabled = isBiometricEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setLanguage(lang: String, onLanguageChanged: () -> Unit) {
        viewModelScope.launch {
            settingsPreferencesManager.setLanguage(lang)
            onLanguageChanged()
        }
    }

    fun setTheme(isDark: Boolean?) {
        viewModelScope.launch {
            themePreferencesManager.setDarkMode(isDark)
        }
    }

    fun togglePasscode(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                // In a real app, this would navigate to a screen to set a passcode
                // For now we just toggle it for the UI logic
                settingsPreferencesManager.setPasscodeEnabled(true)
            } else {
                settingsPreferencesManager.setPasscode(null)
            }
        }
    }

    fun checkBiometricSupport(): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                val support = checkBiometricSupport()
                if (support == BiometricManager.BIOMETRIC_SUCCESS) {
                    settingsPreferencesManager.setBiometricEnabled(true)
                } else {
                    _uiState.update { it.copy(error = "Biometric not supported or not set up") }
                }
            } else {
                settingsPreferencesManager.setBiometricEnabled(false)
            }
        }
    }

    fun showDeleteAccountDialog() {
        _uiState.update { it.copy(isDeleteAccountDialogShown = true) }
    }

    fun dismissDeleteAccountDialog() {
        _uiState.update { it.copy(isDeleteAccountDialogShown = false) }
    }

    fun deleteUserAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isDeleteAccountDialogShown = false) }
                onSuccess()
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.message ?: "Failed to delete account"
                    ) 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}