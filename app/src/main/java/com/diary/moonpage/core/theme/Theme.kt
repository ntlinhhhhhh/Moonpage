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
    val errorColor: Color,
    val popupBgColor: Color,
    val cancelBtnBgColor: Color,
    val cancelBtnTextColor: Color
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
        errorColor = Color.Unspecified,
        popupBgColor = Color.Unspecified,
        cancelBtnBgColor = Color.Unspecified,
        cancelBtnTextColor = Color.Unspecified
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
    val colorScheme = if (darkTheme) {
        DarkColorScheme 
    } else {
        when (themeType) {
            MoonThemeType.DEFAULT -> LightColorScheme
            MoonThemeType.SPROUT -> lightColorScheme(
                primary = Color(0xFF66BB6A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF1F8E9)
            )
            MoonThemeType.BLUSHING -> lightColorScheme(
                primary = Color(0xFFD2847A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF0F3)
            )
            MoonThemeType.KITTY -> lightColorScheme(
                primary = Color(0xFF8A9AFF),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF0F3FF)
            )
            MoonThemeType.SUNNY -> lightColorScheme(
                primary = Color(0xFFFFB300),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF8E1)
            )
            MoonThemeType.SKY -> lightColorScheme(
                primary = Color(0xFF29B6F6),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE1F5FE)
            )
            MoonThemeType.FOREST -> lightColorScheme(
                primary = Color(0xFF26A69A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE0F2F1)
            )
            MoonThemeType.COFFEE -> lightColorScheme(
                primary = Color(0xFF8D6E63),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFEFEBE9)
            )
            MoonThemeType.LEMON -> lightColorScheme(
                primary = Color(0xFFCDDC39),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF9FBE7)
            )
            MoonThemeType.CHERRY -> lightColorScheme(
                primary = Color(0xFFEF5350),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFEBEE)
            )
            MoonThemeType.LAVENDER -> lightColorScheme(
                primary = Color(0xFFAB47BC),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF3E5F5)
            )
            MoonThemeType.OCEAN -> lightColorScheme(
                primary = Color(0xFF42A5F5),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE3F2FD)
            )
            MoonThemeType.MIDNIGHT -> lightColorScheme(
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
            errorColor = Color(0xFFEF5350),
            popupBgColor = Color(0xFF2C2C2C),
            cancelBtnBgColor = Color(0xFF454545),
            cancelBtnTextColor = Color(0xFFE0E0E0)
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
            errorColor = Color(0xFFF44336),
            popupBgColor = Color.White,
            cancelBtnBgColor = Color(0xFFEBEBEB), // Slightly darker than White (0xFFF2F2F2 was too light)
            cancelBtnTextColor = Color(0xFF616161) // Slightly darker than 0xFF757575
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
