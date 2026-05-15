package com.diary.moonpage.presentation.screens.auth

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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
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
fun MoodLoggingSlide(isVisible: Boolean) {
    var step by remember { mutableStateOf(0) }
    var selectedMoodIdx by remember { mutableStateOf(-1) }
    var selectedActivityIdxs by remember { mutableStateOf(setOf<Int>()) }

    // Positions: -300 (Off top), 0 (Top slot), 165 (Bottom slot), 500 (Off bottom)
    val moodY = animateFloatAsState(targetValue = when(step) { 0 -> 60f; 1 -> 0f; else -> -300f }, animationSpec = spring(0.8f, Spring.StiffnessMedium))
    val weatherY = animateFloatAsState(targetValue = when(step) { 0 -> 500f; 1 -> 165f; 2 -> 0f; else -> -300f }, animationSpec = spring(0.8f, Spring.StiffnessMedium))
    val socialY = animateFloatAsState(targetValue = when(step) { 0, 1 -> 500f; 2 -> 165f; 3 -> 0f; else -> -300f }, animationSpec = spring(0.8f, Spring.StiffnessMedium))
    val feelingsY = animateFloatAsState(targetValue = when(step) { 0, 1, 2 -> 500f; 3 -> 165f; else -> 165f }, animationSpec = spring(0.8f, Spring.StiffnessMedium))

    LaunchedEffect(isVisible) {
        if (isVisible) {
            step = 0; selectedMoodIdx = -1; selectedActivityIdxs = emptySet()
            
            delay(1000)
            selectedMoodIdx = 3 // Click Happy
            delay(800)
            step = 1 // Show Mood + Weather
            delay(1200)
            selectedActivityIdxs = selectedActivityIdxs + 0 // Click Sunny
            delay(800)
            step = 2 // Show Weather + Social
            delay(1200)
            selectedActivityIdxs = selectedActivityIdxs + 5 // Click Group
            delay(800)
            step = 3 // Show Social + Feelings
            delay(1200)
            selectedActivityIdxs = selectedActivityIdxs + 10 // Click Excited
        } else {
            step = 0
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SlideHeader(title = "The simplest diary", description = "Record your day with just a few taps", isVisible = isVisible)

        Spacer(modifier = Modifier.height(32.dp))

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
                        Text("How was your day?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            val moodPalette = listOf(Color(0xFF5D4037), Color(0xFFFB8C00), Color(0xFFFFB74D), Color(0xFFFFE082), Color(0xFFFFF9E1))
                            listOf(1, 2, 3, 4, 5).forEachIndexed { index, level ->
                                val isSelected = selectedMoodIdx == index
                                val moodIcon = MoonIcons.Moods.getMoodVisual(level, MoonThemeType.DEFAULT)
                                Box(
                                    modifier = Modifier.size(52.dp).clip(CircleShape).background(if (isSelected) moodPalette[index] else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(painter = painterResource(id = moodIcon.drawableRes!!), contentDescription = null, tint = if (isSelected) Color(0xFF3E2723) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Weather Card
            Box(modifier = Modifier.padding(horizontal = 24.dp).offset(y = weatherY.value.dp)) {
                ActivityCard {
                    ActivityGroup("Weather", listOf(Icons.Rounded.WbSunny, Icons.Rounded.Cloud, Icons.Rounded.Umbrella, Icons.Rounded.Air), selectedActivityIdxs, 0)
                }
            }

            // Social Card
            Box(modifier = Modifier.padding(horizontal = 24.dp).offset(y = socialY.value.dp)) {
                ActivityCard {
                    ActivityGroup("Social", listOf(Icons.Rounded.Star, Icons.Rounded.Group, Icons.Rounded.Favorite, Icons.Rounded.Groups), selectedActivityIdxs, 4)
                }
            }

            // Feelings Card
            Box(modifier = Modifier.padding(horizontal = 24.dp).offset(y = feelingsY.value.dp)) {
                ActivityCard {
                    ActivityGroup("Feelings", listOf(Icons.Rounded.SentimentVerySatisfied, Icons.Rounded.Celebration, Icons.Rounded.Bedtime, Icons.Rounded.FlashOn), selectedActivityIdxs, 8)
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
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            icons.forEachIndexed { i, icon ->
                val currentIdx = baseIdx + i
                val isSelected = selectedIdxs.contains(currentIdx)
                val bgColor = animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), tween(500))
                val iconColor = animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), tween(500))
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(bgColor.value), contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(28.dp), tint = iconColor.value)
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

// --- SLIDE 2: BEAUTIFUL LOGGING (Theme Showcase) ---
@Composable
fun PhotoLogSlide(isVisible: Boolean) {
    var activeFrameIdx by remember { mutableStateOf(0) }
    var simulatedSelectedItems by remember { mutableStateOf(setOf<String>()) }
    
    val frames = listOf(
        LoggingFrameData(
            title = "Default Theme",
            categories = listOf(
                "Weather" to listOf("sunny", "cloudy", "rainy", "windy"),
                "Social" to listOf("friends", "family", "partner", "none")
            ),
            targetSelected = setOf("sunny"),
            theme = MoonThemeType.DEFAULT,
            accentColor = Color(0xFFAED581)
        ),
        LoggingFrameData(
            title = "Matcha Theme",
            categories = listOf(
                "Social" to listOf("friends", "family", "partner", "none"),
                "Feelings" to listOf("happy", "excited", "tired", "stressed")
            ),
            targetSelected = setOf("family", "tired"),
            theme = MoonThemeType.MATCHA,
            accentColor = Color(0xFF81C784)
        ),
        LoggingFrameData(
            title = "Blushing Theme",
            categories = listOf(
                "Food" to listOf("coffee", "healthy", "pizza", "dessert"),
                "Hobby" to listOf("reading", "gaming", "music", "art")
            ),
            targetSelected = setOf("coffee", "reading"),
            theme = MoonThemeType.BLUSHING,
            accentColor = Color(0xFFF06292)
        )
    )

    LaunchedEffect(isVisible, activeFrameIdx) {
        if (isVisible) {
            simulatedSelectedItems = emptySet()
            delay(1000)
            simulatedSelectedItems = frames[activeFrameIdx].targetSelected
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (true) {
                delay(4000)
                activeFrameIdx = (activeFrameIdx + 1) % frames.size
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        SlideHeader(
            title = "Beautiful logging",
            description = "Explore different themes and activities",
            isVisible = isVisible
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
            AnimatedContent(
                targetState = activeFrameIdx,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                },
                label = "logging_slide"
            ) { frameIdx ->
                val frame = frames[frameIdx]
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    frame.categories.forEach { (catName, items) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(catName, style = MaterialTheme.typography.labelLarge, color = frame.accentColor, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    items.forEach { item ->
                                        val isSelected = simulatedSelectedItems.contains(item)
                                        
                                        val bgColor = animateColorAsState(
                                            targetValue = if (isSelected) frame.accentColor.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.1f),
                                            animationSpec = tween(600),
                                            label = "bg"
                                        )
                                        val iconColor = animateColorAsState(
                                            targetValue = if (isSelected) frame.accentColor else Color.Gray.copy(alpha = 0.4f),
                                            animationSpec = tween(600),
                                            label = "tint"
                                        )

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(bgColor.value),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getIconForItem(item),
                                                    contentDescription = null,
                                                    tint = iconColor.value,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                item, 
                                                style = MaterialTheme.typography.labelSmall, 
                                                color = if (isSelected) frame.accentColor else Color.Gray,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = frame.title,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = frame.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
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
fun AnnualLookBackSlide(isVisible: Boolean) {
    var activeThemeIdx by remember { mutableStateOf(0) }
    val themes = listOf(
        ThemeData(
            "Default Theme", 
            Color(0xFFFFFBF4), 
            Color(0xFF8C7E6A), 
            MoonThemeType.DEFAULT,
            listOf(Color(0xFFFFF9E1), Color(0xFFFFE082), Color(0xFFFFB74D), Color(0xFFFB8C00), Color(0xFF5D4037))
        ),
        ThemeData(
            "Coffee Theme", 
            Color(0xFFF7F5F4), 
            Color(0xFF8D6E63), 
            MoonThemeType.COFFEE,
            listOf(Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF5D4037))
        ),
        ThemeData(
            "Blushing Theme", 
            Color(0xFFFFF0F3), 
            Color(0xFFD2847A), 
            MoonThemeType.BLUSHING,
            listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFEF9A9A), Color(0xFFE57373), Color(0xFFD32F2F))
        ),
        ThemeData(
            "Galaxy Theme", 
            Color(0xFFF0F2F9), 
            Color(0xFF3F51B5), 
            MoonThemeType.GALAXY,
            listOf(Color(0xFFE8EAF6), Color(0xFFC5CAE9), Color(0xFF9FA8DA), Color(0xFF7986CB), Color(0xFF3F51B5))
        ),
        ThemeData(
            "Matcha Theme", 
            Color(0xFFF1F8E9), 
            Color(0xFF4CAF50), 
            MoonThemeType.MATCHA,
            listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5), Color(0xFFAED581), Color(0xFF81C784), Color(0xFF4CAF50))
        )
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (true) {
                activeThemeIdx = 0
                delay(1200)
                activeThemeIdx = 1
                delay(1200)
                activeThemeIdx = 2
                delay(1200)
                activeThemeIdx = 3
                delay(1200)
                activeThemeIdx = 4
                delay(1200)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        SlideHeader(
            title = "Monthly themes",
            description = "Pick a different theme for each month",
            isVisible = isVisible
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
            AnimatedContent(
                targetState = activeThemeIdx,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                },
                label = "theme_slide",
                modifier = Modifier.fillMaxSize()
            ) { idx ->
                val theme = themes[idx]
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(theme.bgColor)
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            // Days of week header
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                    val isSat = day == "Sat"
                                    val isSun = day == "Sun"
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
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = theme.name,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                color = theme.accentColor,
                                fontWeight = FontWeight.Bold
                            )
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
fun AdvancedStatsSlide(isVisible: Boolean) {
    var activeFrame by remember { mutableStateOf(0) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (true) {
                activeFrame = 0
                delay(4000)
                activeFrame = 1
                delay(4000)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        SlideHeader(
            title = "Learn about you",
            description = "Gain new insights about your life and mood",
            isVisible = isVisible
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
            AnimatedContent(
                targetState = activeFrame,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                },
                label = "stats_slide"
            ) { frameIdx ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
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

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Mood Flow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
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

                // Draw Y-axis Mood Dots
                moodColors.forEachIndexed { i, color ->
                    val y = (height / (moodColors.size - 1)) * i
                    drawCircle(color, radius = 6.dp.toPx(), center = Offset(15.dp.toPx(), y))
                }
                
                // Draw Vertical Grid Lines and Labels
                val xSteps = 7
                val dates = listOf("6/1", "6/6", "6/11", "6/16", "6/21", "6/26", "6/31")
                repeat(xSteps) { i ->
                    val x = startX + (width / (xSteps - 1)) * i
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Jagged Path Points (Matching the image)
                val rawPoints = listOf(
                    Offset(0f, 0.1f), Offset(0.05f, 0.3f), Offset(0.1f, 0.5f), 
                    Offset(0.15f, 0.7f), Offset(0.2f, 0.3f), Offset(0.23f, 0.1f),
                    Offset(0.26f, 0.1f), Offset(0.3f, 0.4f), Offset(0.33f, 0.7f),
                    Offset(0.37f, 0.4f), Offset(0.42f, 0.1f), Offset(0.45f, 0.1f),
                    Offset(0.48f, 0.1f), Offset(0.53f, 0.5f), Offset(0.58f, 0.8f),
                    Offset(0.62f, 0.9f), Offset(0.66f, 0.7f), Offset(0.7f, 0.4f),
                    Offset(0.73f, 0.9f), Offset(0.77f, 0.9f), Offset(0.82f, 0.7f),
                    Offset(0.86f, 0.6f), Offset(0.9f, 0.4f), Offset(0.95f, 0.1f),
                    Offset(1f, 0.1f), Offset(1.05f, 0.5f)
                )
                
                val points = rawPoints.map { Offset(startX + it.x * (width / 1f), it.y * height) }

                val path = Path()
                points.forEachIndexed { index, p ->
                    if (index == 0) path.moveTo(p.x, p.y)
                    else path.lineTo(p.x, p.y)
                }
                
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, false)
                val partialPath = Path()
                pathMeasure.getSegment(0f, pathMeasure.length * pathProgress.value, partialPath, true)
                
                val chartGreen = Color(0xFF4CAF50)
                drawPath(partialPath, chartGreen, style = Stroke(width = 2.dp.toPx()))
                
                // Draw dots at points
                points.forEach { p ->
                    if (p.x <= startX + width * pathProgress.value && p.x <= startX + width) {
                        drawCircle(chartGreen, radius = 2.5.dp.toPx(), center = p)
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

    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Mood Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(32.dp))
        
        val items = listOf(
            Triple(5, "13%", Color(0xFFFFF9E1)), // Very Happy
            Triple(4, "13%", Color(0xFFFFE082)), // Happy
            Triple(3, "38%", Color(0xFFFFB74D)), // Neutral
            Triple(2, "13%", Color(0xFFFB8C00)), // Sad
            Triple(1, "25%", Color(0xFF5D4037))  // Very Sad
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
        
        Spacer(modifier = Modifier.height(40.dp))
        
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
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600, delayMillis = 200)),
            exit = fadeOut()
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
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
