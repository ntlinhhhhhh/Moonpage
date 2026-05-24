package com.diary.moonpage.ui.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

@Composable
fun PhotoMomentWidgetPreview(
    showStreak: Boolean,
    displayMode: String,
    modifier: Modifier = Modifier
) {
    val previewSurface = Color(0xFFF4F6F1)
    Card(
        modifier = modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(previewSurface)) {
            Image(
                painter = painterResource(id = R.drawable.happy),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = if (displayMode == "CROP") ContentScale.Crop else ContentScale.Fit
            )


            if (showStreak) {
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xAA111111)
                ) {
                    Text(
                        text = "🔥 12",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
