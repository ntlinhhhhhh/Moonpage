package com.diary.moonpage.ui.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diary.moonpage.R

@Composable
fun PasscodeDots(count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < count) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    extraButton: @Composable (() -> Unit)? = null
) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "extra", "0", "delete")

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.width(280.dp)
    ) {
        items(keys) { key ->
            when (key) {
                "extra" -> {
                    if (extraButton != null) {
                        extraButton()
                    } else {
                        Spacer(modifier = Modifier.size(64.dp))
                    }
                }
                "delete" -> KeypadButton(
                    icon = { Icon(Icons.Default.Backspace, contentDescription = stringResource(R.string.delete)) },
                    onClick = onDeleteClick
                )
                else -> KeypadButton(
                    text = key,
                    onClick = { onNumberClick(key) }
                )
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        } else if (icon != null) {
            icon()
        }
    }
}
