package com.diary.moonpage.data.remote.api

import com.diary.moonpage.data.remote.dto.theme.BuyThemeRequest
import com.diary.moonpage.data.remote.dto.theme.CreateThemeRequest
import com.diary.moonpage.data.remote.dto.theme.CreateThemeResponse
import com.diary.moonpage.data.remote.dto.theme.SetActiveThemeRequest
import com.diary.moonpage.data.remote.dto.theme.ThemeMoodResponseDTO
import com.diary.moonpage.data.remote.dto.theme.ThemeResponseDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    ): Response<CreateThemeResponse>

    @Multipart
    @POST("api/themes/upload")
    suspend fun uploadTheme(
        @Part("Id") id: RequestBody,
        @Part("Name") name: RequestBody,
        @Part("Price") price: RequestBody,
        @Part thumbnail: MultipartBody.Part? = null,
        @Part background: MultipartBody.Part? = null,
        @Part("PrimaryColor") primaryColor: RequestBody? = null,
        @Part("BackgroundDarkColor") backgroundDarkColor: RequestBody? = null,
        @Part("BackgroundLightColor") backgroundLightColor: RequestBody? = null,
        @Part("Description") description: RequestBody? = null,
        @Part("IsOfficial") isOfficial: RequestBody? = null,
        @Part("IsActive") isActive: RequestBody? = null,
        @Part("Moods") moods: RequestBody? = null
    ): Response<CreateThemeResponse>

    @POST("api/themes/list")
    suspend fun createThemes(
        @Body request: List<CreateThemeRequest>
    ): Response<CreateThemeResponse>

    @Multipart
    @PUT("api/themes/{id}")
    suspend fun updateTheme(
        @Path("id") id: String,
        @Part("Id") formId: RequestBody? = null,
        @Part("Name") name: RequestBody? = null,
        @Part("Price") price: RequestBody? = null,
        @Part thumbnail: MultipartBody.Part? = null,
        @Part background: MultipartBody.Part? = null,
        @Part("BackgroundDarkColor") backgroundDarkColor: RequestBody? = null,
        @Part("BackgroundLightColor") backgroundLightColor: RequestBody? = null,
        @Part("IsOfficial") isOfficial: RequestBody? = null,
        @Part("IsActive") isActive: RequestBody? = null,
        @Part("Moods") moods: RequestBody? = null
    ): Response<CreateThemeResponse>

    @DELETE("api/themes/{id}")
    suspend fun deleteTheme(
        @Path("id") id: String
    ): Response<CreateThemeResponse>

    @PUT("api/users/me/themes/active")
    suspend fun setActiveTheme(
        @Body request: SetActiveThemeRequest
    ): Response<CreateThemeResponse>
}
