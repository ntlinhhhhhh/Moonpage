package com.diary.moonpage.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.domain.repository.UserRepository
import com.diary.moonpage.core.util.OnboardingPrefsManager
import com.diary.moonpage.core.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun setBirthday(birthday: String) {
        _uiState.value = _uiState.value.copy(birthday = birthday)
    }

    fun setGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Lấy name từ TokenManager (đã lưu tại login) thay vì currentUser
            // để tránh dùng nhầm tên của tài khoản cũ đang cache trong singleton
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
