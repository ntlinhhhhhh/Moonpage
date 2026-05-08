package com.diary.moonpage.presentation.components.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import androidx.compose.ui.platform.LocalLocale
import kotlinx.coroutines.launch
import com.diary.moonpage.core.util.MoonIcons

@Composable
fun CalendarTopBar(
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onThemeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onFilterClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = "App Icon",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { /* TODO */ }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.BreakfastDining,
                    contentDescription = null,
                    tint = Color(0xFFFFCC80),
                    modifier = Modifier.size(28.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onThemeClick() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "Theme",
                    tint = Color(0xFFFFE082),
                    modifier = Modifier.size(28.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSettingsClick() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun CalendarHeader() {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun DayItem(
    day: Int?,
    isSelected: Boolean,
    moodColor: Color?,
    moodIcon: ImageVector? = null,
    moodDrawable: Int? = null,
    isToday: Boolean = false,
    isDimmed: Boolean = false,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    if (day == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp)
        ) {
            Box(modifier = Modifier.size(42.dp).align(Alignment.Center))
            Spacer(modifier = Modifier.height(2.dp + 14.dp))
        }
        return
    }

    val emptyDayBg = colorScheme.surfaceVariant.copy(alpha = 0.7f)

    val circleBg = when {
        moodColor != null -> moodColor
        isSelected        -> Color.Transparent
        else              -> emptyDayBg
    }

    val animatedBg by animateColorAsState(
        targetValue = circleBg,
        animationSpec = tween(200),
        label = "dayBg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(animatedBg)
                .clickable { onClick() }
                .then(
                    when {
                        isSelected && moodColor == null ->
                            Modifier.border(2.dp, colorScheme.primary, CircleShape)
                        isToday && moodColor == null ->
                            Modifier.border(2.dp, colorScheme.primary, CircleShape)
                        else -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (moodDrawable != null) {
                Image(
                    painter = painterResource(id = moodDrawable),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                        .then(if (isDimmed) Modifier.alpha(0.55f) else Modifier)
                )
            } else if (moodIcon != null) {
                Icon(
                    imageVector = moodIcon,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = if (isDimmed) 0.3f else 0.55f),
                    modifier = Modifier.fillMaxSize(0.52f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected || isToday) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayDetailArea(
    date: LocalDate,
    moodIcon: ImageVector? = null,
    moodDrawable: Int? = null,
    moodColor: Color,
    moodLabel: String,
    noteSnippet: String?,
    activityNames: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left: Mood and Date ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(moodColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (moodDrawable != null) {
                    Image(
                        painter = painterResource(id = moodDrawable),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                } else if (moodIcon != null) {
                    Icon(
                        imageVector = moodIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = cs.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${date.dayOfMonth} ${date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
            }
        }

        // --- Divider ---
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(120.dp)
                .background(cs.onSurface.copy(alpha = 0.05f))
        )
        Spacer(modifier = Modifier.width(12.dp))

        // --- Right: Activities ---
        Column(modifier = Modifier.weight(1f)) {
            if (activityNames.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 4
                ) {
                    activityNames.forEach { name ->
                        val icon = MoonIcons.getIconForActivity(name)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(icon.color.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (icon.drawableRes != null) {
                                Image(
                                    painter = painterResource(id = icon.drawableRes),
                                    contentDescription = name,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else if (icon.vector != null) {
                                Icon(
                                    imageVector = icon.vector,
                                    contentDescription = name,
                                    tint = icon.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!noteSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = noteSnippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DiaryEntryPreview(
    date: String,
    moodIcon: ImageVector? = null,
    moodDrawable: Int? = null,
    moodColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(moodColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (moodDrawable != null) {
                    Image(
                        painter = painterResource(id = moodDrawable),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else if (moodIcon != null) {
                    Icon(
                        imageVector = moodIcon,
                        contentDescription = null,
                        tint = moodColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = date,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailBottomSheet(
    date: LocalDate,
    moodIcon: ImageVector? = null,
    moodDrawable: Int? = null,
    moodColor: Color,
    moodLabel: String,
    noteSnippet: String?,
    activityNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(cs.onSurface.copy(alpha = 0.15f), CircleShape)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DayDetailArea(
                    date = date,
                    moodIcon = moodIcon,
                    moodDrawable = moodDrawable,
                    moodColor = moodColor,
                    moodLabel = moodLabel,
                    noteSnippet = noteSnippet,
                    activityNames = activityNames
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onSurface)
                    ) {
                        Icon(Icons.Rounded.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = cs.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = cs.onPrimary, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error),
                        border = BorderStroke(1.dp, cs.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    currentYearMonth: YearMonth,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentYear = java.time.LocalDate.now().year
    val years = remember { (2000..currentYear + 10).map { it.toString() } }
    val months = remember { (1..12).map {
        java.time.Month.of(it).getDisplayName(TextStyle.FULL, Locale.getDefault()).take(3)
    } }

    var tempYear by remember { mutableIntStateOf(currentYearMonth.year) }
    var tempMonth by remember { mutableIntStateOf(currentYearMonth.monthValue) }
    
    val itemHeight = 44.dp

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight * 3),
                    contentAlignment = Alignment.Center
                ) {
                    // Selection indicators spanning across both pickers
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(itemHeight))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            WheelPicker(
                                items = months,
                                initialValue = java.time.Month.of(tempMonth).getDisplayName(TextStyle.FULL, Locale.getDefault()).take(3),
                                onItemSelected = { monthName ->
                                    val monthIndex = months.indexOf(monthName)
                                    if (monthIndex != -1) tempMonth = monthIndex + 1
                                },
                                isCircular = true,
                                itemHeight = itemHeight,
                                showLines = false
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            WheelPicker(
                                items = years,
                                initialValue = tempYear.toString(),
                                onItemSelected = { yearStr ->
                                    tempYear = yearStr.toIntOrNull() ?: tempYear
                                },
                                isCircular = false,
                                itemHeight = itemHeight,
                                showLines = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirm(tempYear, tempMonth) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

private const val PICKER_INFINITE_MULTIPLIER = 1000

@Composable
private fun WheelPicker(
    items: List<String>,
    initialValue: String,
    onItemSelected: (String) -> Unit,
    isCircular: Boolean = false,
    itemHeight: androidx.compose.ui.unit.Dp = 44.dp,
    showLines: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val count = items.size
    
    val initialIndex = items.indexOf(initialValue).coerceAtLeast(0)
    val startIndex = if (isCircular) PICKER_INFINITE_MULTIPLIER / 2 * count + initialIndex else initialIndex
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedRealIndex by remember {
        derivedStateOf { 
            if (isCircular) listState.firstVisibleItemIndex % count 
            else listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex)
        }
    }

    LaunchedEffect(selectedRealIndex) {
        onItemSelected(items[selectedRealIndex])
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (showLines) {
            // Selection indicators (lines)
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .align(Alignment.Center)
                    .offset(y = -itemHeight / 2),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .align(Alignment.Center)
                    .offset(y = itemHeight / 2),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
        }

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val totalItems = if (isCircular) PICKER_INFINITE_MULTIPLIER * count else count
            items(totalItems) { index ->
                val realIndex = if (isCircular) index % count else index
                val isSelected = index == listState.firstVisibleItemIndex
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[realIndex],
                        style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Top click area (scroll up)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(itemHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(listState.firstVisibleItemIndex - 1)
                    }
                }
        )

        // Bottom click area (scroll down)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(itemHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
                    }
                }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareModeBottomSheet(
    onModeSelected: (Boolean) -> Unit, // true for 1x1, false for 9x16
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share Mood Calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShareModeItem(
                    title = "Square (1:1)",
                    icon = Icons.Rounded.CropSquare,
                    onClick = { onModeSelected(true) },
                    modifier = Modifier.weight(1f)
                )
                ShareModeItem(
                    title = "Portrait (9:16)",
                    icon = Icons.Rounded.StayCurrentPortrait,
                    onClick = { onModeSelected(false) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ShareModeItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShareCalendarCard(
    yearMonth: YearMonth,
    dailyLogs: Map<LocalDate, com.diary.moonpage.domain.model.DailyLog>,
    isSquare: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val monthName = yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))
    
    Box(
        modifier = Modifier
            .size(if (isSquare) 1080.dp else 1080.dp, if (isSquare) 1080.dp else 1920.dp)
            .background(colorScheme.background)
            .padding(if (isSquare) 60.dp else 80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MoonPage",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.primary,
                fontSize = if (isSquare) 48.sp else 64.sp
            )
            Spacer(modifier = Modifier.height(if (isSquare) 16.dp else 32.dp))
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = if (isSquare) 32.sp else 40.sp
            )
            
            Spacer(modifier = Modifier.height(if (isSquare) 48.dp else 80.dp))
            
            // Simplified Calendar Grid
            val firstDay = yearMonth.atDay(1)
            val firstDayOffset = if (firstDay.dayOfWeek == java.time.DayOfWeek.SUNDAY) 0 else firstDay.dayOfWeek.value
            val daysInMonth = yearMonth.lengthOfMonth()
            
            val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val totalCells = firstDayOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            
            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (colIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + colIndex
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (cellIndex in firstDayOffset until totalCells) {
                                val dayNum = cellIndex - firstDayOffset + 1
                                val date = yearMonth.atDay(dayNum)
                                val log = dailyLogs[date]
                                val moodId = log?.baseMoodId ?: 0
                                
                                val moodVisual = MoonIcons.Moods.getMoodVisual(moodId)
                                
                                Box(
                                    modifier = Modifier
                                        .size(if (isSquare) 80.dp else 100.dp)
                                        .background(
                                            if (moodId != 0) moodVisual.color else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (moodId != 0 && moodVisual.drawableRes != null) {
                                        Image(
                                            painter = painterResource(id = moodVisual.drawableRes),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(0.7f)
                                        )
                                    } else {
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(if (isSquare) 80.dp else 100.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (!isSquare) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Reflect on your journey with MoonPage",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Footer/Watermark
        if (isSquare) {
            Text(
                text = "moonpage.app",
                modifier = Modifier.align(Alignment.BottomCenter),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun CalendarSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) { data ->
        val isError = data.visuals.message.contains("future", ignoreCase = true) ||
                data.visuals.message.contains("Failed", ignoreCase = true) ||
                data.visuals.message.contains("Please select", ignoreCase = true)
        val isSuccess = data.visuals.message.contains("success", ignoreCase = true) ||
                data.visuals.message.contains("deleted", ignoreCase = true) ||
                data.visuals.message.contains("recorded", ignoreCase = true) ||
                data.visuals.message.contains("updated", ignoreCase = true) ||
                data.visuals.message.contains("edited", ignoreCase = true)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isError -> Icons.Rounded.Error
                    isSuccess -> Icons.Rounded.CheckCircle
                    else -> Icons.Rounded.Info
                },
                contentDescription = null,
                tint = when {
                    isError -> Color(0xFFE57373)
                    isSuccess -> Color(0xFF81C784)
                    else -> Color(0xFFFFB74D)
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.visuals.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
