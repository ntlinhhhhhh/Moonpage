package com.diary.moonpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.ThemePreferencesManager
import com.diary.moonpage.presentation.theme.MoonThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val themePreferencesManager: ThemePreferencesManager,
    private val userRepository: com.diary.moonpage.domain.repository.UserRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            userRepository.getCurrentUser()
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
