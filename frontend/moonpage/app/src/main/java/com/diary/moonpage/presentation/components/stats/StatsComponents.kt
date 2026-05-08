package com.diary.moonpage.presentation.components.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import java.time.YearMonth

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFF4CAF50) else Color.Gray
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(40.dp)
                    .background(color)
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    hint: String? = null,
    onSampleClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    onClick = onSampleClick,
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF66BB6A).copy(alpha = 0.8f)
                ) {
                    Text(
                        "Sample",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (hint != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        hint,
                        color = Color(0xFF66BB6A),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            content()
        }
    }
}

@Composable
fun MoodFlowChart(moodFlow: List<MoodFlowDto>, year: Int, month: Int, isMonthly: Boolean = true) {
    val daysCount = if (isMonthly) YearMonth.of(year, month).lengthOfMonth() else 12
    val primaryColor = Color(0xFF81C784)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(top = 16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val moodLevels = 5
            val paddingY = 40f
            val startX = 60.dp.toPx()
            val endX = width - 20.dp.toPx()
            
            // Draw grid lines
            for (i in 0 until moodLevels) {
                val y = paddingY + (height - 2 * paddingY) * i / (moodLevels - 1)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.05f),
                    start = androidx.compose.ui.geometry.Offset(startX, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (moodFlow.isNotEmpty()) {
                val dx = (endX - startX) / (daysCount - 1).coerceAtLeast(1)
                val path = Path()
                
                var firstPoint = true
                moodFlow.forEachIndexed { index, item ->
                    if (item.moodId in 1..5) {
                        val x = startX + index * dx
                        val y = paddingY + (height - 2 * paddingY) * (item.moodId - 1) / (moodLevels - 1)
                        
                        if (firstPoint) {
                            path.moveTo(x, y)
                            firstPoint = false
                        } else {
                            // Quadratic curve for smoother lines
                            val prevX = startX + (index - 1) * dx
                            val prevY = paddingY + (height - 2 * paddingY) * (moodFlow[index-1].moodId - 1) / (moodLevels - 1)
                            path.quadraticBezierTo(prevX + dx/2, prevY, (prevX + x)/2, (prevY + y)/2)
                            path.lineTo(x, y)
                        }
                        
                        drawCircle(
                            color = primaryColor,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
        
        Column(
            modifier = Modifier.fillMaxHeight().width(50.dp).padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            listOf(
                MoonIcons.Moods.Happy,
                MoonIcons.Moods.Good,
                MoonIcons.Moods.Neutral,
                MoonIcons.Moods.Sad,
                MoonIcons.Moods.Angry
            ).forEach { mood ->
                Image(
                    painter = painterResource(id = mood.drawableRes!!),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(
                start = 54.dp,
                end = 16.dp,
                bottom = 4.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isMonthly) {
                val labelDays = listOf(1, 10, 20, daysCount)
                labelDays.forEach { day ->
                    Text(text = "$month/$day", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                listOf(1, 3, 6, 9, 12).forEach { m ->
                    Text(text = "$m", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun MoodDistributionView(distribution: List<MoodDistributionDto>) {
    val moods = listOf(
        MoonIcons.Moods.Happy,
        MoonIcons.Moods.Good,
        MoonIcons.Moods.Neutral,
        MoonIcons.Moods.Sad,
        MoonIcons.Moods.Angry
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            moods.forEach { mood ->
                val dist = distribution.find { 
                    it.label.equals(mood.name, ignoreCase = true) ||
                    (mood.name == "Happy" && it.label.equals("Rad", ignoreCase = true)) ||
                    (mood.name == "Neutral" && it.label.equals("Meh", ignoreCase = true)) ||
                    (mood.name == "Sad" && it.label.equals("Low", ignoreCase = true)) ||
                    (mood.name == "Angry" && it.label.equals("Bad", ignoreCase = true))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${dist?.percentage ?: 0}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(if(mood.name == "Neutral") 48.dp else 40.dp)
                            .clip(CircleShape)
                            .background(mood.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = mood.drawableRes!!),
                            contentDescription = mood.name,
                            modifier = Modifier.size(if(mood.name == "Neutral") 32.dp else 26.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            moods.forEach { mood ->
                val dist = distribution.find { 
                    it.label.equals(mood.name, ignoreCase = true) ||
                    (mood.name == "Happy" && it.label.equals("Rad", ignoreCase = true)) ||
                    (mood.name == "Neutral" && it.label.equals("Meh", ignoreCase = true)) ||
                    (mood.name == "Sad" && it.label.equals("Low", ignoreCase = true)) ||
                    (mood.name == "Angry" && it.label.equals("Bad", ignoreCase = true))
                }
                val weight = dist?.percentage?.toFloat() ?: 0f
                if (weight > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(weight)
                            .background(mood.color)
                    )
                }
            }
        }
    }
}

@Composable
fun YearInBeansView(year: Int, data: Map<Int, List<Int>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Look back on your $year.", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(24.dp))
            (1..12).forEach { m ->
                Text(
                    text = "$m",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        (1..31).forEach { day ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$day",
                    modifier = Modifier.width(24.dp),
                    fontSize = 9.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(4.dp))
                (1..12).forEach { month ->
                    val moodId = data[month]?.getOrNull(day - 1) ?: 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(1.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (moodId > 0) MoonIcons.Moods.getMoodColor(moodId)
                                else Color(0xFFF5F5F5)
                            )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Download Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun FrequentlyRecordedView(activities: List<BestActivityDto>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            activities.forEachIndexed { index, activity ->
                ActivityRankCard(
                    rank = index + 1,
                    name = activity.activityName,
                    count = activity.occurrence,
                    iconName = activity.activityName,
                    modifier = Modifier.weight(1f)
                )
            }
            repeat(3 - activities.size) {
                Box(modifier = Modifier.weight(1f))
            }
        }
        
        if (activities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You recorded ${activities.first().activityName} the most.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ActivityRankCard(rank: Int, name: String, count: Int, iconName: String, modifier: Modifier = Modifier) {
    val moonIcon = MoonIcons.getIconForActivity(iconName)
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$rank", modifier = Modifier.align(Alignment.Start), color = Color.Gray, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(moonIcon.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (moonIcon.drawableRes != null) {
                    Image(painter = painterResource(id = moonIcon.drawableRes), contentDescription = null, modifier = Modifier.size(32.dp))
                } else if (moonIcon.vector != null) {
                    Icon(moonIcon.vector, contentDescription = null, tint = moonIcon.color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, color = Color.Black)
            Text("x$count", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun BestAndWorstView(best: List<BestActivityDto>, worst: List<BestActivityDto>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (best.isNotEmpty()) {
            Text("When you were feeling good...", color = Color.Gray, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                best.forEachIndexed { index, activity ->
                    ActivityScoreCard(rank = index + 1, name = activity.activityName, score = activity.averageMoodScore, modifier = Modifier.weight(1f))
                }
                repeat(3 - best.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
        
        if (worst.isNotEmpty()) {
            Text("When you were feeling down...", color = Color.Gray, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                worst.forEachIndexed { index, activity ->
                    ActivityScoreCard(rank = index + 1, name = activity.activityName, score = activity.averageMoodScore, modifier = Modifier.weight(1f))
                }
                repeat(3 - worst.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun ActivityScoreCard(rank: Int, name: String, score: Double, modifier: Modifier = Modifier) {
    val moonIcon = MoonIcons.getIconForActivity(name)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$rank", modifier = Modifier.align(Alignment.Start), color = Color.Gray, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(moonIcon.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (moonIcon.drawableRes != null) {
                    Image(painter = painterResource(id = moonIcon.drawableRes), contentDescription = null, modifier = Modifier.size(28.dp))
                } else if (moonIcon.vector != null) {
                    Icon(moonIcon.vector, contentDescription = null, tint = moonIcon.color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(name, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1, color = Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFD700))
                Text(String.format(" %.1f", score), color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
