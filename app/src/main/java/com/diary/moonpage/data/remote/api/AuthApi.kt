package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.auth.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDTO
    ): Response<LoginResponseDTO>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDTO
    ): Response<RegisterResponseDto>

    @POST("api/auth/google-login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequestDTO
    ): Response<LoginResponseDTO>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDTO
    ): Response<Unit>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequestDTO
    ): Response<VerifyOtpResponseDTO>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDTO
    ): Response<Unit>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>
}
