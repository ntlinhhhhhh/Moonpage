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
    DEFAULT, BLUSHING, KITTY, SPROUT, MIDNIGHT, SUNNY, SKY, FOREST, COFFEE, LEMON, CHERRY, LAVENDER, OCEAN
}

fun getThemeShades(themeType: MoonThemeType): List<Color> {
    return when (themeType) {
        MoonThemeType.BLUSHING -> listOf(
            Color(0xFFFF5C8D), Color(0xFFFF7FA3), Color(0xFFFFB2C1), Color(0xFFFFD1DA), Color(0xFFFFEBF0)
        )
        MoonThemeType.KITTY -> listOf(
            Color(0xFF6C7CFF), Color(0xFF8A9AFF), Color(0xFFB4C0FF), Color(0xFFD4DBFF), Color(0xFFECEFFF)
        )
        MoonThemeType.SPROUT -> listOf(
            Color(0xFF4CAF50), Color(0xFF66BB6A), Color(0xFF98EE99), Color(0xFFC8E6C9), Color(0xFFE8F5E9)
        )
        MoonThemeType.SUNNY -> listOf(
            Color(0xFFFFA000), Color(0xFFFFB300), Color(0xFFFFD54F), Color(0xFFFFECB3), Color(0xFFFFF8E1)
        )
        MoonThemeType.SKY -> listOf(
            Color(0xFF039BE5), Color(0xFF29B6F6), Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFE1F5FE)
        )
        MoonThemeType.FOREST -> listOf(
            Color(0xFF00897B), Color(0xFF26A69A), Color(0xFF80CBC4), Color(0xFFB2DFDB), Color(0xFFE0F2F1)
        )
        MoonThemeType.COFFEE -> listOf(
            Color(0xFF6D4C41), Color(0xFF8D6E63), Color(0xFFA1887F), Color(0xFFD7CCC8), Color(0xFFEFEBE9)
        )
        MoonThemeType.LEMON -> listOf(
            Color(0xFFAFB42B), Color(0xFFCDDC39), Color(0xFFE6EE9C), Color(0xFFF0F4C3), Color(0xFFF9FBE7)
        )
        MoonThemeType.CHERRY -> listOf(
            Color(0xFFE53935), Color(0xFFEF5350), Color(0xFFEF9A9A), Color(0xFFFFCDD2), Color(0xFFFFEBEE)
        )
        MoonThemeType.LAVENDER -> listOf(
            Color(0xFF8E24AA), Color(0xFFAB47BC), Color(0xFFCE93D8), Color(0xFFE1BEE7), Color(0xFFF3E5F5)
        )
        MoonThemeType.OCEAN -> listOf(
            Color(0xFF1E88E5), Color(0xFF42A5F5), Color(0xFF90CAF9), Color(0xFFBBDEFB), Color(0xFFE3F2FD)
        )
        MoonThemeType.MIDNIGHT -> listOf(
            Color(0xFFFBC02D), Color(0xFFFDD835), Color(0xFFFFEB3B), Color(0xFFFFF176), Color(0xFFFFF9C4)
        )
        else -> listOf( // Default Bean - Yellow Progression
            Color(0xFFFFB300), Color(0xFFFFC107), Color(0xFFFFD54F), Color(0xFFFFE082), Color(0xFFFFF9C4)
        )
    }
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
                primary = Color(0xFFB6E388),
                onPrimary = Color.Black,
                background = Color.Black,
                surface = Color(0xFF121212),
                surfaceVariant = Color(0xFF1E231E)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF66BB6A), // Vibrant cheerful Green
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFF1F8E9)
            )
        }
        MoonThemeType.BLUSHING -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFB7C5),
                onPrimary = Color(0xFF261B1C),
                background = Color(0xFF261B1C),
                surface = Color(0xFF3D2E2F)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFFF7FA3), // Vibrant bright Pink
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF0F3)
            )
        }
        MoonThemeType.KITTY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFB7C2FF),
                onPrimary = Color(0xFF1B1C26),
                background = Color(0xFF1B1C26),
                surface = Color(0xFF2E2F3D)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF8A9AFF), // Soft vibrant Blue/Lavender
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFF0F3FF)
            )
        }
        MoonThemeType.SUNNY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFD54F),
                onPrimary = Color(0xFF26221B),
                background = Color(0xFF26221B),
                surface = Color(0xFF3D362E)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFFFB300), // Vibrant Sunny Yellow
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF8E1)
            )
        }
        MoonThemeType.SKY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF81D4FA),
                onPrimary = Color(0xFF1B2426),
                background = Color(0xFF1B2426),
                surface = Color(0xFF2E383D)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF29B6F6), // Vibrant Sky Blue
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFE1F5FE)
            )
        }
        MoonThemeType.FOREST -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF80CBC4),
                onPrimary = Color(0xFF1B2625),
                background = Color(0xFF1B2625),
                surface = Color(0xFF2E3D3B)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF26A69A), // Deep vibrant Teal
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFE0F2F1)
            )
        }
        MoonThemeType.COFFEE -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD7CCC8),
                onPrimary = Color(0xFF26211F),
                background = Color(0xFF26211F),
                surface = Color(0xFF3D3330)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF8D6E63), // Warm Coffee Brown
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFEFEBE9)
            )
        }
        MoonThemeType.LEMON -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD4E157),
                onPrimary = Color(0xFF24261B),
                background = Color(0xFF24261B),
                surface = Color(0xFF363D2E)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFCDDC39), // Vibrant Lemon Green
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFF9FBE7)
            )
        }
        MoonThemeType.CHERRY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFEF9A9A),
                onPrimary = Color(0xFF261B1B),
                background = Color(0xFF261B1B),
                surface = Color(0xFF3D2E2E)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFEF5350), // Vibrant Cherry Red
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFEBEE)
            )
        }
        MoonThemeType.LAVENDER -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFCE93D8),
                onPrimary = Color(0xFF241B26),
                background = Color(0xFF241B26),
                surface = Color(0xFF382E3D)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFAB47BC), // Vibrant Lavender Purple
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFF3E5F5)
            )
        }
        MoonThemeType.OCEAN -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF90CAF9),
                onPrimary = Color(0xFF1B2126),
                background = Color(0xFF1B2126),
                surface = Color(0xFF2E343D)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF42A5F5), // Vibrant Ocean Blue
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFE3F2FD)
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
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFE0E2EA)
            )
        }
    }

    val customColors = if (darkTheme) {
        // Generic Dark Custom Colors (optimized for pop and readability)
        MoonCustomColors(
            logItemBg = Color(0xFF5D647A),      // Slightly deeper for unselected
            logItemSelect = Color(0xFFC0C7E1),  // Much brighter for selection
            logItemAccent = MoonLogItemAccentDark,
            logItemIconUnselected = Color(0xFFD0D6E0), // Visible light gray
            logCardBg = Color(0xFF494F63),      // Dark gray-blue card
            logCardOnBg = Color.White,          // White text
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
