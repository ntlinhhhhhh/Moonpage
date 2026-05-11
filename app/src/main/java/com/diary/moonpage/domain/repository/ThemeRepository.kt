package com.diary.moonpage.domain.repository

import com.diary.moonpage.domain.model.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    suspend fun getAllThemes(): Result<List<Theme>>
    suspend fun getOwnedThemes(): Result<List<Theme>>
    suspend fun buyTheme(themeId: String): Result<Unit>
    suspend fun setActiveTheme(themeId: String): Result<Unit>
    
    suspend fun getMoodsForTheme(themeId: String): List<com.diary.moonpage.data.local.entity.ThemeMoodEntity>
    suspend fun getActiveThemeId(): String?
    
    val ownedThemes: Flow<List<Theme>>
    val allThemes: Flow<List<Theme>>

    suspend fun clearCache()
}
