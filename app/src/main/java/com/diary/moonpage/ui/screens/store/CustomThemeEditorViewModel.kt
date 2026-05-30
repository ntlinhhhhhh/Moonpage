package com.diary.moonpage.ui.screens.store

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.LocaleUtils
import com.diary.moonpage.core.util.customThemeImageFormat
import com.diary.moonpage.core.util.saveBitmapToInternalStorage
import com.diary.moonpage.domain.repository.CreateThemeMoodPayload
import com.diary.moonpage.domain.repository.CreateThemePayload
import com.diary.moonpage.domain.repository.ThemeRepository
import com.diary.moonpage.domain.usecase.theme.BuyThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.max

data class DrawStroke(
    val points: List<Offset>,
    val color: Long,
    val strokeWidth: Float,
    val brushType: BrushType,
    val isEraser: Boolean = false
)

sealed class EditorScreenState {
    object Home : EditorScreenState()
    object SolidBg : EditorScreenState()
    object GradientBg : EditorScreenState()
    object EditComponents : EditorScreenState()
    object FinalPreview : EditorScreenState()
}

enum class GradientNode { Start, End }

enum class EditMode { None, Draw, Palette }

enum class BrushType {
    Fine, Bold, Pencil, Spray
}

enum class ColorFocusTarget {
    Primary, Icon
}

enum class BackgroundFillMode {
    Solid, Gradient
}

enum class EditorAppearanceMode {
    Light, Dark
}

data class ThemeAppearanceState(
    val backgroundUri: String? = null,
    val backgroundScale: Float = 1f,
    val backgroundRotation: Float = 0f,
    val backgroundOffsetX: Float = 0f,
    val backgroundOffsetY: Float = 0f,
    val backgroundFillMode: BackgroundFillMode = BackgroundFillMode.Solid,
    val solidBackgroundColor: Long = 0xFFFFF7EC,
    val gradientStartColor: Long = 0xFFFFF7EC,
    val gradientEndColor: Long = 0xFFE8F5E9,
    val primaryColor: Long = 0xFF8D6E63,
    val iconColor: Long = 0xFFEF9A9A,
    val iconColors: List<Long> = listOf(0xFFFFCA28, 0xFF81C784, 0xFF64B5F6, 0xFFBA68C8, 0xFF8D6E63)
)

data class CustomThemeEditorUiState(
    val name: String = "",
    val pendingBackgroundUri: String? = null,
    val editingMode: EditorAppearanceMode = EditorAppearanceMode.Light,
    val lightAppearance: ThemeAppearanceState = ThemeAppearanceState(),
    val darkAppearance: ThemeAppearanceState = ThemeAppearanceState(
        backgroundFillMode = BackgroundFillMode.Solid,
        solidBackgroundColor = 0xFF1C1C1C,
        gradientStartColor = 0xFF232323,
        gradientEndColor = 0xFF393939,
        primaryColor = 0xFFFFF9EF,
        iconColor = 0xFFFFD54F,
        iconColors = listOf(0xFFFFD54F, 0xFF81C784, 0xFF4FC3F7, 0xFF9575CD, 0xFFEF9A9A)
    ),
    val selectedIconIndex: Int = 0,
    val colorFocusTarget: ColorFocusTarget = ColorFocusTarget.Primary,
    val brushColor: Long = 0xFF8D6E63,
    val brushSize: Float = 8f,
    val brushType: BrushType = BrushType.Fine,
    val isEraser: Boolean = false,
    val currentScreen: EditorScreenState = EditorScreenState.Home,
    val recentColors: List<Long> = emptyList(),
    val activeGradientNode: GradientNode = GradientNode.Start,
    val activeEditMode: EditMode = EditMode.None,
    val strokes: List<DrawStroke> = emptyList(),
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val hasUnsavedChanges: Boolean = false
) {
    val activeAppearance: ThemeAppearanceState
        get() = if (editingMode == EditorAppearanceMode.Dark) darkAppearance else lightAppearance

    val backgroundUri: String?
        get() = activeAppearance.backgroundUri
    val backgroundScale: Float
        get() = activeAppearance.backgroundScale
    val backgroundRotation: Float
        get() = activeAppearance.backgroundRotation
    val backgroundOffsetX: Float
        get() = activeAppearance.backgroundOffsetX
    val backgroundOffsetY: Float
        get() = activeAppearance.backgroundOffsetY
    val backgroundFillMode: BackgroundFillMode
        get() = activeAppearance.backgroundFillMode
    val solidBackgroundColor: Long
        get() = activeAppearance.solidBackgroundColor
    val gradientStartColor: Long
        get() = activeAppearance.gradientStartColor
    val gradientEndColor: Long
        get() = activeAppearance.gradientEndColor
    val primaryColor: Long
        get() = activeAppearance.primaryColor
    val iconColor: Long
        get() = activeAppearance.iconColors[selectedIconIndex.coerceIn(0, activeAppearance.iconColors.lastIndex)]
    val iconColors: List<Long>
        get() = activeAppearance.iconColors
}

