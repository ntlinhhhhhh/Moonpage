package com.diary.moonpage.presentation.components.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
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
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
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
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Mood indicators on the left
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            moodColors.forEach { color ->
                Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(start = 35.dp, end = 10.dp, bottom = 16.dp)) {
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
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Menstruation Background (Monthly only)
                if (isMonthly && menstruationDates.isNotEmpty()) {
                    val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
                    val dx = if (daysInMonth > 1) width / (daysInMonth - 1) else 0f
                    val currentMonthStr = String.format("%04d-%02d", year, month)
                    
                    menstruationDates.filter { it.startsWith(currentMonthStr) }.forEach { dateStr ->
                        try {
                            val d = dateStr.split("-").last().toInt() - 1
                            val x = d * dx
                            drawRect(
                                color = Color(0xFFFFCDD2).copy(alpha = 0.4f),
                                topLeft = androidx.compose.ui.geometry.Offset(x - dx/2, 0f),
                                size = androidx.compose.ui.geometry.Size(dx, height)
                            )
                        } catch (e: Exception) {}
                    }
                }

                if (moodFlow.isNotEmpty()) {
                    val currentMonthStr = String.format("%04d-%02d", year, month)
                    val filteredMoods = if (isMonthly) {
                        moodFlow.filter { it.date.startsWith(currentMonthStr) }
                    } else {
                        moodFlow.filter { it.date.startsWith(year.toString()) }
                    }

                    val path = Path()
                    val maxSlots = if (isMonthly) {
                        java.time.YearMonth.of(year, month).lengthOfMonth() - 1
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
                        val y = height * (5 - item.moodId).coerceIn(0, 4) / 4f
                        
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
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
                    val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            moods.forEach { mood ->
                val matchingDists = distribution.filter { 
                    it.label.equals(mood.name, ignoreCase = true) ||
                    (mood.name == "Happy" && it.label.equals("Rad", ignoreCase = true)) ||
                    (mood.name == "Good" && it.label.equals("Glad", ignoreCase = true)) ||
                    (mood.name == "Neutral" && it.label.equals("Meh", ignoreCase = true)) ||
                    (mood.name == "Sad" && it.label.equals("Gloomy", ignoreCase = true)) ||
                    (mood.name == "Angry" && (it.label.equals("Bad", ignoreCase = true) || it.label.equals("Awful", ignoreCase = true)))
                }
                
                val rawPercent = matchingDists.sumOf { it.percentage }
                val currentMoodPercent: Int = Math.round(rawPercent).toInt()
                
                val iconSize = 32.dp
                val containerSize = 48.dp
                val tintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) 
                val colorFilter = if (rawPercent > 0) {
                    androidx.compose.ui.graphics.ColorFilter.tint(Color.Black.copy(alpha = 0.7f)) // Or theme-aware?
                } else {
                    androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
                }
                
                val iconTint = if (rawPercent > 0) {
                    // Use a slightly darker color on light backgrounds
                    Color.Black.copy(alpha = 0.7f)
                } else tintColor
                
                val tagBgColor = if (rawPercent > 0) {
                    mood.color.copy(alpha = 0.85f)
                } else MoonTheme.customColors.logItemBg
                
                // Use a very dark color for text to ensure visibility on all backgrounds
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
                                painter = painterResource(id = mood.drawableRes!!),
                                contentDescription = null,
                                modifier = Modifier.size(iconSize),
                                colorFilter = if (rawPercent > 0) androidx.compose.ui.graphics.ColorFilter.tint(iconTint) else androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
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
            val moodMappings = listOf(
                listOf("Happy", "Rad", "Very Happy"),
                listOf("Good", "Glad", "Content", "Nice"),
                listOf("Neutral", "Meh", "Normal"),
                listOf("Sad", "Gloomy", "Low"),
                listOf("Angry", "Bad", "Awful", "Very Sad")
            )

            moods.forEachIndexed { index, mood ->
                val aliases = moodMappings[index]
                val weight = distribution
                    .filter { d -> aliases.any { alias -> d.label.equals(alias, ignoreCase = true) } }
                    .sumOf { it.percentage }
                    .toFloat()
                
                if (weight > 0) {
                    Box(modifier = Modifier.fillMaxHeight().weight(weight).background(mood.color))
                }
            }
            
            // Check for any remaining data from API (Optional safety check)
            val totalApiWeight = distribution.sumOf { it.percentage }.toFloat()
            val matchedWeight = moods.indices.sumOf { index ->
                val aliases = moodMappings[index]
                distribution
                    .filter { d -> aliases.any { alias -> d.label.equals(alias, ignoreCase = true) } }
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
    menstruationDates: List<String> = emptyList(),
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MoonTheme.customColors.logItemBg
    
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
                            val dateStr = String.format("%04d-%02d-%02d", 2026, month, day)
                            val mood = yearlyMoodGrid.find { it.date == dateStr }
                            val isPeriod = menstruationDates.contains(dateStr)
                            
                            val color = if (mood != null) {
                                MoonIcons.Moods.getMoodColor(mood.moodId, themeType)
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
    totalSteps: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryItem(
            label = "Avg Sleep", 
            value = String.format(Locale.ENGLISH, "%.1fh", averageSleepHours), 
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Bedtime
        )
        SummaryItem(
            label = "Bedtime", 
            value = averageSleepStartTime ?: "--:--", 
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Alarm
        )
        SummaryItem(
            label = "Steps", 
            value = String.format(Locale.ENGLISH, "%,d", totalSteps), 
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Rounded.DirectionsRun
        )
    }
}

@Composable
fun SleepAnalysisChart(sleepData: List<com.diary.moonpage.data.remote.dto.stats.SleepAnalysisDto>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (sleepData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No sleep data yet", color = labelColor.copy(alpha = 0.5f))
                }
            } else {
                sleepData.forEach { data ->
                    val heightFactor = (data.duration / 12.0).coerceIn(0.0, 1.0).toFloat()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFactor)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (data.duration >= 7.0 && data.duration <= 9.0) primaryColor else primaryColor.copy(alpha = 0.4f))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Horizontal Target Line (8h)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0h", fontSize = 11.sp, color = labelColor)
            Text("Ideal: 7-9h", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
            Text("12h+", fontSize = 11.sp, color = labelColor)
        }
    }
}

@Composable
fun SleepMoodCorrelationChart(sleepData: List<com.diary.moonpage.data.remote.dto.stats.SleepAnalysisDto>, themeType: MoonThemeType) {
    if (sleepData.isEmpty()) return

    val ranges = listOf(
        "Short" to (0.0..6.0),
        "Healthy" to (6.0..9.0),
        "Long" to (9.0..24.0)
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ranges.forEach { (label, range) ->
            val entriesInRange = sleepData.filter { it.duration in range }
            if (entriesInRange.isNotEmpty()) {
                val total = entriesInRange.size.toFloat()
                val moodCounts = (1..5).associateWith { id -> entriesInRange.count { it.moodId == id } }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = label,
                        modifier = Modifier.width(60.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MoonTheme.customColors.logItemBg)
                    ) {
                        (1..5).forEach { moodId ->
                            val count = moodCounts[moodId] ?: 0
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(count / total)
                                        .background(MoonIcons.Moods.getMoodColor(moodId, themeType))
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
fun MonthlyMoodAverageChart(yearlyMoodGrid: List<MoodFlowDto>, year: Int, themeType: MoonThemeType) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val averages = (1..12).map { m ->
        val currentMonthStr = String.format("%04d-%02d", year, m)
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
                text = String.format("%.1f", selectedActivity.averageMoodScore),
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
        
        // Premium segmented distribution bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MoonTheme.customColors.logItemBg)
        ) {
            // Mocking distribution: 50% Happy, 30% Good, 10% Neutral, 10% Sad, 0% Angry
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
fun FrequentlyRecordedView(activities: List<BestActivityDto>, onIconClick: (String) -> Unit = {}) {
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
    val errorColor = MaterialTheme.colorScheme.error
    
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        // Best Section
        Column {
            Text(
                text = buildAnnotatedString {
                    append("When you were feeling ")
                    withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                        append("good")
                    }
                    append("...")
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (best.isEmpty()) {
                Text("No data yet", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
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
                    // Fill remaining space if less than 3
                    repeat(3 - best.size.coerceAtMost(3)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Worst Section
        Column {
            Text(
                text = buildAnnotatedString {
                    append("When you were feeling ")
                    withStyle(style = SpanStyle(color = errorColor, fontWeight = FontWeight.Bold)) {
                        append("not so good")
                    }
                    append("...")
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (worst.isEmpty()) {
                Text("No data yet", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
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
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
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
                    text = String.format("%.1f", score), 
                    color = color, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun PremiumAnalysisSection(themeType: MoonThemeType = MoonThemeType.DEFAULT) {
    val shades = getThemeShades(themeType)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = primaryColor, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Premium Analysis", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp)
    }
    
    StatsCard(title = "Icon Deep Dive") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Your mood for ")
                    withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                        append("coffee")
                    }
                },
                color = onSurfaceVariant,
                fontSize = 18.sp
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = primaryColor,
                onClick = {}
            ) {
                Text(
                    "Sample",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MoonTheme.customColors.logItemBg),
                contentAlignment = Alignment.Center
            ) {
                val icon = MoonIcons.getIconForActivity("Coffee")
                MoonActivityIcon(icon = icon, size = 52.dp)
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Segmented Bar máº«u trong Premium
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MoonTheme.customColors.logItemBg)
            ) {
                shades.forEach { color ->
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(color))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Recorded together with snack", color = onSurfaceVariant, fontSize = 16.sp)
    }
}
