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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.core.theme.parseThemeBrush
import com.diary.moonpage.core.theme.previewBackgroundImagePath
import com.diary.moonpage.core.theme.previewBackgroundBrush
import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.ui.components.feedback.AppSnackbarVisuals
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.ui.navigation.AppNavigation
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

private const val AppSnackbarDisplayMillis = 2000L

/**
 * Stateful Component
 */
@Composable
fun MoonPageApp(
    viewModel: MainViewModel,
    widgetTargetRoute: String? = null,
    onWidgetTargetRouteConsumed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = uiState.isDarkMode ?: isSystemInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        GlobalSnackbarManager.messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            val snackbarJob = launch {
                snackbarHostState.showSnackbar(
                    AppSnackbarVisuals(
                        uiText = message.uiText,
                        type = message.type,
                        actionLabel = message.actionLabel,
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = message.withDismissAction
                    )
                )
            }
            val timeoutJob = launch {
                delay(AppSnackbarDisplayMillis)
                snackbarHostState.currentSnackbarData?.dismiss()
            }
            snackbarJob.join()
            timeoutJob.cancel()
        }
    }
    
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            GlobalSnackbarManager.show(it, uiState.snackbarType)
            viewModel.dismissSnackbar()
        }
    }

    MoonPageAppContent(
        uiState = uiState,
        isDark = isDark,
        snackbarHostState = snackbarHostState,
        widgetTargetRoute = widgetTargetRoute,
        onWidgetTargetRouteConsumed = onWidgetTargetRouteConsumed
    )
}

/**
 * Stateless Component
 */
@Composable
fun MoonPageAppContent(
    uiState: MainUiState,
    isDark: Boolean,
    snackbarHostState: SnackbarHostState,
    widgetTargetRoute: String? = null,
    onWidgetTargetRouteConsumed: () -> Unit = {}
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
        val customBackgroundModel = remember(uiState.activeTheme, isDark) {
            uiState.activeTheme.customBackgroundModel(isDark)
        }
        val customBackgroundBrush = remember(uiState.activeTheme, isDark) {
            uiState.activeTheme.customBackgroundBrush(isDark)
        }
        val customImageScrim = remember(uiState.activeTheme, isDark) {
            uiState.activeTheme.customImageScrim(isDark)
        }
        val hasCustomVisualBackground = customBackgroundModel != null || customBackgroundBrush != null
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
                } else if (customBackgroundBrush != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(customBackgroundBrush)
                    )
                }
                customImageScrim?.let { scrim ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(scrim)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (hasCustomVisualBackground) Color.Transparent else MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isReady) {
                            AppNavigation(
                                widgetTargetRoute = widgetTargetRoute,
                                onWidgetTargetRouteConsumed = onWidgetTargetRouteConsumed
                            )
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

private fun Theme?.customBackgroundModel(isDark: Boolean): Any? {
    val theme = this ?: return null
    val path = theme.previewBackgroundImagePath(isDark) ?: return null
    val normalized = normalizeAppImageUrl(path) ?: return null
    if (normalized.startsWith("file://")) {
        val file = File(normalized.removePrefix("file://"))
        if (file.exists()) return file
    }
    return normalized
}

private fun Theme?.customBackgroundBrush(isDark: Boolean): Brush? {
    val theme = this ?: return null
    if (theme.previewBackgroundImagePath(isDark) != null) return null
    return theme.previewBackgroundBrush(isDark)
}

private fun Theme?.customImageScrim(isDark: Boolean): Color? {
    val theme = this ?: return null
    if (theme.previewBackgroundImagePath(isDark) == null) return null
    return if (isDark) {
        Color.Black.copy(alpha = 0.58f)
    } else {
        Color.White.copy(alpha = 0.50f)
    }
}
