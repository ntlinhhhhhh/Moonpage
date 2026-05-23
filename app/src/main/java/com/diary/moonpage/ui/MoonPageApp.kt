package com.diary.moonpage.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.ui.navigation.AppNavigation
import coil.compose.AsyncImage
import org.json.JSONObject
import java.io.File

/**
 * Stateful Component
 */
@Composable
fun MoonPageApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = uiState.isDarkMode ?: isSystemInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    MoonPageAppContent(
        uiState = uiState,
        isDark = isDark,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Stateless Component
 */
@Composable
fun MoonPageAppContent(
    uiState: MainUiState,
    isDark: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    MoonPageTheme(
        themeType = uiState.themeType,
        activeTheme = uiState.activeTheme,
        darkTheme = isDark
    ) {
        val customBackgroundModel = remember(uiState.activeTheme) {
            uiState.activeTheme.customBackgroundModel()
        }
        val hasCustomImageBackground = remember(uiState.activeTheme) {
            uiState.activeTheme.hasCustomImageBackground()
        }
        CompositionLocalProvider(
            com.diary.moonpage.core.theme.LocalLocale provides uiState.language
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (customBackgroundModel != null) {
                    AsyncImage(
                        model = customBackgroundModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isDark && hasCustomImageBackground) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.32f))
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isReady) {
                            AppNavigation()
                        }

                        MoonSnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}

private fun Theme?.customBackgroundModel(): Any? {
    val theme = this ?: return null
    if (!theme.isCustomTheme()) return null
    val path = theme.thumbnailUrl?.takeIf { it.isNotBlank() && !it.isThemeColor() } ?: return null
    val file = File(path)
    return if (file.exists()) file else path
}

private fun Theme?.hasCustomImageBackground(): Boolean {
    val theme = this ?: return false
    if (!theme.isCustomTheme()) return false
    return theme.description.appearanceObject("light")?.optString("backgroundUri").isThemeAssetPath() ||
        theme.description.appearanceObject("dark")?.optString("backgroundUri").isThemeAssetPath()
}

private fun Theme.isCustomTheme(): Boolean {
    return id.startsWith("custom_") ||
        decoration.equals("CUSTOM", ignoreCase = true) ||
        collection.equals("Custom Theme", ignoreCase = true)
}

private fun String.isThemeColor(): Boolean {
    val value = when {
        startsWith("#") -> drop(1)
        startsWith("0x", ignoreCase = true) -> drop(2)
        else -> this
    }
    return (value.length == 6 || value.length == 8) && value.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
}

private fun String?.appearanceObject(mode: String): JSONObject? {
    if (isNullOrBlank()) return null
    return runCatching { JSONObject(this).optJSONObject(mode) }.getOrNull()
}

private fun String?.isThemeAssetPath(): Boolean {
    return !isNullOrBlank() && !isThemeColor()
}
