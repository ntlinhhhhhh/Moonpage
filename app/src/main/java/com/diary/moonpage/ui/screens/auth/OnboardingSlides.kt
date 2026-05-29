package com.diary.moonpage.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.R
import com.diary.moonpage.core.theme.MoonThemeType
import com.diary.moonpage.core.util.MoonIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MoodLoggingSlide(
    isVisible: Boolean,
    showHeader: Boolean = true
) {
    var step by remember { mutableStateOf(0) }
    var selectedMoodIdx by remember { mutableStateOf(-1) }
    var selectedActivityIdxs by remember { mutableStateOf(setOf<Int>()) }
    val cardSlideSpec = tween<Float>(
        durationMillis = 900,
        easing = FastOutSlowInEasing
    )

    // Positions: -300 (Off top), 0 (Top slot), 165 (Bottom slot), 500 (Off bottom)
    val moodY = animateFloatAsState(
        targetValue = when (step) { 0 -> 60f; 1 -> 0f; else -> -300f },
        animationSpec = cardSlideSpec,
        label = "moodCardY"
    )
    val weatherY = animateFloatAsState(
        targetValue = when (step) { 0 -> 500f; 1 -> 165f; 2 -> 0f; else -> -300f },
        animationSpec = cardSlideSpec,
        label = "weatherCardY"
    )
    val socialY = animateFloatAsState(
        targetValue = when (step) { 0, 1 -> 500f; 2 -> 165f; 3 -> 0f; else -> -300f },
        animationSpec = cardSlideSpec,
        label = "socialCardY"
    )
    val feelingsY = animateFloatAsState(
        targetValue = when (step) { 0, 1, 2 -> 500f; 3 -> 165f; else -> 165f },
        animationSpec = cardSlideSpec,
        label = "feelingsCardY"
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            step = 0; selectedMoodIdx = -1; selectedActivityIdxs = emptySet()
            
            delay(1000)
            selectedMoodIdx = 3 // Click Happy
            delay(1000)
            step = 1 // Show Mood + Weather
            delay(1000)
            selectedActivityIdxs = selectedActivityIdxs + 0 // Click Sunny
            delay(1000)
            step = 2 // Show Weather + Social
            delay(1000)
            selectedActivityIdxs = selectedActivityIdxs + 5 // Click Group
            delay(1000)
            step = 3 // Show Social + Feelings
            delay(1000)
            selectedActivityIdxs = selectedActivityIdxs + 10 // Click Excited
        } else {
            step = 0
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = if (showHeader) 24.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showHeader) {
            SlideHeader(
                title = stringResource(R.string.onboarding_simple_diary_title),
                description = stringResource(R.string.onboarding_simple_diary_desc),
                isVisible = isVisible
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
            // Mood Card
            Box(modifier = Modifier.padding(horizontal = 24.dp).offset(y = moodY.value.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.select_mood), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(1, 2, 3, 4, 5).forEachIndexed { index, level ->
                                val isSelected = selectedMoodIdx == index
                                val moodIcon = MoonIcons.Moods.getMoodVisual(level, MoonThemeType.DEFAULT)
                                val unselectedBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                val unselectedIcon = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                val selectedBg = moodIcon.color
                                val selectedIcon = Color(0xFF3E2723) // Dark brown for the face
                                
                                val bgColor = animateColorAsState(if (isSelected) selectedBg else unselectedBg)
                                val tintColor = animateColorAsState(if (isSelected) selectedIcon else unselectedIcon)

                                Box(
                                    modifier = Modifier.size(52.dp).clip(CircleShape).background(bgColor.value),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(painter = painterResource(id = moodIcon.drawableRes!!), contentDescription = null, tint = tintColor.value, modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Weather Card
            Box(modifier = Modifier.padding(horizontal = 24.dp).offset(y = weatherY.value.dp)) {
                ActivityCard {
                    ActivityGroup(stringResource(R.string.activity_category_weather), listOf(Icons.Rounded.WbSunny, Icons.Rounded.Cloud, Icons.Rounded.Umbrella, Icons.Rounded.Air), selectedActivityIdxs, 0)
                }
            }

            // Social Card
            Box(modifier = Modifier.padding(horizontal = 24.dp).offset(y = socialY.value.dp)) {
                ActivityCard {
                    ActivityGroup(stringResource(R.string.activity_category_social), listOf(Icons.Rounded.Star, Icons.Rounded.Group, Icons.Rounded.Favorite, Icons.Rounded.Groups), selectedActivityIdxs, 4)
                }
            }

            // Feelings Card
            Box(modifier = Modifier.padding(horizontal = 24.dp).offset(y = feelingsY.value.dp)) {
                ActivityCard {
                    ActivityGroup(stringResource(R.string.activity_category_feelings), listOf(Icons.Rounded.SentimentVerySatisfied, Icons.Rounded.Celebration, Icons.Rounded.Bedtime, Icons.Rounded.FlashOn), selectedActivityIdxs, 8)
                }
            }
        }
    }
}

@Composable
fun ActivityCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun ActivityGroup(title: String, icons: List<ImageVector>, selectedIdxs: Set<Int>, baseIdx: Int) {
    val selectionColor = MoonIcons.Moods.getMoodVisual(4, MoonThemeType.DEFAULT).color
    val unselectedBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val unselectedIcon = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val selectedBg = selectionColor.copy(alpha = 0.15f)
    val selectedIcon = selectionColor

    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            icons.forEachIndexed { i, icon ->
                val currentIdx = baseIdx + i
                val isSelected = selectedIdxs.contains(currentIdx)
                val bgColor = animateColorAsState(if (isSelected) selectedBg else unselectedBg, tween(500))
                val tintColor = animateColorAsState(if (isSelected) selectedIcon else unselectedIcon, tween(500))
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(bgColor.value), contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(28.dp), tint = tintColor.value)
                }
            }
        }
    }
}

