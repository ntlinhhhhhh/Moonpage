package com.diary.moonpage.presentation.components.calendar

import com.diary.moonpage.presentation.screens.calendar.CalendarViewMode

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import com.diary.moonpage.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import com.diary.moonpage.core.theme.LocalLocale
import com.diary.moonpage.core.theme.MoonPageTheme
import com.diary.moonpage.core.theme.MoonTheme
import kotlinx.coroutines.launch
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.core.theme.MoonThemeType

@Composable
fun CalendarTopBar(
    viewMode: CalendarViewMode,
    onFilterClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onThemeClick: () -> Unit = {},
    isFilterActive: Boolean = false,
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
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFilterClick() }
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = stringResource(R.string.filter_title),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(0.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* TODO */ }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onThemeClick() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = stringResource(R.string.app_theme),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleViewMode() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (viewMode == CalendarViewMode.CALENDAR) Icons.Rounded.ViewHeadline else Icons.Rounded.CalendarMonth,
                    contentDescription = "Switch View",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun CalendarHeader(
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val daysOfWeek = listOf(
        stringResource(R.string.sun),
        stringResource(R.string.mon),
        stringResource(R.string.tue),
        stringResource(R.string.wed),
        stringResource(R.string.thu),
        stringResource(R.string.fri),
        stringResource(R.string.sat)
    )
    val currentDayIndex = LocalDate.now().dayOfWeek.value % 7 // Sun=0, Mon=1, ..., Sat=6
    val shades = com.diary.moonpage.core.theme.getThemeShades(themeType)
    val highlightColor = if (shades.size > 3) shades[3] else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        val isDark = MoonTheme.customColors.isDark
        daysOfWeek.forEachIndexed { index, day ->
            val isCurrentDay = index == currentDayIndex
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isCurrentDay -> if (isDark) {
                        if (shades.size > 1) shades[1] else highlightColor
                    } else {
                        if (shades.size > 4) shades[4] else highlightColor
                    }
                    isDark -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
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
    isFiltered: Boolean = false,
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    isActuallyDark: Boolean = false,
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

    val shades = remember(themeType) { com.diary.moonpage.core.theme.getThemeShades(themeType) }
    
    val circleBg = remember(moodColor, isFiltered, isDimmed, isActuallyDark, shades) {
        when {
            moodColor != null && (!isFiltered || !isDimmed) -> moodColor
            else -> if (isActuallyDark) Color(0xFF505457) else shades[0].copy(alpha = 0.4f)
        }
    }

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
                .background(circleBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
                .then(
                    if (isSelected) Modifier.border(2.dp, colorScheme.primary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (moodColor != null && (!isFiltered || !isDimmed)) {
                if (moodDrawable != null) {
                    Image(
                        painter = painterResource(id = moodDrawable),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(0.75f)
                    )
                } else if (moodIcon != null) {
                    Icon(
                        imageVector = moodIcon,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxSize(0.52f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isToday -> if (isActuallyDark) {
                    if (shades.size > 1) shades[1] else shades[0]
                } else {
                    if (shades.size > 4) shades[4] else shades[0]
                }
                isFiltered && isDimmed -> colorScheme.onSurface.copy(alpha = 0.2f)
                isSelected -> colorScheme.primary
                isActuallyDark -> Color.White.copy(alpha = 0.85f)
                else -> colorScheme.onSurface.copy(alpha = 0.6f)
            }
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
    dailyPhotos: List<String> = emptyList(),
    sleepHours: Double? = null,
    isMenstruation: Boolean = false,
    steps: Int? = null,
    musicRecord: String? = null,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val isActuallyDark = MoonTheme.customColors.isDark

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
    ) {
        // --- Left: Mood and Date ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(moodColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (moodDrawable != null) {
                    Image(
                        painter = painterResource(id = moodDrawable),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                } else if (moodIcon != null) {
                    Icon(
                        imageVector = moodIcon,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = if (isActuallyDark) MoonTheme.customColors.logItemBg else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            ) {
                val dayOfWeek = when(date.dayOfWeek.value) {
                    1 -> stringResource(R.string.mon)
                    2 -> stringResource(R.string.tue)
                    3 -> stringResource(R.string.wed)
                    4 -> stringResource(R.string.thu)
                    5 -> stringResource(R.string.fri)
                    6 -> stringResource(R.string.sat)
                    else -> stringResource(R.string.sun)
                }
                Text(
                    text = "${date.dayOfMonth} $dayOfWeek",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp
                )
            }
        }

        // --- Divider ---
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(cs.onSurface.copy(alpha = 0.1f))
        )
        Spacer(modifier = Modifier.width(16.dp))

        // --- Right: Activities and Info ---
        Column(modifier = Modifier.weight(1f)) {
            if (activityNames.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 5
                ) {
                    activityNames.forEach { name ->
                        val icon = MoonIcons.getIconForActivity(name)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    MoonTheme.customColors.logItemBg, 
                                    CircleShape
                                ),
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
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!noteSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = noteSnippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface,
                    fontSize = 15.sp
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Stats Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MoonTheme.customColors.logItemBg
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Sleep
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                        Icon(Icons.Rounded.Nightlight, contentDescription = null, tint = Color(0xFFFFCA28), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        val sleepText = if (sleepHours != null && sleepHours > 0.0) {
                            val hrs = sleepHours.toInt()
                            val mins = ((sleepHours - hrs) * 60).toInt()
                            if (mins == 0) "${hrs}h" else "${hrs}h ${mins}m"
                        } else {
                            "No data"
                        }
                        Text(sleepText, color = cs.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                    
                    // Menstruation
                    if (isMenstruation) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = Color(0xFFF48FB1), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.on_day_x, 3), color = cs.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }

                    // Steps
                    if (steps != null && steps > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.DirectionsWalk, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(String.format(Locale.ENGLISH, "%,d steps", steps), color = cs.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                }
            }

            if (!musicRecord.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Music Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MoonTheme.customColors.logItemBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cs.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = cs.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            val parts = musicRecord.split(" - ")
                            Text(parts.first(), color = cs.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            if (parts.size > 1) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(parts[1], color = cs.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            if (dailyPhotos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.photos),
                    color = cs.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    dailyPhotos.forEach { photoUrl ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val imageRequest = remember(photoUrl) {
                            coil.request.ImageRequest.Builder(context)
                                .data(photoUrl)
                                .crossfade(true)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build()
                        }
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(cs.onSurface.copy(alpha = 0.05f))
                        ) {
                            coil.compose.AsyncImage(
                                model = imageRequest,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }
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
    dailyPhotos: List<String> = emptyList(),
    sleepHours: Double? = null,
    isMenstruation: Boolean = false,
    steps: Int? = null,
    musicRecord: String? = null,
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
                    activityNames = activityNames,
                    dailyPhotos = dailyPhotos,
                    sleepHours = sleepHours,
                    isMenstruation = isMenstruation,
                    steps = steps,
                    musicRecord = musicRecord
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
                        Text(stringResource(R.string.share), fontSize = 13.sp)
                    }
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = cs.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.edit), color = cs.onPrimary, fontSize = 13.sp)
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
                        Text(stringResource(R.string.delete), fontSize = 13.sp)
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
    onDismiss: () -> Unit,
    showMonth: Boolean = true
) {
    val currentYear = java.time.LocalDate.now().year
    val years = remember { (2000..currentYear + 10).map { it.toString() } }
    val currentLanguage = LocalLocale.current
    val months = remember(currentLanguage) { (1..12).map {
        com.diary.moonpage.core.util.LocaleUtils.getFormattedMonthName(it, currentLanguage)
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
            color = MoonTheme.customColors.popupBgColor,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showMonth) stringResource(R.string.select_date) else stringResource(R.string.select_year),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
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
                        if (showMonth) {
                            Box(modifier = Modifier.weight(1f)) {
                                WheelPicker(
                                    items = months,
                                    initialValue = com.diary.moonpage.core.util.LocaleUtils.getFormattedMonthName(tempMonth, currentLanguage),
                                    onItemSelected = { monthName ->
                                        val monthIndex = months.indexOf(monthName)
                                        if (monthIndex != -1) tempMonth = monthIndex + 1
                                    },
                                    isCircular = true,
                                    itemHeight = itemHeight,
                                    showLines = false
                                )
                            }
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
                            containerColor = MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = MoonTheme.customColors.cancelBtnTextColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirm(tempYear, tempMonth) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
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
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
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
                text = stringResource(R.string.share_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShareModeItem(
                    title = stringResource(R.string.square),
                    icon = Icons.Rounded.CropSquare,
                    onClick = { onModeSelected(true) },
                    modifier = Modifier.weight(1f)
                )
                ShareModeItem(
                    title = stringResource(R.string.portrait),
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
    isSquare: Boolean = true,
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val colorScheme = MaterialTheme.colorScheme
    val currentLanguage = LocalLocale.current
    val monthName = stringResource(R.string.month_format, yearMonth.monthValue, yearMonth.year)

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

            val daysInMonth = yearMonth.lengthOfMonth()
            val firstDayOfMonth = yearMonth.atDay(1)
            val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7

            val daysOfWeek = listOf(
                stringResource(R.string.sun_short),
                stringResource(R.string.mon_short),
                stringResource(R.string.tue_short),
                stringResource(R.string.wed_short),
                stringResource(R.string.thu_short),
                stringResource(R.string.fri_short),
                stringResource(R.string.sat_short)
            )
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

                                val moodVisual = if (moodId != 0) MoonIcons.Moods.getMoodVisual(moodId, themeType) else null

                                Box(
                                    modifier = Modifier
                                        .size(if (isSquare) 80.dp else 100.dp)
                                        .background(
                                            moodVisual?.color ?: colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (moodVisual != null && moodVisual.drawableRes != null) {
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
                    text = stringResource(R.string.reflect_journey),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedMoodIds: Set<Int>,
    selectedActivityIds: Set<String>,
    dynamicActivities: List<com.diary.moonpage.domain.model.Activity>,
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    onMoodToggled: (Int) -> Unit,
    onActivityToggled: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (selectedMoodIds.isNotEmpty() || selectedActivityIds.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text(stringResource(R.string.clear_all), color = cs.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Moods Section
            Text(
                stringResource(R.string.filter_by_mood),
                style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val moods = listOf(1, 2, 3, 4, 5)
                moods.forEach { moodId ->
                    val isSelected = selectedMoodIds.contains(moodId)
                    val visual = MoonIcons.Moods.getMoodVisual(moodId, themeType)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) visual.color else visual.color.copy(alpha = 0.12f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onMoodToggled(moodId) }
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) cs.onSurface.copy(alpha = 0.5f) else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (visual.drawableRes != null) {
                            Image(
                                painter = painterResource(id = visual.drawableRes),
                                contentDescription = visual.name,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Activities Section
            Text(
                stringResource(R.string.filter_by_activities),
                style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        com.diary.moonpage.presentation.screens.calendar.DailyLogGrid(
                            items = dynamicActivities.filter { activity ->
                                true 
                            }.map { 
                                com.diary.moonpage.presentation.screens.calendar.DailyActivity(
                                    id = it.id,
                                    label = it.name,
                                    icon = MoonIcons.getIconForActivity(it.name)
                                )
                            },
                            selectedIds = selectedActivityIds.toList(),
                            onItemClick = onActivityToggled
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.show_results))
            }
        }
    }
}
