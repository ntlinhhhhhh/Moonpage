package com.diary.moonpage.data.repository

import com.diary.moonpage.core.util.ImageUtils
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.core.util.UserManager
import com.diary.moonpage.data.remote.api.UserApi
import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.data.remote.dto.auth.UserResponseDto
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    override val localAvatarPath: Flow<String?> = userManager.getLocalAvatarPath()

    init {
        // Load user from DataStore (local storage) on initialization to show UI immediately
        CoroutineScope(Dispatchers.IO).launch {
            userManager.getUser().collect { savedUserDto ->
                _currentUser.value = savedUserDto?.toDomain()
            }
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        val token = tokenManager.getToken().first() ?: ""
        if (token.isBlank()) {
            return Result.failure(Exception("Not authenticated"))
        }

        val cached = _currentUser.value
        
        return try {
            val response = userApi.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                val user = userDto.toDomain(token)
                _currentUser.value = user
                userManager.saveUser(userDto) // Update persistence with DTO
                Result.success(user)
            } else {
                cached?.let { Result.success(it) } ?: Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            cached?.let { Result.success(it) } ?: Result.failure(e)
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): Result<User> {
        val token = tokenManager.getToken().first() ?: ""
        return try {
            val response = userApi.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val latestProfileResponse = userApi.getCurrentUser()
                val updatedUserDto = if (latestProfileResponse.isSuccessful) latestProfileResponse.body()!! else response.body()!!
                val user = updatedUserDto.toDomain(token)
                
                _currentUser.value = user 
                userManager.saveUser(updatedUserDto)
                Result.success(user)
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

    override suspend fun updateAvatar(image: okhttp3.MultipartBody.Part, localFile: File): Result<User> {
        val token = tokenManager.getToken().first() ?: ""
        return try {
            val response = userApi.updateAvatar(image)
            if (response.isSuccessful && response.body() != null) {
                val savedPath = ImageUtils.saveAvatarLocally(context, localFile)
                userManager.saveLocalAvatarPath(savedPath)

                val latestProfileResponse = userApi.getCurrentUser()
                val updatedUserDto = if (latestProfileResponse.isSuccessful) latestProfileResponse.body()!! else response.body()!!
                val user = updatedUserDto.toDomain(token)
                
                _currentUser.value = user
                userManager.saveUser(updatedUserDto)
                Result.success(user)
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

    override suspend fun updateLanguage(language: String): Result<Unit> {
        return try {
            val response = userApi.updateLanguage(com.diary.moonpage.data.remote.api.LanguageRequest(language))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync language: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMyAccount(): Result<Unit> {
        return try {
            val response = userApi.deleteMe()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(old: String, new: String): Result<Unit> {
        return try {
            val request = com.diary.moonpage.data.remote.dto.auth.ChangePasswordRequestDTO(old, new)
            val response = userApi.changePassword(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to change password: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmPassword(password: String?, googleIdToken: String?): Result<Unit> {
        return try {
            val request = com.diary.moonpage.data.remote.dto.auth.ConfirmPasswordRequestDTO(
                password = password,
                googleIdToken = googleIdToken
            )
            val response = userApi.confirmPassword(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Confirmation failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buyStreakFreeze(): Result<Unit> {
        return try {
            val response = userApi.buyStreakFreeze()
            if (response.isSuccessful) {
                // Refresh user snapshot immediately so collectors on Store/Profile/Calendar update.
                getCurrentUser()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Purchase failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recoverStreak(): Result<Unit> {
        return try {
            val response = userApi.recoverStreak()
            if (response.isSuccessful) {
                // Refresh user snapshot immediately so collectors on Store/Profile/Calendar update.
                getCurrentUser()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Recovery failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun spendCoinsLocally(amount: Int): Result<User> {
        val current = _currentUser.value ?: return Result.failure(Exception("No user loaded"))
        if (current.coinBalance < amount) return Result.failure(Exception("Not enough coins"))

        val updated = current.copy(coinBalance = current.coinBalance - amount)
        _currentUser.value = updated
        userManager.saveUser(
            UserResponseDto(
                id = updated.userId,
                name = updated.name,
                email = updated.email,
                avatarUrl = updated.avatarUrl,
                gender = updated.gender,
                birthday = updated.birthday,
                coinBalance = updated.coinBalance,
                authProvider = updated.authProvider,
                streakFreezeCount = updated.streakFreezeCount,
                currentStreak = updated.currentStreak,
                longestStreak = updated.longestStreak
            )
        )
        return Result.success(updated)
    }
}
