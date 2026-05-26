package com.diary.moonpage.ui.components.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import com.diary.moonpage.core.util.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class SnackbarType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

class AppSnackbarVisuals(
    val uiText: UiText,
    val type: SnackbarType,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals {
    override val message: String = (uiText as? UiText.DynamicString)?.value.orEmpty()
}

data class AppSnackbarMessage(
    val uiText: UiText,
    val type: SnackbarType,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val withDismissAction: Boolean = false
)

object GlobalSnackbarManager {
    private val _messages = MutableSharedFlow<AppSnackbarMessage>(extraBufferCapacity = 16)
    val messages = _messages.asSharedFlow()

    suspend fun show(
        uiText: UiText,
        type: SnackbarType = SnackbarType.INFO,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        withDismissAction: Boolean = false
    ) {
        _messages.emit(
            AppSnackbarMessage(
                uiText = uiText,
                type = type,
                actionLabel = actionLabel,
                duration = duration,
                withDismissAction = withDismissAction
            )
        )
    }

    suspend fun show(
        message: String,
        type: SnackbarType = inferType(message),
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        withDismissAction: Boolean = false
    ) {
        show(
            uiText = UiText.DynamicString(message),
            type = type,
            actionLabel = actionLabel,
            duration = duration,
            withDismissAction = withDismissAction
        )
    }

    fun tryShow(
        message: String,
        type: SnackbarType = inferType(message),
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        withDismissAction: Boolean = false
    ) {
        _messages.tryEmit(
            AppSnackbarMessage(
                uiText = UiText.DynamicString(message),
                type = type,
                actionLabel = actionLabel,
                duration = duration,
                withDismissAction = withDismissAction
            )
        )
    }
}

fun inferType(message: String): SnackbarType {
    val lower = message.lowercase()
    return when {
        listOf(
            "failed", "fail", "error", "invalid", "unable", "couldn't", "cannot", "can't",
            "thất bại", "lỗi", "không thể", "không khả dụng"
        ).any { it in lower } ->
            SnackbarType.ERROR
        listOf(
            "please", "warning", "already", "permission", "required", "select",
            "vui lòng", "cảnh báo", "quyền", "bắt buộc", "chọn"
        ).any { it in lower } ->
            SnackbarType.WARNING
        listOf(
            "success", "saved", "deleted", "updated", "created", "recorded", "activated", "sent", "linked", "uploaded",
            "thành công", "đã lưu", "đã xóa", "đã cập nhật", "đã tạo", "đã kích hoạt", "đã gửi", "đã nhập", "đã liên kết"
        ).any { it in lower } ->
            SnackbarType.SUCCESS
        else -> SnackbarType.INFO
    }
}
