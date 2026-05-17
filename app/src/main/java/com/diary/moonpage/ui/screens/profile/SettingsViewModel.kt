package com.diary.moonpage.ui.screens.profile

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
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val reminderManager: com.diary.moonpage.core.util.ReminderManager,
    private val tokenManager: com.diary.moonpage.core.util.TokenManager,
    private val userManager: com.diary.moonpage.core.util.UserManager,
    private val onboardingPrefsManager: com.diary.moonpage.core.util.OnboardingPrefsManager,
    private val activityPreferencesManager: com.diary.moonpage.core.util.ActivityPreferencesManager,
    private val database: com.diary.moonpage.data.local.MoonPageDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            settingsPreferencesManager.language,
            themePreferencesManager.themeType,
            themePreferencesManager.isDarkMode,
            settingsPreferencesManager.isPasscodeEnabled,
            settingsPreferencesManager.isBiometricEnabled,
            settingsPreferencesManager.isReminderEnabled,
            settingsPreferencesManager.reminderTime
        ) { params ->
            SixParams(
                params[0] as String,
                params[1] as MoonThemeType,
                params[2] as Boolean?,
                params[3] as Boolean,
                params[4] as Boolean,
                params[5] as Boolean,
                params[6] as String
            )
        },
        _uiState
    ) { params, currentState ->
        currentState.copy(
            language = params.language,
            themeType = params.themeType,
            isDarkMode = params.isDarkMode,
            isPasscodeEnabled = params.isPasscodeEnabled,
            isBiometricEnabled = params.isBiometricEnabled,
            isReminderEnabled = params.isReminderEnabled,
            reminderTime = params.reminderTime
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private data class SixParams(
        val language: String,
        val themeType: MoonThemeType,
        val isDarkMode: Boolean?,
        val isPasscodeEnabled: Boolean,
        val isBiometricEnabled: Boolean,
        val isReminderEnabled: Boolean,
        val reminderTime: String
    )

    fun setLanguage(lang: String, onLanguageChanged: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.updateLanguage(lang)
            if (result.isSuccess) {
                settingsPreferencesManager.setLanguage(lang)
                
                // Update app-wide locale using AppCompatDelegate
                val appLocale: androidx.core.os.LocaleListCompat = androidx.core.os.LocaleListCompat.forLanguageTags(lang)
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
                
                _uiState.update { it.copy(isLoading = false) }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onLanguageChanged()
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to sync language"
                    )
                }
            }
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

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesManager.setReminderEnabled(enabled)
            if (enabled) {
                val time = uiState.value.reminderTime.split(":")
                reminderManager.scheduleDailyReminder(time[0].toInt(), time[1].toInt())
            } else {
                reminderManager.cancelReminder()
            }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val time = String.format("%02d:%02d", hour, minute)
            settingsPreferencesManager.setReminderTime(time)
            if (uiState.value.isReminderEnabled) {
                reminderManager.scheduleDailyReminder(hour, minute)
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
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 1. Call API to delete account on server
            val result = authRepository.deleteAccount()
            
            if (result.isSuccess) {
                // 2. Clear all local data securely
                try {
                    // Clear Database
                    database.clearAllTables()
                    
                    // Clear all DataStores
                    tokenManager.clearAll()
                    userManager.clearUser()
                    settingsPreferencesManager.clearAll()
                    onboardingPrefsManager.resetOnboarding("") // We don't have a specific userId here anymore, but can clear general onboarding if needed
                    activityPreferencesManager.clearAll()
                    
                    // Clear App Cache
                    context.cacheDir.deleteRecursively()
                    context.filesDir.deleteRecursively()
                    
                    _uiState.update { it.copy(isLoading = false, isDeleteAccountDialogShown = false) }
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onSuccess()
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Account deleted on server, but local cleanup failed: ${e.message}"
                        ) 
                    }
                }
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
