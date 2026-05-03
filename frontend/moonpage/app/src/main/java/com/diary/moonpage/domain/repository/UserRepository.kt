package com.diary.moonpage.domain.repository

import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.data.remote.dto.auth.UserResponseDto
import com.diary.moonpage.domain.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface UserRepository {
    val currentUser: StateFlow<UserResponseDto?>
    val localAvatarPath: Flow<String?>
    
    suspend fun getCurrentUser(): Result<UserResponseDto>
    suspend fun updateProfile(request: UpdateProfileRequestDto): Result<UserResponseDto>
    suspend fun getMyThemes(): Result<List<Theme>>
    suspend fun deleteUser(id: String): Result<Unit>
    suspend fun updateAvatar(image: okhttp3.MultipartBody.Part, localFile: File): Result<UserResponseDto>
    suspend fun clearCache()
}
