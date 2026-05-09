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
    val logItemIconSelected: Color,
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
        logItemIconSelected = Color.Unspecified,
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
    onBackground = MoonTextDarkNew,
    surface = MoonSurfaceDark,
    onSurface = MoonTextDarkNew,
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

enum class MoonThemeType {
    DEFAULT, BLUSHING, KITTY, SPROUT, MIDNIGHT, SUNNY, SKY, FOREST, COFFEE, LEMON, CHERRY, LAVENDER, OCEAN
}

fun getThemeShades(themeType: MoonThemeType): List<Color> {
    return when (themeType) {
        MoonThemeType.BLUSHING -> listOf(
            Color(0xFFFFC3BB), Color(0xFFFF9F98), Color(0xFFF07063), Color(0xFFC24B42), Color(0xFFA03F38)
        )
        MoonThemeType.KITTY -> listOf(
            Color(0xFFD6DFFF), Color(0xFFB3C2FF), Color(0xFF7A93FF), Color(0xFF536FE6), Color(0xFF3B54BF)
        )
        MoonThemeType.SPROUT -> listOf(
            Color(0xFFD4F0D6), Color(0xFFAAE0AF), Color(0xFF6EC276), Color(0xFF489E50), Color(0xFF307A37)
        )
        MoonThemeType.SUNNY -> listOf(
            Color(0xFFFFE6C2), Color(0xFFFFCD8F), Color(0xFFFAAA4B), Color(0xFFD68322), Color(0xFFA86010)
        )
        MoonThemeType.SKY -> listOf(
            Color(0xFFD1F2FF), Color(0xFFA3E5FF), Color(0xFF5CCBFA), Color(0xFF34A6D6), Color(0xFF1E82AB)
        )
        MoonThemeType.FOREST -> listOf(
            Color(0xFFD1EBE8), Color(0xFFA8D9D4), Color(0xFF6BB5AE), Color(0xFF44948D), Color(0xFF2B736D)
        )
        MoonThemeType.COFFEE -> listOf(
            Color(0xFFEBE2DD), Color(0xFFD6C6BC), Color(0xFFA68D81), Color(0xFF826659), Color(0xFF61483D)
        )
        MoonThemeType.LEMON -> listOf(
            Color(0xFFF4FAD2), Color(0xFFE8F2A0), Color(0xFFC8D65A), Color(0xFFA1AF35), Color(0xFF7B8721)
        )
        MoonThemeType.CHERRY -> listOf(
            Color(0xFFFFD4D9), Color(0xFFFFA3AC), Color(0xFFEB606E), Color(0xFFC43543), Color(0xFF991D29)
        )
        MoonThemeType.LAVENDER -> listOf(
            Color(0xFFF2DFFF), Color(0xFFE0B8FF), Color(0xFFB570EB), Color(0xFF9147C9), Color(0xFF702C9E)
        )
        MoonThemeType.OCEAN -> listOf(
            Color(0xFFD6EBFF), Color(0xFFA8D3FF), Color(0xFF66AAEB), Color(0xFF4083C4), Color(0xFF26629E)
        )
        MoonThemeType.MIDNIGHT -> listOf(
            Color(0xFFFFF7D1), Color(0xFFF5E69A), Color(0xFFD4C059), Color(0xFFA89532), Color(0xFF806F18)
        )
        else -> listOf( // Default Bean - Yellow Progression
            Color(0xFFFFF2C2), Color(0xFFFFE18A), Color(0xFFFFC547), Color(0xFFDB9D1F), Color(0xFFA8730D)
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
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF66BB6A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF1F8E9)
            )
        }
        MoonThemeType.BLUSHING -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFECA79D),
                onPrimary = Color(0xFF261B1C),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFD2847A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF0F3)
            )
        }
        MoonThemeType.KITTY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFB7C2FF),
                onPrimary = Color(0xFF1B1C26),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF8A9AFF),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF0F3FF)
            )
        }
        MoonThemeType.SUNNY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFFD54F),
                onPrimary = Color(0xFF26221B),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFFFB300),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF8E1)
            )
        }
        MoonThemeType.SKY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF81D4FA),
                onPrimary = Color(0xFF1B2426),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF29B6F6),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE1F5FE)
            )
        }
        MoonThemeType.FOREST -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF80CBC4),
                onPrimary = Color(0xFF1B2625),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF26A69A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE0F2F1)
            )
        }
        MoonThemeType.COFFEE -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD7CCC8),
                onPrimary = Color(0xFF26211F),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF8D6E63),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFEFEBE9)
            )
        }
        MoonThemeType.LEMON -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD4E157),
                onPrimary = Color(0xFF24261B),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFCDDC39),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF9FBE7)
            )
        }
        MoonThemeType.CHERRY -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFEF9A9A),
                onPrimary = Color(0xFF261B1B),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFEF5350),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFEBEE)
            )
        }
        MoonThemeType.LAVENDER -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFCE93D8),
                onPrimary = Color(0xFF241B26),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFAB47BC),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF3E5F5)
            )
        }
        MoonThemeType.OCEAN -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF90CAF9),
                onPrimary = Color(0xFF1B2126),
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF42A5F5),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE3F2FD)
            )
        }
        MoonThemeType.MIDNIGHT -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFF0E68C),
                onPrimary = Color.Black,
                background = MoonBgDark,
                surface = MoonSurfaceDark,
                onBackground = MoonTextDarkNew,
                onSurface = MoonTextDarkNew
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF1A1B26),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE0E2EA)
            )
        }
    }

    val customColors = if (darkTheme) {
        MoonCustomColors(
            logItemBg = Color(0xFF404040),
            logItemSelect = colorScheme.primary.copy(alpha = 0.3f),
            logItemAccent = MoonLogItemAccentDark,
            logItemIconUnselected = Color(0xFFAEAEAE),
            logItemIconSelected = colorScheme.primary,
            logCardBg = Color(0xFF292929),
            logCardOnBg = Color(0xFFE4E4E4),
            snackbarBg = Color(0xFF161921),
            snackbarOnBg = MoonTextLight,
            successColor = Color(0xFF66BB6A),
            warningColor = Color(0xFFFFCA28),
            errorColor = Color(0xFFEF5350)
        )
    } else {
        MoonCustomColors(
            logItemBg = Color(0xFFF0F2F5),
            logItemSelect = colorScheme.primary.copy(alpha = 0.15f),
            logItemAccent = MoonLogItemAccentLight,
            logItemIconUnselected = MoonUnselectedLight.copy(alpha = 0.4f),
            logItemIconSelected = Color.Unspecified,
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
    MoonPageTheme(
        themeType = MoonThemeType.DEFAULT,
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
