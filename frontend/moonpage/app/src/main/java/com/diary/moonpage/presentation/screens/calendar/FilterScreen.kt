package com.diary.moonpage.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.presentation.theme.MoonTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FilterScreen(
    onDismiss: () -> Unit,
    onSeeResults: () -> Unit,
    mainViewModel: com.diary.moonpage.MainViewModel = hiltViewModel()
) {
    val themeType by mainViewModel.themeType.collectAsState()
    
    // Stateful logic for filters
    var selectedMood by remember { mutableStateOf<Int?>(null) }
    val selectedHobbies = remember { mutableStateListOf<String>() }

    FilterContent(
        selectedMood = selectedMood,
        themeType = themeType,
        onMoodSelect = { selectedMood = it },
        onDismiss = onDismiss,
        onReset = {
            selectedMood = null
            selectedHobbies.clear()
        },
        onSeeResults = onSeeResults
    )
}

@Composable
fun FilterContent(
    selectedMood: Int?,
    themeType: com.diary.moonpage.presentation.theme.MoonThemeType,
    onMoodSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onSeeResults: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp)) // To center title
                Text(
                    text = "When did I record...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    // Mood Section
                    FilterSectionTitle("Mood")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(5) { i ->
                            val moodId = i + 1
                            val color = MoonIcons.Moods.getMoodColor(moodId, themeType)
                            val visual = MoonIcons.Moods.getMoodVisual(moodId, themeType)
                            MoodItem(
                                color = color,
                                visual = visual,
                                isSelected = selectedMood == moodId,
                                onClick = { onMoodSelect(moodId) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    // Hobbies Section
                    FilterSectionTitle("Hobbies")
                    val hobbies = listOf(
                        Icons.Rounded.FitnessCenter, Icons.Rounded.Tv, Icons.Rounded.LocalMovies,
                        Icons.Rounded.Gamepad, Icons.Rounded.MenuBook, Icons.Rounded.DirectionsRun,
                        Icons.Rounded.Headphones, Icons.Rounded.Palette
                    )
                    FlowRow(hobbies)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    // Emotions Section
                    FilterSectionTitle("Emotions")
                    val emotions = listOf(Icons.Rounded.Weekend, Icons.Rounded.Bedtime)
                    FlowRow(emotions)
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MoonTheme.customColors.logItemBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onSeeResults,
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("See results", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun MoodItem(color: Color, visual: com.diary.moonpage.core.util.MoonIcon, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(if (isSelected) color else color.copy(alpha = 0.2f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (visual.drawableRes != null) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = visual.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(icons: List<ImageVector>) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icons.forEach { icon ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MoonTheme.customColors.logItemBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
