package com.diary.moonpage.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.core.util.MoonIcon
import com.diary.moonpage.core.util.MoonIcons
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

// ── Category data ─────────────────────────────────────────────────────────────

data class ActivityCategoryInfo(
    val key: String,
    val displayName: String,
    val subtitle: String,
    val previewIcons: List<MoonIcon>
)

val ALL_ACTIVITY_CATEGORIES = listOf(
    ActivityCategoryInfo("Hobbies",      "Hobbies",      "exercise, movie, reading, ...",     listOf(MoonIcons.Hobbies.Exercise,      MoonIcons.Hobbies.Movie,          MoonIcons.Hobbies.Gaming)),
    ActivityCategoryInfo("Emotions",     "Emotions",     "happy, proud, anxious, ...",        listOf(MoonIcons.Emotions.Happy,        MoonIcons.Emotions.Proud,         MoonIcons.Emotions.Anxious)),
    ActivityCategoryInfo("Meals",        "Meals",        "breakfast, lunch, dinner, ...",     listOf(MoonIcons.Meals.Breakfast,       MoonIcons.Meals.Lunch,            MoonIcons.Meals.Dinner)),
    ActivityCategoryInfo("SelfCare",     "Self-Care",    "shower, brush teeth, ...",          listOf(MoonIcons.SelfCare.Shower,       MoonIcons.SelfCare.BrushTeeth,    MoonIcons.SelfCare.WashFace)),
    ActivityCategoryInfo("Chores",       "Chores",       "cleaning, laundry, dishes, ...",    listOf(MoonIcons.Chores.Cleaning,       MoonIcons.Chores.Cooking,         MoonIcons.Chores.Laundry)),
    ActivityCategoryInfo("Events",       "Events",       "stay home, cafe, travel, ...",      listOf(MoonIcons.Events.StayHome,       MoonIcons.Events.Cafe,            MoonIcons.Events.Travel)),
    ActivityCategoryInfo("People",       "People",       "friends, family, partner, ...",     listOf(MoonIcons.People.Friends,        MoonIcons.People.Family,          MoonIcons.People.Partner)),
    ActivityCategoryInfo("Beauty",       "Beauty",       "hair, nails, skincare, ...",        listOf(MoonIcons.Beauty.Hair,           MoonIcons.Beauty.Nails,           MoonIcons.Beauty.Skincare)),
    ActivityCategoryInfo("Weather",      "Weather",      "sunny, cloudy, rainy, ...",         listOf(MoonIcons.Weather.Sunny,         MoonIcons.Weather.Cloudy,         MoonIcons.Weather.Rainy)),
    ActivityCategoryInfo("Health",       "Health",       "sick, hospital, medicine, ...",     listOf(MoonIcons.Health.Sick,           MoonIcons.Health.Hospital,        MoonIcons.Health.Medicine)),
    ActivityCategoryInfo("Work",         "Work",         "overtime, vacation, ...",           listOf(MoonIcons.Work.Work,             MoonIcons.Work.Overtime,          MoonIcons.Work.Vacation)),
    ActivityCategoryInfo("Other",        "Other",        "snack, coffee, tea, ...",           listOf(MoonIcons.Other.Snack,           MoonIcons.Other.Coffee,           MoonIcons.Other.Tea)),
    ActivityCategoryInfo("School",       "School",       "class, homework, exam, ...",        listOf(MoonIcons.School.Class,          MoonIcons.School.Study,           MoonIcons.School.Exam)),
    ActivityCategoryInfo("Relationship", "Relationship", "date, anniversary, gift, ...",      listOf(MoonIcons.Relationship.Date,     MoonIcons.Relationship.Anniversary, MoonIcons.Relationship.Gift))
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ActivityCategorySelectionScreen(
    viewModel: ActivityCategoryViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val enabledCategories by viewModel.enabledCategories.collectAsState()

    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No pressure, you can always edit this later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onBackground.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.save(onDone = onNext) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Next", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    // Skip = dùng default categories + mark onboarding completed
                    onClick = { viewModel.saveDefaults(onDone = onSkip) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for now", color = colorScheme.onBackground.copy(alpha = 0.45f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Top picks for you! ✨",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Edit the list to keep only what you want!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = {}) {
                        Text(
                            text = "What am I choosing?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onBackground.copy(alpha = 0.55f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Category cards
            items(ALL_ACTIVITY_CATEGORIES) { category ->
                val isSelected = category.key in enabledCategories
                ActivityCategoryCard(
                    category = category,
                    isSelected = isSelected,
                    onClick = { viewModel.toggle(category.key) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ── Category Card ─────────────────────────────────────────────────────────────

@Composable
private fun ActivityCategoryCard(
    category: ActivityCategoryInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox circle
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(2.dp, colorScheme.onBackground.copy(alpha = 0.25f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.45f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Preview icons (3 circles)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                category.previewIcons.take(3).forEach { moonIcon ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(moonIcon.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (moonIcon.drawableRes != null) {
                            Image(
                                painter = painterResource(id = moonIcon.drawableRes),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (moonIcon.vector != null) {
                            Icon(
                                imageVector = moonIcon.vector,
                                contentDescription = null,
                                tint = moonIcon.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
