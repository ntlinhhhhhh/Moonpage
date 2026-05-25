package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

private val ThemeDefaultPreviewSurface = Color(0xFFF4F6F1)
private val ThemeDefaultMoodCircle = Color(0xFFFFF2C2)

@Composable
fun QuickMoodWidgetPreview(
    modifier: Modifier = Modifier
) {
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
            val moods = listOf(R.drawable.very_happy, R.drawable.happy, R.drawable.neutral, R.drawable.sad, R.drawable.very_sad)
            moods.forEach { mood ->
                Surface(
                    shape = CircleShape,
                    color = ThemeDefaultMoodCircle,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = mood),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}