enum class InteractionType { MOOD, TAG }

@Composable
fun OnboardingInteractionCard(
    title: String? = null,
    items: List<Any>,
    highlightIndex: Int,
    type: InteractionType,
    isVisible: Boolean
) {
    var isSelected by remember { mutableStateOf(false) }
    
    // Simulate selection
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Reset state
            isSelected = false
            delay(1200)
            isSelected = true
        } else {
            isSelected = false
        }
    }

    val selectionFraction = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "selection"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isTarget = index == highlightIndex
                    val highlightColor = MaterialTheme.colorScheme.primary
                    val normalColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    
                    val backgroundColor = if (isTarget) {
                        lerp(normalColor, highlightColor, selectionFraction.value)
                    } else normalColor

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            when (type) {
                                InteractionType.MOOD -> {
                                    val level = item as Int
                                    val moodIcon = MoonIcons.Moods.getMoodVisual(level, MoonThemeType.DEFAULT)
                                    Icon(
                                        painter = painterResource(id = moodIcon.drawableRes!!),
                                        contentDescription = null,
                                        tint = if (isTarget) {
                                            lerp(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), Color.White, selectionFraction.value)
                                        } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                InteractionType.TAG -> {
                                    val label = item as String
                                    val icon = when (label) {
                                        "sunny" -> Icons.Rounded.WbSunny
                                        "cloudy" -> Icons.Rounded.Cloud
                                        "rainy" -> Icons.Rounded.Umbrella
                                        "windy" -> Icons.Rounded.Air
                                        "friends" -> Icons.Rounded.Group
                                        "family" -> Icons.Rounded.Group
                                        "partner" -> Icons.Rounded.Favorite
                                        "none" -> Icons.Rounded.Cancel
                                        "happy" -> Icons.Rounded.SentimentVerySatisfied
                                        "excited" -> Icons.Rounded.Celebration
                                        "tired" -> Icons.Rounded.Bedtime
                                        "stressed" -> Icons.Rounded.FlashOn
                                        "exercise" -> Icons.Rounded.DirectionsRun
                                        "meditation" -> Icons.Rounded.SelfImprovement
                                        "water" -> Icons.Rounded.WaterDrop
                                        "sleep" -> Icons.Rounded.NightsStay
                                        "flight" -> Icons.Rounded.Flight
                                        "beach" -> Icons.Rounded.BeachAccess
                                        "mountain" -> Icons.Rounded.Terrain
                                        "city" -> Icons.Rounded.LocationCity
                                        "coffee" -> Icons.Rounded.Coffee
                                        "healthy" -> Icons.Rounded.Restaurant
                                        "pizza" -> Icons.Rounded.LocalPizza
                                        "dessert" -> Icons.Rounded.Cake
                                        "reading" -> Icons.Rounded.MenuBook
                                        "gaming" -> Icons.Rounded.Gamepad
                                        "music" -> Icons.Rounded.MusicNote
                                        "art" -> Icons.Rounded.Palette
                                        else -> Icons.Rounded.Tag
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isTarget) {
                                            lerp(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), Color.White, selectionFraction.value)
                                        } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                        if (type == InteractionType.TAG) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item as String,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isTarget && isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isTarget && isSelected) 1f else 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper to lerp colors
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}

