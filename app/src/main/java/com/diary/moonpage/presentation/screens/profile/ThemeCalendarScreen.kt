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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.MainViewModel
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.core.theme.MoonThemeType

// ---- Data model for theme packs ----

data class BeanThemePack(
    val id: String,
    val name: String,
    val description: String,
    val accentColor: Color,
    val secondaryColor: Color,
    val icon: ImageVector,
    val isLocked: Boolean = false,
    val previewMoods: List<Color> = emptyList()
)

private val beanThemes = listOf(
    BeanThemePack(
        id = "basic",
        name = "Basic Bean",
        description = "The classic round bean â€” simple and expressive.",
        accentColor = Color(0xFF4CAF50),
        secondaryColor = Color(0xFFE8F5E9),
        icon = Icons.Rounded.Circle,
        previewMoods = listOf(
            Color(0xFFFFEB3B), Color(0xFFAED581), Color(0xFF66BB6A),
            Color(0xFF78909C), Color(0xFF546E7A)
        )
    ),
    BeanThemePack(
        id = "heart",
        name = "Heart Beans",
        description = "Spread love with heart-shaped mood beans.",
        accentColor = Color(0xFFE91E63),
        secondaryColor = Color(0xFFFCE4EC),
        icon = Icons.Rounded.Favorite,
        isLocked = false,
        previewMoods = listOf(
            Color(0xFFF48FB1), Color(0xFFEC407A), Color(0xFFE91E63),
            Color(0xFFAD1457), Color(0xFF880E4F)
        )
    )
)

@Composable
fun ThemeCalendarScreen(
    onNavigateBack: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val currentThemeType by mainViewModel.themeType.collectAsState()
    val isDarkModePref by mainViewModel.isDarkMode.collectAsState()
    val isDark = isDarkModePref ?: androidx.compose.foundation.isSystemInDarkTheme()

    ThemePickerContent(
        availableThemes = listOf(
            Triple(MoonThemeType.DEFAULT, "Classic Yellow", Color(0xFFFFC547)),
            Triple(MoonThemeType.BLUSHING, "Blushing", Color(0xFFFF7FA3)),
            Triple(MoonThemeType.KITTY, "Kitty", Color(0xFF8A9AFF)),
            Triple(MoonThemeType.SPROUT, "Sprout", Color(0xFF66BB6A)),
            Triple(MoonThemeType.SUNNY, "Sunny", Color(0xFFFFB300)),
            Triple(MoonThemeType.SKY, "Sky", Color(0xFF29B6F6)),
            Triple(MoonThemeType.FOREST, "Forest", Color(0xFF26A69A)),
            Triple(MoonThemeType.COFFEE, "Coffee", Color(0xFF8D6E63)),
            Triple(MoonThemeType.LEMON, "Lemon", Color(0xFFCDDC39)),
            Triple(MoonThemeType.CHERRY, "Cherry", Color(0xFFEF5350)),
            Triple(MoonThemeType.LAVENDER, "Lavender", Color(0xFFAB47BC)),
            Triple(MoonThemeType.OCEAN, "Ocean", Color(0xFF42A5F5)),
            Triple(MoonThemeType.MIDNIGHT, "Midnight", Color(0xFF1A1B26)),
            Triple(MoonThemeType.NEBULA, "Nebula", Color(0xFFBA68C8)),
            Triple(MoonThemeType.MATCHA, "Matcha", Color(0xFF81C784)),
            Triple(MoonThemeType.SUNSET, "Sunset", Color(0xFFFFB74D)),
            Triple(MoonThemeType.GALAXY, "Galaxy", Color(0xFF7986CB)),
            Triple(MoonThemeType.AUTUMN, "Autumn", Color(0xFFE64A19)),
            Triple(MoonThemeType.GRAY_BROWN, "Gray Brown", Color(0xFF8D6E63)),
            Triple(MoonThemeType.COOKIE_BATCH, "Cookie Batch", Color(0xFFFFA000)),
            Triple(MoonThemeType.HEART_FELT, "Heart Felt", Color(0xFFE91E63)),
            Triple(MoonThemeType.WEATHER_CYCLE, "Weather Cycle", Color(0xFF607D8B))
        ),
        currentThemeType = currentThemeType,
        isDarkMode = isDark,
        onThemeSelected = { mainViewModel.setTheme(it) },
        onDarkModeToggled = { mainViewModel.setDarkMode(it) },
        onApply = { onNavigateBack() },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerContent(
    availableThemes: List<Triple<MoonThemeType, String, Color>>,
    currentThemeType: MoonThemeType,
    isDarkMode: Boolean,
    onThemeSelected: (MoonThemeType) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit,
    onApply: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
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
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Dark Mode", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeToggled,
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            // Section 2: Bean Themes
            item {
                Text(
                    "Bean Themes",
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

@Composable
fun BeanThemeCard(
    theme: BeanThemePack,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) theme.secondaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(theme.accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(theme.icon, null, tint = theme.accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(theme.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(theme.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (isSelected) {
                Icon(Icons.Rounded.CheckCircle, null, tint = theme.accentColor)
            }
        }
    }
}
