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
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val api: ThemeApi,
    private val dao: com.diary.moonpage.data.local.dao.ThemeDao
) : ThemeRepository {

    override suspend fun getAllThemes(token: String): Result<List<Theme>> {
        val cached = dao.getAllThemes().first().map { it.toDomain() }
        
        return try {
            val response = api.getAllThemes()
            if (response.isSuccessful && response.body() != null) {
                val networkThemes = response.body()!!.map { it.toDomain() }
                
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

    override suspend fun getOwnedThemes(token: String): Result<List<Theme>> {
        val cachedOwned = dao.getOwnedThemes().first().map { it.toDomain() }

        return try {
            val response = api.getOwnedThemes("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val ownedIds = response.body()!!
                
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

    override suspend fun buyTheme(token: String, themeId: String): Result<Unit> {
        return try {
            // Find price from cache
            val cachedTheme = dao.getThemeById(themeId)
            val price = cachedTheme?.price ?: 0
            
            val response = api.buyTheme("Bearer $token", BuyThemeRequest(themeId, price))
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

    override suspend fun setActiveTheme(token: String, themeId: String): Result<Unit> {
        return try {
            val response = api.setActiveTheme("Bearer $token", SetActiveThemeRequest(themeId))
            if (response.isSuccessful) {
                dao.clearActiveTheme()
                dao.setActiveTheme(themeId)
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
