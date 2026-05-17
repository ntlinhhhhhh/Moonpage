package com.diary.moonpage.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.diary.moonpage.core.util.ImageUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProfileUiEffect>()
    val uiEffect = _uiEvent.asSharedFlow()

    init {
        // Observe cache changes from repository
        viewModelScope.launch {
            userRepository.currentUser.collectLatest { user ->
                _uiState.update { it.copy(user = user) }
            }
        }

        viewModelScope.launch {
            userRepository.localAvatarPath.collectLatest { path ->
                _uiState.update { it.copy(localAvatarPath = path, tempAvatarPath = null) }
            }
        }

        loadProfile(forceRefresh = false)
        loadMyThemes()
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = it.totalLogs == 0 && it.totalPhotos == 0) }
                val response = statisticsRepository.getGlobalSummary()
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!
                    android.util.Log.d("ProfileViewModel", "Stats received: Logs=${stats.totalLogs}, Photos=${stats.totalPhotos}")
                    _uiState.update { it.copy(
                        totalLogs = stats.totalLogs,
                        totalPhotos = stats.totalPhotos,
                        isLoading = false
                    ) }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    android.util.Log.e("ProfileViewModel", "Stats failed: $errorMsg")
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Stats exception", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.DeleteAccount -> deleteAccount(event.id) {
                viewModelScope.launch { _uiEvent.emit(ProfileUiEffect.AccountDeleted) }
            }
            else -> {}
        }
    }

    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                userRepository.clearCache()
            }

            // Only show loading if we don't have cached user
            _uiState.update { it.copy(isLoading = it.user == null) }

            userRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                    loadStatistics()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun loadMyThemes() {
        viewModelScope.launch {
            userRepository.getMyThemes()
                .onSuccess { themes ->
                    _uiState.update { it.copy(myThemes = themes) }
                }
        }
    }

    fun updateProfile(name: String, gender: String?, birthday: String?) {
        viewModelScope.launch {
            val currentAvatarUrl = _uiState.value.user?.avatarUrl
            _uiState.update { it.copy(isUpdating = true) }
            val request = UpdateProfileRequestDto(
                name = name,
                avatarUrl = currentAvatarUrl,
                gender = gender,
                birthday = birthday
            )
            userRepository.updateProfile(request)
                .onSuccess {
                    _uiEvent.emit(ProfileUiEffect.UpdateSuccess)
                    _uiEvent.emit(ProfileUiEffect.ShowSnackBar("Profile updated successfully"))
                    _uiState.update { it.copy(isUpdating = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUpdating = false) }
                    _uiEvent.emit(ProfileUiEffect.ShowSnackBar(e.message ?: "Update failed"))
                }
        }
    }

    fun updateAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                val compressedFile = ImageUtils.compressAndCropSquare(context, uri)
                if (compressedFile != null) {
                    // Optimistic update: show picked image immediately
                    _uiState.update { it.copy(tempAvatarPath = compressedFile.absolutePath) }

                    val imagePart = MultipartBody.Part.createFormData(
                        "imageFile",
                        compressedFile.name,
                        compressedFile.asRequestBody("image/webp".toMediaTypeOrNull())
                    )
                    userRepository.updateAvatar(imagePart, compressedFile)
                        .onSuccess {
                            _uiEvent.emit(ProfileUiEffect.UpdateSuccess)
                            _uiEvent.emit(ProfileUiEffect.ShowSnackBar("Avatar updated successfully"))
                            _uiState.update { it.copy(isUpdating = false) }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isUpdating = false) }
                            _uiEvent.emit(ProfileUiEffect.ShowSnackBar(e.message ?: "Avatar update failed"))
                        }
                } else {
                    _uiState.update { it.copy(isUpdating = false) }
                    _uiEvent.emit(ProfileUiEffect.ShowSnackBar("Failed to process image"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false) }
                _uiEvent.emit(ProfileUiEffect.ShowSnackBar(e.message ?: "An error occurred"))
            }
        }
    }

    fun deleteAccount(id: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            userRepository.deleteUser(id)
                .onSuccess {
                    onDeleted()
                }
                .onFailure { e ->
                    _uiEvent.emit(ProfileUiEffect.ShowSnackBar(e.message ?: "Delete failed"))
                }
        }
    }
}
