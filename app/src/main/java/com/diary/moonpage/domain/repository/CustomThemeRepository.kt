package com.diary.moonpage.domain.repository

import com.diary.moonpage.data.local.entity.CustomThemeEntity
import kotlinx.coroutines.flow.Flow

data class CustomThemeAppearanceConfig(
    val backgroundUri: String?,
    val backgroundScale: Float,
    val backgroundRotation: Float,
    val backgroundOffsetX: Float,
    val backgroundOffsetY: Float,
    val solidBackgroundColor: String,
    val primaryColor: String,
    val iconColor: String,
    val iconColors: List<String>
)

interface CustomThemeRepository {
    val customThemes: Flow<List<CustomThemeEntity>>

    suspend fun saveCustomTheme(
        name: String,
        bgFilePath: String,
        lightConfig: CustomThemeAppearanceConfig,
        darkConfig: CustomThemeAppearanceConfig
    ): Result<CustomThemeEntity>
}
