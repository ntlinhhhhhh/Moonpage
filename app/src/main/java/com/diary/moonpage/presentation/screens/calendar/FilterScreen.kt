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
import androidx.compose.ui.draw.alpha
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
    val isAnySelected = selectedItem != null
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) MaterialTheme.colorScheme.background else Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "When did I record...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color.White else Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selection Display Box (Pill)
        Box(
            modifier = Modifier
                .height(56.dp)
                .defaultMinSize(minWidth = 120.dp)
                .align(Alignment.CenterHorizontally)
                .background(if (isDark) Color(0xFF333333) else Color(0xFFF2F2F2), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedItem == null) {
                // Empty pill
                Spacer(modifier = Modifier.width(120.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SelectedItemDisplay(selectedItem!!, themeType)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
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
                            isAnySelected = isAnySelected,
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
                                isAnySelected = isAnySelected,
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
                    FilterItem.Special("sleep_long", "6-8h of sleep", Icons.Rounded.Timer),
                    FilterItem.Special("menstruation", "Menstruation", Icons.Rounded.WaterDrop)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    specials.forEach { special ->
                        val isSelected = (selectedItem as? FilterItem.Special)?.id == special.id
                        SpecialFilterItem(
                            item = special,
                            isSelected = isSelected,
                            isAnySelected = isAnySelected,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnBgColor,
                    contentColor = com.diary.moonpage.core.theme.MoonTheme.customColors.cancelBtnTextColor
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Reset", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onSeeResults(selectedItem) },
                modifier = Modifier.weight(1.5f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("See results", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SelectedItemDisplay(item: FilterItem, themeType: com.diary.moonpage.core.theme.MoonThemeType) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black

    when (item) {
        is FilterItem.Mood -> {
            val visual = MoonIcons.Moods.getMoodVisual(item.id, themeType)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(visual.color),
                contentAlignment = Alignment.Center
            ) {
                if (visual.drawableRes != null) {
                    Image(
                        painter = painterResource(id = visual.drawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        is FilterItem.Activity -> {
            val icon = MoonIcons.getIconForActivity(item.name)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF404040) else MoonTheme.customColors.logItemSelect),
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(
                        painter = painterResource(id = icon.drawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (icon.vector != null) {
                    Icon(
                        icon.vector,
                        contentDescription = null,
                        tint = icon.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(item.name, color = textColor, fontWeight = FontWeight.Bold)
        }
        is FilterItem.Special -> {
            Icon(
                item.icon,
                contentDescription = null,
                tint = com.diary.moonpage.core.theme.MoonTheme.customColors.successColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(item.name, color = textColor, fontWeight = FontWeight.Bold)
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
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val dimmedBg = if (isDark) Color(0xFF404040) else Color(0xFFE0E0E0)
    val dimmedAlpha = 0.3f

    val bg = if (!isAnySelected || isSelected) visual.color else dimmedBg

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bg)
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
                    .then(if (isAnySelected && !isSelected) Modifier.alpha(dimmedAlpha) else Modifier)
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
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val activeBg = if (isSelected) MoonTheme.customColors.logItemSelect else if (isDark) Color(0xFF292929) else Color.White
    val dimmedBg = if (isDark) Color(0xFF404040) else Color(0xFFE0E0E0)
    
    val bg = if (!isAnySelected) activeBg else if (isSelected) activeBg else dimmedBg
    val iconTint = if (!isAnySelected) MaterialTheme.colorScheme.onSurface else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val textTint = if (!isAnySelected) MaterialTheme.colorScheme.onSurface else if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .then(if (isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)) else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = item.name,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(item.name, color = textTint, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityFilterGrid(
    items: List<FilterItem.Activity>,
    selectedId: String?,
    isAnySelected: Boolean,
    onItemClick: (FilterItem.Activity) -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val activeBg = MoonTheme.customColors.logItemBg
    val selectedBg = MoonTheme.customColors.logItemSelect
    val dimmedBg = if (isDark) Color(0xFF404040) else Color(0xFFE0E0E0)

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            val isSelected = item.id == selectedId
            val icon = MoonIcons.getIconForActivity(item.name)
            
            val bg = if (isSelected) selectedBg else if (!isAnySelected) activeBg else dimmedBg
            
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(bg)
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
                        modifier = Modifier
                            .size(32.dp)
                            .then(if (isAnySelected && !isSelected) Modifier.alpha(0.3f) else Modifier)
                    )
                } else if (icon.vector != null) {
                    Icon(
                        icon.vector,
                        contentDescription = item.name,
                        tint = if (isAnySelected && !isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else icon.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
