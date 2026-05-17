package com.diary.moonpage.domain.repository

import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface UserRepository {
    val currentUser: StateFlow<User?>
    val localAvatarPath: Flow<String?>
    
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateProfile(request: UpdateProfileRequestDto): Result<User>
    suspend fun getMyThemes(): Result<List<Theme>>
    suspend fun deleteUser(id: String): Result<Unit>
    suspend fun updateAvatar(image: okhttp3.MultipartBody.Part, localFile: File): Result<User>
    suspend fun clearCache()
    suspend fun updateLanguage(language: String): Result<Unit>
    suspend fun deleteMyAccount(): Result<Unit>
}
