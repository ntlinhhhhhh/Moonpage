package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.theme.BuyThemeRequest
import com.diary.moonpage.data.remote.dto.theme.CreateThemeRequest
import com.diary.moonpage.data.remote.dto.theme.CreateThemeResponse
import com.diary.moonpage.data.remote.dto.theme.SetActiveThemeRequest
import com.diary.moonpage.data.remote.dto.theme.ThemeMoodResponseDTO
import com.diary.moonpage.data.remote.dto.theme.ThemeResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface ThemeApi {
    @GET("api/themes")
    suspend fun getAllThemes(): Response<List<ThemeResponseDTO>>

    @GET("api/themes/me")
    suspend fun getMyThemes(): Response<List<ThemeResponseDTO>>

    @GET("api/themes/{id}")
    suspend fun getThemeDetail(
        @Path("id") id: String
    ): Response<ThemeResponseDTO>

    @GET("api/themes/{id}/moods")
    suspend fun getThemeMoods(
        @Path("id") id: String
    ): Response<List<ThemeMoodResponseDTO>>

    @GET("api/users/me/themes")
    suspend fun getOwnedThemes(): Response<List<String>>

    @POST("api/users/me/store/buy-theme")
    suspend fun buyTheme(
        @Body request: BuyThemeRequest
    ): Response<Unit>

    @POST("api/themes")
    suspend fun createThemes(
        @Body request: List<CreateThemeRequest>
    ): Response<CreateThemeResponse>

    @PUT("api/themes/{id}")
    suspend fun updateTheme(
        @Path("id") id: String,
        @Body request: CreateThemeRequest
    ): Response<Unit>

    @PUT("api/users/me/themes/active")
    suspend fun setActiveTheme(
        @Body request: SetActiveThemeRequest
    ): Response<Unit>
}
