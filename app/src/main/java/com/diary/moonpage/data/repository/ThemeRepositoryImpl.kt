package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.ThemeApi
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.repository.ThemeRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val api: ThemeApi,
    private val dao: com.diary.moonpage.data.local.dao.ThemeDao
) : ThemeRepository {

    override suspend fun getAllThemes(token: String): Result<List<Theme>> {
        return try {
            val response = api.getAllThemes("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val themes = response.body()!!.map { it.toDomain() }
                dao.insertThemes(themes.map { com.diary.moonpage.data.local.entity.ThemeEntity.fromDomain(it) })
                Result.success(themes)
            } else {
                // Return cached if available
                val cached = mutableListOf<Theme>()
                dao.getAllThemes().first().forEach { cached.add(it.toDomain()) }
                if (cached.isNotEmpty()) Result.success(cached)
                else {
                    val errorMsg = parseErrorResponse(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            val cached = mutableListOf<Theme>()
            dao.getAllThemes().first().forEach { cached.add(it.toDomain()) }
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getOwnedThemes(token: String): Result<List<Theme>> {
        return try {
            val response = api.getOwnedThemes("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val themes = response.body()!!.map { it.toDomain() }
                dao.insertThemes(themes.map { com.diary.moonpage.data.local.entity.ThemeEntity.fromDomain(it) })
                Result.success(themes)
            } else {
                val cached = mutableListOf<Theme>()
                dao.getOwnedThemes().first().forEach { cached.add(it.toDomain()) }
                if (cached.isNotEmpty()) Result.success(cached)
                else {
                    val errorMsg = parseErrorResponse(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            val cached = mutableListOf<Theme>()
            dao.getOwnedThemes().first().forEach { cached.add(it.toDomain()) }
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun buyTheme(token: String, themeId: String): Result<Unit> {
        return try {
            val response = api.buyTheme("Bearer $token", themeId)
            if (response.isSuccessful) {
                // Update local status if possible
                val theme = dao.getThemeById(themeId)
                theme?.let {
                    dao.insertThemes(listOf(it.copy(isOwned = true)))
                }
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setActiveTheme(token: String, themeId: String): Result<Unit> {
        return try {
            val response = api.setActiveTheme("Bearer $token", themeId)
            if (response.isSuccessful) {
                dao.clearActiveTheme()
                dao.setActiveTheme(themeId)
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorResponse(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "An unknown error occurred"
        return try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = Gson().fromJson(errorBody, type)
            val message = errorMap["message"]
                ?: errorMap["error"]
                ?: errorMap["errors"]
            message?.toString() ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}
