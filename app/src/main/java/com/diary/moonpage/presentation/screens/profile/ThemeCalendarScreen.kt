package com.diary.moonpage.presentation.screens.profile

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
import com.diary.moonpage.MainViewModel
import com.diary.moonpage.core.util.ThemeConstants
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost

@Composable
fun ThemeCalendarScreen(
    onNavigateBack: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    storeViewModel: com.diary.moonpage.presentation.screens.store.StoreViewModel = hiltViewModel()
) {
    val uiState by storeViewModel.uiState.collectAsState()
    val currentThemeType by mainViewModel.themeType.collectAsState()
    val isDarkModePref by mainViewModel.isDarkMode.collectAsState()

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
            if (effect is com.diary.moonpage.presentation.screens.store.StoreUiEffect.ShowSnackBar) {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ThemePickerContent(
        availableThemes = allSelectableThemes,
        currentThemeType = currentThemeType,
        isDarkMode = isDarkModePref,
        onThemeSelected = { type ->
            mainViewModel.setTheme(type)
            val theme = uiState.ownedThemes.find { it.decoration.toMoonThemeType() == type }
            theme?.let { storeViewModel.activateTheme(it.id) }
        },
        onDarkModeToggled = { mainViewModel.setDarkMode(it) },
        onApply = { onNavigateBack() },
        onNavigateBack = onNavigateBack,
        isLoading = uiState.isLoading,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerContent(
    availableThemes: List<Triple<MoonThemeType, String, Color>>,
    currentThemeType: MoonThemeType,
    isDarkMode: Boolean?,
    onThemeSelected: (MoonThemeType) -> Unit,
    onDarkModeToggled: (Boolean?) -> Unit,
    onApply: () -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { MoonSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Themes & Styles", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 12.dp) {
                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Activate", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
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
                        "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val modes = listOf(
                            Triple("System", Icons.Rounded.Settings, null as Boolean?),
                            Triple("Light", Icons.Rounded.LightMode, false as Boolean?),
                            Triple("Dark", Icons.Rounded.DarkMode, true as Boolean?)
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
                        "My Themes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                items(availableThemes) { (type, name, color) ->
                    val isSelected = currentThemeType == type
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onThemeSelected(type) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) BorderStroke(2.dp, color) else null
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Circle, null, tint = color, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (type == MoonThemeType.DEFAULT) "Classic moon beans" else "Custom colored beans",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = color)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.toMoonThemeType(): MoonThemeType {
    return try {
        MoonThemeType.valueOf(this.uppercase())
    } catch (e: Exception) {
        MoonThemeType.DEFAULT
    }
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
            .background(if (isSelected) colorScheme.primary.copy(alpha = 0.1f) else colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
