package com.diary.moonpage.ui

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
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.SettingsPreferencesManager
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.data.remote.api.SpotifyApi
import com.diary.moonpage.ui.components.feedback.SnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themePreferencesManager: ThemePreferencesManager,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository,
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val tokenManager: TokenManager,
    private val spotifyApi: SpotifyApi,
    private val notificationRepository: com.diary.moonpage.domain.repository.NotificationRepository,
    private val reminderManager: com.diary.moonpage.core.util.ReminderManager,
    private val notificationBus: com.diary.moonpage.core.util.NotificationBus
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                val response = notificationRepository.getNotifications()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(notifications = response.body()?.data ?: emptyList()) }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch notifications", e)
            }
        }
    }

    fun setLocked(locked: Boolean) {
        _uiState.update { it.copy(isAppLocked = locked) }
    }

    override fun onStop(owner: LifecycleOwner) {
        viewModelScope.launch {
            if (settingsPreferencesManager.isPasscodeEnabled.first()) {
                setLocked(true)
            }
        }
    }

    init {
        createNotificationChannel()

        viewModelScope.launch {
            notificationBus.events.collect { event ->
                showSnackbar(UiText.DynamicString("${event.title}: ${event.body}"))
                fetchNotifications()
            }
        }

        viewModelScope.launch {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM_TOKEN", "MÃ TOKEN CỦA MÁY NÀY LÀ: ${task.result}")
                }
            }

            userRepository.getCurrentUser()
            
            combine(
                themePreferencesManager.themeType,
                themePreferencesManager.isDarkMode,
                settingsPreferencesManager.language,
                themeRepository.activeTheme
            ) { themeType, isDarkMode, language, activeTheme ->
                _uiState.update { it.copy(
                    themeType = themeType,
                    isDarkMode = isDarkMode,
                    language = language,
                    activeTheme = activeTheme
                ) }
            }.onStart {
                // Ensure we wait for the first emission before declaring ready
            }.onEach {
                if (!_uiState.value.isReady) {
                    delay(400)
                    _uiState.update { it.copy(isReady = true) }
                }
            }.launchIn(viewModelScope)

            if (settingsPreferencesManager.isPasscodeEnabled.first()) {
                setLocked(true)
            }

            if (settingsPreferencesManager.isReminderEnabled.first()) {
                val timeStr = settingsPreferencesManager.reminderTime.first()
                val time = timeStr.split(":")
                if (time.size == 2) {
                    reminderManager.scheduleDailyReminder(time[0].toInt(), time[1].toInt())
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val channel = NotificationChannel("moonpage_notification_channel", name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSnackbar(message: UiText, type: SnackbarType = SnackbarType.INFO) {
        _uiState.update { it.copy(snackbarMessage = message, snackbarType = type) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
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

    fun handleSpotifyIntent(uri: Uri?) {
        if (uri != null && uri.scheme == "moonpage" && uri.host == "spotify-callback") {
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            
            if (error != null) {
                showSnackbar(UiText.StringResource(R.string.spotify_error, error), SnackbarType.ERROR)
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
                                tokenManager.saveSpotifyToken(body.accessToken, body.refreshToken, body.expiresIn)
                                showSnackbar(UiText.StringResource(R.string.spotify_linked_success), SnackbarType.SUCCESS)
                            } else {
                                showSnackbar(
                                    UiText.StringResource(
                                        R.string.token_exchange_failed,
                                        response.errorBody()?.string() ?: ""
                                    ),
                                    SnackbarType.ERROR
                                )
                            }
                        } catch (e: Exception) {
                            showSnackbar(
                                UiText.StringResource(R.string.api_error, e.message ?: ""),
                                SnackbarType.ERROR
                            )
                        }
                    } else {
                        showSnackbar(UiText.StringResource(R.string.missing_local_verifier), SnackbarType.ERROR)
                    }
                }
            }
        }
    }

    // For backward compatibility during refactoring
    val isAppLocked: StateFlow<Boolean> = _uiState.map { it.isAppLocked }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
