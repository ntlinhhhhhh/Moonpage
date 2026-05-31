package com.diary.moonpage.core.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.Modifier
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.domain.model.toBackgroundBrush
import org.json.JSONObject
import java.io.File

private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : Modifier.Node() {}
    }
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}

val LocalLocale = staticCompositionLocalOf { "en" }

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
    val cancelBtnTextColor: Color,
    val bottomNavBg: Color,
    val bottomNavUnselected: Color,
    val isDark: Boolean
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
        cancelBtnTextColor = Color.Unspecified,
        bottomNavBg = Color.Unspecified,
        bottomNavUnselected = Color.Unspecified,
        isDark = false
    )
}

object MoonTheme {
    val customColors: MoonCustomColors
        @Composable
        get() = LocalMoonCustomColors.current
}

private data class VisualBackgroundProtection(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val logItemBg: Color,
    val logItemSelect: Color,
    val logItemAccent: Color,
    val logCardBg: Color,
    val popupBg: Color,
    val cancelButtonBg: Color,
    val cancelButtonText: Color,
    val bottomNavBg: Color,
    val bottomNavUnselected: Color
)

private fun visualBackgroundProtection(darkTheme: Boolean): VisualBackgroundProtection {
    return if (darkTheme) {
        val text = Color(0xFFF7F2EA)
        VisualBackgroundProtection(
            background = Color.Transparent,
            surface = Color(0xEE1E1E1E),
            surfaceVariant = Color(0xE62F2F2F),
            onBackground = text,
            onSurface = text,
            logItemBg = Color(0xE62B2B2B),
            logItemSelect = Color(0x4DFFFFFF),
            logItemAccent = Color(0xE63A3A3A),
            logCardBg = Color(0xEE1F1F1F),
            popupBg = Color(0xF2242424),
            cancelButtonBg = Color(0xE63A3A3A),
            cancelButtonText = Color(0xFFE6E0D8),
            bottomNavBg = Color(0xF0262626),
            bottomNavUnselected = Color(0xFFAAA39A)
        )
    } else {
        val text = Color(0xFF2E261F)
        VisualBackgroundProtection(
            background = Color.Transparent,
            surface = Color(0xF2FFFCF6),
            surfaceVariant = Color(0xEAF3EDE2),
            onBackground = text,
            onSurface = text,
            logItemBg = Color(0xEFFFFFFB),
            logItemSelect = Color(0x4D2E261F),
            logItemAccent = Color(0xE8E7DDD1),
            logCardBg = Color(0xF2FFFCF6),
            popupBg = Color(0xF7FFFCF6),
            cancelButtonBg = Color(0xEAF2ECE3),
            cancelButtonText = Color(0xFF4E443B),
            bottomNavBg = Color(0xF5FFFCF6),
            bottomNavUnselected = Color(0xFF7F756B)
        )
    }
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
    DEFAULT, BLUSHING, KITTY, SPROUT, MIDNIGHT, SUNNY, SKY, FOREST, COFFEE, LEMON, CHERRY, LAVENDER, OCEAN,
    NEBULA, MATCHA, SUNSET, GALAXY, AUTUMN,
    GRAY_BROWN, COOKIE_BATCH, HEART_FELT, WEATHER_CYCLE
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
        MoonThemeType.NEBULA -> listOf(
            Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFBA68C8), Color(0xFF9C27B0), Color(0xFF7B1FA2)
        )
        MoonThemeType.MATCHA -> listOf(
            Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7), Color(0xFF81C784), Color(0xFF66BB6A)
        )
        MoonThemeType.SUNSET -> listOf(
            Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFB74D), Color(0xFFFFA726), Color(0xFFFF9800)
        )
        MoonThemeType.GALAXY -> listOf(
            Color(0xFFE8EAF6), Color(0xFFC5CAE9), Color(0xFF9FA8DA), Color(0xFF7986CB), Color(0xFF5C6BC0)
        )
        MoonThemeType.AUTUMN -> listOf(
            Color(0xFFFBE9E7), Color(0xFFFFCCBC), Color(0xFFFFAB91), Color(0xFFE64A19), Color(0xFFBF360C)
        )
        MoonThemeType.GRAY_BROWN -> listOf( // Sophisticated neutral
            Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF5D4037)
        )
        MoonThemeType.COOKIE_BATCH -> listOf( // Delicious warm cookies
            Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFD54F), Color(0xFFFFA000), Color(0xFF8D6E63)
        )
        MoonThemeType.HEART_FELT -> listOf( // Sweet soft pinks
            Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFF06292), Color(0xFFE91E63), Color(0xFFAD1457)
        )
        MoonThemeType.WEATHER_CYCLE -> listOf( // Clean gray/blue weather
            Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF90A4AE), Color(0xFF607D8B), Color(0xFF455A64)
        )
        else -> listOf(
            Color(0xFFFFF2C2), Color(0xFFFFE18A), Color(0xFFFFC547), Color(0xFFDB9D1F), Color(0xFFA8730D)
        )
    }
}

