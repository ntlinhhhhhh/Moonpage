package com.diary.moonpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.core.theme.MoonThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val themePreferencesManager: ThemePreferencesManager,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()

    fun setLocked(locked: Boolean) {
        _isAppLocked.value = locked
    }

    init {
        viewModelScope.launch {
            // Pre-fetch critical data
            userRepository.getCurrentUser()
            
            // Wait for both theme and dark mode to be loaded from DataStore
            // We combine them to ensure we have both before proceeding
            combine(
                themePreferencesManager.themeType,
                themePreferencesManager.isDarkMode
            ) { _, _ -> }.first()
            
            // Small additional delay to ensure the StateFlows have updated 
            // and the UI has collected the new values
            kotlinx.coroutines.delay(600)
            _isReady.value = true
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

    private val _snackbarMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
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
}
