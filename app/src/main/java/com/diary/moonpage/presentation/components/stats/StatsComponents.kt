package com.diary.moonpage.presentation.components.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    themeType: MoonThemeType = MoonThemeType.DEFAULT
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

        Column(modifier = Modifier.fillMaxSize().padding(start = 35.dp, end = 10.dp)) {
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

                if (moodFlow.isNotEmpty()) {
                    val path = Path()
                    // If monthly, we show days. Max days in month is 31.
                    // If annual, we show months. 12 months.
                    val maxSlots = if (isMonthly) {
                        YearMonth.of(year, month).lengthOfMonth() - 1
                    } else {
                        11
                    }
                    
                    val dx = if (maxSlots > 0) width / maxSlots else 0f
                    
                    moodFlow.forEachIndexed { index, item ->
                        // Try to parse day from date string "yyyy-MM-dd"
                        val dayOfMonth = try {
                            val parts = item.date.split("-")
                            if (isMonthly) parts.last().toInt() - 1 else parts[1].toInt() - 1
                        } catch (e: Exception) {
                            index
                        }
                        
                        val x = (dayOfMonth * dx).coerceIn(0f, width)
                        val y = height * (item.moodId - 1).coerceIn(0, 4) / 4f
                        
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                    }
                    drawPath(path = path, color = primaryColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            
            // X-Axis Labels
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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

@Composable
fun MoodDistributionView(
    distribution: List<MoodDistributionDto>,
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val moods = listOf(
        MoonIcons.Moods.getMoodVisual(1, themeType),
        MoonIcons.Moods.getMoodVisual(2, themeType),
        MoonIcons.Moods.getMoodVisual(3, themeType),
        MoonIcons.Moods.getMoodVisual(4, themeType),
        MoonIcons.Moods.getMoodVisual(5, themeType)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            moods.forEachIndexed { index, mood ->
                val moodId = index + 1
                val dist = distribution.find { 
                    it.label.equals(mood.name, ignoreCase = true) ||
                    (moodId == 1 && it.label.equals("Rad", ignoreCase = true)) ||
                    (moodId == 3 && it.label.equals("Meh", ignoreCase = true)) ||
                    (moodId == 4 && it.label.equals("Low", ignoreCase = true)) ||
                    (moodId == 5 && it.label.equals("Bad", ignoreCase = true))
                }
                val percentage = dist?.percentage ?: 0
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (mood.drawableRes != null) {
                        Image(
                            painter = painterResource(id = mood.drawableRes!!),
                            contentDescription = null,
                            modifier = Modifier.size(if (percentage > 25) 64.dp else 52.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (percentage > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MoonTheme.customColors.logItemBg
                    ) {
                        Text(
                            "$percentage%",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (percentage > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Thanh phÃ¢n Ä‘oáº¡n mÃ u sáº¯c bÃªn dÆ°á»›i
        Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(22.dp)).background(MoonTheme.customColors.logItemBg)) {
            moods.forEach { mood ->
                val dist = distribution.find { it.label.equals(mood.name, true) }
                val weight = dist?.percentage?.toFloat() ?: 0f
                if (weight > 0) {
                    Box(modifier = Modifier.fillMaxHeight().weight(weight).background(mood.color))
                }
            }
        }
    }
}

@Composable
fun YearInBeansView(
    year: Int,
    themeType: MoonThemeType = MoonThemeType.DEFAULT
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("Look back on your ")
                withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append("$year")
                }
                append(".")
            },
            color = onSurfaceVariant,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Column {
                // Header row
                Row {
                    Spacer(modifier = Modifier.width(35.dp))
                    (1..12).forEach { m ->
                        Text(
                            text = "$m",
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Samples showing first few rows like in the image
                (1..5).forEach { day ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(
                            text = "$day",
                            modifier = Modifier.width(35.dp),
                            fontSize = 14.sp,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Start
                        )
                        (1..12).forEach { month ->
                            // Giáº£ láº­p mÃ u sáº¯c tÃ¢m tráº¡ng cho lÆ°á»›i
                            val color = if ((month + day) % 2 == 0) MoonIcons.Moods.getMoodColor(1, themeType) else MoonIcons.Moods.getMoodColor(2, themeType)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("OK", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun FrequentlyRecordedView(activities: List<BestActivityDto>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) { index ->
                if (index < activities.size) {
                    val activity = activities[index]
                    ActivityRankCard(rank = index + 1, name = activity.activityName, count = activity.occurrence, modifier = Modifier.weight(1f))
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
fun ActivityRankCard(rank: Int, name: String, count: Int, modifier: Modifier = Modifier) {
    val icon = MoonIcons.getIconForActivity(name)
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$rank", modifier = Modifier.align(Alignment.Start), color = onSurfaceVariant, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MoonTheme.customColors.logItemBg),
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(painter = painterResource(id = icon.drawableRes), contentDescription = null, modifier = Modifier.size(44.dp))
                }
            }
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, textAlign = TextAlign.Center)
            Text("x$count", color = onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
fun BestAndWorstView(best: List<BestActivityDto>, worst: List<BestActivityDto>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
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
            modifier = Modifier.padding(bottom = 20.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) { index ->
                if (index < best.size) {
                    val activity = best[index]
                    ActivityScoreCard(rank = index + 1, name = activity.activityName, score = activity.averageMoodScore, modifier = Modifier.weight(1f))
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActivityScoreCard(rank: Int, name: String, score: Double, modifier: Modifier = Modifier) {
    val icon = MoonIcons.getIconForActivity(name)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MoonTheme.customColors.logCardBg),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$rank", modifier = Modifier.align(Alignment.Start), color = onSurfaceVariant, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MoonTheme.customColors.logItemBg),
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(painter = painterResource(id = icon.drawableRes), contentDescription = null, modifier = Modifier.size(44.dp))
                }
            }
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, textAlign = TextAlign.Center)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = primaryColor.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                    Text(String.format(" %.1f", score), color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
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
                if (icon.drawableRes != null) {
                    Image(painter = painterResource(id = icon.drawableRes), contentDescription = null, modifier = Modifier.size(52.dp))
                }
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
