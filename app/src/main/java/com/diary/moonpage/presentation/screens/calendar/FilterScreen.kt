package com.diary.moonpage.presentation.screens.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.core.theme.MoonTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FilterScreen(
    onDismiss: () -> Unit,
    onSeeResults: (FilterItem?) -> Unit,
    currentFilter: FilterItem? = null,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity> = emptyList(),
    themeType: com.diary.moonpage.core.theme.MoonThemeType = com.diary.moonpage.core.theme.MoonThemeType.DEFAULT
) {
    var selectedItem by remember { mutableStateOf<FilterItem?>(currentFilter) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                }
                Text(
                    text = "When did I record...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Selection Display Box
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedItem == null) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    SelectedItemDisplay(selectedItem!!, themeType)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    // Mood Section
                    FilterSectionTitle("Mood")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..5).forEach { moodId ->
                            val visual = MoonIcons.Moods.getMoodVisual(moodId, themeType)
                            val isSelected = (selectedItem as? FilterItem.Mood)?.id == moodId
                            MoodFilterItem(
                                visual = visual,
                                isSelected = isSelected,
                                onClick = { selectedItem = FilterItem.Mood(moodId) }
                            )
                        }
                    }
                }

                item {
                    // Grouped Activities Section
                    val groupedActivities = dynamicActivities.groupBy { it.category }
                    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                        groupedActivities.forEach { (category, activities) ->
                            Column {
                                FilterSectionTitle(category.ifBlank { "Activities" })
                                val activityItems = activities.map { 
                                    FilterItem.Activity(it.id, it.name)
                                }
                                ActivityFilterGrid(
                                    items = activityItems,
                                    selectedId = (selectedItem as? FilterItem.Activity)?.id,
                                    onItemClick = { selectedItem = it }
                                )
                            }
                        }
                    }
                }

                item {
                    // Special Section
                    FilterSectionTitle("Special")
                    val specials = listOf(
                        FilterItem.Special("music", "Music", Icons.Rounded.MusicNote),
                        FilterItem.Special("sleep", "Sleep", Icons.Rounded.Bedtime),
                        FilterItem.Special("sleep_long", "6-8h Sleep", Icons.Rounded.Timer),
                        FilterItem.Special("menstruation", "Menstruation", Icons.Rounded.WaterDrop)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        specials.forEach { special ->
                            val isSelected = (selectedItem as? FilterItem.Special)?.id == special.id
                            SpecialFilterItem(
                                item = special,
                                isSelected = isSelected,
                                onClick = { selectedItem = special }
                            )
                        }
                    }
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
                    onClick = { selectedItem = null },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSeeResults(selectedItem) },
                    modifier = Modifier.weight(1.5f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("See results", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SelectedItemDisplay(item: FilterItem, themeType: com.diary.moonpage.core.theme.MoonThemeType) {
    when (item) {
        is FilterItem.Mood -> {
            val visual = MoonIcons.Moods.getMoodVisual(item.id, themeType)
            if (visual.drawableRes != null) {
                Image(
                    painter = painterResource(id = visual.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        is FilterItem.Activity -> {
            val icon = MoonIcons.getIconForActivity(item.name)
            if (icon.drawableRes != null) {
                Image(
                    painter = painterResource(id = icon.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
            } else if (icon.vector != null) {
                Icon(
                    icon.vector,
                    contentDescription = null,
                    tint = icon.color,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
        is FilterItem.Special -> {
            Icon(
                item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun MoodFilterItem(
    visual: com.diary.moonpage.core.util.MoonIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isSelected) visual.color.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (visual.drawableRes != null) {
            Image(
                painter = painterResource(id = visual.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(if (isSelected) 44.dp else 40.dp)
            )
        }
    }
}

@Composable
fun SpecialFilterItem(
    item: FilterItem.Special,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            item.icon,
            contentDescription = item.name,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityFilterGrid(
    items: List<FilterItem.Activity>,
    selectedId: String?,
    onItemClick: (FilterItem.Activity) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            val isSelected = item.id == selectedId
            val icon = MoonIcons.getIconForActivity(item.name)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) icon.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onItemClick(item) },
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(
                        painter = painterResource(id = icon.drawableRes),
                        contentDescription = item.name,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (icon.vector != null) {
                    Icon(
                        icon.vector,
                        contentDescription = item.name,
                        tint = if (isSelected) icon.color else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
