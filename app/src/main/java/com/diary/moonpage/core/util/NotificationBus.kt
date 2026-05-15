package com.diary.moonpage.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationBus @Inject constructor() {
    private val _events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    suspend fun postEvent(title: String, body: String, type: String? = null, targetId: String? = null) {
        _events.emit(NotificationEvent(title, body, type, targetId))
    }
}

data class NotificationEvent(
    val title: String,
    val body: String,
    val type: String? = null,
    val targetId: String? = null
)
