package com.diary.moonpage.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.ui.MainViewModel
import com.diary.moonpage.core.util.ThemeConstants
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.domain.model.Theme
import com.diary.moonpage.ui.screens.store.components.ConfirmActivationDialog
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.ui.components.feedback.SnackbarType
import kotlinx.coroutines.launch
import org.json.JSONObject

private data class ThemePickerItem(
    val id: String,
    val type: MoonThemeType?,
    val name: String,
    val color: Color,
    val isCustom: Boolean = false
)

/**
 * Stateful Route Component
 */
@Composable
fun ThemeCalendarRoute(
    onNavigateBack: () -> Unit,
    onActivated: () -> Unit = onNavigateBack,
    mainViewModel: MainViewModel = hiltViewModel(androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity),
    storeViewModel: com.diary.moonpage.ui.screens.store.StoreViewModel = hiltViewModel()
) {
    val uiState by storeViewModel.uiState.collectAsState()
    val mainUiState by mainViewModel.uiState.collectAsState()
    val currentThemeType = mainUiState.themeType
    val currentThemeId = mainUiState.activeTheme?.id
    val isDarkModePref = mainUiState.isDarkMode
    val defaultThemeName = stringResource(R.string.theme_calendar_classic_yellow)
    val currentSelectionKey = currentThemeId ?: currentThemeType.name

    // Track initial values to enable/disable Done button
    val initialSelectionKey = remember { currentSelectionKey }
    val initialDarkMode = remember { isDarkModePref }
    val hasChanges = currentSelectionKey != initialSelectionKey || isDarkModePref != initialDarkMode

    // 1. Define Local Default Themes
    val systemThemes = listOf(
        ThemePickerItem(
            id = ThemeConstants.DEFAULT_THEME_ID,
            type = MoonThemeType.DEFAULT,
            name = defaultThemeName,
            color = Color(0xFFFFC547)
        )
    )

    // 2. Map API Owned Themes (Purchased)
    val ownedThemes = remember(uiState.ownedThemes) {
        uiState.ownedThemes.filter { it.id != ThemeConstants.DEFAULT_THEME_ID && !it.isCustomTheme() }.map { theme ->
            val type = theme.id.toMoonThemeTypeOrNull()
                ?: theme.decoration.toMoonThemeTypeOrNull()
                ?: MoonThemeType.DEFAULT
            ThemePickerItem(
                id = theme.id,
                type = type,
                name = theme.name,
                color = theme.themePickerColor()
            )
        }
    }

    val customThemes = remember(uiState.customThemes) {
        uiState.customThemes
            .distinctBy { it.id }
            .map { theme ->
                ThemePickerItem(
                    id = theme.id,
                    type = null,
                    name = theme.name,
                    color = theme.themePickerColor(),
                    isCustom = true
                )
            }
    }

    // 3. Combined List (System + Purchased)
    val allSelectableThemes = (systemThemes + ownedThemes + customThemes).distinctBy { it.id }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        storeViewModel.uiEffect.collect { effect ->
            when (effect) {
                is com.diary.moonpage.ui.screens.store.StoreUiEffect.ShowSnackBar -> {
                    GlobalSnackbarManager.show(effect.message, effect.type)
                }
                is com.diary.moonpage.ui.screens.store.StoreUiEffect.ThemeActivated -> {
                    GlobalSnackbarManager.show(
                        effect.message ?: UiText.StringResource(R.string.theme_updated_success),
                        SnackbarType.SUCCESS
                    )
                }
                is com.diary.moonpage.ui.screens.store.StoreUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    ThemePickerContent(
        availableThemes = allSelectableThemes,
        currentThemeType = currentThemeType,
        currentThemeId = currentThemeId,
        isDarkMode = isDarkModePref,
        onThemeSelected = { item ->
            storeViewModel.activateTheme(item.id)
        },
        onDarkModeToggled = { mainViewModel.setDarkMode(it) },
        onApply = onActivated,
        onNavigateBack = onNavigateBack,
        isLoading = uiState.isLoading,
        snackbarHostState = snackbarHostState,
        showConfirmActivation = uiState.showConfirmActivationDialog,
        temporarySelectedThemeId = uiState.temporarySelectedThemeId,
        onConfirmActivation = { storeViewModel.confirmActivation() },
        onCancelActivation = { storeViewModel.cancelActivation() },
        isDoneEnabled = hasChanges
    )
}

/**
 * Stateless Component representing the "Themes & Styles" Picker UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePickerContent(
    availableThemes: List<ThemePickerItem>,
    currentThemeType: MoonThemeType,
    currentThemeId: String?,
    isDarkMode: Boolean?,
    onThemeSelected: (ThemePickerItem) -> Unit,
    onDarkModeToggled: (Boolean?) -> Unit,
    onApply: () -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState,
    showConfirmActivation: Boolean = false,
    temporarySelectedThemeId: String? = null,
    onConfirmActivation: () -> Unit = {},
    onCancelActivation: () -> Unit = {},
    isDoneEnabled: Boolean = true
) {
    val backText = stringResource(R.string.back)
    val systemText = stringResource(R.string.system)
    val lightText = stringResource(R.string.light)
    val darkText = stringResource(R.string.dark)
    val classicMoonBeansText = stringResource(R.string.theme_calendar_classic_moon_beans)
    val customColoredBeansText = stringResource(R.string.theme_calendar_custom_colored_beans)
    val fallbackThemeText = stringResource(R.string.theme_calendar_this_theme)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Static Top Bar to avoid any Material3 animations or flickering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = backText,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.theme_calendar_themes_styles),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RectangleShape
                )
            ) {
                Button(
                    onClick = onApply,
                    enabled = isDoneEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Done",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDoneEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Section 1: Dark Mode Toggle
                item {
                    Text(
                        text = stringResource(R.string.theme_calendar_appearance),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val modes = listOf(
                            Triple(systemText, Icons.Rounded.Settings, null as Boolean?),
                            Triple(lightText, Icons.Rounded.LightMode, false as Boolean?),
                            Triple(darkText, Icons.Rounded.DarkMode, true as Boolean?)
                        )

                        modes.forEach { (name, icon, value) ->
                            val isSelected = isDarkMode == value
                            AppThemeItem(
                                name = name,
                                icon = icon,
                                isSelected = isSelected,
                                modifier = Modifier.weight(1f),
                                onClick = { onDarkModeToggled(value) }
                            )
                        }
                    }
                }

                // Section 2: My Themes
                item {
                    Text(
                        text = stringResource(R.string.my_themes),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                items(availableThemes) { item ->
                    val isSelected = if (currentThemeId != null) {
                        currentThemeId == item.id
                    } else {
                        item.type != null && currentThemeType == item.type
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onThemeSelected(item) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) BorderStroke(2.dp, item.color) else null
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(item.color.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Circle, null, tint = item.color, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = if (item.type == MoonThemeType.DEFAULT && !item.isCustom) classicMoonBeansText else customColoredBeansText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = item.color)
                            }
                        }
                    }
                }
            }
            if (showConfirmActivation) {
                val themeData = availableThemes.find { it.id == temporarySelectedThemeId }
                ConfirmActivationDialog(
                    themeName = themeData?.name ?: fallbackThemeText,
                    onConfirm = onConfirmActivation,
                    onCancel = onCancelActivation,
                    primaryColor = themeData?.color
                )
            }
        }
    }
}

private fun Theme.isCustomTheme(): Boolean {
    return id.startsWith("custom_") ||
        decoration.equals("CUSTOM", ignoreCase = true) ||
        collection.equals("Custom Theme", ignoreCase = true)
}

private fun Theme.themePickerColor(): Color {
    return ThemeConstants.THEMES.find { it.id == id }?.thumbnailUrl.toColorOrNull()
        ?: description.appearanceColor("light", "primaryColor")
        ?: description.appearanceColor("dark", "primaryColor")
        ?: primaryColor.toColorOrNull()
        ?: backgroundUrl.toColorOrNull()
        ?: thumbnailUrl.toColorOrNull()
        ?: Color(0xFFFFC547)
}

private fun String?.appearanceColor(mode: String, key: String): Color? {
    if (isNullOrBlank()) return null
    return runCatching {
        JSONObject(this).optJSONObject(mode)?.optString(key).toColorOrNull()
    }.getOrNull()
}

private fun String.toMoonThemeTypeOrNull(): MoonThemeType? {
    if (this == ThemeConstants.DEFAULT_THEME_ID) return MoonThemeType.DEFAULT
    val decoration = if (this.startsWith("theme_")) this.substringAfter("theme_") else this
    val enumName = when (decoration.uppercase()) {
        "MOON" -> "DEFAULT"
        "BROWN" -> "GRAY_BROWN"
        "COOKIE" -> "COOKIE_BATCH"
        "HEART" -> "HEART_FELT"
        "WEATHER" -> "WEATHER_CYCLE"
        else -> decoration.uppercase()
    }
    return runCatching { MoonThemeType.valueOf(enumName) }.getOrNull()
}

private fun String?.toColorOrNull(): Color? {
    if (isNullOrBlank()) return null
    val raw = trim()
    val normalized = when {
        raw.startsWith("#") -> raw
        raw.startsWith("0x", ignoreCase = true) -> "#${raw.drop(2)}"
        raw.length == 6 || raw.length == 8 -> "#$raw"
        else -> return null
    }
    return runCatching {
        Color(android.graphics.Color.parseColor(normalized))
    }.getOrNull()
}

@Composable
fun AppThemeItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

