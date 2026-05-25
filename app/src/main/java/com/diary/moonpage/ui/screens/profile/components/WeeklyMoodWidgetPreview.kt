package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ThemeDefaultPreviewSurface = Color(0xFFF4F6F1)
private val ThemeDefaultMoodCircle = Color(0xFFFFF2C2)

@Composable
fun WeeklyMoodWidgetPreview(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDefaultPreviewSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            days.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(day, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Surface(
                        shape = CircleShape,
                        color = ThemeDefaultMoodCircle,
                        modifier = Modifier.size(24.dp)
                    ) {}
                }
            }
        }
    }
}
