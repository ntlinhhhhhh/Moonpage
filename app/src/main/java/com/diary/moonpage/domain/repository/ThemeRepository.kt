package com.diary.moonpage.domain.repository

import com.diary.moonpage.domain.model.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    suspend fun getAllThemes(token: String): Result<List<Theme>>
    suspend fun getOwnedThemes(token: String): Result<List<Theme>>
    suspend fun buyTheme(token: String, themeId: String): Result<Unit>
    suspend fun setActiveTheme(token: String, themeId: String): Result<Unit>
}
