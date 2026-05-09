package com.diary.moonpage.presentation.components.moment

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TagChip(
    tag: MomentTag,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    // When no explicit containerColor, use surfaceVariant → content should be onSurfaceVariant
    val effectiveContentColor = if (tag.containerColor == null) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        tag.contentColor
    }
    val resolvedTag = tag.copy(contentColor = effectiveContentColor)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            color = tag.containerColor ?: MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp),
            modifier = modifier
        ) {
            TagChipContent(resolvedTag)
        }
    } else {
        Surface(
            color = tag.containerColor ?: MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp),
            modifier = modifier
        ) {
            TagChipContent(resolvedTag)
        }
    }
}

@Composable
private fun TagChipContent(tag: MomentTag) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tag.icon?.let {
            Icon(
                it,
                null,
                tint = tag.contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            tag.text,
            color = tag.contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

