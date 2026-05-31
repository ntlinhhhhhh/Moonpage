package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ThemeDefaultPreviewSurface = Color(0xFFF4F6F1)
private val ThemeDefaultPreviewText = Color(0xFF333333)
private val ThemeDefaultPreviewSubText = Color(0xFF888888)
private val ThemeDefaultPreviewMuted = Color(0xFFE0DDD8)
private val ThemeDefaultMoodCircles = listOf(
    Color(0xFFF5DE6E),
    Color(0xFFA8D96E),
    Color(0xFF5BAD6E),
    Color(0xFF2D6E45),
    Color(0xFF4A4A4A)
)

@Composable
fun WeeklyMoodWidgetPreview(
    showStreak: Boolean,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val startOfWeek = today.minusDays(if (today.dayOfWeek == DayOfWeek.SUNDAY) 0 else today.dayOfWeek.value.toLong())
    val monthLabel = today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))
    val sampleIcons = listOf(
        R.drawable.very_happy,
        R.drawable.happy,
        null,
        R.drawable.neutral,
        R.drawable.sad,
        null,
        null
    )
    val days = (0..6).map { offset ->
        val date = startOfWeek.plusDays(offset.toLong())
        Triple(date, sampleIcons[offset], date == today)
    }
    val dayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDefaultPreviewSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthLabel,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        color = ThemeDefaultPreviewText,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemeDefaultPreviewSubText,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    days.forEachIndexed { index, (date, iconRes, isToday) ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                iconRes != null -> {
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                        color = ThemeDefaultMoodCircles[index],
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Image(
                                                painter = painterResource(id = iconRes),
                                                contentDescription = null,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                                isToday -> {
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, ThemeDefaultPreviewSubText),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ThemeDefaultPreviewSubText
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                        color = ThemeDefaultPreviewMuted,
                                        modifier = Modifier.size(28.dp)
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }

            if (showStreak) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    color = Color(0xCC000000)
                ) {
                    Text(
                        text = "\uD83D\uDD25 12",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
