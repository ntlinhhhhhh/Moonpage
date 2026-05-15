package com.diary.moonpage

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.data.remote.api.SpotifyApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themePreferencesManager: ThemePreferencesManager,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository,
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val tokenManager: TokenManager,
    private val spotifyApi: SpotifyApi,
    private val notificationRepository: com.diary.moonpage.domain.repository.NotificationRepository,
    private val reminderManager: com.diary.moonpage.core.util.ReminderManager,
    private val notificationBus: com.diary.moonpage.core.util.NotificationBus
) : ViewModel(), DefaultLifecycleObserver {

    private val _notifications = MutableStateFlow<List<com.diary.moonpage.data.remote.dto.notification.NotificationDto>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                val response = notificationRepository.getNotifications()
                if (response.isSuccessful) {
                    _notifications.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch notifications", e)
            }
        }
    }

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()

    fun setLocked(locked: Boolean) {
        _isAppLocked.value = locked
    }

    override fun onStop(owner: LifecycleOwner) {
        // When app goes to background, set locked to true if enabled
        viewModelScope.launch {
            if (settingsPreferencesManager.isPasscodeEnabled.first()) {
                setLocked(true)
            }
        }
    }

    init {
        createNotificationChannel()

        // Listen for in-app notifications
        viewModelScope.launch {
            notificationBus.events.collect { event ->
                showSnackbar("${event.title}: ${event.body}")
                fetchNotifications() // Refresh list if needed
            }
        }

        viewModelScope.launch {
            // Log FCM Token for debugging
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM_TOKEN", "=================================")
                    Log.d("FCM_TOKEN", "MÃ TOKEN CỦA MÁY NÀY LÀ:")
                    Log.d("FCM_TOKEN", token)
                    Log.d("FCM_TOKEN", "=================================")
                }
            }

            // Pre-fetch critical data
            userRepository.getCurrentUser()
            
            // Wait for both theme and dark mode to be loaded from DataStore
            combine(
                themePreferencesManager.themeType,
                themePreferencesManager.isDarkMode
            ) { _, _ -> }.first()

            // Lock app on start if enabled
            if (settingsPreferencesManager.isPasscodeEnabled.first()) {
                setLocked(true)
            }

            // Initialize daily reminder
            if (settingsPreferencesManager.isReminderEnabled.first()) {
                val timeStr = settingsPreferencesManager.reminderTime.first()
                val time = timeStr.split(":")
                if (time.size == 2) {
                    reminderManager.scheduleDailyReminder(time[0].toInt(), time[1].toInt())
                }
            }
            
            kotlinx.coroutines.delay(600)
            _isReady.value = true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Moonpage Notifications"
            val descriptionText = "Daily reminders and system notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("moonpage_notification_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    val themeType: StateFlow<MoonThemeType> = themePreferencesManager.themeType
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MoonThemeType.DEFAULT
        )

    val isDarkMode: StateFlow<Boolean?> = themePreferencesManager.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    fun setTheme(theme: MoonThemeType) {
        viewModelScope.launch {
            themePreferencesManager.setThemeType(theme)
        }
    }

    fun setDarkMode(isDark: Boolean?) {
        viewModelScope.launch {
            themePreferencesManager.setDarkMode(isDark)
        }
    }

    val language: Flow<String> = settingsPreferencesManager.language

    fun handleSpotifyIntent(uri: Uri?) {
        Log.d("SpotifyAuth", "handleSpotifyIntent called with URI: $uri")
        if (uri != null && uri.scheme == "moonpage") {
            if (uri.host == "spotify-callback") {
                val code = uri.getQueryParameter("code")
                val error = uri.getQueryParameter("error")
                
                if (error != null) {
                    showSnackbar("Spotify Error: $error")
                    return
                }

                if (code != null) {
                    viewModelScope.launch {
                        val verifier = tokenManager.getSpotifyVerifier()
                        if (verifier != null) {
                            try {
                                val response = spotifyApi.exchangeToken(
                                    clientId = SpotifyApi.CLIENT_ID,
                                    code = code,
                                    redirectUri = SpotifyApi.REDIRECT_URI,
                                    codeVerifier = verifier
                                )
                                if (response.isSuccessful && response.body() != null) {
                                    val body = response.body()!!
                                    tokenManager.saveSpotifyToken(
                                        token = "Bearer ${body.accessToken}",
                                        refreshToken = body.refreshToken,
                                        expiresIn = body.expiresIn
                                    )
                                    showSnackbar("Spotify linked successfully!")
                                } else {
                                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                    showSnackbar("Token exchange failed: $errorBody")
                                }
                            } catch (e: Exception) {
                                showSnackbar("API Error: ${e.message}")
                            }
                        } else {
                            showSnackbar("Error: Missing local verifier")
                        }
                    }
                } else {
                    showSnackbar("Error: No code received from Spotify")
                }
            } else {
                showSnackbar("Error: Unknown host ${uri.host}")
            }
        }
    }
}
