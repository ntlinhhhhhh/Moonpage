package com.diary.moonpage.ui.screens.stats.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.ui.screens.stats.ActivityCorrelation
import com.diary.moonpage.ui.screens.stats.IconDeepDiveResult
import com.diary.moonpage.ui.screens.stats.MoodDistributionEntry
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.core.theme.*
import com.diary.moonpage.R
import com.diary.moonpage.ui.screens.stats.SortOrder
import java.time.YearMonth
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer

import com.diary.moonpage.ui.screens.tutorial.tutorialTarget
import com.diary.moonpage.ui.screens.tutorial.TutorialStep

@Composable
fun MoonActivityIcon(icon: com.diary.moonpage.core.util.MoonIcon, size: androidx.compose.ui.unit.Dp = 44.dp, tint: Color = Color.Unspecified) {
    if (icon.imageUrl != null) {
        AsyncImage(
            model = icon.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else if (icon.drawableRes != null) {
        Image(
            painter = painterResource(id = icon.drawableRes),
            contentDescription = null,
            modifier = Modifier.size(size),
            colorFilter = if (tint != Color.Unspecified) androidx.compose.ui.graphics.ColorFilter.tint(tint) else null
        )
    } else if (icon.vector != null) {
        Icon(
            imageVector = icon.vector,
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = if (tint != Color.Unspecified) tint else icon.color
        )
    }
}


@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(60.dp)
                    .background(color, RoundedCornerShape(1.5.dp))
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}

@Composable
fun SummaryStatsView(
    totalLogs: Int,
    totalPhotos: Int,
    currentStreak: Int,
    longestStreak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().tutorialTarget(TutorialStep.HighlightYearlyReport),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryItem(label = stringResource(R.string.logs), value = totalLogs.toString(), modifier = Modifier.weight(1f))
        SummaryItem(label = stringResource(R.string.photos), value = totalPhotos.toString(), modifier = Modifier.weight(1f))
        SummaryItem(label = stringResource(R.string.streak), value = currentStreak.toString(), modifier = Modifier.weight(1f), icon = Icons.Rounded.Whatshot)
        SummaryItem(label = stringResource(R.string.record), value = longestStreak.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MoonTheme.customColors.logItemBg.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (actionText != null) {
                    val actionColor = MaterialTheme.colorScheme.onSurfaceVariant
                    TextButton(onClick = onActionClick, contentPadding = PaddingValues(0.dp)) {
                        Text(actionText, color = actionColor, fontSize = 16.sp)
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = actionColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun MoodFlowChart(
    moodFlow: List<MoodFlowDto>, 
    year: Int, 
    month: Int, 
    isMonthly: Boolean = true,
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null,
    menstruationDates: List<String> = emptyList()
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val moodColors = listOf(
        MoonIcons.Moods.getMoodColor(5, themeType, customMoods),
        MoonIcons.Moods.getMoodColor(4, themeType, customMoods),
        MoonIcons.Moods.getMoodColor(3, themeType, customMoods),
        MoonIcons.Moods.getMoodColor(2, themeType, customMoods),
        MoonIcons.Moods.getMoodColor(1, themeType, customMoods)
    )
    
    val filteredMoods = remember(moodFlow, year, month, isMonthly) {
        if (isMonthly) {
            val monthStr = String.format(Locale.ENGLISH, "%04d-%02d", year, month)
            moodFlow.filter { it.date.startsWith(monthStr) }
        } else {
            // Aggregate by month for Annual View
            val yearStr = year.toString()
            val yearLogs = moodFlow.filter { it.date.startsWith(yearStr) }
            (1..12).mapNotNull { m ->
                val mStr = String.format(Locale.ENGLISH, "%s-%02d", yearStr, m)
                val monthLogs = yearLogs.filter { it.date.startsWith(mStr) }
                if (monthLogs.isNotEmpty()) {
                    val avgMood = monthLogs.map { it.moodId }.average()
                    MoodFlowDto(date = "$mStr-01", moodId = avgMood)
                } else null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Mood indicators on the left
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(20.dp)
                    .padding(bottom = 48.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                moodColors.forEach { color ->
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                }
            }

            Column(modifier = Modifier.weight(1f).padding(start = 15.dp, end = 10.dp, bottom = 16.dp)) {
                val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val width = size.width
                val height = size.height
                
                // Vertical grid lines
                val gridCount = if (isMonthly) 6 else 11
                for (i in 0..gridCount) {
                    val x = width * i / gridCount
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Menstruation Background (Monthly only)
                if (isMonthly && menstruationDates.isNotEmpty()) {
                    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
                    val dx = if (daysInMonth > 1) width / (daysInMonth - 1) else 0f
                    val currentMonthStr = String.format(Locale.ENGLISH, "%04d-%02d", year, month)
                    
                    menstruationDates.filter { it.startsWith(currentMonthStr) }.forEach { dateStr ->
                        try {
                            val d = dateStr.split("-").last().toInt() - 1
                            val x = d * dx
                            drawRect(
                                color = Color(0xFFFFCDD2).copy(alpha = 0.4f),
                                topLeft = Offset(x - dx/2, 0f),
                                size = Size(dx, height)
                            )
                        } catch (e: Exception) {}
                    }
                }

                if (filteredMoods.isNotEmpty()) {
                    val path = Path()
                    val maxSlots = if (isMonthly) {
                        YearMonth.of(year, month).lengthOfMonth() - 1
                    } else {
                        11
                    }
                    
                    val dx = if (maxSlots > 0) width / maxSlots else 0f
                    
                    filteredMoods.forEachIndexed { index, item ->
                        val dayOfMonth = try {
                            val parts = item.date.split("-")
                            if (isMonthly) parts.last().toInt() - 1 else parts[1].toInt() - 1
                        } catch (e: Exception) {
                            index
                        }
                        
                        val x = (dayOfMonth * dx).coerceIn(0f, width)
                        val y = height * (5.0 - item.moodId).coerceIn(0.0, 4.0).toFloat() / 4f
                        
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                    drawPath(path = path, color = primaryColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // X-Axis Labels - Reverted to bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                if (isMonthly) {
                    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
                    listOf("1", "6", "11", "16", "21", "26", daysInMonth.toString()).forEach {
                        Text(it, fontSize = 12.sp, color = labelColor)
                    }
                } else {
                    (1..12).forEach { Text("$it", fontSize = 12.sp, color = labelColor) }
                }
            }
        }
    }
}
}

@Composable
fun MoodDistributionView(
    distribution: List<MoodDistributionDto>,
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null
) {
    val moods = listOf(
        MoonIcons.Moods.getMoodVisual(5, themeType, customMoods),
        MoonIcons.Moods.getMoodVisual(4, themeType, customMoods),
        MoonIcons.Moods.getMoodVisual(3, themeType, customMoods),
        MoonIcons.Moods.getMoodVisual(2, themeType, customMoods),
        MoonIcons.Moods.getMoodVisual(1, themeType, customMoods)
    )

    // Define consistent aliases for all 5 mood levels
    val moodAliases = listOf(
        listOf("Very Happy", "Rad", "Extreme Happy", "Excellent"),
        listOf("Happy", "Good", "Glad", "Content", "Nice"),
        listOf("Neutral", "Meh", "Normal", "Okay"),
        listOf("Sad", "Gloomy", "Low", "Bad", "Down"),
        listOf("Very Sad", "Angry", "Awful", "Terrible", "Depressed")
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            moods.forEachIndexed { index, mood ->
                val moodLevel = 5 - index
                val aliases = moodAliases[index]
                val matchingDists = distribution.filter { dist ->
                    (dist.baseMoodId != null && dist.baseMoodId == moodLevel) ||
                    (dist.label != null && (dist.label.equals(mood.name, ignoreCase = true) ||
                    aliases.any { alias -> dist.label.equals(alias, ignoreCase = true) }))
                }
                
                val rawPercent = matchingDists.sumOf { it.percentage }
                val currentMoodPercent: Int = rawPercent.roundToInt()
                
                val iconSize = 32.dp
                val containerSize = 48.dp
                val tintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) 
                
                val iconTint = if (rawPercent > 0) {
                    Color.Black.copy(alpha = 0.7f)
                } else tintColor
                
                val tagBgColor = if (rawPercent > 0) {
                    mood.color.copy(alpha = 0.85f)
                } else MoonTheme.customColors.logItemBg
                
                val tagTextColor = if (rawPercent > 0) Color(0xFF1A1C1E) else MaterialTheme.colorScheme.onSurfaceVariant
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(containerSize)
                            .clip(CircleShape)
                            .background(if (rawPercent > 0) mood.color.copy(alpha = 0.85f) else MoonTheme.customColors.logItemBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mood.imageUrl != null) {
                            AsyncImage(
                                model = mood.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(iconSize),
                                contentScale = ContentScale.Fit
                            )
                        } else if (mood.drawableRes != null) {
                            Image(
                                painter = painterResource(id = mood.drawableRes),
                                contentDescription = null,
                                modifier = Modifier.size(iconSize),
                                colorFilter = ColorFilter.tint(if (rawPercent > 0) iconTint else tintColor)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tagBgColor
                    ) {
                        Text(
                            "$currentMoodPercent%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tagTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Multi-colored Segmented Bar
        Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(MoonTheme.customColors.logItemBg)) {
            moods.forEachIndexed { index, mood ->
                val moodLevel = 5 - index
                val aliases = moodAliases[index]
                val weight = distribution
                    .filter { dist -> 
                        (dist.baseMoodId != null && dist.baseMoodId == moodLevel) ||
                        (dist.label != null && (dist.label.equals(mood.name, ignoreCase = true) ||
                        aliases.any { alias -> dist.label.equals(alias, ignoreCase = true) }))
                    }
                    .sumOf { it.percentage }
                    .toFloat()
                
                if (weight > 0) {
                    Box(modifier = Modifier.fillMaxHeight().weight(weight).background(mood.color))
                }
            }
            
            // Safety check for unmapped data
            val totalApiWeight = distribution.sumOf { it.percentage }.toFloat()
            val matchedWeight = moods.indices.sumOf { index ->
                val moodLevel = 5 - index
                val aliases = moodAliases[index]
                distribution
                    .filter { dist -> 
                        (dist.baseMoodId != null && dist.baseMoodId == moodLevel) ||
                        (dist.label != null && (dist.label.equals(moods[index].name, ignoreCase = true) ||
                        aliases.any { alias -> dist.label.equals(alias, ignoreCase = true) }))
                    }
                    .sumOf { it.percentage }
            }.toFloat()
            
            if (totalApiWeight > matchedWeight && (totalApiWeight - matchedWeight) > 0.1f) {
                Box(modifier = Modifier.fillMaxHeight().weight(totalApiWeight - matchedWeight).background(Color.Gray.copy(alpha = 0.6f)))
            }
        }
    }
}

@Composable
fun YearlyGridChart(
    yearlyMoodGrid: List<MoodFlowDto>,
    year: Int,
    menstruationDates: List<String> = emptyList(),
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MoonTheme.customColors.logItemBg
    
    // Optimize lookups by converting list to map
    val moodMap = remember(yearlyMoodGrid) { 
        yearlyMoodGrid.associateBy { it.date } 
    }
    val periodSet = remember(menstruationDates) { 
        menstruationDates.toSet() 
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Column {
                // Header row (Months)
                Row {
                    Spacer(modifier = Modifier.width(35.dp))
                    (1..12).forEach { m ->
                        Text(
                            text = "$m",
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Days grid
                (1..31).forEach { day ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = if (day % 5 == 0 || day == 1) "$day" else "",
                            modifier = Modifier.width(35.dp),
                            fontSize = 12.sp,
                            color = onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Start
                        )
                        (1..12).forEach { month ->
                            val isValid = try {
                                java.time.LocalDate.of(year, month, day)
                                true
                            } catch (e: Exception) {
                                false
                            }

                            if (isValid) {
                                val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month, day)
                                val mood = moodMap[dateStr]
                                val isPeriod = periodSet.contains(dateStr)
                                
                                val color = if (mood != null) {
                                    MoonIcons.Moods.getMoodColor(mood.moodId.toInt(), themeType, customMoods)
                                } else {
                                    gridColor
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPeriod) {
                                        Box(modifier = Modifier.size(4.dp).background(Color.White, CircleShape))
                                    }
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SleepSummaryView(
    averageSleepHours: Double,
    averageSleepStartTime: String?,
    averageWakeUpTime: String?,
    avgSteps: Int,
    avgCalories: Int = 0,
    avgDistance: Double = 0.0
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Full width Avg Sleep Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MoonTheme.customColors.logItemBg.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.Bedtime,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.average_sleep_title), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale.ENGLISH, "%.1f hours", averageSleepHours),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Row with 3 cards: Bedtime, Wake up, Steps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryItem(
                label = stringResource(R.string.bedtime),
                value = averageSleepStartTime ?: "--:--",
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Alarm
            )
            SummaryItem(
                label = stringResource(R.string.wake_up),
                value = averageWakeUpTime ?: "--:--",
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.WbSunny
            )
            SummaryItem(
                label = stringResource(R.string.avg_steps),
                value = String.format(Locale.ENGLISH, "%,d", avgSteps),
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Rounded.DirectionsWalk
            )
        }
        
        // Calories and Distance if available
        if (avgCalories > 0 || avgDistance > 0.0) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (avgCalories > 0) {
                    SummaryItem(
                        label = stringResource(R.string.avg_calories),
                        value = String.format(Locale.ENGLISH, "%,d kcal", avgCalories), 
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.LocalFireDepartment
                    )
                }
                if (avgDistance > 0.0) {
                    SummaryItem(
                        label = stringResource(R.string.avg_distance),
                        value = String.format(Locale.ENGLISH, "%.1f km", avgDistance), 
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Route
                    )
                }
            }
        }
    }
}

@Composable
fun SleepAnalysisChart(
    sleepData: List<com.diary.moonpage.data.remote.dto.stats.SleepAnalysisDto>,
    year: Int,
    month: Int,
    isMonthly: Boolean = true,
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val shades = getThemeShades(themeType)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = onSurfaceVariant.copy(alpha = 0.6f)

    val daysInMonth = try { java.time.YearMonth.of(year, month).lengthOfMonth() } catch(e: Exception) { 31 }
    
    // Filter sleep records by the active month (in monthly view) or active year (in annual view)
    val filteredSleepData = remember(sleepData, year, month, isMonthly) {
        sleepData.filter { dto ->
            try {
                val parts = dto.date.split("-")
                val y = parts[0].toInt()
                val m = parts[1].toInt()
                if (isMonthly) {
                    y == year && m == month
                } else {
                    y == year
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    // Group logs by date to correctly aggregate multiple sleep sessions on the same day (e.g. nap + night sleep)
    val sleepGroups = remember(filteredSleepData) {
        filteredSleepData.groupBy { dto ->
            try {
                val parts = dto.date.split("-")
                val y = parts[0].toInt()
                val m = parts[1].toInt()
                val d = parts[2].toInt()
                String.format(Locale.ENGLISH, "%04d-%02d-%02d", y, m, d)
            } catch (e: Exception) {
                dto.date
            }
        }
    }

    val latestSleep = filteredSleepData.lastOrNull()
    val avgDuration = if (filteredSleepData.isNotEmpty()) filteredSleepData.map { it.duration }.average() else 0.0
    
    Column {
        // Summary Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val bedtime = latestSleep?.startTime?.ifBlank { null } ?: "12:00 AM"
            SleepStatBox(label = stringResource(R.string.bedtime), value = bedtime, modifier = Modifier.weight(1f))
            
            val wakeUpTime = if (latestSleep != null) {
                try {
                    val startTime = latestSleep.startTime?.ifBlank { null } ?: "12:00 AM"
                    val date = parseFlexibleTime(startTime)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        cal.add(Calendar.MINUTE, (latestSleep.duration * 60).toInt())
                        java.text.SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(cal.time)
                    } else "--:--"
                } catch (e: Exception) { "--:--" }
            } else "--:--"
            SleepStatBox(label = stringResource(R.string.wake_up), value = wakeUpTime, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Area
        // Dynamic bounds
        var minHourFromNoon = 6f  // Default 6 PM
        var maxHourFromNoon = 20f // Default 8 AM
        if (filteredSleepData.isNotEmpty()) {
            val allStarts = filteredSleepData.filter { it.duration > 0 }.map { parseTimeFromNoon(it.startTime?.ifBlank { null }) }
            val allEnds = filteredSleepData.filter { it.duration > 0 }.map { parseTimeFromNoon(it.startTime?.ifBlank { null }) + it.duration.toFloat() }
            if (allStarts.isNotEmpty()) {
                minHourFromNoon = allStarts.minOrNull() ?: 6f
                maxHourFromNoon = allEnds.maxOrNull() ?: 20f
            }
        }
        val minHourDisplay = kotlin.math.floor(minHourFromNoon / 2f).toInt() * 2
        val maxHourDisplay = kotlin.math.ceil(maxHourFromNoon / 2f).toInt() * 2
        val hourRange = (maxHourDisplay - minHourDisplay).coerceAtLeast(4)
        
        val labels = mutableListOf<String>()
        for (i in minHourDisplay..maxHourDisplay step 2) {
            val absHour = (i + 12) % 24
            val amPm = if (absHour < 12) "AM" else "PM"
            val displayHour = if (absHour % 12 == 0) 12 else absHour % 12
            labels.add("${displayHour}$amPm")
        }
        val gridLineCount = labels.size - 1

        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            // Y-Axis Labels
            Column(
                modifier = Modifier.fillMaxHeight().width(40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach {
                    Text(it, fontSize = 11.sp, color = labelColor)
                }
            }

            // Grid & Bars
            Box(modifier = Modifier.fillMaxSize().padding(start = 45.dp, end = 10.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val gridColor = onSurfaceVariant.copy(alpha = 0.1f)
                    
                    // Horizontal Grid Lines
                    for (i in 0..gridLineCount) {
                        val y = height * i / gridLineCount.toFloat()
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    if (isMonthly) {
                        // Floating Bars for each day of the month (made wider as requested)
                        val barWidth = (width / daysInMonth) * 0.75f
                        val spacing = width / daysInMonth
                        
                        for (day in 1..daysInMonth) {
                            val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month, day)
                            val dayLogs = sleepGroups[dateStr]
                            
                            if (dayLogs != null && dayLogs.isNotEmpty()) {
                                val x = (day - 1) * spacing + spacing / 2f
                                
                                val totalDurationForDay = dayLogs.sumOf { it.duration }.toFloat()
                                val barColor = when {
                                    totalDurationForDay < 6.0f -> Color(0xFFEF5350) // Solid Red
                                    totalDurationForDay <= 8.0f -> primaryColor
                                    else -> shades[1] // Solid Light Theme Green
                                }
                                
                                for (session in dayLogs) {
                                    if (session.duration <= 0) continue
                                    val startTimeFloat = parseTimeFromNoon(session.startTime?.ifBlank { null })
                                    val sessionDuration = session.duration.toFloat()
                                    
                                    val startY = ((startTimeFloat - minHourDisplay) / hourRange) * height
                                    val endY = ((startTimeFloat + sessionDuration - minHourDisplay) / hourRange) * height
                                    
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(x - barWidth/2, startY.coerceAtLeast(0f)),
                                        size = Size(barWidth, (endY - startY).coerceAtLeast(4f)),
                                        cornerRadius = CornerRadius(barWidth/2, barWidth/2)
                                    )
                                }
                            }
                        }
                    } else {
                        // Floating Bars for each of the 12 months (made narrower as requested)
                        val barWidth = (width / 12) * 0.35f
                        val spacing = width / 12
                        
                        for (m in 1..12) {
                            val monthPrefix = String.format(Locale.ENGLISH, "%04d-%02d", year, m)
                            val monthLogs = filteredSleepData.filter { it.date.startsWith(monthPrefix) || (try { it.date.split("-")[1].toInt() == m } catch(e: Exception) { false }) }
                            
                            if (monthLogs.isNotEmpty()) {
                                val x = (m - 1) * spacing + spacing / 2f
                                
                                val dailyLogs = monthLogs.groupBy { it.date }
                                val dailyDurations = dailyLogs.values.map { it.sumOf { dto -> dto.duration } }
                                val dailyStarts = dailyLogs.values.map { dayList ->
                                    val mainSession = dayList.maxByOrNull { it.duration }
                                    parseTimeFromNoon(mainSession?.startTime?.ifBlank { null })
                                }
                                
                                val avgStartTime = dailyStarts.average().toFloat()
                                val avgDuration = dailyDurations.average().toFloat()
                                
                                val startY = ((avgStartTime - minHourDisplay) / hourRange) * height
                                val endY = ((avgStartTime + avgDuration - minHourDisplay) / hourRange) * height
                                
                                val barColor = when {
                                    avgDuration < 6.0f -> Color(0xFFEF5350) // Solid Red
                                    avgDuration <= 8.0f -> primaryColor
                                    else -> shades[1] // Solid Light Theme Green
                                }
                                
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x - barWidth/2, startY.coerceAtLeast(0f)),
                                    size = Size(barWidth, (endY - startY).coerceAtLeast(4f)),
                                    cornerRadius = CornerRadius(barWidth/2, barWidth/2)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // X-Axis Labels
        if (isMonthly) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 45.dp, end = 10.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelDays = listOf(1, 6, 11, 16, 21, 26, daysInMonth).distinct()
                labelDays.forEach { day ->
                    Text("$month/$day", fontSize = 11.sp, color = labelColor)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 45.dp, end = 10.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val monthLabels = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
                monthLabels.forEach { label ->
                    Text(label, fontSize = 11.sp, color = labelColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Legend Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MoonTheme.customColors.logItemBg
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val totalDays = if (isMonthly) {
                    daysInMonth
                } else {
                    val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
                    if (isLeap) 366 else 365
                }
                
                val reportedDays = sleepGroups.size
                val lessThan6 = sleepGroups.values.count { list -> list.sumOf { it.duration } < 6.0 }
                val between6And8 = sleepGroups.values.count { list -> list.sumOf { it.duration } in 6.0..8.0 }
                val over8 = sleepGroups.values.count { list -> list.sumOf { it.duration } > 8.0 }
                
                SleepLegendItem(color = Color(0xFFEF5350), label = stringResource(R.string.less_than_6h), value = stringResource(R.string.days_ratio, lessThan6, totalDays))
                SleepLegendItem(color = primaryColor, label = stringResource(R.string.six_to_eight_h), value = stringResource(R.string.days_ratio, between6And8, totalDays))
                SleepLegendItem(color = shades[1], label = stringResource(R.string.over_8h), value = stringResource(R.string.days_ratio, over8, totalDays))
                SleepLegendItem(color = Color(0xFFE0E0E0), label = stringResource(R.string.no_record), value = stringResource(R.string.days_ratio, totalDays - reportedDays, totalDays))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        val avgHrs = avgDuration.toInt()
        val avgMins = ((avgDuration - avgHrs) * 60).toInt()
        Text(
            text = buildAnnotatedString {
                append("On average, you slept ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                    append("${avgHrs}h ${avgMins}m.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = labelColor
        )
    }
}

@Composable
private fun SleepStatBox(label: String, value: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun SleepLegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
    }
}

private fun parseFlexibleTime(timeStr: String?): java.util.Date? {
    if (timeStr == null || timeStr.isBlank()) return null
    val normalized = timeStr.uppercase()
        .replace("SA", "AM")
        .replace("CH", "PM")
        .replace("SÁNG", "AM")
        .replace("CHIỀU", "PM")
    val formats = listOf("hh:mm a", "HH:mm", "HH:mm:ss", "h:mm a")
    for (format in formats) {
        try {
            return java.text.SimpleDateFormat(format, Locale.ENGLISH).parse(normalized)
        } catch (e: Exception) {
            // Ignore and try next
        }
    }
    return null
}

private fun parseTimeFromNoon(timeStr: String?): Float {
    if (timeStr == null || timeStr.isBlank()) return 12f // Default to 12 AM/midnight (12 hours after Noon)
    try {
        val date = parseFlexibleTime(timeStr) ?: return 12f
        
        val cal = Calendar.getInstance().apply { time = date }
        val hours = cal.get(Calendar.HOUR_OF_DAY).toFloat()
        val minutes = cal.get(Calendar.MINUTE).toFloat()
        val absoluteHours = hours + minutes / 60f
        
        return if (absoluteHours >= 12f) {
            absoluteHours - 12f
        } else {
            absoluteHours + 12f
        }
    } catch (e: Exception) {
        return 10f
    }
}

@Composable
fun SleepMoodCorrelationChart(
    sleepData: List<com.diary.moonpage.data.remote.dto.stats.SleepAnalysisDto>, 
    themeType: MoonThemeType,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null
) {
    val displayData = sleepData

    val ranges = listOf(
        "<4h" to (0.0..4.0),
        "4-6h" to (4.0..6.0),
        "6-8h" to (6.0..8.0),
        "8-10h" to (8.0..10.0),
        "10h<" to (10.0..24.0)
    )

    Row(
        modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Y-Axis Mood Indicators
        Column(
            modifier = Modifier.fillMaxHeight().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            (5 downTo 1).forEach { moodId ->
                Box(modifier = Modifier.size(8.dp).background(MoonIcons.Moods.getMoodColor(moodId, themeType, customMoods), CircleShape))
            }
        }

        ranges.forEach { (label, range) ->
            val entriesInRange = displayData.filter { it.duration in range }
            val avgMood = if (entriesInRange.isNotEmpty()) entriesInRange.map { it.moodId }.average().toFloat() else 0f
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                if (avgMood > 0) {
                    val heightFactor = (avgMood / 5f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(heightFactor)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MoonIcons.Moods.getMoodColor(avgMood.toInt().coerceIn(1, 5), themeType, customMoods))
                    )
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun MonthlyMoodAverageChart(yearlyMoodGrid: List<MoodFlowDto>, year: Int, themeType: MoonThemeType) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val averages = (1..12).map { m ->
        val currentMonthStr = String.format(Locale.ENGLISH, "%04d-%02d", year, m)
        val monthLogs = yearlyMoodGrid.filter { it.date.startsWith(currentMonthStr) }
        if (monthLogs.isEmpty()) 0f else monthLogs.map { it.moodId }.average().toFloat()
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        averages.forEachIndexed { index, avg ->
            val heightFactor = (avg / 5.0f).coerceIn(0f, 1f)
            val monthLabel = (index + 1).toString()
            
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp * heightFactor)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(if (avg >= 3.5) primaryColor else primaryColor.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(monthLabel, fontSize = 12.sp, color = labelColor)
            }
        }
    }
}

@Composable
fun MusicSummaryView(musicSummary: List<com.diary.moonpage.data.remote.dto.stats.MusicSummaryDto>) {
    if (musicSummary.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_music_data_period), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        musicSummary.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MoonTheme.customColors.logItemBg
                ) {
                    if (item.albumArtUrl != null) {
                        coil.compose.AsyncImage(
                            model = item.albumArtUrl,
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.songTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    Text(item.artistName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Text(stringResource(R.string.times_count, item.occurrence), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun IconDeepDiveView(
    deepDive: IconDeepDiveResult?,
    allActivities: List<BestActivityDto>,
    selectedIconId: String?,
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    onIconClick: (String?) -> Unit = {}
) {
    val shades = getThemeShades(themeType)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val successColor = MoonTheme.customColors.successColor
    val errorColor = MoonTheme.customColors.errorColor

    // Activity selector chips
    if (allActivities.isNotEmpty()) {
        val displayActivities = allActivities.take(8)
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(displayActivities.size) { idx ->
                val act = displayActivities[idx]
                val isSelected = act.activityId == (selectedIconId ?: allActivities.firstOrNull()?.activityId)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) primaryColor.copy(alpha = 0.15f) else MoonTheme.customColors.logItemBg.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) primaryColor.copy(alpha = 0.6f) else androidx.compose.ui.graphics.Color.Transparent
                    ),
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onIconClick(act.activityId) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                            MoonActivityIcon(icon = MoonIcons.getIconForActivity(act.activityName), size = 18.dp)
                        }
                        Text(act.activityName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) primaryColor else onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    if (deepDive == null) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.select_activity_details), color = onSurfaceVariant.copy(alpha = 0.5f))
        }
        return
    }

    // Header
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MoonTheme.customColors.logItemBg),
            contentAlignment = Alignment.Center
        ) {
            MoonActivityIcon(icon = MoonIcons.getIconForActivity(deepDive.activityName), size = 40.dp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(deepDive.activityName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text(stringResource(R.string.recordings_this_period, deepDive.totalOccurrence), color = onSurfaceVariant, fontSize = 13.sp)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ---- Metric 2: Average Mood Score ----
    val avgScore = deepDive.averageMoodScore
    val scoreColor = when {
        avgScore >= 4.0 -> successColor
        avgScore <= 2.5 -> errorColor
        else -> primaryColor
    }
    val scoreLabel = when {
        avgScore >= 4.0 -> "Positive influence ✨"
        avgScore >= 3.0 -> "Neutral influence 😌"
        else -> "Negative influence 💙"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(String.format(Locale.ENGLISH, "%.1f", avgScore), fontSize = 42.sp, fontWeight = FontWeight.Black, color = scoreColor, lineHeight = 42.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Surface(shape = RoundedCornerShape(8.dp), color = scoreColor.copy(alpha = 0.1f)) {
                Text(scoreLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = scoreColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.avg_mood_score), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.55f))
        }
    }

    if (avgScore > 3.5) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = successColor.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.stats_positive_energy),
                modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = successColor, lineHeight = 18.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ---- Metric 1: Mood Distribution ----
    if (deepDive.moodDistribution.isNotEmpty() && deepDive.totalOccurrence > 0) {
        Text(stringResource(R.string.mood_distribution), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        // Stacked bar
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp)).background(MoonTheme.customColors.logItemBg)
        ) {
            deepDive.moodDistribution.forEachIndexed { i, entry ->
                if (entry.percentage > 0f) {
                    val segColor = shades.getOrElse(4 - i) { androidx.compose.ui.graphics.Color.Gray }
                    Box(modifier = Modifier.weight(entry.percentage / 100f).fillMaxHeight().background(segColor))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Legend
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            deepDive.moodDistribution.forEachIndexed { i, entry ->
                if (entry.count > 0) {
                    val moodLabel = when (entry.moodId) { 5 -> "Rad"; 4 -> "Good"; 3 -> "Okay"; 2 -> "Bad"; else -> "Awful" }
                    val segColor = shades.getOrElse(4 - i) { androidx.compose.ui.graphics.Color.Gray }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(segColor))
                        Text("$moodLabel ${entry.percentage.toInt()}%", fontSize = 10.sp, color = onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // ---- Metrics 3: Stats Row (Streak + Frequency) ----
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = MoonTheme.customColors.logItemBg.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.LocalFireDepartment, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                Text("${deepDive.longestStreak}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = onSurface)
                Text(stringResource(R.string.longest_streak_days), fontSize = 11.sp, color = onSurfaceVariant.copy(alpha = 0.6f), lineHeight = 15.sp)
            }
        }
        Surface(shape = RoundedCornerShape(16.dp), color = MoonTheme.customColors.logItemBg.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.CalendarMonth, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                Text(String.format(Locale.ENGLISH, "%.1fx", deepDive.weeklyFrequency), fontSize = 24.sp, fontWeight = FontWeight.Black, color = onSurface)
                Text(stringResource(R.string.times_per_week_avg), fontSize = 11.sp, color = onSurfaceVariant.copy(alpha = 0.6f), lineHeight = 15.sp)
            }
        }
    }

    // ---- Metric 4: Co-occurrence ----
    if (deepDive.relatedActivities.isNotEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MoonTheme.customColors.logItemBg.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Link, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.often_paired_with), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = onSurface)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    deepDive.relatedActivities.forEach { (name, count) ->
                        Surface(shape = RoundedCornerShape(20.dp), color = primaryColor.copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
                                    MoonActivityIcon(MoonIcons.getIconForActivity(name), 14.dp)
                                }
                                Text(name, fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ActivityListItem(rank: Int, activity: BestActivityDto, modifier: Modifier = Modifier) {
    val icon = MoonIcons.getIconForActivity(activity.activityName)
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank.",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            MoonActivityIcon(icon = icon, size = 24.dp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.activityName, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(R.string.activity_records, activity.occurrence), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RankedActivityList(activities: List<BestActivityDto>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(activities) { index, activity ->
            ActivityListItem(rank = index + 1, activity = activity)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun FilterToggle(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Rounded.FilterList, contentDescription = stringResource(R.string.filter), tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SortToggle(currentOrder: SortOrder, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            if (currentOrder == SortOrder.MOST_RECORDED) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
            contentDescription = stringResource(R.string.sort),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ActivityFilterModal(
    categories: Set<String>,
    selectedFilter: Set<String>,
    onFilterChange: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_activities_title)) },
        text = {
            Column {
                categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { onFilterChange(category, !selectedFilter.contains(category)) }
                    ) {
                        Checkbox(
                            checked = selectedFilter.contains(category),
                            onCheckedChange = { onFilterChange(category, it) }
                        )
                        Text(category.ifBlank { "Uncategorized" })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.apply)) }
        }
    )
}


@Composable
fun FrequentlyRecordedView(activities: List<BestActivityDto>, onIconClick: (String?) -> Unit = {}) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) { index ->
                if (index < activities.size) {
                    val activity = activities[index]
                    ActivityRankCard(
                        rank = index + 1, 
                        name = activity.activityName, 
                        count = activity.occurrence, 
                        modifier = Modifier.weight(1f),
                        onClick = { onIconClick(activity.activityId) }
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
        if (activities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = buildAnnotatedString {
                    append("You recorded ")
                    withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                        append(activities.first().activityName)
                    }
                    append(" the most.")
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = onSurfaceVariant,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ActivityRankCard(rank: Int, name: String, count: Int, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val icon = MoonIcons.getIconForActivity(name)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        modifier = modifier
            .height(150.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = MoonTheme.customColors.logItemBg.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#$rank", 
                modifier = Modifier.align(Alignment.Start), 
                color = primaryColor.copy(alpha = 0.7f), 
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                MoonActivityIcon(icon = icon, size = 32.dp)
            }
            
            Text(
                text = name, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 13.sp, 
                maxLines = 1, 
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "$count times", 
                color = onSurfaceVariant, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BestAndWorstView(
    bestCorrelations: List<ActivityCorrelation> = emptyList(),
    worstCorrelations: List<ActivityCorrelation> = emptyList(),
    bestLegacy: List<BestActivityDto> = emptyList(),
    worstLegacy: List<BestActivityDto> = emptyList()
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MoonTheme.customColors.errorColor
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val successColor = MoonTheme.customColors.successColor

    // Use correlation-based data if available, fallback to legacy
    val hasCorrData = bestCorrelations.isNotEmpty() || worstCorrelations.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        // Info banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = primaryColor.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (hasCorrData)
                        "Based on % of days each activity appeared with best/worst moods."
                    else
                        "Correlation analysis shows which habits spark joy or weigh you down.",
                    fontSize = 13.sp, color = onSurfaceVariant, lineHeight = 18.sp
                )
            }
        }

        // Best Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(successColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.SentimentVerySatisfied, null, tint = successColor, modifier = Modifier.size(16.dp)) }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(stringResource(R.string.activities_positive_vibes), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = onSurface)
                    Text(stringResource(R.string.appear_most_great_moods), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
            if (hasCorrData && bestCorrelations.isNotEmpty()) {
                bestCorrelations.forEachIndexed { idx, corr ->
                    CorrelationActivityRow(rank = idx + 1, correlation = corr, accentColor = successColor, isPositive = true)
                }
            } else if (bestLegacy.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    bestLegacy.take(3).forEachIndexed { idx, act ->
                        ActivityScoreCard(rank = idx + 1, name = act.activityName, score = act.averageMoodScore, color = successColor, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - bestLegacy.size.coerceAtMost(3)) { Spacer(modifier = Modifier.weight(1f)) }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.log_more_days_to_see_results), color = onSurfaceVariant.copy(alpha = 0.45f), fontSize = 13.sp)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Worst Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(errorColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.SentimentVeryDissatisfied, null, tint = errorColor, modifier = Modifier.size(16.dp)) }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(stringResource(R.string.emotional_challenges), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = onSurface)
                    Text(stringResource(R.string.appear_most_tough_moods), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
            if (hasCorrData && worstCorrelations.isNotEmpty()) {
                worstCorrelations.forEachIndexed { idx, corr ->
                    CorrelationActivityRow(rank = idx + 1, correlation = corr, accentColor = errorColor, isPositive = false)
                }
            } else if (worstLegacy.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    worstLegacy.take(3).forEachIndexed { idx, act ->
                        ActivityScoreCard(rank = idx + 1, name = act.activityName, score = act.averageMoodScore, color = errorColor, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - worstLegacy.size.coerceAtMost(3)) { Spacer(modifier = Modifier.weight(1f)) }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.everything_balanced_so_far), color = onSurfaceVariant.copy(alpha = 0.45f), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CorrelationActivityRow(rank: Int, correlation: ActivityCorrelation, accentColor: Color, isPositive: Boolean) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val icon = MoonIcons.getIconForActivity(correlation.activityName)
    val ratePercent = if (isPositive) correlation.bestRatePercent else correlation.worstRatePercent

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MoonTheme.customColors.logItemBg.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#$rank", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor.copy(alpha = 0.7f), modifier = Modifier.width(22.dp))
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MoonTheme.customColors.logItemBg),
                contentAlignment = Alignment.Center
            ) { MoonActivityIcon(icon = icon, size = 24.dp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(correlation.activityName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = onSurface, maxLines = 1)
                Text(stringResource(R.string.times_recorded, correlation.occurrence), fontSize = 11.sp, color = onSurfaceVariant.copy(alpha = 0.55f))
                Spacer(modifier = Modifier.height(6.dp))
                // Percentage bar
                Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(accentColor.copy(alpha = 0.15f))) {
                    Box(modifier = Modifier.fillMaxWidth(fraction = (ratePercent / 100f).coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(accentColor))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("$ratePercent%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
        }
    }
}

@Composable
fun ActivityScoreCard(rank: Int, name: String, score: Double, color: Color, modifier: Modifier = Modifier) {
    val icon = MoonIcons.getIconForActivity(name)
    
    Surface(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(24.dp),
        color = MoonTheme.customColors.logItemBg.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#$rank", 
                modifier = Modifier.align(Alignment.Start), 
                color = color.copy(alpha = 0.7f), 
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                MoonActivityIcon(icon = icon, size = 32.dp)
            }
            
            Text(
                text = name, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 13.sp, 
                maxLines = 1, 
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                repeat(5) { i ->
                    val active = (i + 1) <= score.toInt()
                    Icon(
                        Icons.Rounded.Star, 
                        contentDescription = null, 
                        modifier = Modifier.size(10.dp), 
                        tint = if (active) color else color.copy(alpha = 0.2f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.ENGLISH, "%.1f", score), 
                    color = color, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun YearlyRecapCard(
    year: Int,
    totalLogs: Int,
    totalPhotos: Int,
    yearlyMoodGrid: List<MoodFlowDto>,
    themeType: MoonThemeType,
    modifier: Modifier = Modifier,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null,
    bestActivities: List<BestActivityDto> = emptyList(),
    averageDistance: Double = 0.0,
    averageSteps: Int = 0,
    longestStreak: Int = 0,
    isLarger: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Calculate Dominant Mood
    val dominantMood = if (yearlyMoodGrid.isNotEmpty()) {
        val moodCounts = yearlyMoodGrid.groupBy { it.moodId.toInt() }.mapValues { it.value.size }
        val dominantId = moodCounts.maxByOrNull { it.value }?.key ?: 3
        dominantId
    } else 3
    
    val dominantMoodName = when(dominantMood) {
        5 -> "Rad"
        4 -> "Good"
        3 -> "Meh"
        2 -> "Bad"
        else -> "Awful"
    }

    // Calculate Happiest Month
    val happiestMonth = if (yearlyMoodGrid.isNotEmpty()) {
        val monthlyAverages = (1..12).map { m ->
            val monthStr = String.format(Locale.ENGLISH, "%04d-%02d", year, m)
            val monthLogs = yearlyMoodGrid.filter { it.date.startsWith(monthStr) }
            val avg = if (monthLogs.isNotEmpty()) monthLogs.map { it.moodId }.average() else 0.0
            m to avg
        }.filter { it.second > 0 }
        
        val bestMonth = monthlyAverages.maxByOrNull { it.second }?.first ?: 1
        java.time.Month.of(bestMonth).name.lowercase().replaceFirstChar { it.titlecase() }
    } else "N/A"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MoonTheme.customColors.logCardBg
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = stringResource(R.string.stats_year_recap, year),
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Main Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RecapStatItem("Days Logged", "$totalLogs", Modifier.weight(1f))
                RecapStatItem("Photos", "$totalPhotos", Modifier.weight(1f))
                RecapStatItem("Longest Streak", "$longestStreak", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RecapStatItem("Happiest Month", happiestMonth, Modifier.weight(1f))
                RecapStatItem("Dominant Mood", dominantMoodName, Modifier.weight(1f), color = MoonIcons.Moods.getMoodColor(dominantMood, themeType, customMoods))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Year in Pixels Grid
            Text(
                stringResource(R.string.stats_year_in_pixels),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MoonTheme.customColors.logItemBg.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                YearInPixelsGrid(yearlyMoodGrid, year, themeType, customMoods = customMoods, isLarger = isLarger)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mood Legend for the Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(5, 4, 3, 2, 1).forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MoonIcons.Moods.getMoodColor(level, themeType, customMoods))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(stringResource(R.string.mood_intensity), fontSize = 10.sp, color = onSurfaceVariant.copy(alpha = 0.6f))
            }
            
            if (bestActivities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    stringResource(R.string.stats_top_activities),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bestActivities.take(3).forEach { activity ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MoonTheme.customColors.logItemBg),
                                contentAlignment = Alignment.Center
                            ) {
                                MoonActivityIcon(MoonIcons.getIconForActivity(activity.activityName), size = 28.dp)
                            }
                            Text(activity.activityName, fontSize = 11.sp, color = onSurfaceVariant, maxLines = 1, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            if (averageSteps > 0 || averageDistance > 0) {
                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = primaryColor.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Rounded.DirectionsWalk, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Text(String.format(Locale.ENGLISH, "%,d", averageSteps), fontWeight = FontWeight.Bold, color = primaryColor)
                            Text(stringResource(R.string.avg_steps), fontSize = 10.sp, color = onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Route, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Text(String.format(Locale.ENGLISH, "%.1f km", averageDistance), fontWeight = FontWeight.Bold, color = primaryColor)
                            Text(stringResource(R.string.avg_distance), fontSize = 10.sp, color = onSurfaceVariant)
                        }
                    }
                }
            }
            
            // Narrative Summary
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = onSurface.copy(alpha = 0.03f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("In $year, you shared ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                            append("$totalLogs days")
                        }
                        append(" of your journey. Your spirit was mostly ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MoonIcons.Moods.getMoodColor(dominantMood, themeType, customMoods))) {
                            append(dominantMoodName.lowercase())
                        }
                        append(". ")
                        if (bestActivities.isNotEmpty()) {
                            append("You found the most joy in ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                                append(bestActivities.first().activityName)
                            }
                            append(".")
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NightsStay, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.app_name), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RecapStatItem(label: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun YearInPixelsGrid(
    yearlyMoodGrid: List<MoodFlowDto>, 
    year: Int, 
    themeType: MoonThemeType,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null,
    isLarger: Boolean = false
) {
    val moodMap = remember(yearlyMoodGrid) { yearlyMoodGrid.associateBy { it.date } }
    val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)

    val cellSize = if (isLarger) 18.dp else 12.dp
    val spacing = if (isLarger) 6.dp else 4.dp
    val textHeight = if (isLarger) 24.dp else 18.dp
    val textSize = if (isLarger) 12.sp else 10.sp
    val horizontalSpacing = if (isLarger) 4.dp else 2.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        // Leading Column: Days 1 to 31
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = if (isLarger) 10.dp else 6.dp)
        ) {
            // Spacer to align with month headers
            Spacer(modifier = Modifier.height(textHeight))
            (1..31).forEach { day ->
                Text(
                    text = if (day % 5 == 0 || day == 1) "$day" else "",
                    fontSize = textSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(cellSize),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Columns for Months 1 to 12
        (1..12).forEach { month ->
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = horizontalSpacing)
            ) {
                // Month Header
                Text(
                    text = "$month",
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.height(textHeight),
                    textAlign = TextAlign.Center
                )

                (1..31).forEach { day ->
                    val isValid = try {
                        java.time.LocalDate.of(year, month, day)
                        true
                    } catch (e: Exception) {
                        false
                    }

                    if (isValid) {
                        val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month, day)
                        val mood = moodMap[dateStr]
                        val cellColor = if (mood != null) {
                            MoonIcons.Moods.getMoodColor(mood.moodId.toInt(), themeType)
                        } else {
                            emptyColor
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(CircleShape)
                                .background(cellColor)
                        )
                    } else {
                        // Invisible placeholder for invalid days
                        Spacer(modifier = Modifier.size(cellSize))
                    }
                }
            }
        }
    }
}

// ===========================
// DASHBOARD WIDGET CARDS
// ===========================

@Composable
fun MoodOverviewCard(
    stats: com.diary.moonpage.data.remote.dto.stats.StatisticsResponse?,
    themeType: MoonThemeType,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null,
    isMonthly: Boolean = true,
    year: Int = java.time.LocalDate.now().year,
    month: Int = java.time.LocalDate.now().monthValue,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val moodDistribution = stats?.moodDistribution ?: emptyList()
    val moodFlow = stats?.moodFlow ?: emptyList()
    
    val (dominantMoodId, dominantMoodPercent) = remember(stats) {
        val moodAliases = listOf(
            5 to listOf("Very Happy", "Rad", "Extreme Happy", "Excellent"),
            4 to listOf("Happy", "Good", "Glad", "Content", "Nice"),
            3 to listOf("Neutral", "Meh", "Normal", "Okay"),
            2 to listOf("Sad", "Gloomy", "Low", "Bad", "Down"),
            1 to listOf("Very Sad", "Angry", "Awful", "Terrible", "Depressed")
        )

        val resolvedDistribution = moodDistribution.map { dist ->
            val resolvedId = dist.baseMoodId ?: moodAliases.find { (_, aliases) ->
                dist.label != null && aliases.any { it.equals(dist.label, ignoreCase = true) }
            }?.first ?: 3
            dist.copy(baseMoodId = resolvedId)
        }

        val fromDist = resolvedDistribution.maxByOrNull { it.percentage }
        if (fromDist != null && fromDist.percentage > 0) {
            (fromDist.baseMoodId ?: 3) to fromDist.percentage.roundToInt()
        } else if (moodFlow.isNotEmpty()) {
            val total = moodFlow.size.toFloat()
            val moodCounts = moodFlow.groupBy { it.moodId.toInt() }
            val dominantEntry = moodCounts.maxByOrNull { it.value.size }
            val id = dominantEntry?.key ?: 3
            val percent = ((dominantEntry?.value?.size ?: 0) / total * 100).roundToInt()
            id to percent
        } else {
            3 to 0
        }
    }
    
    val dominantMoodVisual = MoonIcons.Moods.getMoodVisual(dominantMoodId, themeType, customMoods)

    val moodText = when (dominantMoodId) {
        5 -> if (isMonthly) "This month looks great! 🌟" else "This year looks great! 🌟"
        4 -> if (isMonthly) "This month has been pretty good! 😊" else "This year has been pretty good! 😊"
        3 -> if (isMonthly) "This month has been stable. 😌" else "This year has been stable. 😌"
        2 -> if (isMonthly) "This month has been a bit tough. 💙" else "This year has been a bit tough. 💙"
        else -> if (isMonthly) "This month has been quite heavy. 🌙" else "This year has been quite heavy. 🌙"
    }

    val processedMoodFlow = remember(moodFlow, isMonthly, year) {
        if (isMonthly) {
            moodFlow
        } else {
            // Aggregate by month for Annual View
            val yearStr = year.toString()
            val yearLogs = moodFlow.filter { it.date.startsWith(yearStr) }
            (1..12).mapNotNull { m ->
                val mStr = String.format(Locale.ENGLISH, "%s-%02d", yearStr, m)
                val monthLogs = yearLogs.filter { it.date.startsWith(mStr) }
                if (monthLogs.isNotEmpty()) {
                    val avgMood = monthLogs.map { it.moodId }.average()
                    MoodFlowDto(date = "$mStr-01", moodId = avgMood)
                } else null
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_mood_overview),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariant.copy(alpha = 0.55f)
                )
                Icon(
                    Icons.Rounded.ChevronRight, contentDescription = null,
                    tint = onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(68.dp).clip(CircleShape)
                        .background(dominantMoodVisual.color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (dominantMoodVisual.imageUrl != null) {
                        AsyncImage(
                            model = dominantMoodVisual.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (dominantMoodVisual.drawableRes != null) {
                        Image(
                            painter = painterResource(id = dominantMoodVisual.drawableRes),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            colorFilter = ColorFilter.tint(Color.Black)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$dominantMoodPercent%",
                        fontSize = 36.sp, fontWeight = FontWeight.Black,
                        color = onSurface, lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = moodText, fontSize = 14.sp, color = onSurfaceVariant, lineHeight = 20.sp)
                }
            }

            if (processedMoodFlow.size >= 2) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                ) {
                    MoodSparkline(
                        moodFlow = processedMoodFlow,
                        primaryColor = primaryColor,
                        isMonthly = isMonthly,
                        year = year,
                        month = month
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodSparkline(
    moodFlow: List<MoodFlowDto>,
    primaryColor: Color,
    isMonthly: Boolean,
    year: Int,
    month: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val sorted = moodFlow.sortedBy { it.date }
        if (sorted.isEmpty()) return@Canvas

        val maxSlots = if (isMonthly) {
            java.time.YearMonth.of(year, month).lengthOfMonth() - 1
        } else {
            11
        }

        val dx = if (maxSlots > 0) size.width / maxSlots else 0f
        val path = Path()

        // Find the last logged date's index in the total slots
        var lastIndex = 0

        sorted.forEachIndexed { index, item ->
            val timeIndex = try {
                val parts = item.date.split("-")
                if (isMonthly) {
                    parts.last().toInt() - 1
                } else {
                    parts[1].toInt() - 1
                }
            } catch (e: Exception) {
                index
            }

            if (timeIndex > lastIndex) {
                lastIndex = timeIndex
            }

            val x = (timeIndex * dx).coerceIn(0f, size.width)
            val y = size.height * (5.0 - item.moodId).coerceIn(0.0, 4.0).toFloat() / 4f

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        val lastLoggedX = (lastIndex * dx).coerceIn(0f, size.width)

        if (sorted.size >= 2) {
            val fillPath = Path()
            fillPath.addPath(path)
            fillPath.lineTo(lastLoggedX, size.height)
            fillPath.lineTo(0f, size.height)
            fillPath.close()

            drawPath(path = fillPath, color = primaryColor.copy(alpha = 0.08f))
            drawPath(path = path, color = primaryColor.copy(alpha = 0.65f), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
fun SleepPhysicalRow(
    stats: com.diary.moonpage.data.remote.dto.stats.StatisticsResponse?,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SleepWidgetCard(avgSleepHours = stats?.averageSleepHours ?: 0.0, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = onClick)
        PhysicalWidgetCard(avgSteps = stats?.averageSteps?.toInt() ?: 0, avgCalories = stats?.averageCalories?.toInt() ?: 0, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = onClick)
    }
}

@Composable
private fun SleepWidgetCard(avgSleepHours: Double, modifier: Modifier, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Bedtime, null, tint = primary, modifier = Modifier.size(20.dp))
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(R.string.sleep_label), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.55f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (avgSleepHours > 0) String.format(Locale.ENGLISH, "%.1f", avgSleepHours) else "--",
                    fontSize = 30.sp, fontWeight = FontWeight.Black, color = onSurface, modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.per_night_suffix), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.45f), modifier = Modifier.alignByBaseline())
            }
        }
    }
}

@Composable
private fun PhysicalWidgetCard(avgSteps: Int, avgCalories: Int, modifier: Modifier, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.DirectionsRun, null, tint = primary, modifier = Modifier.size(20.dp))
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(R.string.avg_health_steps), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.55f))
            Spacer(modifier = Modifier.height(6.dp))
            if (avgSteps > 0) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(String.format(Locale.ENGLISH, "%,d", avgSteps), fontSize = 24.sp, fontWeight = FontWeight.Black, color = onSurface, modifier = Modifier.alignByBaseline())
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.steps_per_day), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.45f), modifier = Modifier.alignByBaseline())
                }
            } else {
                Text("--", fontSize = 30.sp, fontWeight = FontWeight.Black, color = onSurface)
            }
            if (avgCalories > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = primary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format(Locale.ENGLISH, "%,d kcal/day", avgCalories), fontSize = 12.sp, color = primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ActivityHabitsCard(frequentlyRecorded: List<BestActivityDto>, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val top3 = frequentlyRecorded.take(3)
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.stats_activities_habits), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
                ) {
                    Text(stringResource(R.string.more), fontSize = 14.sp, color = onSurfaceVariant.copy(alpha = 0.6f))
                    Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (frequentlyRecorded.isEmpty()) {
                Text(stringResource(R.string.no_activity_data_available), fontSize = 14.sp, color = onSurfaceVariant.copy(alpha = 0.5f))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    top3.forEachIndexed { index, activity ->
                        FrequentlyRecordedMiniCard(
                            rank = index + 1,
                            activity = activity,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill empty spots
                    repeat(3 - top3.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
                if (top3.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("You recorded ")
                            withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                                append(top3.first().activityName)
                            }
                            append(" the most.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = primaryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequentlyRecordedMiniCard(rank: Int, activity: BestActivityDto, modifier: Modifier = Modifier) {
    val icon = MoonIcons.getIconForActivity(activity.activityName)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Rank number
        Text(
            text = "$rank",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.Start)
        )
        // Icon container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MoonTheme.customColors.logItemBg.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            MoonActivityIcon(icon = icon, size = 36.dp)
        }
        // Activity name
        Text(
            text = activity.activityName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        // Occurrence count
        Text(
            text = "x${activity.occurrence}",
            fontSize = 11.sp,
            color = onSurfaceVariant.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun InsightsTeaserCard(bestActivities: List<BestActivityDto>, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.insights_deep_dive), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = onSurfaceVariant.copy(alpha = 0.55f))
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.stats_sparks_joy),
                fontSize = 16.sp, color = onSurface, lineHeight = 24.sp, fontWeight = FontWeight.Medium
            )
            if (bestActivities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bestActivities.take(2).forEach { a ->
                        Surface(shape = RoundedCornerShape(20.dp), color = MoonTheme.customColors.successColor.copy(alpha = 0.12f)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                                    MoonActivityIcon(MoonIcons.getIconForActivity(a.activityName), 16.dp)
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(a.activityName, fontSize = 12.sp, color = MoonTheme.customColors.successColor, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopMusicCard(
    musicSummary: List<com.diary.moonpage.data.remote.dto.stats.MusicSummaryDto>?,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val topSong = musicSummary?.firstOrNull()
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(MoonTheme.customColors.logItemBg),
                contentAlignment = Alignment.Center
            ) {
                if (topSong?.albumArtUrl != null) {
                    coil.compose.AsyncImage(
                        model = topSong.albumArtUrl, contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Icon(Icons.Rounded.MusicNote, null, tint = primary.copy(alpha = 0.5f), modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.top_music), fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                if (topSong != null) {
                    Text(topSong.songTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = onSurface, maxLines = 1)
                    Text(topSong.artistName, fontSize = 12.sp, color = onSurfaceVariant, maxLines = 1)
                } else {
                    Text(stringResource(R.string.no_data_available), fontSize = 14.sp, color = onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ShowcaseThemeGrid(
    shades: List<Color>, 
    themeType: MoonThemeType = MoonThemeType.DEFAULT,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val dividerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // ... (Month headers code) ...
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(30.dp)) // Spacer for y-axis labels
            (1..12).forEach { month ->
                Text(
                    text = "$month",
                    fontSize = 11.sp,
                    color = labelColor,
                    modifier = Modifier.width(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Horizontal divider line
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(24.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(dividerColor)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Grid Rows
        (1..5).forEach { row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                // Row Label (1 to 5)
                val moodLevel = 6 - row
                Text(
                    text = "$moodLevel",
                    fontSize = 11.sp,
                    color = labelColor,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )
                
                // Vertical divider line (drawn once for first column)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(dividerColor)
                )
                
                Spacer(modifier = Modifier.width(5.dp))
                
                // Dots for columns 1..12
                val dotColor = MoonIcons.Moods.getMoodColor(moodLevel, themeType, customMoods)
                
                (1..12).forEach { _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
fun YearInMoonpageMiniatureCard(
    year: Int,
    themeType: MoonThemeType,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
    customMoods: Map<Int, com.diary.moonpage.core.util.MoonIcon>? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val shades = getThemeShades(themeType)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = stringResource(R.string.stats_year_in_moonpage),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle: "Look back on your 2026."
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.stats_look_back_year, "").replace(".", ""),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$year.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Showcase Grid
            ShowcaseThemeGrid(shades = shades, themeType = themeType, customMoods = customMoods)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Button: "View Details" (always in English as per requested preference "default ngôn ngữ để tiếng anh nên xem chi tiết để tiếng anh")
            Button(
                onClick = onDetailClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.diary.moonpage.R.string.view_details),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
