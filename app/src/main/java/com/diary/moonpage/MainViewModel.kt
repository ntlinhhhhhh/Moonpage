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

    init {
        viewModelScope.launch {
            userRepository.getCurrentUser()
            val now = java.time.LocalDate.now()
            statisticsRepository.getStatisticsSummary(now.year, now.monthValue, true)
            
            // Wait for first emission of theme settings
            combine(themePreferencesManager.themeType, themePreferencesManager.isDarkMode) { _, _ -> true }
                .take(1)
                .collect {
                    kotlinx.coroutines.delay(200) // Small delay to prevent white flash
                    _isReady.value = true
                }
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
