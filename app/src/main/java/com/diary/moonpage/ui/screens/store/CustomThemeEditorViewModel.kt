package com.diary.moonpage.ui.screens.store

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.saveBitmapToInternalStorage
import com.diary.moonpage.domain.repository.CustomThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

data class CustomThemeEditorUiState(
    val name: String = "My Custom Theme",
    val backgroundUri: String? = null,
    val pendingBackgroundUri: String? = null,
    val backgroundScale: Float = 1f,
    val backgroundRotation: Float = 0f,
    val backgroundOffsetX: Float = 0f,
    val backgroundOffsetY: Float = 0f,
    val solidBackgroundColor: Long = 0xFFFFF7EC,
    val primaryColor: Long = 0xFF8D6E63,
    val iconColor: Long = 0xFFEF9A9A,
    val iconColors: List<Long> = listOf(0xFFFFCA28, 0xFF81C784, 0xFF64B5F6, 0xFFBA68C8, 0xFF8D6E63),
    val selectedIconIndex: Int = 0,
    val colorFocusTarget: ColorFocusTarget = ColorFocusTarget.Primary,
    val brushColor: Long = 0xFF8D6E63,
    val brushSize: Float = 8f,
    val brushType: BrushType = BrushType.Fine,
    val isEraser: Boolean = false,
    val selectedTool: ThemeEditorTool = ThemeEditorTool.Background,
    val strokes: List<DrawStroke> = emptyList(),
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

sealed class CustomThemeEditorEffect {
    object Saved : CustomThemeEditorEffect()
    data class Error(val message: String) : CustomThemeEditorEffect()
}

@HiltViewModel
class CustomThemeEditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customThemeRepository: CustomThemeRepository
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
        _uiState.update {
            it.copy(
                backgroundUri = it.pendingBackgroundUri,
                pendingBackgroundUri = null,
                backgroundScale = scale,
                backgroundRotation = rotation,
                backgroundOffsetX = offsetX,
                backgroundOffsetY = offsetY,
                hasUnsavedChanges = true
            )
        }
    }

    fun cancelPendingBackground() {
        _uiState.update { it.copy(pendingBackgroundUri = null) }
    }

    fun setSolidBackgroundColor(color: Long) {
        _uiState.update { it.copy(solidBackgroundColor = color, backgroundUri = null, hasUnsavedChanges = true) }
    }

    fun setPrimaryColor(color: Long) {
        _uiState.update { it.copy(primaryColor = color, colorFocusTarget = ColorFocusTarget.Primary, hasUnsavedChanges = true) }
    }

    fun setIconColor(color: Long) {
        _uiState.update { state ->
            val updated = state.iconColors.toMutableList().also { colors ->
                colors[state.selectedIconIndex] = color
            }
            state.copy(iconColor = color, iconColors = updated, colorFocusTarget = ColorFocusTarget.Icon, hasUnsavedChanges = true)
        }
    }

    fun selectIcon(index: Int) {
        _uiState.update {
            val safeIndex = index.coerceIn(0, it.iconColors.lastIndex)
            it.copy(
                selectedIconIndex = safeIndex,
                iconColor = it.iconColors[safeIndex],
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
        _uiState.update { it.copy(selectedTool = tool) }
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
            }.mapCatching { path ->
                customThemeRepository.saveCustomTheme(
                    name = state.name.ifBlank { "My Custom Theme" },
                    bgFilePath = path,
                    primaryColor = state.primaryColor.toColorHex(),
                    iconColor = state.iconColor.toColorHex(),
                    iconColors = state.iconColors.map { it.toColorHex() }
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

    private fun Long.toColorHex(): String {
        return "#%08X".format(this)
    }
}