// --- SLIDE 2: LOGS & MOMENTS (Refactored to match Slide 1) ---
@Composable
fun PhotoLogSlide(
    isVisible: Boolean,
    showHeader: Boolean = true
) {
    Column(modifier = Modifier.fillMaxSize().padding(top = if (showHeader) 24.dp else 0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showHeader) {
            SlideHeader(
                title = stringResource(R.string.onboarding_beautiful_logging_title),
                description = stringResource(R.string.onboarding_beautiful_logging_desc),
                isVisible = isVisible
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp)) {
            OnboardingDailyLogCard()
        }
    }
}

@Composable
fun OnboardingDailyLogCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                val moodIcon = MoonIcons.Moods.getMoodVisual(4, MoonThemeType.DEFAULT)
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(id = moodIcon.drawableRes!!), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.onboarding_preview_date), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.onboarding_preview_year), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Activities
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(Icons.Rounded.Group, Icons.Rounded.BeachAccess, Icons.Rounded.Restaurant, Icons.Rounded.WbSunny).forEach { icon ->
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Note
            Text(
                stringResource(R.string.onboarding_preview_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Photos Placeholder
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Image, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.size(32.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Music Item
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.onboarding_music_sample_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.onboarding_music_sample_artist), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingMomentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Image, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
                ) {
                    Text(
                        stringResource(R.string.onboarding_moment_sample_title),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AccessTime, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.onboarding_music_sample_time), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

data class LoggingFrameData(
    val title: String,
    val categories: List<Pair<String, List<String>>>,
    val targetSelected: Set<String>,
    val theme: MoonThemeType,
    val accentColor: Color
)

fun getIconForItem(item: String): ImageVector = when (item) {
    "sunny" -> Icons.Rounded.WbSunny
    "cloudy" -> Icons.Rounded.Cloud
    "rainy" -> Icons.Rounded.Umbrella
    "windy" -> Icons.Rounded.Air
    "friends" -> Icons.Rounded.Group
    "family" -> Icons.Rounded.Groups
    "partner" -> Icons.Rounded.Favorite
    "none" -> Icons.Rounded.Cancel
    "happy" -> Icons.Rounded.SentimentVerySatisfied
    "excited" -> Icons.Rounded.Celebration
    "tired" -> Icons.Rounded.Bedtime
    "stressed" -> Icons.Rounded.FlashOn
    "coffee" -> Icons.Rounded.Coffee
    "healthy" -> Icons.Rounded.Restaurant
    "pizza" -> Icons.Rounded.LocalPizza
    "dessert" -> Icons.Rounded.Cake
    "reading" -> Icons.Rounded.MenuBook
    "gaming" -> Icons.Rounded.Gamepad
    "music" -> Icons.Rounded.MusicNote
    "art" -> Icons.Rounded.Palette
    else -> Icons.Rounded.Tag
}

// --- SLIDE 3: MONTHLY THEMES ---
@Composable
fun AnnualLookBackSlide(
    isVisible: Boolean,
    showHeader: Boolean = true
) {
    var activeThemeIdx by remember { mutableStateOf(0) }
    val previewCardHeight = 320.dp
    val themeFrameDuration = 1800L
    val themes = listOf(
        ThemeData(
            stringResource(R.string.onboarding_default_theme),
            Color(0xFFFFFBF4), 
            Color(0xFF8C7E6A), 
            MoonThemeType.DEFAULT,
            listOf(Color(0xFFFFF9E1), Color(0xFFFFE082), Color(0xFFFFB74D), Color(0xFFFB8C00), Color(0xFF5D4037))
        ),
        ThemeData(
            stringResource(R.string.onboarding_coffee_theme),
            Color(0xFFF7F5F4), 
            Color(0xFF8D6E63), 
            MoonThemeType.COFFEE,
            listOf(Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF5D4037))
        ),
        ThemeData(
            stringResource(R.string.onboarding_blushing_theme),
            Color(0xFFFFF0F3), 
            Color(0xFFD2847A), 
            MoonThemeType.BLUSHING,
            listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFEF9A9A), Color(0xFFE57373), Color(0xFFD32F2F))
        ),
        ThemeData(
            stringResource(R.string.onboarding_galaxy_theme),
            Color(0xFFF0F2F9), 
            Color(0xFF3F51B5), 
            MoonThemeType.GALAXY,
            listOf(Color(0xFFE8EAF6), Color(0xFFC5CAE9), Color(0xFF9FA8DA), Color(0xFF7986CB), Color(0xFF3F51B5))
        ),
        ThemeData(
            stringResource(R.string.onboarding_matcha_theme),
            Color(0xFFF1F8E9), 
            Color(0xFF4CAF50), 
            MoonThemeType.MATCHA,
            listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5), Color(0xFFAED581), Color(0xFF81C784), Color(0xFF4CAF50))
        )
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            activeThemeIdx = 0
            for (idx in 1 until themes.size) {
                delay(themeFrameDuration)
                activeThemeIdx = idx
            }
        } else {
            activeThemeIdx = 0
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = if (showHeader) 24.dp else 0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showHeader) {
            SlideHeader(
                title = stringResource(R.string.onboarding_monthly_themes_title),
                description = stringResource(R.string.onboarding_monthly_themes_desc),
                isVisible = isVisible
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
            AnimatedContent(
                targetState = activeThemeIdx,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                        initialOffsetX = { it }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                            targetOffsetX = { -it }
                        )
                    )
                },
                label = "theme_slide",
                modifier = Modifier.fillMaxSize()
            ) { idx ->
                val theme = themes[idx]
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(previewCardHeight),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(theme.bgColor)
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            // Days of week header
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf(
                                    stringResource(R.string.sun),
                                    stringResource(R.string.mon),
                                    stringResource(R.string.tue),
                                    stringResource(R.string.wed),
                                    stringResource(R.string.thu),
                                    stringResource(R.string.fri),
                                    stringResource(R.string.sat)
                                ).forEachIndexed { dayIndex, day ->
                                    val isSat = dayIndex == 6
                                    val isSun = dayIndex == 0
                                    Text(
                                        text = day, 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = if (isSat || isSun) theme.accentColor else Color.Gray.copy(alpha = 0.6f), 
                                        modifier = Modifier.width(36.dp), 
                                        textAlign = TextAlign.Center,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // 30-day Grid
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val startOffset = (idx + 2) % 7 // Varied starting day for each theme
                                repeat(5) { r ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        repeat(7) { c ->
                                            val dayIdx = r * 7 + c
                                            val day = dayIdx - startOffset + 1
                                            
                                            if (day in 1..30) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
                                                    val shadeIdx = (day % 5)
                                                    val circleColor = theme.palette[shadeIdx]
                                                    val moodLevel = (day % 5) + 1
                                                    val moodIcon = MoonIcons.Moods.getMoodVisual(moodLevel, theme.type)
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(circleColor),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = moodIcon.drawableRes!!),
                                                            contentDescription = null,
                                                            tint = Color(0xFF212121),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = day.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Gray.copy(alpha = 0.8f),
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(36.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ThemeData(
    val name: String, 
    val bgColor: Color, 
    val accentColor: Color, 
    val type: MoonThemeType,
    val palette: List<Color> = emptyList()
)

// --- SLIDE 4: LEARN ABOUT YOU ---
@Composable
fun AdvancedStatsSlide(
    isVisible: Boolean,
    showHeader: Boolean = true
) {
    var activeFrame by remember { mutableStateOf(0) }
    val previewCardHeight = 320.dp
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            activeFrame = 0
            delay(3800)
            activeFrame = 1
        } else {
            activeFrame = 0
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = if (showHeader) 24.dp else 0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showHeader) {
            SlideHeader(
                title = stringResource(R.string.onboarding_learn_about_you_title),
                description = stringResource(R.string.onboarding_learn_about_you_desc),
                isVisible = isVisible
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
            AnimatedContent(
                targetState = activeFrame,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                        initialOffsetX = { it }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                            targetOffsetX = { -it }
                        )
                    )
                },
                label = "stats_slide"
            ) { frameIdx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(previewCardHeight),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (frameIdx == 0) {
                        MoodFlowFrame(isVisible)
                    } else {
                        MoodDistributionFrame(isVisible)
                    }
                }
            }
        }
    }
}

