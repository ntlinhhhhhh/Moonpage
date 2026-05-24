package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.auth.UpdateProfileRequestDto
import com.diary.moonpage.data.remote.dto.auth.UserResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<UserResponseDto>

    @PUT("api/users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequestDto
    ): Response<UserResponseDto>

    @GET("api/users/me/themes")
    suspend fun getOwnedThemeIds(): Response<List<String>>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: String
    ): Response<Unit>

    @Multipart
    @PUT("api/users/me/avatar")
    suspend fun updateAvatar(
        @Part image: MultipartBody.Part
    ): Response<UserResponseDto>

    @DELETE("api/users/me")
    suspend fun deleteMe(): Response<Unit>

    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("name") name: String,
        @Query("limit") limit: Int
    ): Response<List<UserResponseDto>>

    @POST("api/users/me/store/buy-freeze")
    suspend fun buyStreakFreeze(): Response<Unit>

    @POST("api/users/me/streak/recover")
    suspend fun recoverStreak(): Response<Unit>

    @PUT("api/users/me/language")
    suspend fun updateLanguage(
        @Body request: LanguageRequest
    ): Response<Unit>

    @POST("api/users/me/change-password")
    suspend fun changePassword(
        @Body request: com.diary.moonpage.data.remote.dto.auth.ChangePasswordRequestDTO
    ): Response<Unit>

    @POST("api/users/me/confirm-password")
    suspend fun confirmPassword(
        @Body request: com.diary.moonpage.data.remote.dto.auth.ConfirmPasswordRequestDTO
    ): Response<Unit>
}

data class LanguageRequest(
    val language: String
)
