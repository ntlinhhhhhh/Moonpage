package com.diary.moonpage.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

@Immutable
data class MoonCustomColors(
    val logItemBg: Color,
    val logItemSelect: Color,
    val logItemAccent: Color,
    val logItemIconUnselected: Color,
    val logCardBg: Color,
    val logCardOnBg: Color,
    val snackbarBg: Color,
    val snackbarOnBg: Color,
    val successColor: Color,
    val warningColor: Color,
    val errorColor: Color
)

val LocalMoonCustomColors = staticCompositionLocalOf {
    MoonCustomColors(
        logItemBg = Color.Unspecified,
        logItemSelect = Color.Unspecified,
        logItemAccent = Color.Unspecified,
        logItemIconUnselected = Color.Unspecified,
        logCardBg = Color.Unspecified,
        logCardOnBg = Color.Unspecified,
        snackbarBg = Color.Unspecified,
        snackbarOnBg = Color.Unspecified,
        successColor = Color.Unspecified,
        warningColor = Color.Unspecified,
        errorColor = Color.Unspecified
    )
}

object MoonTheme {
    val customColors: MoonCustomColors
        @Composable
        get() = LocalMoonCustomColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = MoonActionDark,
    onPrimary = MoonTextDark,
    background = MoonBgDark,
    onBackground = MoonTextLight,
    surface = MoonSurfaceDark,
    onSurface = MoonTextLight,
    surfaceVariant = MoonInputBgDark,
    tertiary = MoonLinkDark,
    error = MoonErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = MoonActionLight,
    onPrimary = Color.White,
    background = MoonBgLight,
    onBackground = MoonTextDark,
    surface = Color.White,
    onSurface = MoonTextDark,
    surfaceVariant = MoonInputBgLight,
    tertiary = MoonLinkLight,
    error = MoonErrorLight
)

private val GreenColorScheme = lightColorScheme(
    primary = MoonGreenPrimary,
    onPrimary = Color.White,
    background = MoonGreenBg,
    onBackground = MoonGreenTextPrimary,
    surface = Color.White,
    onSurface = MoonGreenTextPrimary,
    surfaceVariant = MoonGreenSurface,
    secondary = MoonGreenSecondary,
    tertiary = MoonGreenTertiary,
    outline = MoonGreenTextSecondary,
    error = MoonErrorLight
)

enum class MoonThemeType {
    DEFAULT, BLUSHING, KITTY, SPROUT, MIDNIGHT
}

@Composable
fun MoonPageTheme(
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        MoonThemeType.DEFAULT -> if (darkTheme) DarkColorScheme else LightColorScheme
        MoonThemeType.SPROUT -> if (darkTheme) {
            darkColorScheme(
                primary = MoonGreenPrimary,
                onPrimary = Color.Black,
                background = Color(0xFF1B261B),
                surface = Color(0xFF2E3D2E)
            )
        } else GreenColorScheme
        MoonThemeType.BLUSHING -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFDDE1),
                onPrimary = Color.Black,
                background = Color(0xFF261B1C),
                surface = Color(0xFF3D2E2F)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFFFB7C5),
                onPrimary = Color.White,
                background = Color(0xFFFFF0F3),
                surface = Color.White
            )
        }
        MoonThemeType.KITTY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD1D9FF),
                onPrimary = Color.Black,
                background = Color(0xFF1B1C26),
                surface = Color(0xFF2E2F3D)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFB7C2FF),
                onPrimary = Color.White,
                background = Color(0xFFF0F3FF),
                surface = Color.White
            )
        }
        MoonThemeType.MIDNIGHT -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFF0E68C),
                onPrimary = Color.Black,
                background = Color(0xFF1A1B26),
                surface = Color(0xFF24283B)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF1A1B26),
                onPrimary = Color.White,
                background = Color(0xFFE0E2EA),
                surface = Color.White
            )
        }
    }

    val customColors = if (darkTheme) {
        // Generic Dark Custom Colors for all themes (can be refined per theme if needed)
        MoonCustomColors(
            logItemBg = Color(0xFF888888),
            logItemSelect = Color(0xFF666666),
            logItemAccent = MoonLogItemAccentDark,
            logItemIconUnselected = Color(0xFF333333),
            logCardBg = Color(0xFFAAAAAA),
            logCardOnBg = MoonTextDark,
            snackbarBg = Color(0xFF161921),
            snackbarOnBg = MoonTextLight,
            successColor = Color(0xFF66BB6A),
            warningColor = Color(0xFFFFCA28),
            errorColor = Color(0xFFEF5350)
        )
    } else {
        // Generic Light Custom Colors for all themes
        MoonCustomColors(
            logItemBg = MoonLogItemBgLight,
            logItemSelect = MoonLogItemSelectLight,
            logItemAccent = MoonLogItemAccentLight,
            logItemIconUnselected = Color.Gray,
            logCardBg = Color.White,
            logCardOnBg = MoonTextDark,
            snackbarBg = Color(0xFFE8E1DA),
            snackbarOnBg = MoonTextDark,
            successColor = Color(0xFF4CAF50),
            warningColor = Color(0xFFFFC107),
            errorColor = Color(0xFFF44336)
        )
    }

    CompositionLocalProvider(
        LocalMoonCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MoonPageTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Keep this for backward compatibility if needed, but point to DEFAULT
    MoonPageTheme(
        themeType = MoonThemeType.DEFAULT,
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
