package com.diary.moonpage.ui.screens.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.SettingsPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsPreferencesManager.isPasscodeEnabled,
                settingsPreferencesManager.passcode,
                settingsPreferencesManager.isBiometricEnabled
            ) { isPasscodeEnabled, passcode, isBiometricEnabled ->
                SecurityUiState(
                    isPasscodeEnabled = isPasscodeEnabled,
                    savedPasscode = passcode,
                    isBiometricEnabled = isBiometricEnabled,
                    isBiometricAvailable = checkBiometricAvailability()
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun verifyPasscode(input: String): Boolean {
        val saved = _uiState.value.savedPasscode
        android.util.Log.d("SecurityVM", "Verifying passcode. Input: $input, Saved: $saved")
        return saved != null && input == saved
    }

    fun setPasscode(passcode: String?) {
        viewModelScope.launch {
            settingsPreferencesManager.setPasscode(passcode)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesManager.setBiometricEnabled(enabled)
        }
    }

    private fun checkBiometricAvailability(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    // Kept for backward compatibility if needed during refactoring phase
    val isPasscodeEnabled = settingsPreferencesManager.isPasscodeEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
