package com.diary.moonpage.presentation.components.core.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoonDivider(text: String) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = lineColor)
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = lineColor)
    }
}
