package com.diary.moonpage.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

@Immutable
data class MoonCustomColors(
    val logItemBg: Color,
    val logItemSelect: Color
)

val LocalMoonCustomColors = staticCompositionLocalOf {
    MoonCustomColors(
        logItemBg = Color.Unspecified,
        logItemSelect = Color.Unspecified
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
    LIGHT, DARK, GREEN
}

@Composable
fun MoonPageTheme(
    themeType: MoonThemeType = if (isSystemInDarkTheme()) MoonThemeType.DARK else MoonThemeType.LIGHT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (themeType == MoonThemeType.DARK) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeType == MoonThemeType.DARK -> DarkColorScheme
        themeType == MoonThemeType.GREEN -> GreenColorScheme
        else -> LightColorScheme
    }

    val customColors = when (themeType) {
        MoonThemeType.DARK -> MoonCustomColors(
            logItemBg = MoonLogItemBgDark,
            logItemSelect = MoonLogItemSelectDark
        )
        MoonThemeType.GREEN -> MoonCustomColors(
            logItemBg = MoonGreenSurface,
            logItemSelect = MoonGreenSecondary.copy(alpha = 0.3f)
        )
        else -> MoonCustomColors(
            logItemBg = MoonLogItemBgLight,
            logItemSelect = MoonLogItemSelectLight
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

/**
 * Legacy support for the old boolean parameter
 */
@Composable
fun MoonPageTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MoonPageTheme(
        themeType = if (darkTheme) MoonThemeType.DARK else MoonThemeType.LIGHT,
        dynamicColor = dynamicColor,
        content = content
    )
}
