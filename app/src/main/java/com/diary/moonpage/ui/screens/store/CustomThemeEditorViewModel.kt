package com.diary.moonpage.ui.screens.store

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.saveBitmapToInternalStorage
import com.diary.moonpage.domain.repository.CreateThemeMoodPayload
import com.diary.moonpage.domain.repository.CreateThemePayload
import com.diary.moonpage.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DrawStroke(
    val points: List<Offset>,
    val color: Long,
    val strokeWidth: Float,
    val brushType: BrushType,
    val isEraser: Boolean = false
)

enum class ThemeEditorTool {
    Background, Draw, Colors, Preview
}

enum class BrushType {
    Fine, Bold, Pencil, Spray
}

enum class ColorFocusTarget {
    Primary, Icon
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
    val solidBackgroundColor: Long = 0xFFFFF7EC,
    val primaryColor: Long = 0xFF8D6E63,
    val iconColor: Long = 0xFFEF9A9A,
    val iconColors: List<Long> = listOf(0xFFFFCA28, 0xFF81C784, 0xFF64B5F6, 0xFFBA68C8, 0xFF8D6E63)
)

data class CustomThemeEditorUiState(
    val name: String = "My Custom Theme",
    val pendingBackgroundUri: String? = null,
    val editingMode: EditorAppearanceMode = EditorAppearanceMode.Light,
    val lightAppearance: ThemeAppearanceState = ThemeAppearanceState(),
    val darkAppearance: ThemeAppearanceState = ThemeAppearanceState(
        solidBackgroundColor = 0xFF1C1C1C,
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
    val selectedTool: ThemeEditorTool = ThemeEditorTool.Background,
    val lastEditingTool: ThemeEditorTool = ThemeEditorTool.Background,
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
    val solidBackgroundColor: Long
        get() = activeAppearance.solidBackgroundColor
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
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomThemeEditorUiState())
    val uiState: StateFlow<CustomThemeEditorUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomThemeEditorEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, hasUnsavedChanges = true) }
    }

    fun setBackgroundUri(uri: String?) {
        _uiState.update { it.copy(pendingBackgroundUri = uri) }
    }

    fun applyPendingBackground(scale: Float, rotation: Float, offsetX: Float, offsetY: Float) {
        updateAppearance { appearance ->
            appearance.copy(
                backgroundUri = _uiState.value.pendingBackgroundUri,
                backgroundScale = scale,
                backgroundRotation = rotation,
                backgroundOffsetX = offsetX,
                backgroundOffsetY = offsetY
            )
        }
        _uiState.update { it.copy(pendingBackgroundUri = null, hasUnsavedChanges = true) }
    }

    fun cancelPendingBackground() {
        _uiState.update { it.copy(pendingBackgroundUri = null) }
    }

    fun setSolidBackgroundColor(color: Long) {
        updateAppearance { appearance ->
            appearance.copy(
                solidBackgroundColor = color,
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
        _uiState.update { it.copy(brushType = type, hasUnsavedChanges = true) }
    }

    fun setEraser(enabled: Boolean) {
        _uiState.update { it.copy(isEraser = enabled, hasUnsavedChanges = true) }
    }

    fun setTool(tool: ThemeEditorTool) {
        _uiState.update {
            it.copy(
                selectedTool = tool,
                lastEditingTool = if (tool == ThemeEditorTool.Preview) it.selectedTool else it.lastEditingTool
            )
        }
    }

    fun exitPreview() {
        _uiState.update { it.copy(selectedTool = it.lastEditingTool) }
    }

    fun addStroke(stroke: DrawStroke) {
        if (stroke.points.size < 2) return
        _uiState.update { it.copy(strokes = it.strokes + stroke, hasUnsavedChanges = true) }
    }

    fun undoStroke() {
        _uiState.update { it.copy(strokes = it.strokes.dropLast(1), hasUnsavedChanges = true) }
    }

    fun onBackRequested(onNavigateBack: () -> Unit) {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showDiscardDialog = true) }
        } else {
            onNavigateBack()
        }
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun saveTheme(bitmap: Bitmap) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val fileName = "custom_theme_${System.currentTimeMillis()}.png"
            runCatching {
                context.saveBitmapToInternalStorage(bitmap, fileName)
            }.mapCatching { previewPath ->
                themeRepository.createThemes(
                    listOf(
                        CreateThemePayload(
                            id = "custom_${UUID.randomUUID()}",
                            name = state.name.ifBlank { "My Custom Theme" },
                            price = 0,
                            thumbnailUrl = previewPath,
                            backgroundUrl = previewPath,
                            isOfficial = false,
                            isActive = true,
                            moods = state.lightAppearance.toMoodPayloads()
                        )
                    )
                ).getOrThrow()
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                _effect.emit(CustomThemeEditorEffect.Saved)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false) }
                _effect.emit(CustomThemeEditorEffect.Error(error.message ?: "Could not save custom theme"))
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
                iconUrl = iconColors.getOrElse(index) { primaryColor }.toColorHex(),
                customName = customName
            )
        }
    }

    private fun Long.toColorHex(): String = "#%08X".format(this)
}