sealed class CustomThemeEditorEffect {
    object Saved : CustomThemeEditorEffect()
    data class Error(val message: String) : CustomThemeEditorEffect()
}

@HiltViewModel
class CustomThemeEditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeRepository: ThemeRepository,
    private val buyThemeUseCase: BuyThemeUseCase,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomThemeEditorUiState(name = localizedString(R.string.my_custom_theme)))
    val uiState: StateFlow<CustomThemeEditorUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomThemeEditorEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, hasUnsavedChanges = true) }
    }

    fun setBackgroundUri(uri: String?) {
        if (uri != null && uri.startsWith("content://")) {
            runCatching {
                val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(Uri.parse(uri), flags)
            }
        }
        _uiState.update { it.copy(pendingBackgroundUri = uri) }
    }

    fun applyPendingBackground(scale: Float, rotation: Float, offsetX: Float, offsetY: Float) {
        val backgroundUri = _uiState.value.pendingBackgroundUri
        _uiState.update { state ->
            state.copy(
                lightAppearance = state.lightAppearance.copy(
                    backgroundUri = backgroundUri,
                    backgroundScale = scale,
                    backgroundRotation = rotation,
                    backgroundOffsetX = offsetX,
                    backgroundOffsetY = offsetY
                ),
                darkAppearance = state.darkAppearance.copy(
                    backgroundUri = backgroundUri,
                    backgroundScale = scale,
                    backgroundRotation = rotation,
                    backgroundOffsetX = offsetX,
                    backgroundOffsetY = offsetY
                ),
                pendingBackgroundUri = null,
                hasUnsavedChanges = true
            )
        }
    }

    fun cancelPendingBackground() {
        _uiState.update { it.copy(
            pendingBackgroundUri = null,
            currentScreen = EditorScreenState.Home
        ) }
    }

    fun setSolidBackgroundColor(color: Long) {
        updateAppearance { appearance ->
            appearance.copy(
                backgroundFillMode = BackgroundFillMode.Solid,
                solidBackgroundColor = color,
                backgroundUri = null
            )
        }
        _uiState.update { it.copy(hasUnsavedChanges = true) }
    }

    fun setBackgroundFillMode(mode: BackgroundFillMode) {
        updateAppearance { appearance ->
            appearance.copy(
                backgroundFillMode = mode,
                backgroundUri = null
            )
        }
        _uiState.update { it.copy(hasUnsavedChanges = true) }
    }

    fun setGradientBackgroundStartColor(color: Long) {
        updateAppearance { appearance ->
            appearance.copy(
                backgroundFillMode = BackgroundFillMode.Gradient,
                gradientStartColor = color,
                backgroundUri = null
            )
        }
        _uiState.update { it.copy(hasUnsavedChanges = true) }
    }

    fun setGradientBackgroundEndColor(color: Long) {
        updateAppearance { appearance ->
            appearance.copy(
                backgroundFillMode = BackgroundFillMode.Gradient,
                gradientEndColor = color,
                backgroundUri = null
            )
        }
        _uiState.update { it.copy(hasUnsavedChanges = true) }
    }

    fun setPrimaryColor(color: Long) {
        updateAppearance { it.copy(primaryColor = color) }
        _uiState.update { it.copy(colorFocusTarget = ColorFocusTarget.Primary, hasUnsavedChanges = true) }
    }

    fun setIconColor(color: Long) {
        updateAppearance { appearance ->
            val updated = appearance.iconColors.toMutableList().also { colors ->
                colors[_uiState.value.selectedIconIndex] = color
            }
            appearance.copy(iconColor = color, iconColors = updated)
        }
        _uiState.update { it.copy(colorFocusTarget = ColorFocusTarget.Icon, hasUnsavedChanges = true) }
    }

    fun toggleEditingMode() {
        _uiState.update { state ->
            state.copy(
                editingMode = if (state.editingMode == EditorAppearanceMode.Light) {
                    EditorAppearanceMode.Dark
                } else {
                    EditorAppearanceMode.Light
                }
            )
        }
    }

    fun selectIcon(index: Int) {
        _uiState.update {
            it.copy(
                selectedIconIndex = index.coerceIn(0, it.iconColors.lastIndex),
                colorFocusTarget = ColorFocusTarget.Icon
            )
        }
    }

    fun focusPrimaryColor() {
        _uiState.update { it.copy(colorFocusTarget = ColorFocusTarget.Primary) }
    }

    fun applyFocusedColor(color: Long) {
        when (_uiState.value.colorFocusTarget) {
            ColorFocusTarget.Primary -> setPrimaryColor(color)
            ColorFocusTarget.Icon -> setIconColor(color)
        }
    }

    fun setBrushColor(color: Long) {
        _uiState.update { it.copy(brushColor = color, hasUnsavedChanges = true) }
    }

    fun setBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size, hasUnsavedChanges = true) }
    }

    fun setBrushType(type: BrushType) {
        _uiState.update { it.copy(brushType = type, isEraser = false, hasUnsavedChanges = true) }
    }

    fun setEraser(enabled: Boolean) {
        _uiState.update { it.copy(isEraser = enabled, hasUnsavedChanges = true) }
    }

    fun setScreen(screen: EditorScreenState) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setEditMode(mode: EditMode) {
        _uiState.update { it.copy(activeEditMode = mode) }
    }

    fun setGradientNode(node: GradientNode) {
        _uiState.update { it.copy(activeGradientNode = node) }
    }

    fun addColorToRecent(color: Long) {
        _uiState.update { state ->
            if (state.recentColors.contains(color)) {
                state // Bỏ qua, không lưu trùng lặp và không đẩy lên đầu
            } else {
                val updated = listOf(color) + state.recentColors
                state.copy(recentColors = updated.take(10))
            }
        }
    }

    fun addStroke(stroke: DrawStroke) {
        if (stroke.points.size < 2) return
        _uiState.update { it.copy(strokes = it.strokes + stroke, hasUnsavedChanges = true) }
    }

    fun undoStroke() {
        _uiState.update { it.copy(strokes = it.strokes.dropLast(1), hasUnsavedChanges = true) }
    }

    fun clearStrokes() {
        _uiState.update { state ->
            if (state.strokes.isEmpty()) state else state.copy(strokes = emptyList(), hasUnsavedChanges = true)
        }
    }

    fun onBackRequested(onNavigateBack: () -> Unit) {
        val state = _uiState.value
        when (state.currentScreen) {
            EditorScreenState.Home -> {
                if (state.hasUnsavedChanges) {
                    _uiState.update { it.copy(showDiscardDialog = true) }
                } else {
                    onNavigateBack()
                }
            }
            EditorScreenState.FinalPreview -> setScreen(EditorScreenState.EditComponents)
            else -> setScreen(EditorScreenState.Home)
        }
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun saveTheme() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            runCatching {
                withContext(Dispatchers.IO) {
                    coroutineScope {
                        val timestamp = System.currentTimeMillis()
                        val thumbnailFileName = "custom_theme_thumb_$timestamp.webp"
                        val backgroundFileName = "custom_theme_bg_$timestamp.webp"
                        val themeName = state.name.ifBlank { localizedString(R.string.my_custom_theme) }
                        
                        val user = userRepository.currentUser.value ?: userRepository.getCurrentUser().getOrThrow()
                        val themeId = "custom_${user.userId.toThemeIdPart()}_$timestamp"
                        
                        val backgroundAppearance = state.imageBackgroundAppearance()
                        
                        // Optimization: Decode background bitmap once and reuse
                        val sharedBackgroundBitmap = backgroundAppearance?.backgroundUri?.let { context.decodeThemeBitmap(it) }
                        
                        val backgroundDeferred = async {
                            backgroundAppearance?.backgroundUri?.takeIf { it.isNotBlank() }?.let {
                                context.saveBitmapToInternalStorage(
                                    state.createBackgroundBitmap(backgroundAppearance, sharedBackgroundBitmap),
                                    backgroundFileName,
                                    format = customThemeImageFormat(),
                                    quality = CUSTOM_THEME_IMAGE_QUALITY
                                )
                            }
                        }
                        
                        val thumbnailDeferred = async {
                            context.saveBitmapToInternalStorage(
                                state.createThumbnailBitmap(sharedBackgroundBitmap),
                                thumbnailFileName,
                                format = customThemeImageFormat(),
                                quality = CUSTOM_THEME_IMAGE_QUALITY
                            )
                        }
                        
                        val backgroundPath = backgroundDeferred.await()
                        val thumbnailPath = thumbnailDeferred.await()
                        
                        // Important: Recycle the shared bitmap if it was created
                        sharedBackgroundBitmap?.recycle()

                        val hasImageBackground = backgroundPath != null
                        themeRepository.createThemes(
                            listOf(
                                CreateThemePayload(
                                    id = themeId,
                                    name = themeName,
                                    price = CUSTOM_THEME_SLOT_PRICE,
                                    thumbnailUrl = thumbnailPath,
                                    backgroundUrl = backgroundPath,
                                    primaryColor = state.lightAppearance.primaryColor.toColorHex(),
                                    primaryLightColor = state.lightAppearance.primaryColor.toColorHex(),
                                    primaryDarkColor = state.darkAppearance.primaryColor.toColorHex(),
                                    backgroundColor = null,
                                    backgroundLightColor = if (hasImageBackground) null else state.lightAppearance.themeBackgroundPayload(),
                                    backgroundDarkColor = if (hasImageBackground) null else state.darkAppearance.themeBackgroundPayload(),
                                    description = state.toThemeConfigJson(backgroundPath),
                                    isOfficial = false,
                                    isActive = true,
                                    moods = state.lightAppearance.toMoodPayloads()
                                )
                            )
                        ).getOrThrow()
                        
                        buyThemeUseCase(themeId, CUSTOM_THEME_SLOT_PRICE).getOrThrow()
                        themeRepository.setActiveTheme(themeId).getOrThrow()
                        themeRepository.getMyThemes().getOrThrow()
                        userRepository.getCurrentUser()
                    }
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                _effect.emit(CustomThemeEditorEffect.Saved)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false) }
                _effect.emit(CustomThemeEditorEffect.Error(error.message ?: localizedString(R.string.could_not_save_custom_theme)))
            }
        }
    }

    private fun updateAppearance(transform: (ThemeAppearanceState) -> ThemeAppearanceState) {
        _uiState.update { state ->
            if (state.editingMode == EditorAppearanceMode.Dark) {
                state.copy(darkAppearance = transform(state.darkAppearance))
            } else {
                state.copy(lightAppearance = transform(state.lightAppearance))
            }
        }
    }

    private fun ThemeAppearanceState.toMoodPayloads(): List<CreateThemeMoodPayload> {
        val moodConfigs = listOf(
            5 to "Very Happy",
            4 to "Happy",
            3 to "Neutral",
            2 to "Sad",
            1 to "Very Sad"
        )
        return moodConfigs.mapIndexed { index, (moodId, customName) ->
            CreateThemeMoodPayload(
                baseMoodId = moodId,
                iconUrl = iconColors.getOrElse(index) { primaryColor }.toRgbColorHex(),
                customName = customName
            )
        }
    }

    private fun Long.toColorHex(): String = "#%08X".format(this)

    private fun Long.toRgbColorHex(): String = "#%06X".format(this and 0x00FFFFFF)

    private fun Long.toApiColorHex(): String = "0x%08X".format(this)

    private fun localizedString(@StringRes resId: Int): String {
        val localizedContext = LocaleUtils.applyLocale(context, LocaleUtils.getCurrentLanguage())
        return localizedContext.getString(resId)
    }

    private fun ThemeAppearanceState.themeBackgroundPayload(): String {
        return when (backgroundFillMode) {
            BackgroundFillMode.Gradient -> {
                "${gradientStartColor.toColorHex()},${gradientEndColor.toColorHex()}"
            }
            BackgroundFillMode.Solid -> solidBackgroundColor.toColorHex()
        }
    }

    private fun CustomThemeEditorUiState.imageBackgroundAppearance(): ThemeAppearanceState? {
        return lightAppearance.takeIf { !it.backgroundUri.isNullOrBlank() }
            ?: darkAppearance.takeIf { !it.backgroundUri.isNullOrBlank() }
    }

    private fun CustomThemeEditorUiState.toThemeConfigJson(backgroundPath: String?): String {
        return JSONObject()
            .put("light", lightAppearance.toJson(backgroundPath))
            .put("dark", darkAppearance.toJson(backgroundPath))
            .toString()
    }

    private fun ThemeAppearanceState.toJson(backgroundPath: String?): JSONObject {
        return JSONObject()
            .put("backgroundUri", backgroundPath ?: backgroundUri)
            .put("backgroundScale", backgroundScale)
            .put("backgroundRotation", backgroundRotation)
            .put("backgroundOffsetX", backgroundOffsetX)
            .put("backgroundOffsetY", backgroundOffsetY)
            .put("backgroundFillMode", backgroundFillMode.name)
            .put("solidBackgroundColor", solidBackgroundColor.toColorHex())
            .put("gradientStartColor", gradientStartColor.toColorHex())
            .put("gradientEndColor", gradientEndColor.toColorHex())
            .put("primaryColor", primaryColor.toColorHex())
            .put("iconColor", iconColor.toColorHex())
            .put("iconColors", JSONArray(iconColors.map { it.toColorHex() }))
    }

    private fun CustomThemeEditorUiState.createThumbnailBitmap(sharedBackground: Bitmap? = null): Bitmap {
        val bitmap = Bitmap.createBitmap(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawThemeBackground(lightAppearance, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, strokes, sharedBackground)
        canvas.drawMoodIconStrip(lightAppearance.iconColors, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
        return bitmap
    }

    private fun CustomThemeEditorUiState.createBackgroundBitmap(
        appearance: ThemeAppearanceState = lightAppearance,
        sharedBackground: Bitmap? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(BACKGROUND_WIDTH, BACKGROUND_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawThemeBackground(appearance, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, strokes, sharedBackground)
        return bitmap
    }

    private fun Canvas.drawThemeBackground(
        appearance: ThemeAppearanceState,
        width: Int,
        height: Int,
        strokes: List<DrawStroke>,
        sharedBackground: Bitmap? = null
    ) {
        val image = sharedBackground ?: appearance.backgroundUri?.let { context.decodeThemeBitmap(it) }
        if (image != null) {
            drawBitmap(
                image,
                appearance.backgroundMatrix(image.width, image.height, width, height),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )
        } else if (appearance.backgroundFillMode == BackgroundFillMode.Gradient) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    height.toFloat(),
                    appearance.gradientStartColor.toArgbInt(),
                    appearance.gradientEndColor.toArgbInt(),
                    Shader.TileMode.CLAMP
                )
            }
            drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            drawColor(appearance.solidBackgroundColor.toArgbInt())
        }
        drawEditorStrokes(strokes, width, height)
    }

    private fun ThemeAppearanceState.backgroundMatrix(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Matrix {
        val baseScale = max(
            targetWidth.toFloat() / sourceWidth.toFloat(),
            targetHeight.toFloat() / sourceHeight.toFloat()
        )
        val targetScale = baseScale * backgroundScale * rotationCoverMultiplier(backgroundRotation)
        return Matrix().apply {
            postTranslate(-sourceWidth / 2f, -sourceHeight / 2f)
            postScale(targetScale, targetScale)
            postRotate(backgroundRotation)
            postTranslate(
                targetWidth / 2f + backgroundOffsetX * (targetWidth / PREVIEW_BASE_WIDTH),
                targetHeight / 2f + backgroundOffsetY * (targetHeight / PREVIEW_BASE_HEIGHT)
            )
        }
    }

    private fun Canvas.drawEditorStrokes(strokes: List<DrawStroke>, width: Int, height: Int) {
        if (strokes.isEmpty()) return
        val scaleX = width / PREVIEW_BASE_WIDTH
        val scaleY = height / PREVIEW_BASE_HEIGHT
        strokes.filterNot { it.isEraser }.forEach { stroke ->
            val path = Path()
            stroke.points.forEachIndexed { index, point ->
                val x = point.x * scaleX
                val y = point.y * scaleY
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val widthMultiplier = when (stroke.brushType) {
                BrushType.Fine -> 1f
                BrushType.Bold -> 1.8f
                BrushType.Pencil -> 0.8f
                BrushType.Spray -> 2.2f
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stroke.color.toArgbInt()
                alpha = if (stroke.brushType == BrushType.Pencil) 174 else 255
                style = Paint.Style.STROKE
                strokeWidth = stroke.strokeWidth * widthMultiplier * ((scaleX + scaleY) / 2f)
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            drawPath(path, paint)
        }
    }

    private fun Canvas.drawMoodIconStrip(iconColors: List<Long>, width: Int, height: Int) {
        val iconResources = listOf(
            R.drawable.very_happy,
            R.drawable.happy,
            R.drawable.neutral,
            R.drawable.sad,
            R.drawable.very_sad
        )
        val iconSize = width * 0.13f
        val centerY = height * 0.55f
        val startX = width * 0.18f
        val gap = (width - startX * 2f) / 4f
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        iconResources.forEachIndexed { index, resId ->
            val centerX = startX + gap * index
            circlePaint.color = iconColors.getOrElse(index) { 0xFF8D6E63 }.toArgbInt()
            drawCircle(centerX, centerY, iconSize / 2f, circlePaint)

            val icon = BitmapFactory.decodeResource(context.resources, resId) ?: return@forEachIndexed
            val inset = iconSize * 0.22f
            drawBitmap(
                icon,
                null,
                RectF(
                    centerX - iconSize / 2f + inset,
                    centerY - iconSize / 2f + inset,
                    centerX + iconSize / 2f - inset,
                    centerY + iconSize / 2f - inset
                ),
                imagePaint
            )
        }
    }

    private fun Context.decodeThemeBitmap(value: String): Bitmap? {
        return runCatching {
            val uri = Uri.parse(value)
            when {
                value.startsWith("content://") || value.startsWith("android.resource://") -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }
                    } else {
                        contentResolver.openInputStream(uri)?.use { stream -> BitmapFactory.decodeStream(stream) }
                    }
                }
                value.startsWith("file://") -> BitmapFactory.decodeFile(uri.path)
                else -> BitmapFactory.decodeFile(value)
            }
        }.getOrNull()
    }

    private suspend fun resolveServerThemeId(
        thumbnailFileName: String,
        thumbnailPath: String,
        backgroundFileName: String,
        backgroundPath: String?,
        themeName: String
    ): String {
        repeat(CREATE_THEME_LOOKUP_RETRIES) { attempt ->
            val myThemes = themeRepository.getMyThemes().getOrThrow()
            val matchedTheme = myThemes.firstOrNull { theme ->
                theme.thumbnailUrl == thumbnailPath ||
                    (backgroundPath != null && theme.backgroundUrl == backgroundPath) ||
                    theme.thumbnailUrl?.contains(thumbnailFileName) == true ||
                    (backgroundPath != null && theme.backgroundUrl?.contains(backgroundFileName) == true)
            } ?: myThemes.lastOrNull { it.name == themeName }

            if (matchedTheme != null) {
                return matchedTheme.id
            }

            if (attempt < CREATE_THEME_LOOKUP_RETRIES - 1) {
                delay(CREATE_THEME_LOOKUP_DELAY_MS)
            }
        }

        throw IllegalStateException("Created theme is not visible from server yet")
    }
}

private fun Long.toArgbInt(): Int = toInt()

private fun String.toThemeIdPart(): String {
    return trim()
        .replace(Regex("[^A-Za-z0-9_-]"), "_")
        .trim('_')
        .ifBlank { "user" }
}

private fun rotationCoverMultiplier(rotation: Float): Float {
    val normalized = kotlin.math.abs(rotation) / 45f
    return 1f + normalized.coerceIn(0f, 1f) * 0.34f
}

private const val THUMBNAIL_WIDTH = 640
private const val THUMBNAIL_HEIGHT = 360
private const val BACKGROUND_WIDTH = 720
private const val BACKGROUND_HEIGHT = 1280
private const val PREVIEW_BASE_WIDTH = 360f
private const val PREVIEW_BASE_HEIGHT = 580f
private const val CUSTOM_THEME_IMAGE_QUALITY = 80
private const val CREATE_THEME_LOOKUP_RETRIES = 6
private const val CREATE_THEME_LOOKUP_DELAY_MS = 350L
