package com.diary.moonpage.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.diary.moonpage.MainViewModel
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.presentation.navigation.AppNavigation

@Composable
fun MoonPageApp(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val isReady by mainViewModel.isReady.collectAsState()
    val themeType by mainViewModel.themeType.collectAsState()
    val isDarkModePref by mainViewModel.isDarkMode.collectAsState()
    val isDark = isDarkModePref ?: isSystemInDarkTheme()
    val snackbarMessage by mainViewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val language by mainViewModel.language.collectAsState(initial = "en")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle result if needed
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.dismissSnackbar()
        }
    }

    MoonPageTheme(
        themeType = themeType,
        darkTheme = isDark
    ) {
        CompositionLocalProvider(
            com.diary.moonpage.core.theme.LocalLocale provides language
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isReady) {
                        AppNavigation()
                    }
                    
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
