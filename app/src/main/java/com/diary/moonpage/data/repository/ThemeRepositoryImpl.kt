package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.ThemeApi
import com.diary.moonpage.data.local.entity.ThemeEntity
import com.diary.moonpage.data.local.entity.ThemeMoodEntity
import com.diary.moonpage.data.remote.dto.theme.BuyThemeRequest
import com.diary.moonpage.data.remote.dto.theme.SetActiveThemeRequest
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.domain.repository.ThemeRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.diary.moonpage.core.util.ThemeConstants
import com.diary.moonpage.core.util.PredefinedTheme
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val api: ThemeApi,
    private val dao: com.diary.moonpage.data.local.dao.ThemeDao
) : ThemeRepository {
    override val ownedThemes: Flow<List<Theme>> = dao.getOwnedThemes().map { entities ->
        entities.map { it.toDomain() }
    }

    override val allThemes: Flow<List<Theme>> = dao.getAllThemes().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getAllThemes(): Result<List<Theme>> {
        val cached = dao.getAllThemes().first().map { it.toDomain() }
        
        return try {
            val response = api.getAllThemes()
            if (response.isSuccessful && response.body() != null) {
                val networkThemes = response.body()!!.map { it.toDomain() }
                    .filter { it.id != ThemeConstants.DEFAULT_THEME_ID } // Hide default from store
                    .map { theme ->
                        // Override with predefined data if available
                        val predefined = ThemeConstants.THEMES.find { it.id == theme.id }
                        if (predefined != null) {
                            theme.copy(
                                name = predefined.name,
                                price = predefined.price,
                                thumbnailUrl = predefined.thumbnailUrl,
                                backgroundUrl = predefined.backgroundUrl
                            )
                        } else theme
                    }
                
                val currentCached = dao.getAllThemes().first()
                val updatedEntities = networkThemes.map { theme ->
                    val existing = currentCached.find { it.id == theme.id }
                    ThemeEntity.fromDomain(theme).copy(
                        isOwned = existing?.isOwned ?: false,
                        isActive = existing?.isActive ?: false
                    )
                }
                dao.insertThemes(updatedEntities)
                Result.success(networkThemes)
            } else {
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getOwnedThemes(): Result<List<Theme>> {
        val cachedOwned = dao.getOwnedThemes().first().map { it.toDomain() }

        return try {
            val response = api.getOwnedThemes()
            if (response.isSuccessful && response.body() != null) {
                val ownedIds = response.body()!!.toMutableList()
                
                // Ensure default theme is always in owned list
                if (!ownedIds.contains(ThemeConstants.DEFAULT_THEME_ID)) {
                    ownedIds.add(0, ThemeConstants.DEFAULT_THEME_ID)
                }

                val allCached = dao.getAllThemes().first()
                val toUpdate = mutableListOf<ThemeEntity>()
                
                ownedIds.forEach { themeId ->
                    val entity = allCached.find { it.id == themeId }
                    if (entity == null) {
                        fetchAndCacheThemeDetails(themeId)
                    } else if (!entity.isOwned) {
                        toUpdate.add(entity.copy(isOwned = true))
                    }
                }
                if (toUpdate.isNotEmpty()) dao.insertThemes(toUpdate)
                
                val finalOwned = dao.getOwnedThemes().first().map { it.toDomain() }
                Result.success(finalOwned)
            } else {
                if (cachedOwned.isNotEmpty()) Result.success(cachedOwned)
                else Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            if (cachedOwned.isNotEmpty()) Result.success(cachedOwned) else Result.failure(e)
        }
    }

    private suspend fun fetchAndCacheThemeDetails(themeId: String) {
        val predefined = ThemeConstants.THEMES.find { it.id == themeId }
        if (predefined != null) {
            insertPredefinedThemeLocally(predefined)
            return
        }

        try {
            val detailResponse = api.getThemeDetail(themeId)
            if (detailResponse.isSuccessful && detailResponse.body() != null) {
                val dto = detailResponse.body()!!
                val existing = dao.getThemeById(themeId)
                val updated = ThemeEntity.fromDomain(dto.toDomain()).copy(
                    isOwned = true,
                    isActive = existing?.isActive ?: false
                )
                dao.insertThemes(listOf(updated))
                
                // Fetch moods
                val moodsResponse = api.getThemeMoods(themeId)
                if (moodsResponse.isSuccessful && moodsResponse.body() != null) {
                    val moods = moodsResponse.body()!!.map { moodDto ->
                        ThemeMoodEntity(
                            themeId = themeId,
                            baseMoodId = moodDto.baseMoodId,
                            iconUrl = moodDto.iconUrl,
                            customName = moodDto.customName
                        )
                    }
                    dao.insertThemeMoods(moods)
                }
            }
        } catch (e: Exception) { }
    }

    private suspend fun insertPredefinedThemeLocally(predefined: PredefinedTheme) {
        val existing = dao.getThemeById(predefined.id)
        val entity = ThemeEntity(
            id = predefined.id,
            name = predefined.name,
            collection = "Collection",
            price = predefined.price,
            isFree = predefined.price == 0,
            thumbnailUrl = predefined.thumbnailUrl,
            backgroundUrl = predefined.backgroundUrl,
            isOwned = true,
            isActive = existing?.isActive ?: (predefined.id == ThemeConstants.DEFAULT_THEME_ID),
            description = null,
            type = ThemeType.THEME.name,
            icons = "VERY_HAPPY,HAPPY,NEUTRAL,SAD,ANGRY",
            primaryColor = predefined.thumbnailUrl,
            decoration = predefined.decoration
        )
        dao.insertThemes(listOf(entity))

        val moods = predefined.moods.map { mood ->
            ThemeMoodEntity(
                themeId = predefined.id,
                baseMoodId = mood.baseMoodId,
                iconUrl = mood.iconUrl,
                customName = mood.customName
            )
        }
        dao.insertThemeMoods(moods)
    }

    override suspend fun buyTheme(themeId: String): Result<Unit> {
        return try {
            // Find price from cache
            val cachedTheme = dao.getThemeById(themeId)
            val price = cachedTheme?.price ?: 0
            
            val response = api.buyTheme(BuyThemeRequest(themeId, price))
            if (response.isSuccessful) {
                cachedTheme?.let { dao.insertThemes(listOf(it.copy(isOwned = true))) }
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setActiveTheme(themeId: String): Result<Unit> {
        return try {
            val response = api.setActiveTheme(SetActiveThemeRequest(themeId))
            if (response.isSuccessful) {
                dao.clearActiveTheme()
                dao.setActiveTheme(themeId, System.currentTimeMillis())
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMoodsForTheme(themeId: String): List<ThemeMoodEntity> {
        val cachedMoods = dao.getMoodsForTheme(themeId)
        if (cachedMoods.isEmpty()) {
            // Check predefined first
            val predefined = ThemeConstants.THEMES.find { it.id == themeId }
            if (predefined != null) {
                val moods = predefined.moods.map { mood ->
                    ThemeMoodEntity(
                        themeId = themeId,
                        baseMoodId = mood.baseMoodId,
                        iconUrl = mood.iconUrl,
                        customName = mood.customName
                    )
                }
                dao.insertThemeMoods(moods)
                return moods
            }

            try {
                val response = api.getThemeMoods(themeId)
                if (response.isSuccessful && response.body() != null) {
                    val moods = response.body()!!.map { dto ->
                        ThemeMoodEntity(
                            themeId = themeId,
                            baseMoodId = dto.baseMoodId,
                            iconUrl = dto.iconUrl,
                            customName = dto.customName
                        )
                    }
                    dao.insertThemeMoods(moods)
                    return moods
                }
            } catch (e: Exception) { }
        }
        return cachedMoods
    }

    override suspend fun getActiveThemeId(): String? {
        return dao.getActiveTheme()?.id
    }

    private fun parseErrorResponse(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "An unknown error occurred"
        return try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = Gson().fromJson(errorBody, type)
            val message = errorMap["message"] ?: errorMap["error"] ?: errorMap["errors"]
            message?.toString() ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}
