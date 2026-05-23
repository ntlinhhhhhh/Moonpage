package com.diary.moonpage.data.repository

import com.diary.moonpage.data.remote.api.ThemeApi
import com.diary.moonpage.data.local.entity.ThemeEntity
import com.diary.moonpage.data.local.entity.ThemeMoodEntity
import com.diary.moonpage.data.remote.dto.theme.BuyThemeRequest
import com.diary.moonpage.data.remote.dto.theme.CreateThemeMoodRequest
import com.diary.moonpage.data.remote.dto.theme.CreateThemeRequest
import com.diary.moonpage.data.remote.dto.theme.SetActiveThemeRequest
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.ThemeType
import com.diary.moonpage.domain.repository.CreateThemePayload
import com.diary.moonpage.domain.repository.ThemeRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import com.diary.moonpage.core.util.ThemeConstants
import com.diary.moonpage.core.util.PredefinedTheme
import org.json.JSONObject
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val api: ThemeApi,
    private val dao: com.diary.moonpage.data.local.dao.ThemeDao,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager
) : ThemeRepository {
    private val myThemesState = MutableStateFlow<List<Theme>>(emptyList())

    override val ownedThemes: Flow<List<Theme>> = dao.getOwnedThemes().map { entities ->
        entities.map { it.toDomain() }
    }

    override val activeTheme: Flow<Theme?> = dao.observeActiveTheme().map { it?.toDomain() }

    override val allThemes: Flow<List<Theme>> = dao.getAllThemes().map { entities ->
        entities.map { it.toDomain() }
    }

    override val myThemes: Flow<List<Theme>> = myThemesState.asStateFlow()

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

    override suspend fun getMyThemes(): Result<List<Theme>> {
        return try {
            val response = api.getMyThemes()
            if (response.isSuccessful && response.body() != null) {
                val cachedThemes = dao.getAllThemes().first()
                val myThemes = response.body()!!
                    .map { dto ->
                        val networkTheme = dto.toDomain().copy(
                            collection = "Custom Theme",
                            price = CUSTOM_THEME_PRICE,
                            isFree = false,
                            isOwned = true,
                            isActive = dto.isActive,
                            decoration = "CUSTOM",
                            primaryColor = dto.thumbnailUrl.takeIfThemeColor()
                        )
                        val cachedTheme = cachedThemes.findCachedCustomTheme(networkTheme)
                        val cachedPrimary = cachedTheme?.primaryColor.takeIfThemeColor()
                        val networkPrimary = networkTheme.primaryColor.takeIfThemeColor()
                        val moodPrimary = if (cachedPrimary == null && networkPrimary == null) {
                            loadThemeMoodPrimaryColor(networkTheme.id)
                        } else {
                            null
                        }
                        networkTheme.copy(
                            thumbnailUrl = networkTheme.thumbnailUrl ?: cachedTheme?.thumbnailUrl,
                            backgroundUrl = cachedTheme?.backgroundUrl?.takeIfThemeColor()
                                ?: networkTheme.backgroundUrl,
                            primaryColor = cachedPrimary ?: networkPrimary ?: moodPrimary,
                            description = cachedTheme?.description ?: networkTheme.description,
                            activatedAt = cachedTheme?.activatedAt
                        )
                    }

                if (myThemes.any { it.isActive }) {
                    dao.clearActiveTheme()
                }
                dao.insertThemes(myThemes.map { ThemeEntity.fromDomain(it) })
                myThemesState.value = myThemes
                Result.success(myThemes)
            } else {
                Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun loadThemeMoodPrimaryColor(themeId: String): String? {
        val cachedMoodColor = dao.getMoodsForTheme(themeId)
            .firstNotNullOfOrNull { it.iconUrl.takeIfThemeColor() }
        if (cachedMoodColor != null) return cachedMoodColor

        return try {
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
                if (moods.isNotEmpty()) {
                    dao.insertThemeMoods(moods)
                }
                moods.firstNotNullOfOrNull { it.iconUrl.takeIfThemeColor() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createThemes(themes: List<CreateThemePayload>): Result<Unit> {
        return try {
            val request = themes.map { theme ->
                CreateThemeRequest(
                    id = theme.id,
                    name = theme.name,
                    price = theme.price,
                    thumbnailUrl = theme.thumbnailUrl,
                    backgroundUrl = theme.backgroundUrl,
                    backgroundDarkColor = theme.backgroundDarkColor,
                    backgroundLightColor = theme.backgroundLightColor,
                    isOfficial = theme.isOfficial,
                    isActive = theme.isActive,
                    moods = theme.moods.map { mood ->
                        CreateThemeMoodRequest(
                            baseMoodId = mood.baseMoodId,
                            iconUrl = mood.iconUrl,
                            customName = mood.customName
                        )
                    }
                )
            }

            val response = api.createThemes(request)
            if (response.isSuccessful) {
                themes.forEach { theme ->
                    dao.insertThemes(
                        listOf(
                            ThemeEntity(
                                id = theme.id,
                                name = theme.name,
                                collection = "Custom Theme",
                                price = theme.price,
                                isFree = theme.price == 0,
                                thumbnailUrl = theme.thumbnailUrl,
                                backgroundUrl = theme.backgroundColor ?: theme.backgroundUrl,
                                isOwned = false,
                                isActive = theme.isActive,
                                description = theme.description,
                                type = ThemeType.THEME.name,
                                icons = "VERY_HAPPY,HAPPY,NEUTRAL,SAD,ANGRY",
                                primaryColor = theme.primaryColor
                                    ?: theme.thumbnailUrl.takeIfThemeColor()
                                    ?: theme.backgroundColor
                                    ?: theme.backgroundLightColor
                                    ?: theme.backgroundUrl.takeIfThemeColor(),
                                decoration = "CUSTOM",
                                activatedAt = null
                            )
                        )
                    )
                    dao.insertThemeMoods(
                        theme.moods.map { mood ->
                            ThemeMoodEntity(
                                themeId = theme.id,
                                baseMoodId = mood.baseMoodId.toThemeMoodName(),
                                iconUrl = mood.iconUrl,
                                customName = mood.customName
                            )
                        }
                    )
                }
                getMyThemes()
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameTheme(themeId: String, name: String): Result<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Theme name cannot be empty"))
        }

        return try {
            val cachedEntity = dao.getThemeById(themeId)
            val theme = cachedEntity?.toDomain()
                ?: myThemesState.value.firstOrNull { it.id == themeId }
                ?: return Result.failure(IllegalStateException("Theme not found"))

            val request = CreateThemeRequest(
                id = theme.id,
                name = trimmedName,
                price = theme.price,
                thumbnailUrl = theme.thumbnailUrl,
                backgroundUrl = theme.backgroundUrl,
                backgroundDarkColor = theme.description.backgroundColorForMode("dark")
                    ?: theme.backgroundUrl.takeIfThemeColor()?.toApiColorHex(),
                backgroundLightColor = theme.description.backgroundColorForMode("light")
                    ?: theme.backgroundUrl.takeIfThemeColor()?.toApiColorHex(),
                isOfficial = false,
                isActive = theme.isActive,
                moods = buildThemeMoodRequests(theme)
            )

            val response = api.updateTheme(themeId, request)
            if (response.isSuccessful) {
                cachedEntity?.let { dao.insertThemes(listOf(it.copy(name = trimmedName))) }
                myThemesState.value = myThemesState.value.map { customTheme ->
                    if (customTheme.id == themeId) customTheme.copy(name = trimmedName) else customTheme
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    override suspend fun buyTheme(themeId: String, price: Int?): Result<Unit> {
        return try {
            val cachedTheme = dao.getThemeById(themeId)
            val requestPrice = price ?: cachedTheme?.price ?: 0
            
            val response = api.buyTheme(BuyThemeRequest(themeId, requestPrice))
            if (response.isSuccessful) {
                cachedTheme?.let { dao.insertThemes(listOf(it.copy(price = requestPrice, isFree = requestPrice == 0, isOwned = true))) }
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setActiveTheme(themeId: String): Result<Unit> {
        // Optimistic UI Update: change local state immediately
        try {
            ensureThemeCached(themeId)
            val timestamp = System.currentTimeMillis()
            val cachedTheme = dao.getThemeById(themeId)
            dao.clearActiveTheme()
            if (cachedTheme != null) {
                dao.insertThemes(
                    listOf(
                        cachedTheme.copy(
                            isOwned = true,
                            isActive = true,
                            activatedAt = timestamp
                        )
                    )
                )
            } else {
                dao.setActiveTheme(themeId, timestamp)
            }

            // Also sync the themeType DataStore for real-time presets reactivity
            val themeType = themeId.toMoonThemeTypeOrNull()
                ?: dao.getThemeById(themeId)?.decoration?.toMoonThemeTypeOrNull()
            if (themeType != null) {
                themePreferencesManager.setThemeType(themeType)
            }
            myThemesState.value = myThemesState.value.map { theme ->
                theme.copy(isActive = theme.id == themeId)
            }
        } catch (e: Exception) {
            android.util.Log.e("ThemeRepository", "Optimistic update failed", e)
        }

        return try {
            val response = api.setActiveTheme(SetActiveThemeRequest(themeId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            // Keep the optimistic change for offline support
            Result.success(Unit)
        }
    }

    private suspend fun ensureThemeCached(themeId: String) {
        if (dao.getThemeById(themeId) != null) return

        val myTheme = myThemesState.value.find { it.id == themeId }
        if (myTheme != null) {
            dao.insertThemes(listOf(ThemeEntity.fromDomain(myTheme).copy(isOwned = true)))
            return
        }

        fetchAndCacheThemeDetails(themeId)
    }

    private suspend fun buildThemeMoodRequests(theme: Theme): List<CreateThemeMoodRequest> {
        val cachedMoods = dao.getMoodsForTheme(theme.id)
        if (cachedMoods.isNotEmpty()) {
            return cachedMoods.mapIndexed { index, mood ->
                CreateThemeMoodRequest(
                    baseMoodId = mood.baseMoodId.toThemeMoodIdOrNull() ?: DEFAULT_MOOD_IDS[index.coerceIn(DEFAULT_MOOD_IDS.indices)],
                    iconUrl = mood.iconUrl,
                    customName = mood.customName
                )
            }
        }

        val remoteMoods = try {
            val response = api.getThemeMoods(theme.id)
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        if (remoteMoods.isNotEmpty()) {
            return remoteMoods.mapIndexed { index, mood ->
                CreateThemeMoodRequest(
                    baseMoodId = mood.baseMoodId.toThemeMoodIdOrNull() ?: DEFAULT_MOOD_IDS[index.coerceIn(DEFAULT_MOOD_IDS.indices)],
                    iconUrl = mood.iconUrl,
                    customName = mood.customName
                )
            }
        }

        val fallbackColor = theme.primaryColor
            ?: theme.thumbnailUrl.takeIfThemeColor()
            ?: theme.backgroundUrl.takeIfThemeColor()
            ?: "#FF8D6E63"
        val names = listOf("Very Happy", "Happy", "Neutral", "Sad", "Very Sad")
        return DEFAULT_MOOD_IDS.mapIndexed { index, moodId ->
            CreateThemeMoodRequest(
                baseMoodId = moodId,
                iconUrl = fallbackColor,
                customName = names[index]
            )
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

    override suspend fun clearCache() {
        dao.deleteAllThemes()
        dao.deleteAllThemeMoods()
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

private fun Int.toThemeMoodName(): String = when (this) {
    1 -> "Awful"
    2 -> "Bad"
    3 -> "Meh"
    4 -> "Good"
    5 -> "Rad"
    else -> "Meh"
}

private fun List<ThemeEntity>.findCachedCustomTheme(theme: Theme): ThemeEntity? {
    return firstOrNull { it.id == theme.id && it.isCustomThemeEntity() }
        ?: firstOrNull { cached ->
            cached.isCustomThemeEntity() && cached.matchesCustomTheme(theme)
        }
}

private fun ThemeEntity.matchesCustomTheme(theme: Theme): Boolean {
    val themePaths = listOfNotNull(theme.thumbnailUrl, theme.backgroundUrl).filter { it.isNotBlank() }
    val cachedPaths = listOfNotNull(thumbnailUrl, backgroundUrl).filter { it.isNotBlank() }
    val hasSharedPath = themePaths.any { path ->
        cachedPaths.any { cachedPath ->
            path == cachedPath || path.contains(cachedPath) || cachedPath.contains(path)
        }
    }
    return hasSharedPath || name == theme.name
}

private fun ThemeEntity.isCustomThemeEntity(): Boolean {
    return id.startsWith("custom_") ||
        decoration.equals("CUSTOM", ignoreCase = true) ||
        collection.equals("Custom Theme", ignoreCase = true)
}

private fun String?.takeIfThemeColor(): String? {
    return takeIf { it.isThemeColor() }
}

private fun String?.isThemeColor(): Boolean {
    if (isNullOrBlank()) return false
    val value = when {
        startsWith("#") -> drop(1)
        startsWith("0x", ignoreCase = true) -> drop(2)
        else -> this
    }
    return (value.length == 6 || value.length == 8) && value.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
}

private fun String.toMoonThemeTypeOrNull(): MoonThemeType? {
    if (this == ThemeConstants.DEFAULT_THEME_ID) return MoonThemeType.DEFAULT
    val normalized = if (startsWith("theme_")) substringAfter("theme_") else this
    val enumName = when (normalized.uppercase()) {
        "MOON" -> "DEFAULT"
        "BROWN" -> "GRAY_BROWN"
        "COOKIE" -> "COOKIE_BATCH"
        "HEART" -> "HEART_FELT"
        "WEATHER" -> "WEATHER_CYCLE"
        "CUSTOM" -> return null
        else -> normalized.uppercase()
    }
    return runCatching { MoonThemeType.valueOf(enumName) }.getOrNull()
}

private fun String.toMoonThemeType(): MoonThemeType {
    if (this == ThemeConstants.DEFAULT_THEME_ID) return MoonThemeType.DEFAULT
    val normalized = if (startsWith("theme_")) substringAfter("theme_") else this
    val enumName = when (normalized.uppercase()) {
        "MOON" -> "DEFAULT"
        "BROWN" -> "GRAY_BROWN"
        "COOKIE" -> "COOKIE_BATCH"
        "HEART" -> "HEART_FELT"
        "WEATHER" -> "WEATHER_CYCLE"
        else -> normalized.replace("-", "_").uppercase()
    }
    return runCatching { MoonThemeType.valueOf(enumName) }.getOrNull() ?: MoonThemeType.DEFAULT
}

private fun String?.backgroundColorForMode(mode: String): String? {
    if (isNullOrBlank()) return null
    val appearance = runCatching { JSONObject(this).optJSONObject(mode) }.getOrNull() ?: return null
    val fillMode = appearance.optString("backgroundFillMode", "Solid")
    val key = if (fillMode.equals("Gradient", ignoreCase = true)) {
        "gradientStartColor"
    } else {
        "solidBackgroundColor"
    }
    return appearance.optString(key).toApiColorHex()
}

private fun String?.toApiColorHex(): String? {
    if (!isThemeColor()) return null
    val raw = this ?: return null
    val value = when {
        raw.startsWith("#") -> raw.drop(1)
        raw.startsWith("0x", ignoreCase = true) -> raw.drop(2)
        else -> raw
    }
    return "0x$value"
}

private fun String.toThemeMoodIdOrNull(): Int? {
    toIntOrNull()?.let { return it }
    return when (trim().replace("_", " ").uppercase()) {
        "RAD", "VERY HAPPY", "VERYHAPPY" -> 5
        "GOOD", "HAPPY" -> 4
        "MEH", "NEUTRAL" -> 3
        "BAD", "SAD" -> 2
        "AWFUL", "VERY SAD", "VERYSAD", "ANGRY" -> 1
        else -> null
    }
}

private val DEFAULT_MOOD_IDS = listOf(5, 4, 3, 2, 1)

private const val CUSTOM_THEME_PRICE = 500
