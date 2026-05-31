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
import com.diary.moonpage.domain.repository.CreateThemeMoodPayload
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
import com.diary.moonpage.core.util.primaryPreviewColor
import com.diary.moonpage.core.util.toAppearanceDescription
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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

    override val activeTheme: Flow<Theme?> = kotlinx.coroutines.flow.combine(
        themePreferencesManager.activeThemeJson,
        dao.observeActiveTheme()
    ) { json, entity ->
        val entityTheme = entity?.toDomain()
        if (json != null) {
            try {
                Gson().fromJson(json, Theme::class.java)
                    .withFreshRemoteBackgroundFrom(entityTheme)
            } catch (e: Exception) {
                entityTheme
            }
        } else {
            entityTheme
        }
    }

    override val allThemes: Flow<List<Theme>> = dao.getAllThemes().map { entities ->
        entities
            .filterNot { ThemeConstants.isDefaultThemeId(it.id) }
            .filterNot { it.isCustomThemeEntity() }
            .map { it.toDomain() }
    }

    override val myThemes: Flow<List<Theme>> = myThemesState.asStateFlow()

    override suspend fun getAllThemes(): Result<List<Theme>> {
        return try {
            seedPredefinedThemes()
            val themes = dao.getAllThemes().first()
                .filterNot { ThemeConstants.isDefaultThemeId(it.id) }
                .filterNot { it.isCustomThemeEntity() }
                .map { it.toDomain() }
            Result.success(themes)
        } catch (e: Exception) {
            val cached = dao.getAllThemes().first()
                .filterNot { ThemeConstants.isDefaultThemeId(it.id) }
                .filterNot { it.isCustomThemeEntity() }
                .map { it.toDomain() }
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getOwnedThemes(): Result<List<Theme>> {
        seedPredefinedThemes()
        val cachedOwned = dao.getOwnedThemes().first().map { it.toDomain() }

        return try {
            val response = api.getOwnedThemes()
            if (response.isSuccessful && response.body() != null) {
                val ownedIds = response.body()!!
                    .map { ThemeConstants.normalizeThemeId(it) }
                    .toMutableList()

                // Ensure default theme is always in owned list
                if (!ownedIds.contains(ThemeConstants.DEFAULT_THEME_ID)) {
                    ownedIds.add(0, ThemeConstants.DEFAULT_THEME_ID)
                }

                seedPredefinedThemes(ownedIds.toSet())
                val allCached = dao.getAllThemes().first()
                val toUpdate = mutableListOf<ThemeEntity>()

                ownedIds.forEach { themeId ->
                    val entity = allCached.find { it.id == themeId }
                    if (entity == null) {
                        if (!themeId.isCustomThemeId()) {
                            fetchAndCacheThemeDetails(themeId)
                        }
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
                val currentActive = dao.getActiveTheme()
                val recentActiveThemeId = currentActive
                    ?.takeIf { entity ->
                        entity.activatedAt?.let { System.currentTimeMillis() - it < ACTIVE_THEME_SYNC_GRACE_MS } == true
                    }
                    ?.id

                val myThemes = coroutineScope {
                    val deferredThemes = response.body()!!.map { dto ->
                        async {
                            val dtoTheme = dto.toDomain()
                            val networkTheme = dtoTheme.copy(
                                collection = "Custom Theme",
                                price = CUSTOM_THEME_PRICE,
                                isFree = false,
                                isOwned = true,
                                isActive = if (recentActiveThemeId != null) dto.id == recentActiveThemeId else dto.isActive,
                                decoration = "CUSTOM",
                                primaryColor = dtoTheme.primaryColor.takeIfThemeColor()
                                    ?: dtoTheme.description.primaryColorForMode("light")
                                    ?: dtoTheme.description.primaryColorForMode("dark")
                            )
                            val cachedTheme = cachedThemes.findCachedCustomTheme(networkTheme)
                            val cachedPrimary = cachedTheme?.primaryColor.takeIfThemeColor()
                                ?: cachedTheme?.description.primaryColorForMode("light")
                                ?: cachedTheme?.description.primaryColorForMode("dark")
                            val networkPrimary = networkTheme.primaryColor.takeIfThemeColor()
                                ?: networkTheme.description.primaryColorForMode("light")
                                ?: networkTheme.description.primaryColorForMode("dark")
                            val backendIconColors = loadBackendThemeMoodIconColors(networkTheme.id)
                            val cachedIconColors = if (backendIconColors.size >= DEFAULT_MOOD_IDS.size) {
                                emptyList()
                            } else {
                                loadCachedThemeMoodIconColors(cachedTheme)
                            }
                            val moodIconColors = backendIconColors
                                .takeIf { it.size >= DEFAULT_MOOD_IDS.size }
                                ?: cachedIconColors.takeIf { it.size >= DEFAULT_MOOD_IDS.size }
                            val moodPrimary = if (cachedPrimary == null && networkPrimary == null) {
                                moodIconColors?.firstOrNull() ?: loadThemeMoodPrimaryColor(networkTheme.id)
                            } else {
                                null
                            }
                            val networkFillModes = networkTheme.description.backgroundFillModes()
                            val hasExplicitImageBackground = networkFillModes.any { it.isImageFillMode() }
                            val hasExplicitColorBackground = networkFillModes.any { it.isSolidOrGradientFillMode() } &&
                                !hasExplicitImageBackground
                            val baseDescription = if (networkFillModes.isNotEmpty()) {
                                networkTheme.description
                            } else {
                                cachedTheme?.description ?: networkTheme.description
                            }
                            val description = moodIconColors
                                ?.let { baseDescription.withMoodIconColors(it) }
                                ?: baseDescription
                            val hasRemoteBackgroundAsset = networkTheme.backgroundUrl?.let {
                                it.isNotBlank() && it.lowercase() != "null" && it.lowercase() != "pending" && !it.isThemeColor() && !it.contains(",")
                            } == true
                            val hasNetworkImage = hasExplicitImageBackground || (!hasExplicitColorBackground && hasRemoteBackgroundAsset)
                            val resolvedBackgroundUrl = if (hasNetworkImage) {
                                cachedTheme?.backgroundUrl.takeIfLocalThemeAsset()
                                    ?: networkTheme.backgroundUrl
                                    ?: cachedTheme?.backgroundUrl
                            } else {
                                networkTheme.backgroundUrl
                            }
                            networkTheme.copy(
                                thumbnailUrl = cachedTheme?.thumbnailUrl.takeIfLocalThemeAsset()
                                    ?: networkTheme.thumbnailUrl
                                    ?: cachedTheme?.thumbnailUrl,
                                backgroundUrl = resolvedBackgroundUrl,
                                primaryColor = cachedPrimary ?: networkPrimary ?: moodPrimary,
                                description = description.withRemoteBackgroundImage(resolvedBackgroundUrl),
                                activatedAt = cachedTheme?.activatedAt
                            )
                        }
                    }
                    deferredThemes.awaitAll()
                }

                if (myThemes.any { it.isActive }) {
                    dao.clearActiveTheme()
                    val activeTheme = myThemes.firstOrNull { it.isActive }
                    themePreferencesManager.setActiveThemeJson(if (activeTheme != null) Gson().toJson(activeTheme) else null)
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

    private suspend fun loadBackendThemeMoodIconColors(themeId: String): List<String> {
        val localThemeId = ThemeConstants.normalizeThemeId(themeId)
        val remoteMoods = try {
            val response = api.getThemeMoods(themeId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { dto ->
                    ThemeMoodEntity(
                        themeId = localThemeId,
                        baseMoodId = dto.baseMoodId.toString(),
                        iconUrl = dto.iconColor,
                        customName = dto.customName.orEmpty()
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }

        if (remoteMoods.isNotEmpty()) {
            if (dao.getThemeById(localThemeId) != null) {
                dao.insertThemeMoods(remoteMoods)
            }
            val remoteColors = remoteMoods.toOrderedIconColors()
            if (remoteColors.size >= DEFAULT_MOOD_IDS.size) return remoteColors
        }

        return dao.getMoodsForTheme(localThemeId).toOrderedIconColors()
    }

    private suspend fun loadCachedThemeMoodIconColors(cachedTheme: ThemeEntity?): List<String> {
        if (cachedTheme == null) return emptyList()
        val descriptionColors = cachedTheme.description.explicitIconColorsFromThemeDescription()
        if (descriptionColors.size >= DEFAULT_MOOD_IDS.size) return descriptionColors.take(DEFAULT_MOOD_IDS.size)

        return dao.getMoodsForTheme(cachedTheme.id).toOrderedIconColors()
    }

    private suspend fun loadThemeMoodPrimaryColor(themeId: String): String? {
        val localThemeId = ThemeConstants.normalizeThemeId(themeId)
        dao.getThemeById(localThemeId)
            ?.customMoodEntitiesFromDescription()
            ?.firstNotNullOfOrNull { it.iconUrl.takeIfThemeColor() }
            ?.let { return it }

        val cachedMoodColor = dao.getMoodsForTheme(localThemeId)
            .firstNotNullOfOrNull { it.iconUrl.takeIfThemeColor() }
        if (cachedMoodColor != null) return cachedMoodColor

        return try {
            val response = api.getThemeMoods(themeId)
            if (response.isSuccessful && response.body() != null) {
                val moods = response.body()!!.map { dto ->
                    ThemeMoodEntity(
                        themeId = localThemeId,
                        baseMoodId = dto.baseMoodId.toString(),
                        iconUrl = dto.iconColor,
                        customName = dto.customName.orEmpty()
                    )
                }
                if (moods.isNotEmpty() && dao.getThemeById(localThemeId) != null) {
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
            val uploadThemes = themes.filter { it.hasLocalThemeFile() }
            val listThemes = themes.filterNot { it.hasLocalThemeFile() }

            if (listThemes.isNotEmpty()) {
                val response = api.createThemes(listThemes.toCreateThemeRequests())
                if (!response.isSuccessful) {
                    return Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
                }
            }

            if (uploadThemes.isNotEmpty()) {
                coroutineScope {
                    val deferredUploads = uploadThemes.map { theme ->
                        async {
                            val response = api.uploadTheme(
                                id = theme.id.toTextRequestBody(),
                                name = theme.name.toTextRequestBody(),
                                price = theme.price.toString().toTextRequestBody(),
                                thumbnail = theme.thumbnailUrl.toLocalFileOrNull()?.toImagePart("Thumbnail"),
                                background = theme.backgroundUrl.toLocalFileOrNull()?.toImagePart("Background"),
                                primaryLightColor = theme.primaryLightColor?.toTextRequestBody(),
                                primaryDarkColor = theme.primaryDarkColor?.toTextRequestBody(),
                                backgroundDarkColor = theme.backgroundDarkColor?.toTextRequestBody(),
                                backgroundLightColor = theme.backgroundLightColor?.toTextRequestBody(),
                                description = theme.description?.toTextRequestBody(),
                                isOfficial = theme.isOfficial.toString().toTextRequestBody(),
                                isActive = theme.isActive.toString().toTextRequestBody(),
                                moods = theme.moods.toUploadMoodsJson().toTextRequestBody()
                            )
                            if (!response.isSuccessful) {
                                throw Exception(parseErrorResponse(response.errorBody()?.string()))
                            }
                        }
                    }
                    deferredUploads.awaitAll()
                }
            }

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
                            backgroundUrl = theme.backgroundUrl,
                            isOwned = false,
                            isActive = theme.isActive,
                            description = theme.description,
                            type = ThemeType.THEME.name,
                            icons = "VERY_HAPPY,HAPPY,NEUTRAL,SAD,ANGRY",
                            primaryColor = theme.primaryLightColor
                                ?: theme.primaryDarkColor
                                ?: theme.description.primaryColorForMode("light")
                                ?: theme.description.primaryColorForMode("dark")
                                ?: theme.thumbnailUrl.takeIfThemeColor()
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
                            baseMoodId = mood.baseMoodId.toString(),
                            iconUrl = mood.iconColor,
                            customName = mood.customName
                        )
                    }
                )
            }
            getMyThemes()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun List<CreateThemePayload>.toCreateThemeRequests(): List<CreateThemeRequest> {
        return map { theme ->
            CreateThemeRequest(
                id = theme.id,
                name = theme.name,
                price = theme.price,
                thumbnailUrl = theme.thumbnailUrl,
                backgroundUrl = theme.backgroundUrl,
                primaryLightColor = theme.primaryLightColor,
                primaryDarkColor = theme.primaryDarkColor,
                backgroundDarkColor = theme.backgroundDarkColor,
                backgroundLightColor = theme.backgroundLightColor,
                description = theme.description,
                isOfficial = theme.isOfficial,
                isActive = theme.isActive,
                moods = theme.moods.map { mood ->
                    CreateThemeMoodRequest(
                        baseMoodId = mood.baseMoodId,
                        iconColor = mood.iconColor,
                        customName = mood.customName
                    )
                }
            )
        }
    }

    private fun CreateThemePayload.hasLocalThemeFile(): Boolean {
        return thumbnailUrl.toLocalFileOrNull() != null || backgroundUrl.toLocalFileOrNull() != null
    }

    private fun String.toTextRequestBody() = toRequestBody("text/plain".toMediaTypeOrNull())

    private fun String?.toLocalFileOrNull(): File? {
        if (isNullOrBlank()) return null
        return File(this).takeIf { it.isFile }
    }

    private fun File.toImagePart(name: String): MultipartBody.Part {
        val mediaType = when (extension.lowercase()) {
            "webp" -> "image/webp"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "image/*"
        }
        return MultipartBody.Part.createFormData(
            name,
            this.name,
            asRequestBody(mediaType.toMediaTypeOrNull())
        )
    }

    private fun List<CreateThemeMoodPayload>.toUploadMoodsJson(): String {
        return JSONArray().apply {
            forEach { mood ->
                put(
                    JSONObject()
                        .put("BaseMoodId", mood.baseMoodId)
                        .put("IconColor", mood.iconColor)
                        .put("CustomName", mood.customName)
                )
            }
        }.toString()
    }

    override suspend fun renameTheme(themeId: String, name: String): Result<Unit> {
        val normalizedThemeId = themeId.trim()
        val trimmedName = name.trim()
        if (normalizedThemeId.isBlank()) {
            return Result.failure(IllegalArgumentException("Theme id cannot be empty"))
        }
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Theme name cannot be empty"))
        }

        return try {
            val cachedEntity = dao.getThemeById(normalizedThemeId)
            if (cachedEntity == null && myThemesState.value.none { it.id == normalizedThemeId }) {
                return Result.failure(IllegalStateException("Theme not found"))
            }

            val response = api.updateTheme(
                id = normalizedThemeId,
                formId = normalizedThemeId.toTextRequestBody(),
                name = trimmedName.toTextRequestBody()
            )
            if (response.isSuccessful) {
                cachedEntity?.let { dao.insertThemes(listOf(it.copy(name = trimmedName))) }
                myThemesState.value = myThemesState.value.map { customTheme ->
                    if (customTheme.id == normalizedThemeId) customTheme.copy(name = trimmedName) else customTheme
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
        val predefined = ThemeConstants.findTheme(themeId)
        if (predefined != null) {
            insertPredefinedThemeLocally(predefined)
            return
        }
        if (themeId.isCustomThemeId()) return

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

                // Fetch moods — prefer moods already included in detail response
                val moodsFromDetail = dto.moods
                if (!moodsFromDetail.isNullOrEmpty()) {
                    val moods = moodsFromDetail.map { moodDto ->
                        ThemeMoodEntity(
                            themeId = themeId,
                            baseMoodId = moodDto.baseMoodId.toString(),
                            iconUrl = moodDto.iconColor,
                            customName = moodDto.customName.orEmpty()
                        )
                    }
                    dao.insertThemeMoods(moods)
                } else {
                    val moodsResponse = api.getThemeMoods(themeId)
                    if (moodsResponse.isSuccessful && moodsResponse.body() != null) {
                        val moods = moodsResponse.body()!!.map { moodDto ->
                            ThemeMoodEntity(
                                themeId = themeId,
                                baseMoodId = moodDto.baseMoodId.toString(),
                                iconUrl = moodDto.iconColor,
                                customName = moodDto.customName.orEmpty()
                            )
                        }
                        dao.insertThemeMoods(moods)
                    }
                }
            }
        } catch (e: Exception) { }
    }

    private suspend fun insertPredefinedThemeLocally(predefined: PredefinedTheme) {
        val existing = dao.getThemeById(predefined.id)
        val entity = predefined.toThemeEntity(
            existing = existing,
            isOwned = true,
            isActive = existing?.isActive ?: (predefined.id == ThemeConstants.DEFAULT_THEME_ID && dao.getActiveTheme() == null)
        )
        dao.insertThemes(listOf(entity))
        dao.insertThemeMoods(predefined.toMoodEntities())
    }

    private suspend fun seedPredefinedThemes(ownedThemeIds: Set<String> = emptySet()) {
        val normalizedOwnedIds = ownedThemeIds.map { ThemeConstants.normalizeThemeId(it) }.toSet()
        val currentCached = dao.getAllThemes().first()
        val existingById = currentCached.associateBy { ThemeConstants.normalizeThemeId(it.id) }
        val hasActiveTheme = currentCached.any { it.isActive }
        val entities = ThemeConstants.THEMES.map { predefined ->
            val existing = existingById[predefined.id]
            predefined.toThemeEntity(
                existing = existing,
                isOwned = existing?.isOwned == true ||
                        predefined.id == ThemeConstants.DEFAULT_THEME_ID ||
                        predefined.id in normalizedOwnedIds,
                isActive = existing?.isActive ?: (predefined.id == ThemeConstants.DEFAULT_THEME_ID && !hasActiveTheme)
            )
        }
        dao.insertThemes(entities)
        dao.insertThemeMoods(ThemeConstants.THEMES.flatMap { it.toMoodEntities() })
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
        val localThemeId = ThemeConstants.normalizeThemeId(themeId)
        // Optimistic UI Update: change local state immediately
        try {
            ensureThemeCached(localThemeId)
            val timestamp = System.currentTimeMillis()
            val cachedTheme = dao.getThemeById(localThemeId)
            val activeDomainTheme = myThemesState.value
                .firstOrNull { ThemeConstants.normalizeThemeId(it.id) == localThemeId }
                ?.copy(isOwned = true, isActive = true, activatedAt = timestamp)
                ?: cachedTheme?.toDomain()?.copy(isOwned = true, isActive = true, activatedAt = timestamp)
            dao.clearActiveTheme()
            if (activeDomainTheme != null) {
                dao.insertThemes(listOf(ThemeEntity.fromDomain(activeDomainTheme)))
            } else {
                dao.setActiveTheme(localThemeId, timestamp)
            }

            // Also sync the themeType DataStore for real-time presets reactivity
            themePreferencesManager.setActiveThemeJson(if (activeDomainTheme != null) Gson().toJson(activeDomainTheme) else null)

            val themeType = localThemeId.toMoonThemeTypeOrNull()
                ?: cachedTheme?.decoration?.toMoonThemeTypeOrNull()
                ?: com.diary.moonpage.core.theme.MoonThemeType.DEFAULT
            themePreferencesManager.setThemeType(themeType)
            myThemesState.value = myThemesState.value.map { theme ->
                theme.copy(isActive = ThemeConstants.normalizeThemeId(theme.id) == localThemeId)
            }
        } catch (e: Exception) {
            android.util.Log.e("ThemeRepository", "Optimistic update failed", e)
        }

        return try {
            val response = api.setActiveTheme(SetActiveThemeRequest(themeId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                if (ThemeConstants.findTheme(localThemeId) != null) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(parseErrorResponse(response.errorBody()?.string())))
                }
            }
        } catch (e: Exception) {
            // Keep the optimistic change for offline support
            Result.success(Unit)
        }
    }

    private suspend fun ensureThemeCached(themeId: String) {
        val localThemeId = ThemeConstants.normalizeThemeId(themeId)
        if (dao.getThemeById(localThemeId) != null) return

        val myTheme = myThemesState.value.find { ThemeConstants.normalizeThemeId(it.id) == localThemeId }
        if (myTheme != null) {
            dao.insertThemes(listOf(ThemeEntity.fromDomain(myTheme).copy(isOwned = true)))
            return
        }

        fetchAndCacheThemeDetails(localThemeId)
    }

    private suspend fun buildThemeMoodRequests(theme: Theme): List<CreateThemeMoodRequest> {
        val cachedMoods = dao.getMoodsForTheme(theme.id)
        if (cachedMoods.isNotEmpty()) {
            return cachedMoods.mapIndexed { index, mood ->
                CreateThemeMoodRequest(
                    baseMoodId = mood.baseMoodId.toThemeMoodIdOrNull() ?: DEFAULT_MOOD_IDS[index.coerceIn(DEFAULT_MOOD_IDS.indices)],
                    iconColor = mood.iconUrl,
                    customName = mood.customName.orEmpty()
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
                    baseMoodId = mood.baseMoodId,
                    iconColor = mood.iconColor,
                    customName = mood.customName.orEmpty()
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
                iconColor = fallbackColor,
                customName = names[index]
            )
        }
    }

    override suspend fun getMoodsForTheme(themeId: String): List<ThemeMoodEntity> {
        val localThemeId = ThemeConstants.normalizeThemeId(themeId)
        val descriptionMoods = dao.getThemeById(localThemeId)
            ?.customMoodEntitiesFromDescription()
            .orEmpty()
        if (descriptionMoods.isNotEmpty()) {
            dao.insertThemeMoods(descriptionMoods)
            return descriptionMoods
        }

        val cachedMoods = dao.getMoodsForTheme(localThemeId)
        if (cachedMoods.isEmpty()) {
            // Check predefined first
            val predefined = ThemeConstants.findTheme(localThemeId)
            if (predefined != null) {
                if (dao.getThemeById(predefined.id) == null) {
                    seedPredefinedThemes()
                } else {
                    dao.insertThemeMoods(predefined.toMoodEntities())
                }
                return dao.getMoodsForTheme(predefined.id)
            }
            if (localThemeId.isCustomThemeId()) return cachedMoods

            try {
                val response = api.getThemeMoods(themeId)
                if (response.isSuccessful && response.body() != null) {
                    val moods = response.body()!!.map { dto ->
                        ThemeMoodEntity(
                            themeId = localThemeId,
                            baseMoodId = dto.baseMoodId.toString(),
                            iconUrl = dto.iconColor,
                            customName = dto.customName.orEmpty()
                        )
                    }
                    if (moods.isNotEmpty() && dao.getThemeById(localThemeId) != null) {
                        dao.insertThemeMoods(moods)
                    }
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
        dao.deleteAllThemeMoods()
        dao.deleteAllThemes()
        myThemesState.value = emptyList()
        themePreferencesManager.setActiveThemeJson(null)
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

private fun Theme.withFreshRemoteBackgroundFrom(entityTheme: Theme?): Theme {
    if (entityTheme == null) return this
    if (ThemeConstants.normalizeThemeId(entityTheme.id) != ThemeConstants.normalizeThemeId(id)) return this
    if (!hasMissingLocalImageBackground()) return this
    if (!entityTheme.hasRemoteImageBackground()) return this
    return entityTheme.copy(
        isActive = isActive || entityTheme.isActive,
        activatedAt = activatedAt ?: entityTheme.activatedAt
    )
}

private fun Theme.hasMissingLocalImageBackground(): Boolean {
    val fillModes = description.backgroundFillModes()
    val imageMode = fillModes.any { it.isImageFillMode() } ||
        description.backgroundUris().any { it.isThemeAssetReference() } ||
        backgroundUrl.isThemeAssetReference()
    if (!imageMode || hasRemoteImageBackground()) return false
    return backgroundUrl.isMissingLocalThemeAsset() ||
        description.backgroundUris().any { it.isMissingLocalThemeAsset() }
}

private fun Theme.hasRemoteImageBackground(): Boolean {
    return backgroundUrl.takeIfRemoteThemeAsset() != null ||
        description.backgroundUris().any { it.takeIfRemoteThemeAsset() != null }
}

private fun PredefinedTheme.toThemeEntity(
    existing: ThemeEntity?,
    isOwned: Boolean,
    isActive: Boolean
): ThemeEntity {
    return ThemeEntity(
        id = id,
        name = name,
        collection = "Collection",
        price = price,
        isFree = price == 0,
        thumbnailUrl = thumbnailUrl,
        backgroundUrl = backgroundUrl,
        isOwned = isOwned,
        isActive = isActive,
        description = toAppearanceDescription(),
        type = ThemeType.THEME.name,
        icons = "VERY_HAPPY,HAPPY,NEUTRAL,SAD,ANGRY",
        primaryColor = primaryPreviewColor(),
        decoration = decoration,
        activatedAt = existing?.activatedAt
    )
}

private fun PredefinedTheme.toMoodEntities(): List<ThemeMoodEntity> {
    return moods.map { mood ->
        ThemeMoodEntity(
            themeId = id,
            baseMoodId = mood.baseMoodId,
            iconUrl = mood.iconUrl,
            customName = mood.customName
        )
    }
}

// baseMoodId is now stored as Int string ("1".."5") matching the API
private fun Int.toThemeMoodName(): String = this.toString()

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
    return id.isCustomThemeId() ||
            decoration.equals("CUSTOM", ignoreCase = true) ||
            collection.equals("Custom Theme", ignoreCase = true)
}

private fun ThemeEntity.customMoodEntitiesFromDescription(): List<ThemeMoodEntity> {
    if (!isCustomThemeEntity()) return emptyList()
    val colors = description.iconColorsFromThemeDescription()
    if (colors.size < 5) return emptyList()

    return colors.take(5).mapIndexed { index, color ->
        ThemeMoodEntity(
            themeId = id,
            baseMoodId = DEFAULT_MOOD_NAMES[index],
            iconUrl = color,
            customName = DEFAULT_MOOD_DISPLAY_NAMES[index]
        )
    }
}

private fun String?.iconColorsFromThemeDescription(): List<String> {
    if (isNullOrBlank()) return emptyList()
    val root = runCatching { JSONObject(this) }.getOrNull() ?: return emptyList()
    val modes = listOf(root.optJSONObject("light"), root.optJSONObject("dark"))

    modes.forEach { appearance ->
        val iconColors = appearance?.optJSONArray("iconColors")
            ?.toColorList()
            .orEmpty()
        if (iconColors.size >= 5) return iconColors
    }

    val fallbackColor = modes.firstNotNullOfOrNull { appearance ->
        appearance?.optString("iconColor")?.toCanonicalThemeColorHex()
    } ?: return emptyList()
    return List(5) { fallbackColor }
}

private fun String?.explicitIconColorsFromThemeDescription(): List<String> {
    if (isNullOrBlank()) return emptyList()
    val root = runCatching { JSONObject(this) }.getOrNull() ?: return emptyList()
    val modes = listOf(root.optJSONObject("light"), root.optJSONObject("dark"), root)

    modes.forEach { appearance ->
        val iconColors = appearance?.optJSONArray("iconColors")
            ?.toColorList()
            .orEmpty()
        if (iconColors.size >= DEFAULT_MOOD_IDS.size) return iconColors.take(DEFAULT_MOOD_IDS.size)
    }

    return emptyList()
}

private fun String?.withMoodIconColors(iconColors: List<String>): String {
    val normalizedColors = iconColors
        .mapNotNull { it.toCanonicalThemeColorHex() }
        .take(DEFAULT_MOOD_IDS.size)
    val root = takeIf { !it.isNullOrBlank() }
        ?.let { raw -> runCatching { JSONObject(raw) }.getOrNull() }
        ?: JSONObject()
    if (normalizedColors.size < DEFAULT_MOOD_IDS.size) return root.toString()

    val light = root.optJSONObject("light") ?: JSONObject()
    light.put("iconColor", normalizedColors.first())
    light.put("iconColors", JSONArray(normalizedColors))
    root.put("light", light)

    return root.toString()
}

private fun List<ThemeMoodEntity>.toOrderedIconColors(): List<String> {
    if (isEmpty()) return emptyList()

    val colorsByMoodId = linkedMapOf<Int, String>()
    forEach { mood ->
        val moodId = mood.baseMoodId.toThemeMoodIdOrNull()
        val color = mood.iconUrl.toCanonicalThemeColorHex()
        if (moodId != null && color != null) {
            colorsByMoodId.putIfAbsent(moodId, color)
        }
    }

    val orderedColors = DEFAULT_MOOD_IDS.mapNotNull { moodId -> colorsByMoodId[moodId] }
    if (orderedColors.size >= DEFAULT_MOOD_IDS.size) return orderedColors.take(DEFAULT_MOOD_IDS.size)

    val positionalColors = mapNotNull { mood -> mood.iconUrl.toCanonicalThemeColorHex() }
    return if (positionalColors.size >= DEFAULT_MOOD_IDS.size) {
        positionalColors.take(DEFAULT_MOOD_IDS.size)
    } else {
        emptyList()
    }
}

private fun JSONArray.toColorList(): List<String> {
    return List(length()) { index ->
        optString(index).toCanonicalThemeColorHex()
    }.filterNotNull()
}

private fun String?.takeIfThemeColor(): String? {
    return takeIf { it.isThemeColor() }
}

private fun String?.takeIfLocalThemeAsset(): String? {
    if (isNullOrBlank()) return null
    val rawPath = this.removePrefix("file://")
    val file = File(rawPath)
    return takeIf {
        (file.exists() && file.isFile) ||
                it.startsWith("content://", ignoreCase = true) ||
                it.startsWith("android.resource://", ignoreCase = true)
    }
}

private fun String?.withRemoteBackgroundImage(backgroundUrl: String?): String? {
    val remoteBackground = backgroundUrl.takeIfRemoteThemeAsset() ?: return this
    val root = takeIf { !it.isNullOrBlank() }
        ?.let { raw -> runCatching { JSONObject(raw) }.getOrNull() }
        ?: return this
    var changed = false

    listOf("light", "dark").forEach { mode ->
        val appearance = root.optJSONObject(mode) ?: return@forEach
        val currentUri = appearance.optString("backgroundUri")
        val fillMode = appearance.optString("backgroundFillMode")
        val imageMode = fillMode.isImageFillMode() ||
                (fillMode.isBlank() && currentUri.isThemeAssetReference())
        if (!imageMode) return@forEach

        if (appearance.optString("backgroundFillMode").isBlank()) {
            appearance.put("backgroundFillMode", "background")
            changed = true
        }
        if (currentUri.isBlank() || currentUri.isLocalThemeFilePath()) {
            appearance.put("backgroundUri", remoteBackground)
            changed = true
        }
    }

    return if (changed) root.toString() else this
}

private fun String?.backgroundFillModes(): List<String> {
    val root = takeIf { !it.isNullOrBlank() }
        ?.let { raw -> runCatching { JSONObject(raw) }.getOrNull() }
        ?: return emptyList()
    return listOfNotNull(
        root.optJSONObject("light")?.optString("backgroundFillMode"),
        root.optJSONObject("dark")?.optString("backgroundFillMode")
    ).filter { it.isNotBlank() }
}

private fun String?.backgroundUris(): List<String> {
    val root = takeIf { !it.isNullOrBlank() }
        ?.let { raw -> runCatching { JSONObject(raw) }.getOrNull() }
        ?: return emptyList()
    return listOfNotNull(
        root.optJSONObject("light")?.optString("backgroundUri"),
        root.optJSONObject("dark")?.optString("backgroundUri")
    ).filter { it.isNotBlank() }
}

private fun String?.takeIfRemoteThemeAsset(): String? {
    val value = this?.trim()?.takeIf { it.isThemeAssetReference() } ?: return null
    return value.takeIf {
        it.startsWith("http://", ignoreCase = true) ||
                it.startsWith("https://", ignoreCase = true)
    }
}

private fun String?.isImageFillMode(): Boolean {
    return equals("Image", ignoreCase = true) || equals("background", ignoreCase = true)
}

private fun String?.isSolidOrGradientFillMode(): Boolean {
    return equals("Solid", ignoreCase = true) || equals("Gradient", ignoreCase = true)
}

private fun String?.isLocalThemeFilePath(): Boolean {
    val value = this?.trim() ?: return false
    return value.startsWith("/data/") ||
        value.startsWith("/storage/") ||
        value.startsWith("/sdcard/") ||
        value.startsWith("file://", ignoreCase = true)
}

private fun String?.isMissingLocalThemeAsset(): Boolean {
    return isLocalThemeFilePath() && takeIfLocalThemeAsset() == null
}

private fun String?.isThemeAssetReference(): Boolean {
    val value = this?.trim() ?: return false
    if (value.isBlank() || value.equals("null", ignoreCase = true) || value.equals("pending", ignoreCase = true)) {
        return false
    }
    return !value.contains(",") && !value.isThemeColor()
}

private fun String?.isThemeColor(): Boolean {
    if (isNullOrBlank()) return false
    val raw = trim()
    val value = when {
        raw.startsWith("#") -> raw.drop(1)
        raw.startsWith("0x", ignoreCase = true) -> raw.drop(2)
        else -> raw
    }
    return (value.length == 6 || value.length == 8) && value.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
}

private fun String?.toCanonicalThemeColorHex(): String? {
    if (!isThemeColor()) return null
    val raw = this?.trim() ?: return null
    val value = when {
        raw.startsWith("#") -> raw.drop(1)
        raw.startsWith("0x", ignoreCase = true) -> raw.drop(2)
        else -> raw
    }
    return "#$value"
}

private fun String.toMoonThemeTypeOrNull(): MoonThemeType? {
    if (ThemeConstants.isDefaultThemeId(this)) return MoonThemeType.DEFAULT
    val normalizedThemeId = ThemeConstants.normalizeThemeId(this)
    val normalized = if (normalizedThemeId.startsWith("theme_")) normalizedThemeId.substringAfter("theme_") else normalizedThemeId
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
    if (ThemeConstants.isDefaultThemeId(this)) return MoonThemeType.DEFAULT
    val normalizedThemeId = ThemeConstants.normalizeThemeId(this)
    val normalized = if (normalizedThemeId.startsWith("theme_")) normalizedThemeId.substringAfter("theme_") else normalizedThemeId
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

private fun String?.primaryColorForMode(mode: String): String? {
    if (isNullOrBlank()) return null
    val appearance = runCatching { JSONObject(this).optJSONObject(mode) }.getOrNull() ?: return null
    return appearance.optString("primaryColor").takeIfThemeColor()
}

private fun String?.toApiColorHex(): String? {
    if (!isThemeColor()) return null
    val raw = this?.trim() ?: return null
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

private fun String.isCustomThemeId(): Boolean {
    return startsWith("custom_")
}

private val DEFAULT_MOOD_IDS = listOf(5, 4, 3, 2, 1)
private val DEFAULT_MOOD_NAMES = listOf("Rad", "Good", "Meh", "Bad", "Awful")
private val DEFAULT_MOOD_DISPLAY_NAMES = listOf("Very Happy", "Happy", "Neutral", "Sad", "Very Sad")

private const val CUSTOM_THEME_PRICE = 500
private const val ACTIVE_THEME_SYNC_GRACE_MS = 5 * 60 * 1000L