@Composable
fun MoonPageTheme(
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    activeTheme: Theme? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val customTheme = activeTheme?.takeIf { it.isCustomTheme() }
    val appearanceTheme = activeTheme?.takeIf { it.hasThemeAppearance() } ?: customTheme
    val customThemePrimary = appearanceTheme?.customPrimaryColor(darkTheme)
    val customThemeBackground = appearanceTheme?.customBackgroundColor(darkTheme)
    val hasCustomBackgroundImage = activeTheme.hasCustomBackgroundImage()
    val hasCustomGradientBackground = activeTheme.hasCustomGradientBackground(darkTheme)
    val hasCustomVisualBackground = hasCustomBackgroundImage || hasCustomGradientBackground
    val visualProtection = visualBackgroundProtection(darkTheme).takeIf { hasCustomVisualBackground }

    val themePrimary = customThemePrimary ?: if (darkTheme) {
        when (themeType) {
            MoonThemeType.DEFAULT -> Color(0xFFE8D5C4) // More sophisticated cream/gold for dark
            MoonThemeType.SPROUT -> Color(0xFFB6E388)
            MoonThemeType.BLUSHING -> Color(0xFFECA79D)
            MoonThemeType.KITTY -> Color(0xFFB7C2FF)
            MoonThemeType.SUNNY -> Color(0xFFFFD54F)
            MoonThemeType.SKY -> Color(0xFF81D4FA)
            MoonThemeType.FOREST -> Color(0xFF80CBC4)
            MoonThemeType.COFFEE -> Color(0xFFD7CCC8)
            MoonThemeType.LEMON -> Color(0xFFD4E157)
            MoonThemeType.CHERRY -> Color(0xFFEF9A9A)
            MoonThemeType.LAVENDER -> Color(0xFFCE93D8)
            MoonThemeType.OCEAN -> Color(0xFF90CAF9)
            MoonThemeType.MIDNIGHT -> Color(0xFFF0E68C)
            MoonThemeType.NEBULA -> Color(0xFFBA68C8)
            MoonThemeType.MATCHA -> Color(0xFF81C784)
            MoonThemeType.SUNSET -> Color(0xFFFFB74D)
            MoonThemeType.GALAXY -> Color(0xFF7986CB)
            MoonThemeType.AUTUMN -> Color(0xFFD32F2F)
            MoonThemeType.GRAY_BROWN -> Color(0xFF8D6E63)
            MoonThemeType.COOKIE_BATCH -> Color(0xFFFFA000)
            MoonThemeType.HEART_FELT -> Color(0xFFE91E63)
            MoonThemeType.WEATHER_CYCLE -> Color(0xFF607D8B)
        }
    } else {
        when (themeType) {
            MoonThemeType.DEFAULT -> Color(0xFF8C7E6A) // Deep elegant taupe for light
            else -> MoonActionLight // Fallback to original
        }
    }

    val customThemeFallbackBackground = appearanceTheme?.let {
        if (darkTheme) {
            themePrimary.copy(alpha = 0.18f).compositeOver(MoonBgDark)
        } else {
            themePrimary.copy(alpha = 0.08f).compositeOver(MoonBgLight)
        }
    }

    val targetColorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = themePrimary,
            background = visualProtection?.background
                ?: customThemeBackground
                ?: customThemeFallbackBackground
                ?: MoonBgDark,
            onBackground = visualProtection?.onBackground ?: DarkColorScheme.onBackground,
            surface = visualProtection?.surface ?: DarkColorScheme.surface,
            onSurface = visualProtection?.onSurface ?: DarkColorScheme.onSurface,
            surfaceVariant = visualProtection?.surfaceVariant ?: DarkColorScheme.surfaceVariant
        )
    } else {
        when {
            appearanceTheme != null -> {
                LightColorScheme.copy(
                    primary = themePrimary,
                    background = visualProtection?.background
                        ?: customThemeBackground
                        ?: customThemeFallbackBackground
                        ?: MoonBgLight,
                    onBackground = visualProtection?.onBackground ?: LightColorScheme.onBackground,
                    surface = visualProtection?.surface ?: LightColorScheme.surface,
                    onSurface = visualProtection?.onSurface ?: LightColorScheme.onSurface,
                    surfaceVariant = visualProtection?.surfaceVariant ?: themePrimary.copy(alpha = 0.05f)
                )
            }
            themeType == MoonThemeType.DEFAULT -> LightColorScheme
            themeType == MoonThemeType.SPROUT -> lightColorScheme(
                primary = Color(0xFF66BB6A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF1F8E9)
            )
            themeType == MoonThemeType.BLUSHING -> lightColorScheme(
                primary = Color(0xFFD2847A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF0F3)
            )
            themeType == MoonThemeType.KITTY -> lightColorScheme(
                primary = Color(0xFF8A9AFF),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF0F3FF)
            )
            themeType == MoonThemeType.SUNNY -> lightColorScheme(
                primary = Color(0xFFFFB300),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF8E1)
            )
            themeType == MoonThemeType.SKY -> lightColorScheme(
                primary = Color(0xFF29B6F6),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE1F5FE)
            )
            themeType == MoonThemeType.FOREST -> lightColorScheme(
                primary = Color(0xFF26A69A),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE0F2F1)
            )
            themeType == MoonThemeType.COFFEE -> lightColorScheme(
                primary = Color(0xFF8D6E63),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFEFEBE9)
            )
            themeType == MoonThemeType.LEMON -> lightColorScheme(
                primary = Color(0xFFCDDC39),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF9FBE7)
            )
            themeType == MoonThemeType.CHERRY -> lightColorScheme(
                primary = Color(0xFFEF5350),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFEBEE)
            )
            themeType == MoonThemeType.LAVENDER -> lightColorScheme(
                primary = Color(0xFFAB47BC),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF3E5F5)
            )
            themeType == MoonThemeType.OCEAN -> lightColorScheme(
                primary = Color(0xFF42A5F5),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE3F2FD)
            )
            themeType == MoonThemeType.MIDNIGHT -> lightColorScheme(
                primary = Color(0xFF1A1B26),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE0E2EA)
            )
            themeType == MoonThemeType.NEBULA -> lightColorScheme(
                primary = Color(0xFF9C27B0),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFF3E5F5)
            )
            themeType == MoonThemeType.MATCHA -> lightColorScheme(
                primary = Color(0xFF4CAF50),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE8F5E9)
            )
            themeType == MoonThemeType.SUNSET -> lightColorScheme(
                primary = Color(0xFFFF9800),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF3E0)
            )
            themeType == MoonThemeType.GALAXY -> lightColorScheme(
                primary = Color(0xFF3F51B5),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFE8EAF6)
            )
            themeType == MoonThemeType.AUTUMN -> lightColorScheme(
                primary = Color(0xFFD32F2F),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFDF5E6)
            )
            themeType == MoonThemeType.GRAY_BROWN -> lightColorScheme(
                primary = Color(0xFF6D4C41),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFEFEBE9)
            )
            themeType == MoonThemeType.COOKIE_BATCH -> lightColorScheme(
                primary = Color(0xFF8D6E63),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFFF8E1)
            )
            themeType == MoonThemeType.HEART_FELT -> lightColorScheme(
                primary = Color(0xFFC2185B),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFFCE4EC)
            )
            themeType == MoonThemeType.WEATHER_CYCLE -> lightColorScheme(
                primary = Color(0xFF455A64),
                onPrimary = Color.White,
                background = MoonBgLight,
                surface = Color.White,
                surfaceVariant = Color(0xFFECEFF1)
            )
            else -> LightColorScheme
        }
    }
    val colorScheme = targetColorScheme

    val customColors = if (darkTheme) {
        MoonCustomColors(
            logItemBg = visualProtection?.logItemBg ?: Color(0xFF333333),
            logItemSelect = visualProtection?.logItemSelect ?: colorScheme.primary.copy(alpha = 0.25f),
            logItemAccent = visualProtection?.logItemAccent ?: Color(0xFF424242),
            logItemIconUnselected = Color(0xFF888888),
            logItemIconSelected = colorScheme.primary,
            logCardBg = visualProtection?.logCardBg ?: MoonCardBgDark,
            logCardOnBg = Color(0xFFF5F5F5),
            snackbarBg = Color(0xFFF5F5F5),
            snackbarOnBg = Color(0xFF1A1A1A),
            successColor = Color(0xFF81C784),
            warningColor = Color(0xFFFFD54F),
            errorColor = Color(0xFFE57373),
            popupBgColor = visualProtection?.popupBg ?: Color(0xFF262626),
            cancelBtnBgColor = visualProtection?.cancelButtonBg ?: Color(0xFF383838),
            cancelBtnTextColor = visualProtection?.cancelButtonText ?: Color(0xFFBDBDBD),
            bottomNavBg = visualProtection?.bottomNavBg ?: MoonBottomNavBgDark,
            bottomNavUnselected = visualProtection?.bottomNavUnselected ?: MoonUnselectedDark,
            isDark = true
        )
    } else {
        MoonCustomColors(
            logItemBg = visualProtection?.logItemBg ?: Color(0xFFF8F9FA),
            logItemSelect = visualProtection?.logItemSelect ?: colorScheme.primary.copy(alpha = 0.12f),
            logItemAccent = visualProtection?.logItemAccent ?: Color(0xFFE9ECEF),
            logItemIconUnselected = Color(0xFFADB5BD),
            logItemIconSelected = colorScheme.primary,
            logCardBg = visualProtection?.logCardBg ?: Color.White,
            logCardOnBg = Color(0xFF212529),
            snackbarBg = Color(0xFF343A40),
            snackbarOnBg = Color.White,
            successColor = Color(0xFF28A745),
            warningColor = Color(0xFFFFC107),
            errorColor = Color(0xFFDC3545),
            popupBgColor = visualProtection?.popupBg ?: Color.White,
            cancelBtnBgColor = visualProtection?.cancelButtonBg ?: Color(0xFFF1F3F5),
            cancelBtnTextColor = visualProtection?.cancelButtonText ?: Color(0xFF495057),
            bottomNavBg = visualProtection?.bottomNavBg ?: Color.White,
            bottomNavUnselected = visualProtection?.bottomNavUnselected ?: MoonUnselectedLight,
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            val statusBarBgColor = customThemeBackground
                ?: if (darkTheme) MoonBgDark else MoonBgLight
            val isBgDark = statusBarBgColor.luminance() < 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isBgDark
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    CompositionLocalProvider(
        LocalMoonCustomColors provides customColors,
        LocalLocale provides "en", // This should be updated in MainActivity to provide the real value
        LocalIndication provides NoIndication,
        LocalRippleConfiguration provides null
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

private fun Theme.isCustomTheme(): Boolean {
    return id.startsWith("custom_") ||
        decoration.equals("CUSTOM", ignoreCase = true) ||
        collection.equals("Custom Theme", ignoreCase = true)
}

private fun Theme.hasThemeAppearance(): Boolean {
    return primaryLightColor.toThemeColorOrNull() != null ||
        primaryDarkColor.toThemeColorOrNull() != null ||
        primaryColor.toThemeColorOrNull() != null ||
        description.appearanceObject("light") != null ||
        description.appearanceObject("dark") != null
}

private fun Theme.customPrimaryColor(darkTheme: Boolean): Color? {
    val mode = if (darkTheme) "dark" else "light"
    val preferredColor = if (darkTheme) primaryDarkColor else primaryLightColor
    return preferredColor?.toThemeColorOrNull()
        ?: description.appearanceColor(mode, "primaryColor") 
        ?: primaryColor.toThemeColorOrNull()
}

private fun Theme.customBackgroundColor(darkTheme: Boolean): Color? {
    val mode = if (darkTheme) "dark" else "light"
    return description.appearanceBackgroundColor(mode) ?: backgroundUrl.toThemeColorOrNull()
}

private fun Theme?.hasCustomBackgroundImage(): Boolean {
    val theme = this ?: return false
    return theme.backgroundUrl.isThemeAssetPath() ||
        theme.description.appearanceObject("light")?.optString("backgroundUri").isThemeAssetPath() ||
        theme.description.appearanceObject("dark")?.optString("backgroundUri").isThemeAssetPath()
}

private fun Theme?.hasCustomGradientBackground(darkTheme: Boolean): Boolean {
    val theme = this ?: return false
    val mode = if (darkTheme) "dark" else "light"
    return theme.description.appearanceObject(mode)
        ?.optString("backgroundFillMode")
        ?.equals("Gradient", ignoreCase = true) == true
}

private fun String?.appearanceBackgroundColor(mode: String): Color? {
    val appearance = appearanceObject(mode) ?: return null
    val fillMode = appearance.optString("backgroundFillMode", "Solid")
    val colorKey = if (fillMode.equals("Gradient", ignoreCase = true)) {
        "gradientStartColor"
    } else {
        "solidBackgroundColor"
    }
    return appearance.optString(colorKey).toThemeColorOrNull()
}

private fun String?.appearanceColor(mode: String, key: String): Color? {
    return appearanceObject(mode)?.optString(key).toThemeColorOrNull()
}

private fun String?.appearanceObject(mode: String): JSONObject? {
    if (isNullOrBlank()) return null
    return runCatching { JSONObject(this).optJSONObject(mode) }.getOrNull()
}

private fun String?.toThemeColorOrNull(): Color? {
    if (isNullOrBlank()) return null
    return runCatching {
        var hex = this.trim().replace("[", "").replace("]", "").replace("\"", "").replace("'", "").trim()
        if (hex.startsWith("0x", ignoreCase = true)) hex = hex.drop(2)
        if (hex.startsWith("#")) hex = hex.drop(1)
        
        val longColor = hex.toLongOrNull(16) ?: return@runCatching null
        if (hex.length == 6) {
            Color(longColor or 0xFF000000)
        } else {
            Color(longColor)
        }
    }.getOrNull()
}

private fun String?.isThemeAssetPath(): Boolean {
    if (this == null) return false
    val trimmed = this.trim()
    if (trimmed.isBlank() || trimmed.lowercase() == "null" || trimmed.lowercase() == "pending") {
        return false
    }
    return !trimmed.contains(",") && toThemeColorOrNull() == null
}

@Composable
fun MoonPageTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MoonPageTheme(
        themeType = MoonThemeType.DEFAULT,
        activeTheme = null,
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

var isThemeTestingMode = false

private fun String?.isLocalFilePath(): Boolean {
    if (this == null) return false
    val trimmed = this.trim()
    return trimmed.startsWith("/data/") || 
           trimmed.startsWith("/storage/") || 
           trimmed.startsWith("/sdcard/") || 
           trimmed.startsWith("file://")
}

fun Theme.previewBackgroundImagePath(isDark: Boolean): String? {
    if (isOfficial) return null

    val config = parsedConfig
    val networkFallback = backgroundUrl?.takeIf { 
        it.isNotBlank() && 
        it.lowercase() != "null" && 
        it.lowercase() != "pending" && 
        !it.isLocalFilePath() && 
        !it.contains(",") && 
        it.toThemeColorOrNull() == null
    }

    if (config != null) {
        val modeConfig = if (isDark) config.dark ?: config.light else config.light ?: config.dark
        if (modeConfig != null) {
            val fillMode = modeConfig.backgroundFillMode ?: ""
            val hasBgUri = !modeConfig.backgroundUri.isNullOrBlank()
            val isImageMode = fillMode.equals("Image", ignoreCase = true) 
                || fillMode.equals("background", ignoreCase = true)
                || (fillMode.isBlank() && hasBgUri)
            
            if (isImageMode) {
                val bgUrl = backgroundUrl
                if (!bgUrl.isNullOrBlank() && 
                    !bgUrl.equals("pending", ignoreCase = true) && 
                    !bgUrl.contains(",") && 
                    bgUrl.toThemeColorOrNull() == null
                ) {
                    if (bgUrl.isLocalFilePath()) {
                        if (isThemeTestingMode) return bgUrl
                        val file = File(bgUrl.removePrefix("file://"))
                        if (file.exists()) return bgUrl
                    } else {
                        return bgUrl
                    }
                }
                val path = modeConfig.backgroundUri
                if (path != null && path.isThemeAssetPath()) {
                    if (path.isLocalFilePath()) {
                        if (isThemeTestingMode) return path
                        val file = File(path.removePrefix("file://"))
                        if (file.exists()) return path
                    } else {
                        return path
                    }
                }
                if (networkFallback != null) return networkFallback
            } else if (fillMode.equals("Solid", ignoreCase = true) || fillMode.equals("Gradient", ignoreCase = true)) {
                return null
            }
        }
    }

    if (backgroundUrl.equals("pending", ignoreCase = true)) return null

    val bgUrl = backgroundUrl
    if (!bgUrl.isNullOrBlank() && 
        bgUrl.lowercase() != "null" && 
        !bgUrl.contains(",") && 
        bgUrl.toThemeColorOrNull() == null
    ) {
        if (bgUrl.isLocalFilePath()) {
            if (isThemeTestingMode) return bgUrl
            val file = File(bgUrl.removePrefix("file://"))
            if (file.exists()) return bgUrl
        } else {
            return bgUrl
        }
    }

    val modeConfig = if (isDark) config?.dark ?: config?.light else config?.light ?: config?.dark
    val path = modeConfig?.backgroundUri

    if (path != null && path.isThemeAssetPath()) {
        if (path.isLocalFilePath()) {
            if (isThemeTestingMode) return path
            val file = File(path.removePrefix("file://"))
            if (file.exists()) return path
        } else {
            return path
        }
    }

    return networkFallback
}

fun Theme.previewBackgroundBrush(isDark: Boolean): Brush? {
    if (previewBackgroundImagePath(isDark) != null) return null

    val config = parsedConfig
    if (config != null) {
        val modeConfig = if (isDark) config.dark ?: config.light else config.light ?: config.dark
        if (modeConfig != null) {
            val fillMode = modeConfig.backgroundFillMode ?: ""
            val hasBgUri = !modeConfig.backgroundUri.isNullOrBlank()
            val isImageMode = fillMode.equals("Image", ignoreCase = true) 
                || fillMode.equals("background", ignoreCase = true)
                || (fillMode.isBlank() && hasBgUri)
            
            if (isImageMode) {
                return null
            } else if (fillMode.equals("Solid", ignoreCase = true)) {
                val color = modeConfig.solidBackgroundColor.toThemeColorOrNull()
                if (color != null) return SolidColor(color)
            } else if (fillMode.equals("Gradient", ignoreCase = true)) {
                val startColor = modeConfig.gradientStartColor.toThemeColorOrNull()
                val endColor = modeConfig.gradientEndColor.toThemeColorOrNull()
                if (startColor != null && endColor != null) {
                    return Brush.verticalGradient(listOf(startColor, endColor))
                }
            }
        }
    }

    val bgUrl = backgroundUrl
    if (!bgUrl.isNullOrBlank() && !bgUrl.equals("pending", ignoreCase = true)) {
        if (bgUrl.contains(",")) {
            val parts = bgUrl.split(",").mapNotNull { it.trim().toThemeColorOrNull() }
            if (parts.size >= 2) return Brush.verticalGradient(parts)
        } else {
            val color = bgUrl.toThemeColorOrNull()
            if (color != null) return SolidColor(color)
        }
    }

    val preferred = if (isDark) resolvedBgDarkColor else resolvedBgLightColor
    val fallback = if (isDark) resolvedBgLightColor else resolvedBgDarkColor
    val colorStr = preferred ?: fallback
    if (!colorStr.isNullOrBlank()) {
        if (colorStr.contains(",")) {
            val parts = colorStr.split(",").mapNotNull { it.trim().toThemeColorOrNull() }
            if (parts.size >= 2) return Brush.verticalGradient(parts)
        } else {
            val color = colorStr.toThemeColorOrNull()
            if (color != null) return SolidColor(color)
        }
    }

    return null
}

fun parseThemeBrush(colorStr: String?): Brush? {
    if (colorStr.isNullOrBlank()) return null
    val parts = colorStr.split(",").mapNotNull { part ->
        part.trim().toThemeColorOrNull()
    }
    return when {
        parts.isEmpty() -> null
        parts.size == 1 -> SolidColor(parts[0])
        else -> Brush.verticalGradient(parts)
    }
}
