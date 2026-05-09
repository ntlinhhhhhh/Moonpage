package com.diary.moonpage.presentation.components.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AuthFooter(questionText: String, actionText: String, onActionClick: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.tertiary

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = questionText,
            style = MaterialTheme.typography.labelLarge,
            color = textColor.copy(alpha = 0.7f)
        )
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelLarge.copy(
                color = linkColor,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onActionClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
