package com.diary.moonpage.presentation.components.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.core.theme.*
import java.time.YearMonth
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryItem(label = "Logs", value = totalLogs.toString(), modifier = Modifier.weight(1f))
        SummaryItem(label = "Photos", value = totalPhotos.toString(), modifier = Modifier.weight(1f))
        SummaryItem(label = "Streak", value = currentStreak.toString(), modifier = Modifier.weight(1f), icon = Icons.Rounded.Whatshot)
        SummaryItem(label = "Record", value = longestStreak.toString(), modifier = Modifier.weight(1f))
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
    menstruationDates: List<String> = emptyList()
) {
    val shades = getThemeShades(themeType)
    val primaryColor = MaterialTheme.colorScheme.primary
    val moodColors = listOf(
        shades[0],
        shades[1],
        shades[2],
        shades[3],
        shades[4]
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
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val moods = listOf(
        MoonIcons.Moods.getMoodVisual(5, themeType),
        MoonIcons.Moods.getMoodVisual(4, themeType),
        MoonIcons.Moods.getMoodVisual(3, themeType),
        MoonIcons.Moods.getMoodVisual(2, themeType),
        MoonIcons.Moods.getMoodVisual(1, themeType)
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
                        if (mood.drawableRes != null) {
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
    themeType: MoonThemeType = MoonThemeType.DEFAULT
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
                            val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month, day)
                            val mood = moodMap[dateStr]
                            val isPeriod = periodSet.contains(dateStr)
                            
                            val color = if (mood != null) {
                                MoonIcons.Moods.getMoodColor(mood.moodId.toInt(), themeType)
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
    totalSteps: Int,
    totalCalories: Int = 0,
    totalDistance: Double = 0.0
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
                    Text("Average Sleep", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                label = "Bedtime",
                value = averageSleepStartTime ?: "--:--",
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Alarm
            )
            SummaryItem(
                label = "Wake up",
                value = averageWakeUpTime ?: "--:--",
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.WbSunny
            )
            SummaryItem(
                label = "Steps",
                value = String.format(Locale.ENGLISH, "%,d", totalSteps),
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Rounded.DirectionsWalk
            )
        }
        
        // Calories and Distance if available
        if (totalCalories > 0 || totalDistance > 0.0) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (totalCalories > 0) {
                    SummaryItem(
                        label = "Calories", 
                        value = String.format(Locale.ENGLISH, "%,d", totalCalories), 
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.LocalFireDepartment
                    )
                }
                if (totalDistance > 0.0) {
                    SummaryItem(
                        label = "Distance", 
                        value = String.format(Locale.ENGLISH, "%.1f km", totalDistance), 
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
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val shades = getThemeShades(themeType)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = onSurfaceVariant.copy(alpha = 0.6f)

    // Derive month and year from data or current date
    val calendar = Calendar.getInstance()
    val firstDate = sleepData.firstOrNull()?.date
    val year = firstDate?.split("-")?.get(0)?.toInt() ?: calendar.get(Calendar.YEAR)
    val month = firstDate?.split("-")?.get(1)?.toInt() ?: (calendar.get(Calendar.MONTH) + 1)
    val daysInMonth = try { java.time.YearMonth.of(year, month).lengthOfMonth() } catch(e: Exception) { 31 }
    
    val sleepMap = remember(sleepData) { sleepData.associateBy { it.date } }
    val latestSleep = sleepData.lastOrNull()
    val avgDuration = if (sleepData.isNotEmpty()) sleepData.map { it.duration }.average() else 0.0
    
    Column {
        // Summary Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val bedtime = latestSleep?.startTime?.ifBlank { null } ?: "12:00 AM"
            SleepStatBox(label = "Bedtime", value = bedtime, modifier = Modifier.weight(1f))
            
            val wakeUpTime = if (latestSleep != null) {
                try {
                    val startTime = latestSleep.startTime?.ifBlank { null } ?: "12:00 AM"
                    val date = try {
                        java.text.SimpleDateFormat("hh:mm a", Locale.ENGLISH).parse(startTime)
                    } catch (e: Exception) {
                        try {
                            java.text.SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(startTime)
                        } catch(e: Exception) {
                            java.text.SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(startTime)
                        }
                    }
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        cal.add(Calendar.MINUTE, (latestSleep.duration * 60).toInt())
                        java.text.SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(cal.time)
                    } else "--:--"
                } catch (e: Exception) { "--:--" }
            } else "--:--"
            SleepStatBox(label = "Wake up", value = wakeUpTime, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Area
        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            // Y-Axis Labels
            Column(
                modifier = Modifier.fillMaxHeight().width(40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("6PM", "10PM", "2AM", "6AM", "10AM", "2PM", "6PM").forEach {
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
                    for (i in 0..6) {
                        val y = height * i / 6f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Floating Bars for each day of the month
                    val barWidth = (width / daysInMonth) * 0.6f
                    val spacing = width / daysInMonth
                    
                    for (day in 1..daysInMonth) {
                        val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month, day)
                        val data = sleepMap[dateStr]
                        
                        if (data != null && data.duration > 0) {
                            val x = (day - 1) * spacing + spacing / 2f
                            
                            // Parse Start Time to Float (Hours from 6PM)
                            val startTimeFloat = parseTimeToFloat(data.startTime?.ifBlank { null })
                            val duration = data.duration.toFloat()
                            
                            val startY = (startTimeFloat / 24f) * height
                            val endY = ((startTimeFloat + duration) / 24f) * height
                            
                            val barColor = when {
                                duration < 6.0f -> Color(0xFFEF5350) // Solid Red
                                duration <= 8.0f -> primaryColor
                                else -> shades[1] // Solid Light Theme Green
                            }
                            
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x - barWidth/2, startY),
                                size = Size(barWidth, (endY - startY).coerceAtLeast(4f)),
                                cornerRadius = CornerRadius(barWidth/2, barWidth/2)
                            )
                        }
                    }
                }
            }
        }
        
        // X-Axis Labels (Dates)
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 45.dp, end = 10.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelDays = listOf(1, 6, 11, 16, 21, 26, daysInMonth).distinct()
            labelDays.forEach { day ->
                Text("$month/$day", fontSize = 11.sp, color = labelColor)
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
                val totalDays = daysInMonth
                val reportedDays = sleepData.size
                SleepLegendItem(color = Color(0xFFEF5350), label = "Less than 6h", value = "${sleepData.count { it.duration < 6.0 }}/$totalDays days")
                SleepLegendItem(color = primaryColor, label = "6-8h", value = "${sleepData.count { it.duration in 6.0..8.0 }}/$totalDays days")
                SleepLegendItem(color = shades[1], label = "Over 8h", value = "${sleepData.count { it.duration > 8.0 }}/$totalDays days")
                SleepLegendItem(color = Color(0xFFE0E0E0), label = "No record", value = "${totalDays - reportedDays}/$totalDays days")
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

private fun parseTimeToFloat(timeStr: String?): Float {
    if (timeStr == null || timeStr.isBlank()) return 6f // Default to Midnight (6 hours after 6PM)
    try {
        val date = try {
            java.text.SimpleDateFormat("hh:mm a", Locale.ENGLISH).parse(timeStr)
        } catch (e: Exception) {
            try {
                java.text.SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(timeStr)
            } catch (e: Exception) {
                try {
                    java.text.SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(timeStr)
                } catch (e: Exception) {
                    null
                }
            }
        } ?: return 6f
        
        val cal = Calendar.getInstance().apply { time = date }
        val hours = cal.get(Calendar.HOUR_OF_DAY).toFloat()
        val minutes = cal.get(Calendar.MINUTE).toFloat()
        val totalHoursFromMidnight = hours + minutes / 60f
        
        // Offset so 6PM is 0
        var offset = totalHoursFromMidnight + 6f
        if (totalHoursFromMidnight >= 18f) {
            offset = totalHoursFromMidnight - 18f
        }
        return offset
    } catch (e: Exception) {
        return 6f
    }
}

@Composable
fun SleepMoodCorrelationChart(sleepData: List<com.diary.moonpage.data.remote.dto.stats.SleepAnalysisDto>, themeType: MoonThemeType) {
    if (sleepData.isEmpty()) return

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
                Box(modifier = Modifier.size(8.dp).background(MoonIcons.Moods.getMoodColor(moodId, themeType), CircleShape))
            }
        }

        ranges.forEach { (label, range) ->
            val entriesInRange = sleepData.filter { it.duration in range }
            val avgMood = if (entriesInRange.isNotEmpty()) entriesInRange.map { it.moodId }.average().toFloat() else 0f
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                if (avgMood > 0) {
                    val heightFactor = (avgMood / 5f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(heightFactor)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MoonIcons.Moods.getMoodColor(avgMood.toInt().coerceIn(1, 5), themeType))
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
            Text("No music data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
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
                Text("${item.occurrence} times", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun IconDeepDiveView(
    activityId: String?,
    allActivities: List<BestActivityDto>,
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val shades = getThemeShades(themeType)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val selectedActivity = remember(activityId, allActivities) {
        allActivities.find { it.activityId == activityId } ?: allActivities.firstOrNull()
    }

    if (selectedActivity == null) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Select an activity to see details", color = onSurfaceVariant.copy(alpha = 0.5f))
        }
        return
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MoonTheme.customColors.logItemBg),
                contentAlignment = Alignment.Center
            ) {
                val icon = MoonIcons.getIconForActivity(selectedActivity.activityName)
                MoonActivityIcon(icon = icon, size = 44.dp)
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = selectedActivity.activityName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selectedActivity.occurrence} recordings this period",
                    color = onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Average Mood Impact", style = MaterialTheme.typography.labelLarge, color = onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format(Locale.ENGLISH, "%.1f", selectedActivity.averageMoodScore),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = primaryColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val impact = selectedActivity.averageMoodScore - 3.0
                val impactText = if (impact > 0) "Positive influence" else "Negative influence"
                val impactColor = if (impact > 0) MoonTheme.customColors.successColor else MoonTheme.customColors.errorColor
                
                Text(impactText, color = impactColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("compared to neutral", color = onSurfaceVariant, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Mood Distribution", style = MaterialTheme.typography.labelLarge, color = onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MoonTheme.customColors.logItemBg)
        ) {
            val distribution = listOf(0.5f, 0.3f, 0.1f, 0.1f, 0.0f)
            distribution.forEachIndexed { i, weight ->
                if (weight > 0) {
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(shades.getOrElse(i) { Color.Gray })
                            .border(1.dp, Color.White.copy(alpha = 0.2f))
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Happy", fontSize = 11.sp, color = shades[0], fontWeight = FontWeight.Bold)
            Text("Angry", fontSize = 11.sp, color = shades[4], fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MoonTheme.customColors.logItemBg.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).background(primaryColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "When you record ${selectedActivity.activityName}, your mood is typically ${if (selectedActivity.averageMoodScore > 3.5) "significantly higher" else "more stable"} than usual.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun MoonActivityIcon(icon: com.diary.moonpage.core.util.MoonIcon, size: androidx.compose.ui.unit.Dp = 44.dp) {
    if (icon.drawableRes != null) {
        Image(
            painter = painterResource(id = icon.drawableRes),
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    } else if (icon.vector != null) {
        Icon(
            imageVector = icon.vector,
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = icon.color
        )
    }
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
fun BestAndWorstView(best: List<BestActivityDto>, worst: List<BestActivityDto>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MoonTheme.customColors.errorColor
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        // Insight Header
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MoonTheme.customColors.logItemBg.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = primaryColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Correlation analysis shows which habits spark joy or weigh you down.",
                    fontSize = 14.sp,
                    color = onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }

        // Best Section
        Column {
            Text(
                text = "Activities for Positive Vibes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "These habits often coincide with your best moods.",
                fontSize = 13.sp,
                color = onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (best.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("More data needed to calculate...", color = onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    best.take(3).forEachIndexed { index, activity ->
                        ActivityScoreCard(
                            rank = index + 1, 
                            name = activity.activityName, 
                            score = activity.averageMoodScore, 
                            color = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - best.size.coerceAtMost(3)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Worst Section
        Column {
            Text(
                text = "Emotional Challenges",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "You recorded lower mood levels alongside these activities.",
                fontSize = 13.sp,
                color = onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (worst.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Everything looks balanced so far!", color = onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    worst.take(3).forEachIndexed { index, activity ->
                        ActivityScoreCard(
                            rank = index + 1, 
                            name = activity.activityName, 
                            score = activity.averageMoodScore, 
                            color = errorColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - worst.size.coerceAtMost(3)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
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
    bestActivities: List<BestActivityDto> = emptyList(),
    totalDistance: Double = 0.0,
    totalSteps: Int = 0,
    longestStreak: Int = 0
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val shades = getThemeShades(themeType)

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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MoonTheme.customColors.logCardBg
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "$year Recap",
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
                RecapStatItem("Dominant Mood", dominantMoodName, Modifier.weight(1f), color = MoonIcons.Moods.getMoodColor(dominantMood, themeType))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Year in Pixels Grid
            Text(
                "Your Year in Pixels",
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
                YearInPixelsGrid(yearlyMoodGrid, year, themeType)
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
                            .background(MoonIcons.Moods.getMoodColor(level, themeType))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text("Mood intensity", fontSize = 10.sp, color = onSurfaceVariant.copy(alpha = 0.6f))
            }
            
            if (bestActivities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Top Activities",
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

            if (totalSteps > 0 || totalDistance > 0) {
                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = primaryColor.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Rounded.DirectionsWalk, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Text(String.format(Locale.ENGLISH, "%,d", totalSteps), fontWeight = FontWeight.Bold, color = primaryColor)
                            Text("Total Steps", fontSize = 10.sp, color = onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Route, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Text(String.format(Locale.ENGLISH, "%.1f km", totalDistance), fontWeight = FontWeight.Bold, color = primaryColor)
                            Text("Total Distance", fontSize = 10.sp, color = onSurfaceVariant)
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
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MoonIcons.Moods.getMoodColor(dominantMood, themeType))) {
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
                Text("MoonPage", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant)
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
fun YearInPixelsGrid(yearlyMoodGrid: List<MoodFlowDto>, year: Int, themeType: MoonThemeType) {
    val moodMap = remember(yearlyMoodGrid) { yearlyMoodGrid.associateBy { it.date } }
    val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    
    // We use a LazyVerticalGrid-like approach but since this might be rendered off-screen for capture,
    // a standard Column/Row structure is safer and more reliable for Canvas drawing.
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        for (month in 1..12) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
                for (day in 1..31) {
                    if (day <= daysInMonth) {
                        val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month, day)
                        val mood = moodMap[dateStr]
                        val cellColor = if (mood != null) {
                            MoonIcons.Moods.getMoodColor(mood.moodId.toInt(), themeType)
                        } else {
                            emptyColor
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(0.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cellColor)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(0.5.dp))
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
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val moodDistribution = stats?.moodDistribution ?: emptyList()
    val dominantDist = moodDistribution.maxByOrNull { it.percentage }
    val dominantMoodId = dominantDist?.baseMoodId ?: 3
    val dominantPercent = dominantDist?.percentage?.roundToInt() ?: 0
    val dominantMoodVisual = MoonIcons.Moods.getMoodVisual(dominantMoodId, themeType)

    val moodText = when (dominantMoodId) {
        5 -> "Tháng này bạn có vẻ rất vui! 🌟"
        4 -> "Tháng này bạn khá vui! 😊"
        3 -> "Tháng này bạn khá ổn định. 😌"
        2 -> "Tháng này có vẻ hơi khó khăn. 💙"
        else -> "Tháng này khá nặng nề. 🌙"
    }

    val moodFlow = stats?.moodFlow ?: emptyList()

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
                    text = "Mood Overview",
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
                    if (dominantMoodVisual.drawableRes != null) {
                        Image(
                            painter = painterResource(id = dominantMoodVisual.drawableRes),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.72f))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$dominantPercent%",
                        fontSize = 36.sp, fontWeight = FontWeight.Black,
                        color = onSurface, lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = moodText, fontSize = 14.sp, color = onSurfaceVariant, lineHeight = 20.sp)
                }
            }

            if (moodFlow.size >= 2) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                ) {
                    MoodSparkline(moodFlow = moodFlow, primaryColor = primaryColor)
                }
            }
        }
    }
}

@Composable
private fun MoodSparkline(moodFlow: List<MoodFlowDto>, primaryColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val sorted = moodFlow.sortedBy { it.date }
        if (sorted.size < 2) return@Canvas
        val maxIdx = (sorted.size - 1).toFloat()
        val path = Path()
        sorted.forEachIndexed { index, item ->
            val x = size.width * index / maxIdx
            val y = size.height * (5.0 - item.moodId).coerceIn(0.0, 4.0).toFloat() / 4f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(size.width, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()
        drawPath(path = fillPath, color = primaryColor.copy(alpha = 0.08f))
        drawPath(path = path, color = primaryColor.copy(alpha = 0.65f), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun SleepPhysicalRow(
    stats: com.diary.moonpage.data.remote.dto.stats.StatisticsResponse?,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SleepWidgetCard(avgSleepHours = stats?.averageSleepHours ?: 0.0, modifier = Modifier.weight(1f), onClick = onClick)
        PhysicalWidgetCard(totalSteps = stats?.totalSteps ?: 0, totalCalories = stats?.totalCalories ?: 0, modifier = Modifier.weight(1f), onClick = onClick)
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
            Text("Giấc ngủ", fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.55f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (avgSleepHours > 0) String.format(Locale.ENGLISH, "%.1f", avgSleepHours) else "--",
                fontSize = 30.sp, fontWeight = FontWeight.Black, color = onSurface, lineHeight = 30.sp
            )
            Text("giờ / đêm", fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun PhysicalWidgetCard(totalSteps: Int, totalCalories: Int, modifier: Modifier, onClick: () -> Unit) {
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
            Text("Thể chất", fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.55f))
            Spacer(modifier = Modifier.height(6.dp))
            if (totalSteps > 0) {
                Text(String.format(Locale.ENGLISH, "%,d", totalSteps), fontSize = 24.sp, fontWeight = FontWeight.Black, color = onSurface, lineHeight = 24.sp)
                Text("bước chân", fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.45f))
            } else {
                Text("--", fontSize = 30.sp, fontWeight = FontWeight.Black, color = onSurface)
            }
            if (totalCalories > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = primary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format(Locale.ENGLISH, "%,d kcal", totalCalories), fontSize = 12.sp, color = primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ActivityHabitsCard(frequentlyRecorded: List<BestActivityDto>, onClick: () -> Unit) {
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
                Text("Activity & Habits", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = onSurfaceVariant.copy(alpha = 0.55f))
                Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (frequentlyRecorded.isEmpty()) {
                Text("Chưa có dữ liệu hoạt động.", fontSize = 14.sp, color = onSurfaceVariant.copy(alpha = 0.5f))
            } else {
                val top = frequentlyRecorded.first()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MoonTheme.customColors.logItemBg), contentAlignment = Alignment.Center) {
                        MoonActivityIcon(icon = MoonIcons.getIconForActivity(top.activityName), size = 28.dp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(top.activityName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurface)
                        Text("Hoạt động nhiều nhất · ${top.occurrence} lần", fontSize = 13.sp, color = onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
                if (frequentlyRecorded.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        frequentlyRecorded.drop(1).take(2).forEach { activity ->
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MoonTheme.customColors.logItemBg), contentAlignment = Alignment.Center) {
                                MoonActivityIcon(icon = MoonIcons.getIconForActivity(activity.activityName), size = 24.dp)
                            }
                        }
                    }
                }
            }
        }
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
                    Text("Insights & Deep Dive", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = onSurfaceVariant.copy(alpha = 0.55f))
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Khám phá xem điều gì đang làm bạn vui lên hoặc buồn đi...",
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
                Text("Top Music", fontSize = 12.sp, color = onSurfaceVariant.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                if (topSong != null) {
                    Text(topSong.songTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = onSurface, maxLines = 1)
                    Text(topSong.artistName, fontSize = 12.sp, color = onSurfaceVariant, maxLines = 1)
                } else {
                    Text("Chưa có dữ liệu", fontSize = 14.sp, color = onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
        }
    }
}
