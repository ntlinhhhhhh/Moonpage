package com.diary.moonpage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.diary.moonpage.presentation.navigation.AppNavigation
import com.diary.moonpage.presentation.theme.MoonPageTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeType by mainViewModel.themeType.collectAsState()
            val isDarkModePref by mainViewModel.isDarkMode.collectAsState()
            val isDark = isDarkModePref ?: androidx.compose.foundation.isSystemInDarkTheme()

            MoonPageTheme(
                themeType = themeType,
                darkTheme = isDark
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
