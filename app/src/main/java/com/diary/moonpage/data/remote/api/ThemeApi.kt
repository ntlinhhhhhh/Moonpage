package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.theme.ThemeResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header

interface ThemeApi {
    @GET("api/theme")
    suspend fun getAllThemes(
        @Header("Authorization") token: String
    ): Response<List<ThemeResponseDTO>>

    @GET("api/user/owned-themes")
    suspend fun getOwnedThemes(
        @Header("Authorization") token: String
    ): Response<List<ThemeResponseDTO>>

    @POST("api/user/buy-theme/{themeId}")
    suspend fun buyTheme(
        @Header("Authorization") token: String,
        @Path("themeId") themeId: String
    ): Response<Unit>

    @POST("api/user/update-theme/{themeId}")
    suspend fun setActiveTheme(
        @Header("Authorization") token: String,
        @Path("themeId") themeId: String
    ): Response<Unit>
}
