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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.ui.MainViewModel
import com.diary.moonpage.core.util.ThemeConstants
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.ui.screens.store.components.ConfirmActivationDialog

/**
 * Stateful Component
 */
@Composable
fun ThemeCalendarRoute(
    onNavigateBack: () -> Unit,
    onActivated: () -> Unit = onNavigateBack,
    mainViewModel: MainViewModel = hiltViewModel(),
    storeViewModel: com.diary.moonpage.ui.screens.store.StoreViewModel = hiltViewModel()
) {
    val uiState by storeViewModel.uiState.collectAsState()
    val mainUiState by mainViewModel.uiState.collectAsState()
    val currentThemeType = mainUiState.themeType
    val isDarkModePref = mainUiState.isDarkMode

    // Track initial values to enable/disable Done button
    val initialThemeType = remember { currentThemeType }
    val initialDarkMode = remember { isDarkModePref }
    val hasChanges = currentThemeType != initialThemeType || isDarkModePref != initialDarkMode

    // 1. Define Local Default Themes
    val systemThemes = listOf(
        Triple(MoonThemeType.DEFAULT, "Classic Yellow", Color(0xFFFFC547))
    )

    // 2. Map API Owned Themes (Purchased)
    val ownedThemes = remember(uiState.ownedThemes) {
        uiState.ownedThemes.filter { it.id != ThemeConstants.DEFAULT_THEME_ID }.map { theme ->
            val type = theme.decoration.toMoonThemeType()
            val color = try {
                if (!theme.primaryColor.isNullOrBlank()) {
                    val colorStr = if (theme.primaryColor.startsWith("#")) theme.primaryColor else "#${theme.primaryColor}"
                    Color(android.graphics.Color.parseColor(colorStr))
                } else {
                    Color(0xFFFFC547)
                }
            } catch (e: Exception) {
                Color(0xFFFFC547)
            }
            Triple(type, theme.name, color)
        }
    }

    // 3. Combined List (System + Purchased)
    val allSelectableThemes = (systemThemes + ownedThemes).distinctBy { it.first }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        storeViewModel.uiEffect.collect { effect ->
            when (effect) {
                is com.diary.moonpage.ui.screens.store.StoreUiEffect.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is com.diary.moonpage.ui.screens.store.StoreUiEffect.ThemeActivated -> {
                    snackbarHostState.showSnackbar("Theme updated successfully!")
                }
                is com.diary.moonpage.ui.screens.store.StoreUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    ThemeCalendarScreen(
        availableThemes = allSelectableThemes,
        currentThemeType = currentThemeType,
        isDarkMode = isDarkModePref,
        hasChanges = hasChanges,
        onThemeSelected = { type ->
            val theme = if (type == MoonThemeType.DEFAULT) {
                uiState.ownedThemes.find { it.id == ThemeConstants.DEFAULT_THEME_ID }
            } else {
                uiState.ownedThemes.find { it.decoration.toMoonThemeType() == type }
            }
            if (theme != null) {
                storeViewModel.activateTheme(theme.id)
            }
        },
        onDarkModeChange = { isDark -> mainViewModel.setDarkMode(isDark) },
        onNavigateBack = onNavigateBack,
        onActivated = onActivated,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Stateless Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCalendarScreen(
    availableThemes: List<Triple<MoonThemeType, String, Color>>,
    currentThemeType: MoonThemeType,
    isDarkMode: Boolean?,
    hasChanges: Boolean,
    onThemeSelected: (MoonThemeType) -> Unit,
    onDarkModeChange: (Boolean?) -> Unit,
    onNavigateBack: () -> Unit,
    onActivated: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("App Theme", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    TextButton(
                        onClick = onActivated,
                        enabled = hasChanges
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold, color = if (hasChanges) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorScheme.background)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Appearance Section
                item {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppearanceOption(
                            label = "Light",
                            isSelected = isDarkMode == false,
                            onClick = { onDarkModeChange(false) },
                            modifier = Modifier.weight(1f)
                        )
                        AppearanceOption(
                            label = "Dark",
                            isSelected = isDarkMode == true,
                            onClick = { onDarkModeChange(true) },
                            modifier = Modifier.weight(1f)
                        )
                        AppearanceOption(
                            label = "System",
                            isSelected = isDarkMode == null,
                            onClick = { onDarkModeChange(null) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Themes Section
                item {
                    Text("Color Themes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(availableThemes.chunked(2)) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { theme ->
                            ThemeOptionCard(
                                type = theme.first,
                                name = theme.second,
                                primaryColor = theme.third,
                                isSelected = currentThemeType == theme.first,
                                onClick = { onThemeSelected(theme.first) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            com.diary.moonpage.ui.components.feedback.MoonSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun AppearanceOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    type: MoonThemeType,
    name: String,
    primaryColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .background(
                    if (isSelected) primaryColor.copy(alpha = 0.1f) else colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .border(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) primaryColor else colorScheme.onSurface.copy(alpha = 0.1f),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Mock UI preview inside
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(24.dp).background(primaryColor, CircleShape))
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(primaryColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.width(40.dp).height(8.dp).background(primaryColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp)
                        .background(primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(14.dp), tint = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun String.toMoonThemeType(): MoonThemeType {
    if (this == ThemeConstants.DEFAULT_THEME_ID) return MoonThemeType.DEFAULT
    return try {
        MoonThemeType.valueOf(this.uppercase())
    } catch (e: Exception) {
        MoonThemeType.DEFAULT
    }
}
