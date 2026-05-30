package com.diary.moonpage.ui.screens.calendar

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.R
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.core.theme.MoonTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.core.util.getTranslatedActivityName

/**
 * Stateful Component
 */
@Composable
fun FilterRoute(
    onDismiss: () -> Unit,
    onSeeResults: (List<FilterItem>) -> Unit,
    currentFilters: List<FilterItem> = emptyList(),
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity> = emptyList(),
    themeType: com.diary.moonpage.core.theme.MoonThemeType = com.diary.moonpage.core.theme.MoonThemeType.DEFAULT
) {
    FilterScreen(
        onDismiss = onDismiss,
        onSeeResults = onSeeResults,
        currentFilters = currentFilters,
        dynamicActivities = dynamicActivities,
        themeType = themeType
    )
}

/**
 * Stateless Component
 */
@Composable
fun FilterScreen(
    onDismiss: () -> Unit,
    onSeeResults: (List<FilterItem>) -> Unit,
    currentFilters: List<FilterItem> = emptyList(),
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity> = emptyList(),
    themeType: com.diary.moonpage.core.theme.MoonThemeType = com.diary.moonpage.core.theme.MoonThemeType.DEFAULT
) {
    var selectedItems by remember { mutableStateOf(currentFilters.toSet()) }
    val groupedActivities = remember(dynamicActivities) { dynamicActivities.groupBy { it.category } }
    val categories = remember(groupedActivities) { groupedActivities.keys.toList() }
    var expandedCategories by remember { 
        mutableStateOf(
            currentFilters.filterIsInstance<FilterItem.Activity>()
                .mapNotNull { activityFilter -> dynamicActivities.find { it.id == activityFilter.id }?.category }
                .toSet()
        )
    }

    val isAnySelected = selectedItems.isNotEmpty()
    val colorScheme = MaterialTheme.colorScheme
    val isActuallyDark = colorScheme.surface.let { (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5 }
    
    val bgColor = if (isActuallyDark) com.diary.moonpage.core.theme.MoonBgDark else Color.White
    val textColor = if (isActuallyDark) Color.White else Color.Black
    val pillBg = if (isActuallyDark) Color(0xFF262626) else Color(0xFFF2F2F2)
    val closeIconTint = if (isActuallyDark) Color(0xFFAEAEAE) else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .background(bgColor)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { 
                    selectedItems = emptySet()
                    expandedCategories = emptySet()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = stringResource(R.string.filter_reset),
                    tint = closeIconTint
                )
            }

            Text(
                text = stringResource(R.string.when_did_i_record),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = closeIconTint)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selection Display Box (Pill)
        Box(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(pillBg, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedItems.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_icons_selected),
                    color = textColor.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(selectedItems.toList()) { item ->
                            SelectedItemDisplay(item, themeType)
                        }
                    }
                    
                    // Small Reset Button inside the pill
                    IconButton(
                        onClick = { 
                            selectedItems = emptySet()
                            expandedCategories = emptySet()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Cancel,
                            contentDescription = stringResource(R.string.clear_all),
                            tint = textColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Mood Section
                FilterSectionTitle(stringResource(R.string.filter_mood))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (5 downTo 1).forEach { moodId ->
                        val visual = MoonIcons.Moods.getMoodVisual(moodId, themeType)
                        val isSelected = selectedItems.any { (it as? FilterItem.Mood)?.id == moodId }
                        MoodFilterItem(
                            visual = visual,
                            isSelected = isSelected,
                            isAnySelected = isAnySelected,
                            onClick = { 
                                val item = FilterItem.Mood(moodId)
                                selectedItems = if (isSelected) {
                                    selectedItems.filterNot { (it as? FilterItem.Mood)?.id == moodId }.toSet()
                                } else {
                                    selectedItems + item
                                }
                            }
                        )
                    }
                }
            }

            item {
                // Activity Category Bar
                FilterSectionTitle(stringResource(R.string.filter_activities))
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = expandedCategories.contains(category)
                        val label = com.diary.moonpage.core.util.getTranslatedActivityCategoryName(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                expandedCategories = if (isSelected) {
                                    expandedCategories - category
                                } else {
                                    expandedCategories + category
                                }
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = pillBg,
                                labelColor = textColor
                            ),
                            border = null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Grouped Activities Sections (only show if expanded)
            categories.forEach { category ->
                if (expandedCategories.contains(category)) {
                    val activities = groupedActivities[category] ?: emptyList()
                    item {
                        Column {
                            FilterSectionTitle(category.ifBlank { stringResource(R.string.filter_activities) })
                            val activityItems = activities.map { 
                                FilterItem.Activity(it.id, it.name)
                            }
                            ActivityFilterGrid(
                                items = activityItems,
                                selectedItems = selectedItems.filterIsInstance<FilterItem.Activity>(),
                                isAnySelected = isAnySelected,
                                onItemClick = { activityItem ->
                                    val isSelected = selectedItems.any { (it as? FilterItem.Activity)?.id == activityItem.id }
                                    selectedItems = if (isSelected) {
                                        selectedItems.filterNot { (it as? FilterItem.Activity)?.id == activityItem.id }.toSet()
                                    } else {
                                        selectedItems + activityItem
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                // Special Section
                FilterSectionTitle(stringResource(R.string.filter_special))
                val specials = listOf(
                    FilterItem.Special("music", R.string.filter_music, Icons.Rounded.MusicNote),
                    FilterItem.Special("sleep", R.string.filter_sleep, Icons.Rounded.Bedtime),
                    FilterItem.Special("sleep_long", R.string.filter_sleep_six_to_eight_hours, Icons.Rounded.Timer),
                    FilterItem.Special("menstruation", R.string.filter_menstruation, Icons.Rounded.WaterDrop)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    specials.forEach { special ->
                        val isSelected = selectedItems.any { (it as? FilterItem.Special)?.id == special.id }
                        SpecialFilterItem(
                            item = special,
                            isSelected = isSelected,
                            isAnySelected = isAnySelected,
                            onClick = { 
                                selectedItems = if (isSelected) {
                                    selectedItems.filterNot { (it as? FilterItem.Special)?.id == special.id }.toSet()
                                } else {
                                    selectedItems + special
                                }
                            }
                        )
                    }
                }
            }
        }

        // Bottom Button
        Button(
            onClick = { onSeeResults(selectedItems.toList()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(stringResource(R.string.filter_see_results), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SelectedItemDisplay(item: FilterItem, themeType: com.diary.moonpage.core.theme.MoonThemeType) {
    val colorScheme = MaterialTheme.colorScheme
    val isActuallyDark = colorScheme.surface.let { (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5 }

    when (item) {
        is FilterItem.Mood -> {
            val visual = MoonIcons.Moods.getMoodVisual(item.id, themeType)
            val moodColor = if (isActuallyDark) {
                MoonIcons.brightenMoodColorForDarkMode(visual.color, amount = 0.32f)
            } else {
                visual.color
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(moodColor),
                contentAlignment = Alignment.Center
            ) {
                if (visual.drawableRes != null) {
                    Image(
                        painter = painterResource(id = visual.drawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        is FilterItem.Activity -> {
            val icon = MoonIcons.getIconForActivity(item.name)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isActuallyDark) Color(0xFF404040) else MoonTheme.customColors.logItemSelect),
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(
                        painter = painterResource(id = icon.drawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (icon.vector != null) {
                    Icon(
                        icon.vector,
                        contentDescription = null,
                        tint = icon.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        is FilterItem.Special -> {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isActuallyDark) Color(0xFF404040) else MoonTheme.customColors.logItemSelect),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = com.diary.moonpage.core.theme.MoonTheme.customColors.successColor,
                    modifier = Modifier.size(20.dp)
                )
            }
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
    isAnySelected: Boolean,
    onClick: () -> Unit
) {
    val isActuallyDark = MaterialTheme.colorScheme.surface.let { (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5 }
    val moodColor = if (isActuallyDark) {
        MoonIcons.brightenMoodColorForDarkMode(visual.color, amount = 0.32f)
    } else {
        visual.color
    }
    
    val bg = if (isActuallyDark) {
        val dimmedBg = Color(0xFF3A3A3A)
        if (isSelected) moodColor else dimmedBg
    } else {
        val dimmedBg = Color(0xFFF2F2F2)
        if (isSelected) moodColor else dimmedBg
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
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
                modifier = Modifier
                    .size(40.dp)
                    .then(if (isAnySelected && !isSelected) Modifier.alpha(0.4f) else Modifier)
            )
        }
    }
}

@Composable
fun SpecialFilterItem(
    item: FilterItem.Special,
    isSelected: Boolean,
    isAnySelected: Boolean,
    onClick: () -> Unit
) {
    val isActuallyDark = MaterialTheme.colorScheme.surface.let { (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5 }
    
    val bg = if (isActuallyDark) {
        val dimmedBg = Color(0xFF262626)
        if (isSelected) Color(0xFF404040) else MoonTheme.customColors.logItemBg
    } else {
        val activeBg = if (isSelected) MoonTheme.customColors.logItemSelect else Color(0xFFF2F2F2)
        activeBg
    }
    
    val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAnySelected) 0.4f else 1f)
    val textTint = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAnySelected) 0.4f else 1f)

    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .then(if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = stringResource(item.nameRes),
            tint = if (isActuallyDark && isSelected) Color.White else iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(item.nameRes), color = if (isActuallyDark && isSelected) Color.White else textTint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityFilterGrid(
    items: List<FilterItem.Activity>,
    selectedItems: List<FilterItem.Activity>,
    isAnySelected: Boolean,
    onItemClick: (FilterItem.Activity) -> Unit
) {
    val isActuallyDark = MaterialTheme.colorScheme.surface.let { (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5 }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            val isSelected = selectedItems.any { it.id == item.id }
            val iconAlpha = if (isSelected) 1f else 0.4f
            val icon = MoonIcons.getIconForActivity(item.name)
            
            val bg = if (isActuallyDark) {
                val dimmedBg = Color(0xFF262626)
                if (isSelected) Color(0xFF404040) else MoonTheme.customColors.logItemBg
            } else {
                val activeBg = Color(0xFFF2F2F2)
                val selectedBg = MoonTheme.customColors.logItemSelect
                if (isSelected) selectedBg else activeBg
            }
            
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .then(if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onItemClick(item) },
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(
                        painter = painterResource(id = icon.drawableRes),
                        contentDescription = getTranslatedActivityName(item.name),
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(iconAlpha)
                    )
                } else if (icon.vector != null) {
                    Icon(
                        icon.vector,
                        contentDescription = getTranslatedActivityName(item.name),
                        tint = icon.color.copy(alpha = iconAlpha),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
