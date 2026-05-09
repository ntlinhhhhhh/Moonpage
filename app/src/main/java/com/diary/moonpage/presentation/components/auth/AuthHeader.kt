package com.diary.moonpage.presentation.components.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthHeader(title: String, subtitle: String) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Text(
        text = title,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.displayLarge.copy(
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyLarge,
        color = textColor.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        lineHeight = 24.sp,
        modifier = Modifier.padding(bottom = 32.dp)
    )
}
