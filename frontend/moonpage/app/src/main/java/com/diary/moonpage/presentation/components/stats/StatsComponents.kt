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
import com.diary.moonpage.presentation.theme.*
import java.time.YearMonth

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFF66BB6A) else Color.Gray
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = Color(0xFF333333)
                )
                if (actionText != null) {
                    TextButton(onClick = onActionClick, contentPadding = PaddingValues(0.dp)) {
                        Text(actionText, color = Color.Gray, fontSize = 16.sp)
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun MoodFlowChart(moodFlow: List<MoodFlowDto>, year: Int, month: Int, isMonthly: Boolean = true) {
    val primaryColor = Color(0xFF66BB6A)
    val moodColors = listOf(
        MoonMoodHappy,
        MoonMoodGood,
        MoonMoodNeutral,
        MoonMoodSad,
        MoonMoodAngry
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

        Column(modifier = Modifier.fillMaxSize().padding(start = 35.dp)) {
            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val width = size.width
                val height = size.height
                
                // Vertical grid lines
                val gridCount = if (isMonthly) 6 else 11
                for (i in 0..gridCount) {
                    val x = width * i / gridCount
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (moodFlow.isNotEmpty()) {
                    val path = Path()
                    val dx = width / (if (isMonthly) 30 else 11).coerceAtLeast(1)
                    
                    moodFlow.forEachIndexed { index, item ->
                        val x = index * dx
                        val y = height * (item.moodId - 1).coerceIn(0, 4) / 4f
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                    }
                    drawPath(path = path, color = primaryColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            
            // X-Axis Labels
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                if (isMonthly) {
                    val nextMonth = if (month == 12) 1 else month + 1
                    listOf("$month/1", "$month/6", "$month/11", "$month/16", "$month/21", "$month/26", "$nextMonth/1").forEach {
                        Text(it, fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    (1..12).forEach { Text("$it", fontSize = 12.sp, color = Color.Gray) }
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            moods.forEach { mood ->
                val dist = distribution.find { 
                    it.label.equals(mood.name, ignoreCase = true) ||
                    (mood.name == "Happy" && it.label.equals("Rad", ignoreCase = true)) ||
                    (mood.name == "Neutral" && it.label.equals("Meh", ignoreCase = true)) ||
                    (mood.name == "Sad" && it.label.equals("Low", ignoreCase = true)) ||
                    (mood.name == "Angry" && it.label.equals("Bad", ignoreCase = true))
                }
                val percentage = dist?.percentage ?: 0
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = mood.drawableRes!!),
                        contentDescription = null,
                        modifier = Modifier.size(if (percentage > 25) 64.dp else 52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (percentage > 0) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                    ) {
                        Text(
                            "$percentage%",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (percentage > 0) Color(0xFF66BB6A) else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Thanh phân đoạn màu sắc bên dưới
        Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFFF5F5F5))) {
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
fun YearInBeansView(year: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("Look back on your ")
                withStyle(style = SpanStyle(color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)) {
                    append("$year")
                }
                append(".")
            },
            color = Color.Gray,
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
                            color = Color.Gray
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
                            color = Color.Gray,
                            textAlign = TextAlign.Start
                        )
                        (1..12).forEach { month ->
                            // Giả lập màu sắc tâm trạng cho lưới
                            val color = if ((month + day) % 2 == 0) MoonMoodHappy else MoonMoodGood
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun FrequentlyRecordedView(activities: List<BestActivityDto>) {
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
                    withStyle(style = SpanStyle(color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)) {
                        append(activities.first().activityName)
                    }
                    append(" the most.")
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ActivityRankCard(rank: Int, name: String, count: Int, modifier: Modifier = Modifier) {
    val icon = MoonIcons.getIconForActivity(name)
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$rank", modifier = Modifier.align(Alignment.Start), color = Color.Gray, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F9F5)),
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(painter = painterResource(id = icon.drawableRes), contentDescription = null, modifier = Modifier.size(44.dp))
                }
            }
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, textAlign = TextAlign.Center)
            Text("x$count", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun BestAndWorstView(best: List<BestActivityDto>, worst: List<BestActivityDto>) {
    Column {
        Text(
            text = buildAnnotatedString {
                append("When you were feeling ")
                withStyle(style = SpanStyle(color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)) {
                    append("good")
                }
                append("...")
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 20.dp)
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
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$rank", modifier = Modifier.align(Alignment.Start), color = Color.Gray, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F9F5)),
                contentAlignment = Alignment.Center
            ) {
                if (icon.drawableRes != null) {
                    Image(painter = painterResource(id = icon.drawableRes), contentDescription = null, modifier = Modifier.size(44.dp))
                }
            }
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, textAlign = TextAlign.Center)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF66BB6A))
                    Text(String.format(" %.1f", score), color = Color(0xFF66BB6A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PremiumAnalysisSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFF66BB6A), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Premium Analysis", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 20.sp)
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
                    withStyle(style = SpanStyle(color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)) {
                        append("coffee")
                    }
                },
                color = Color.Gray,
                fontSize = 18.sp
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF66BB6A),
                onClick = {}
            ) {
                Text(
                    "Sample",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Color.White,
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
                    .background(Color(0xFFF9FBF9)),
                contentAlignment = Alignment.Center
            ) {
                val icon = MoonIcons.getIconForActivity("Coffee")
                if (icon.drawableRes != null) {
                    Image(painter = painterResource(id = icon.drawableRes), contentDescription = null, modifier = Modifier.size(52.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Segmented Bar mẫu trong Premium
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                listOf(
                    MoonMoodHappy.copy(alpha = 0.5f),
                    MoonMoodGood.copy(alpha = 0.7f),
                    MoonMoodNeutral.copy(alpha = 0.9f),
                    MoonMoodSad.copy(alpha = 0.6f),
                    MoonMoodAngry.copy(alpha = 0.8f)
                ).forEach { color ->
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(color))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Recorded together with snack", color = Color.Gray, fontSize = 16.sp)
    }
}
