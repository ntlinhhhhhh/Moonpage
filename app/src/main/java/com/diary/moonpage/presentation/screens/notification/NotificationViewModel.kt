package com.diary.moonpage.presentation.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.data.remote.dto.notification.NotificationDto
import com.diary.moonpage.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val notificationBus: com.diary.moonpage.core.util.NotificationBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
        
        // Listen for new incoming notifications to refresh list automatically
        viewModelScope.launch {
            notificationBus.events.collect {
                // Short delay to give the backend time to process the createNotification call from FCM Service
                kotlinx.coroutines.delay(1000)
                loadNotifications()
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getNotifications()
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    android.util.Log.d("NotificationVM", "Loaded ${data.size} notifications")
                    _uiState.update { 
                        it.copy(
                            notifications = data,
                            isLoading = false,
                            error = null
                        ) 
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("NotificationVM", "Load failed: ${response.code()} - $errorBody")
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load notifications: ${response.code()}") }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Load error", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                val response = repository.markAsRead(id)
                if (response.isSuccessful) {
                    android.util.Log.d("NotificationVM", "Marked as read: $id")
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == id) it.copy(isRead = true) else it
                            }
                        )
                    }
                } else {
                    android.util.Log.e("NotificationVM", "Mark as read failed: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Mark as read error", e)
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteNotification(id)
                if (response.isSuccessful) {
                    android.util.Log.d("NotificationVM", "Deleted: $id")
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.filter { it.id != id }
                        )
                    }
                } else {
                    android.util.Log.e("NotificationVM", "Delete failed: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Delete error", e)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            try {
                val response = repository.deleteAllNotifications()
                if (response.isSuccessful) {
                    android.util.Log.d("NotificationVM", "Deleted all")
                    _uiState.update { it.copy(notifications = emptyList()) }
                } else {
                    android.util.Log.e("NotificationVM", "Delete all failed: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Delete all error", e)
            }
        }
    }

    fun sendTestPush() {
        viewModelScope.launch {
            try {
                android.util.Log.d("NotificationVM", "Requesting FCM token for test push...")
                val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                android.util.Log.d("NotificationVM", "FCM Token: $token")
                
                val request = com.diary.moonpage.data.remote.dto.notification.SendPushRequest(
                    token = token,
                    title = "MoonPage Test 🌙",
                    body = "This is a comprehensive test of your notification system! 🚀",
                    imageUrl = null
                )
                
                val response = repository.sendPushNotification(request)
                if (response.isSuccessful) {
                    android.util.Log.d("NotificationVM", "Test push sent successfully to backend")
                    showSnackbar("Test push sent!")
                } else {
                    val errorMsg = "Test push failed: ${response.code()}"
                    android.util.Log.e("NotificationVM", errorMsg)
                    _uiState.update { it.copy(error = errorMsg) }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Test push execution error", e)
                _uiState.update { it.copy(error = "Test push failed: ${e.message}") }
            }
        }
    }

    private fun showSnackbar(message: String) {
        // This could be linked to a global snackbar host if available
    }
}
