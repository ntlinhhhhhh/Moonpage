package com.diary.moonpage.presentation.screens.security

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

    val isPasscodeEnabled: StateFlow<Boolean> = settingsPreferencesManager.isPasscodeEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val savedPasscode: StateFlow<String?> = settingsPreferencesManager.passcode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isBiometricEnabled: StateFlow<Boolean> = settingsPreferencesManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun verifyPasscode(input: String): Boolean {
        val saved = savedPasscode.value
        android.util.Log.d("SecurityVM", "Verifying passcode. Input: $input, Saved: $saved")
        return saved != null && input == saved
    }

    suspend fun setPasscode(passcode: String?) {
        settingsPreferencesManager.setPasscode(passcode)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesManager.setBiometricEnabled(enabled)
        }
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
}
