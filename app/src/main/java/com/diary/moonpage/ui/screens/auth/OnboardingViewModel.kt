package com.diary.moonpage.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.domain.repository.UserRepository
import com.diary.moonpage.core.util.OnboardingPrefsManager
import com.diary.moonpage.core.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val birthday: String = "",
    val gender: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val onboardingPrefsManager: OnboardingPrefsManager,
    private val tokenManager: TokenManager,
    private val settingsPreferencesManager: com.diary.moonpage.core.util.SettingsPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun setReminderTime(time: String) {
        viewModelScope.launch {
            settingsPreferencesManager.setReminderTime(time)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesManager.setReminderEnabled(enabled)
        }
    }

    fun saveSpecialBlocks(music: Boolean, sleep: Boolean, steps: Boolean, menstruation: Boolean) {
        viewModelScope.launch {
            settingsPreferencesManager.setSpecialBlocksEnabled(music, sleep, steps, menstruation)
        }
    }

    suspend fun isReminderSet(): Boolean {
        return settingsPreferencesManager.isReminderEnabled.first()
    }

    fun setBirthday(birthday: String) {
        _uiState.value = _uiState.value.copy(birthday = birthday)
    }

    fun setGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val currentName = tokenManager.getUserName()
                ?: userRepository.getCurrentUser().getOrNull()?.name
                ?: ""

            val state = _uiState.value
            userRepository.updateProfile(
                UpdateProfileRequestDto(
                    name = currentName,
                    gender = state.gender.takeIf { it.isNotEmpty() },
                    birthday = state.birthday.takeIf { it.isNotEmpty() }
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                // Navigate anyway so user isn't stuck
                onSuccess()
            }
        }
    }
}
