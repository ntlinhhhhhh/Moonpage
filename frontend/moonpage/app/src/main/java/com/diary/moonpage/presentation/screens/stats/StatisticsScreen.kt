package com.diary.moonpage.presentation.screens.stats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.core.util.MoonIcons
import com.diary.moonpage.data.remote.dto.stats.BestActivityDto
import com.diary.moonpage.data.remote.dto.stats.MoodDistributionDto
import com.diary.moonpage.data.remote.dto.stats.MoodFlowDto
import com.diary.moonpage.presentation.components.calendar.MonthYearPickerBottomSheet
import com.diary.moonpage.presentation.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatisticsRoute(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsScreen(
        uiState = uiState,
        onMonthChange = viewModel::onMonthSelected,
        onTabChange = viewModel::setMonthly
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
    onMonthChange: (Int, Int) -> Unit,
    onTabChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }

    val stats = uiState.stats
    
    // Process and sort activities from API
    val frequentlyRecorded = stats?.bestActivities?.sortedByDescending { it.occurrence }?.take(3) ?: emptyList()
    val bestActivities = stats?.bestActivities?.sortedByDescending { it.averageMoodScore }?.take(3) ?: emptyList()
    val worstActivities = stats?.bestActivities?.sortedBy { it.averageMoodScore }?.take(3) ?: emptyList()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Report",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colorScheme.background
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabItem("Monthly", uiState.isMonthly, onClick = { onTabChange(true) })
                    TabItem("Annual", !uiState.isMonthly, onClick = { onTabChange(false) })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val date = LocalDate.of(uiState.selectedYear, uiState.selectedMonth, 1)
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onBackground
                    )
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        containerColor = if (isSystemInDarkTheme()) colorScheme.background else Color(0xFFF7F7F2)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mood Flow
                    StatsCard(
                        title = "Mood Flow", 
                        hint = if (stats?.moodFlow.isNullOrEmpty()) "Please add an entry." else null
                    ) {
                        MoodFlowChart(stats?.moodFlow ?: emptyList(), uiState.selectedYear, uiState.selectedMonth)
                    }

                    // Mood Bar
                    StatsCard(
                        title = "Mood Bar",
                        hint = if (stats?.moodDistribution.isNullOrEmpty()) "Please add an entry." else null
                    ) {
                        MoodDistributionView(stats?.moodDistribution ?: emptyList())
                    }

                    // Frequently Recorded
                    StatsCard(
                        title = "Frequently Recorded",
                        hint = if (frequentlyRecorded.isEmpty()) "Please record an icon." else null
                    ) {
                        FrequentlyRecordedView(frequentlyRecorded)
                    }

                    // Best & Worst
                    StatsCard(
                        title = "Best & Worst",
                        hint = if (stats?.bestActivities.isNullOrEmpty()) "You need an icon that has been recorded 3 times or more." else null
                    ) {
                        BestAndWorstView(bestActivities, worstActivities)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        MonthYearPickerBottomSheet(
            currentYearMonth = YearMonth.of(uiState.selectedYear, uiState.selectedMonth),
            onConfirm = { year, month ->
                onMonthChange(year, month)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFF4CAF50) else Color.Gray
    Column(
        modifier = Modifier
            .width(120.dp)
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
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                    color = colorScheme.onSurface
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
                        .background(Color(0xFFE8F5E9).copy(alpha = if (isSystemInDarkTheme()) 0.15f else 1f))
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
fun MoodFlowChart(moodFlow: List<MoodFlowDto>, year: Int, month: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val moodLevels = 5
            val paddingY = 40f
            val startX = 60.dp.toPx()
            val endX = width - 20.dp.toPx()
            
            // Draw horizontal grid lines
            for (i in 0 until moodLevels) {
                val y = paddingY + (height - 2 * paddingY) * i / (moodLevels - 1)
                drawLine(
                    color = colorScheme.onSurface.copy(alpha = 0.05f),
                    start = androidx.compose.ui.geometry.Offset(startX, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (moodFlow.isNotEmpty()) {
                val dx = (endX - startX) / (daysInMonth - 1).coerceAtLeast(1)
                val path = Path()
                
                // Map of day to moodId - handle potential multiple logs per day by taking average or last
                val flowMap = moodFlow.associate { 
                    try {
                        val cleanDate = if (it.date.contains("T")) it.date.split("T")[0] else it.date
                        LocalDate.parse(cleanDate).dayOfMonth to it.moodId
                    } catch (e: Exception) {
                        0 to 0
                    }
                }.filter { it.key != 0 }

                var firstPoint = true
                for (day in 1..daysInMonth) {
                    val moodId = flowMap[day]
                    if (moodId != null && moodId in 1..5) {
                        val x = startX + (day - 1) * dx
                        // 1: Happy (Top), 5: Angry (Bottom)
                        val y = paddingY + (height - 2 * paddingY) * (moodId - 1) / (moodLevels - 1)
                        
                        if (firstPoint) {
                            path.moveTo(x, y)
                            firstPoint = false
                        } else {
                            path.lineTo(x, y)
                        }
                        
                        drawCircle(
                            color = Color(0xFF81C784),
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
                drawPath(
                    path = path,
                    color = Color(0xFF81C784),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        // Left mood icons
        Column(
            modifier = Modifier.fillMaxHeight().width(50.dp).padding(vertical = 10.dp),
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(start = 60.dp, end = 20.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelDays = listOf(1, 6, 11, 16, 21, 26, daysInMonth)
            labelDays.forEach { day ->
                Text(
                    text = if (day == daysInMonth) "${if(month == 12) 1 else month+1}/1" else "$month/$day",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
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
                // Map API labels to internal mood names
                val dist = distribution.find { 
                    it.label.equals(mood.name, ignoreCase = true) ||
                    (mood.name == "Happy" && it.label.equals("Rad", ignoreCase = true)) ||
                    (mood.name == "Neutral" && it.label.equals("Meh", ignoreCase = true)) ||
                    (mood.name == "Sad" && it.label.equals("Low", ignoreCase = true)) ||
                    (mood.name == "Angry" && it.label.equals("Bad", ignoreCase = true))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if(mood.name == "Neutral") 52.dp else 44.dp)
                            .clip(CircleShape)
                            .background(mood.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = mood.drawableRes!!),
                            contentDescription = mood.name,
                            modifier = Modifier.size(if(mood.name == "Neutral") 36.dp else 30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${dist?.percentage ?: 0}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Horizontal distribution bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            var totalWeight = 0f
            moods.forEach { mood ->
                val dist = distribution.find { 
                    it.label.equals(mood.name, ignoreCase = true) ||
                    (mood.name == "Happy" && it.label.equals("Rad", ignoreCase = true)) ||
                    (mood.name == "Neutral" && it.label.equals("Meh", ignoreCase = true))
                }
                val weight = dist?.percentage?.toFloat() ?: 0f
                if (weight > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(weight)
                            .background(mood.color)
                    )
                    totalWeight += weight
                }
            }
            if (totalWeight == 0f) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            }
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ActivityRankCard(rank: Int, name: String, count: Int, iconName: String, modifier: Modifier = Modifier) {
    val moonIcon = MoonIcons.getIconForActivity(iconName)
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f))
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
            Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, color = colorScheme.onSurface)
            Text("x$count", color = colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
            Text(name, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFD700))
                Text(String.format(" %.1f", score), color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
