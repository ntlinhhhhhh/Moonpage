package com.diary.moonpage.domain.repository

import com.diary.moonpage.data.local.entity.CustomThemeEntity
import kotlinx.coroutines.flow.Flow

interface CustomThemeRepository {
    val customThemes: Flow<List<CustomThemeEntity>>

    suspend fun saveCustomTheme(
        name: String,
        bgFilePath: String,
        primaryColor: String,
        iconColor: String,
        iconColors: List<String>
    ): Result<CustomThemeEntity>
}
