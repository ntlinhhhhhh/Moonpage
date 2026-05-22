package com.diary.moonpage.data.repository

import com.diary.moonpage.data.local.dao.CustomThemeDao
import com.diary.moonpage.data.local.dao.ThemeDao
import com.diary.moonpage.data.local.entity.CustomThemeEntity
import com.diary.moonpage.data.local.entity.ThemeEntity
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.domain.repository.CustomThemeAppearanceConfig
import com.diary.moonpage.domain.repository.CustomThemeRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomThemeRepositoryImpl @Inject constructor(
    private val customThemeDao: CustomThemeDao,
    private val themeDao: ThemeDao,
    private val gson: Gson
) : CustomThemeRepository {

    override val customThemes: Flow<List<CustomThemeEntity>> = customThemeDao.observeCustomThemes()

    override suspend fun saveCustomTheme(
        name: String,
        bgFilePath: String,
        lightConfig: CustomThemeAppearanceConfig,
        darkConfig: CustomThemeAppearanceConfig
    ): Result<CustomThemeEntity> {
        return runCatching {
            val id = "custom_${UUID.randomUUID()}"
            val createdAt = System.currentTimeMillis()
            val customTheme = CustomThemeEntity(
                id = id,
                name = name,
                bgFilePath = bgFilePath,
                primaryColor = lightConfig.primaryColor,
                iconColor = lightConfig.iconColor,
                iconColors = lightConfig.iconColors.joinToString(","),
                lightConfigJson = gson.toJson(lightConfig),
                darkConfigJson = gson.toJson(darkConfig),
                createdAt = createdAt
            )
            customThemeDao.upsert(customTheme)

            themeDao.insertThemes(
                listOf(
                    ThemeEntity(
                        id = id,
                        name = name,
                        collection = "Custom Theme",
                        price = 500,
                        isFree = false,
                        thumbnailUrl = bgFilePath,
                        backgroundUrl = bgFilePath,
                        isOwned = true,
                        isActive = false,
                        description = null,
                        type = ThemeType.THEME.name,
                        icons = "VERY_HAPPY,HAPPY,NEUTRAL,SAD,ANGRY",
                        primaryColor = lightConfig.primaryColor,
                        decoration = "CUSTOM",
                        activatedAt = null
                    )
                )
            )

            customTheme
        }
    }
}
