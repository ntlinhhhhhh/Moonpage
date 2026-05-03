package com.diary.moonpage.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.data.remote.dto.auth.UserResponseDto
import com.diary.moonpage.domain.model.Theme
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

data class ProfileUiState(
    val user: UserResponseDto? = null,
    val localAvatarPath: String? = null,
    val tempAvatarPath: String? = null,
    val myThemes: List<Theme> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null
)

sealed class ProfileUiEvent {
    data class ShowSnackBar(val message: String) : ProfileUiEvent()
    object UpdateSuccess : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

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
    }

    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                userRepository.clearCache()
            }
            
            // Only show loading if we don't have cached user
            _uiState.update { it.copy(isLoading = it.user == null) }
            
            userRepository.getCurrentUser()
                .onSuccess { userDto ->
                    _uiState.update { it.copy(user = userDto, isLoading = false) }
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
                .onSuccess { updatedUser ->
                    _uiEvent.emit(ProfileUiEvent.UpdateSuccess)
                    _uiEvent.emit(ProfileUiEvent.ShowSnackBar("Profile updated successfully"))
                    _uiState.update { it.copy(isUpdating = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUpdating = false) }
                    _uiEvent.emit(ProfileUiEvent.ShowSnackBar(e.message ?: "Update failed"))
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
                            _uiEvent.emit(ProfileUiEvent.UpdateSuccess)
                            _uiEvent.emit(ProfileUiEvent.ShowSnackBar("Avatar updated successfully"))
                            _uiState.update { it.copy(isUpdating = false) }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isUpdating = false) }
                            _uiEvent.emit(ProfileUiEvent.ShowSnackBar(e.message ?: "Avatar update failed"))
                        }
                } else {
                    _uiState.update { it.copy(isUpdating = false) }
                    _uiEvent.emit(ProfileUiEvent.ShowSnackBar("Failed to process image"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false) }
                _uiEvent.emit(ProfileUiEvent.ShowSnackBar(e.message ?: "An error occurred"))
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
                    _uiEvent.emit(ProfileUiEvent.ShowSnackBar(e.message ?: "Delete failed"))
                }
        }
    }
}
