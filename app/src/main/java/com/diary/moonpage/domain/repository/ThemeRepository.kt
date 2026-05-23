package com.diary.moonpage.domain.repository

import com.diary.moonpage.domain.model.Theme
import kotlinx.coroutines.flow.Flow

data class CreateThemeMoodPayload(
    val baseMoodId: Int,
    val iconUrl: String,
    val customName: String
)

data class CreateThemePayload(
    val id: String,
    val name: String,
    val price: Int,
    val thumbnailUrl: String?,
    val backgroundUrl: String?,
    val isOfficial: Boolean = false,
    val isActive: Boolean = true,
    val moods: List<CreateThemeMoodPayload>
)

interface ThemeRepository {
    suspend fun getAllThemes(): Result<List<Theme>>
    suspend fun getOwnedThemes(): Result<List<Theme>>
    suspend fun getMyThemes(): Result<List<Theme>>
    suspend fun createThemes(themes: List<CreateThemePayload>): Result<Unit>
    suspend fun buyTheme(themeId: String, price: Int? = null): Result<Unit>
    suspend fun setActiveTheme(themeId: String): Result<Unit>
    
    suspend fun getMoodsForTheme(themeId: String): List<com.diary.moonpage.data.local.entity.ThemeMoodEntity>
    suspend fun getActiveThemeId(): String?
    
    val activeTheme: Flow<Theme?>
    val ownedThemes: Flow<List<Theme>>
    val allThemes: Flow<List<Theme>>
    val myThemes: Flow<List<Theme>>

    suspend fun clearCache()
}
