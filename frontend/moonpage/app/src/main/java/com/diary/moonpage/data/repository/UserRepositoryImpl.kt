package com.diary.moonpage.data.repository

import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.core.util.UserManager
import com.diary.moonpage.data.remote.api.UserApi
import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.data.remote.dto.auth.UserResponseDto
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : UserRepository {

    private val _currentUser = MutableStateFlow<UserResponseDto?>(null)
    override val currentUser: StateFlow<UserResponseDto?> = _currentUser.asStateFlow()
    
    override val localAvatarPath: Flow<String?> = userManager.getLocalAvatarPath()

    init {
        // Load user from DataStore (local storage) on initialization to show UI immediately
        CoroutineScope(Dispatchers.IO).launch {
            userManager.getUser().collect { savedUser ->
                _currentUser.value = savedUser
            }
        }
    }

    override suspend fun getCurrentUser(): Result<UserResponseDto> {
        val token = tokenManager.getToken().first()
        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not authenticated"))
        }

        val cached = _currentUser.value
        
        return try {
            val response = userApi.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                _currentUser.value = user
                userManager.saveUser(user) // Update persistence
                Result.success(user)
            } else {
                cached?.let { Result.success(it) } ?: Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            cached?.let { Result.success(it) } ?: Result.failure(e)
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): Result<UserResponseDto> {
        return try {
            val response = userApi.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                // Fetch the absolute latest profile from server after update to ensure sync
                val latestProfileResponse = userApi.getCurrentUser()
                val updatedUser = if (latestProfileResponse.isSuccessful) latestProfileResponse.body()!! else response.body()!!
                
                _currentUser.value = updatedUser 
                userManager.saveUser(updatedUser)
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyThemes(): Result<List<Theme>> {
        val token = tokenManager.getToken().first()
        if (token.isNullOrBlank()) return Result.success(emptyList())

        return try {
            val response = userApi.getMyThemes()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch themes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            val response = userApi.deleteUser(id)
            if (response.isSuccessful) {
                clearCache()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Deletion failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(image: okhttp3.MultipartBody.Part, localFile: File): Result<UserResponseDto> {
        return try {
            val response = userApi.updateAvatar(image)
            if (response.isSuccessful && response.body() != null) {
                // Save locally first for fast access
                val savedPath = ImageUtils.saveAvatarLocally(context, localFile)
                userManager.saveLocalAvatarPath(savedPath)

                // Fetch the absolute latest profile from server after update to ensure sync
                val latestProfileResponse = userApi.getCurrentUser()
                val updatedUser = if (latestProfileResponse.isSuccessful) latestProfileResponse.body()!! else response.body()!!
                
                _currentUser.value = updatedUser
                userManager.saveUser(updatedUser)
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Avatar update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache() {
        _currentUser.value = null
        userManager.clearUser()
    }
}