@Composable
fun MoodFlowFrame(isVisible: Boolean) {
    val pathProgress = remember { Animatable(0f) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            pathProgress.snapTo(0f)
            delay(500)
            pathProgress.animateTo(1f, tween(2500, easing = LinearEasing))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(R.string.mood_flow), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width - 40.dp.toPx()
                val height = size.height - 30.dp.toPx()
                val startX = 40.dp.toPx()
                
                val moodColors = listOf(
                    Color(0xFFFFE082), // Yellow
                    Color(0xFFC5E1A5), // Light Green
                    Color(0xFF81C784), // Green
                    Color(0xFF43A047), // Dark Green
                    Color(0xFF90A4AE)  // Gray
                )

                // Draw Y-axis Mood Dots (5 at top, 1 at bottom)
                val moodLevels = listOf(5, 4, 3, 2, 1)
                moodLevels.forEachIndexed { i, level ->
                    val color = MoonIcons.Moods.getMoodColor(level, MoonThemeType.DEFAULT)
                    val y = (height / (moodLevels.size - 1)) * i
                    drawCircle(color, radius = 6.dp.toPx(), center = Offset(15.dp.toPx(), y))
                }
                
                // Draw Vertical Grid Lines and Labels
                val xSteps = 7
                repeat(xSteps) { i ->
                    val x = startX + (width / (xSteps - 1)) * i
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Jagged Path Points - Higher density for more detail
                // Snapped to: 0.0=Level 5, 0.25=Level 4, 0.5=Level 3, 0.75=Level 2, 1.0=Level 1
                val rawPoints = listOf(
                    Offset(0.0f, 0.25f), Offset(0.04f, 0.5f), Offset(0.08f, 0.0f), 
                    Offset(0.12f, 0.25f), Offset(0.16f, 0.25f), Offset(0.20f, 0.0f),
                    Offset(0.25f, 0.5f), Offset(0.30f, 0.75f), Offset(0.35f, 0.5f),
                    Offset(0.40f, 0.25f), Offset(0.45f, 0.0f), Offset(0.50f, 0.25f),
                    Offset(0.55f, 0.5f), Offset(0.60f, 0.5f), Offset(0.65f, 0.75f),
                    Offset(0.70f, 1.0f), Offset(0.75f, 0.5f), Offset(0.80f, 0.25f),
                    Offset(0.85f, 0.0f), Offset(0.90f, 0.25f), Offset(0.95f, 0.5f),
                    Offset(1.0f, 0.25f)
                )
                
                // Only take points before 1.0 (Day 31) for the main display if requested
                val displayPoints = rawPoints.filter { it.x <= 1.0f }.map { Offset(startX + it.x * width, it.y * height) }

                val path = Path()
                displayPoints.forEachIndexed { index, p ->
                    if (index == 0) path.moveTo(p.x, p.y)
                    else path.lineTo(p.x, p.y)
                }
                
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, false)
                val totalPathLength = pathMeasure.length
                
                // Calculate cumulative distances for each point to sync dots perfectly
                val pointDistances = mutableListOf<Float>()
                var tempPath = Path()
                displayPoints.forEachIndexed { index, p ->
                    if (index == 0) {
                        pointDistances.add(0f)
                        tempPath.moveTo(p.x, p.y)
                    } else {
                        tempPath.lineTo(p.x, p.y)
                        val tempMeasure = PathMeasure()
                        tempMeasure.setPath(tempPath, false)
                        pointDistances.add(tempMeasure.length)
                    }
                }

                // Current length drawn based on progress
                val currentLength = totalPathLength * pathProgress.value
                val partialPath = Path()
                pathMeasure.getSegment(0f, currentLength, partialPath, true)
                
                val selectionColor = MoonIcons.Moods.getMoodVisual(4, MoonThemeType.DEFAULT).color
                drawPath(partialPath, selectionColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                
                // Draw dots at points (only before Day 31)
                displayPoints.forEachIndexed { index, p ->
                    val pointDist = pointDistances.getOrElse(index) { 0f }
                    // Only draw circles if the line distance has reached that point 
                    // AND it's strictly before the last grid line (Day 31)
                    if (pointDist <= currentLength && p.x < startX + width) {
                        drawCircle(selectionColor, radius = 3.5.dp.toPx(), center = p)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 40.dp), 
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("6/1", "6/6", "6/11", "6/16", "6/21", "6/26", "6/31").forEach { date ->
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MoodDistributionFrame(isVisible: Boolean) {
    val barProgress = remember { Animatable(0f) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            barProgress.snapTo(0f)
            delay(500)
            barProgress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.mood_bar), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(24.dp))
        
        val items = listOf(
            Triple(5, "13%", MoonIcons.Moods.getMoodColor(5, MoonThemeType.DEFAULT)), // Very Happy
            Triple(4, "13%", MoonIcons.Moods.getMoodColor(4, MoonThemeType.DEFAULT)), // Happy
            Triple(3, "38%", MoonIcons.Moods.getMoodColor(3, MoonThemeType.DEFAULT)), // Neutral
            Triple(2, "13%", MoonIcons.Moods.getMoodColor(2, MoonThemeType.DEFAULT)), // Sad
            Triple(1, "25%", MoonIcons.Moods.getMoodColor(1, MoonThemeType.DEFAULT))  // Very Sad
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            items.forEach { (level, pct, color) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val moodIcon = MoonIcons.Moods.getMoodVisual(level, MoonThemeType.DEFAULT)
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = moodIcon.drawableRes!!), 
                            null, 
                            tint = Color(0xFF3E2723), // Dark brown face
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                        Text(pct, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Horizontal Stacked Bar
        Canvas(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(22.dp))) {
            var currentX = 0f
            val totalWidth = size.width * barProgress.value
            items.forEach { (_, pct, color) ->
                val segmentWidth = (pct.removeSuffix("%").toFloat() / 100f) * totalWidth
                drawRect(color, Offset(currentX, 0f), Size(segmentWidth, size.height))
                currentX += segmentWidth
            }
        }
    }
}

@Composable
fun SlideHeader(title: String, description: String, isVisible: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatTag(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}
