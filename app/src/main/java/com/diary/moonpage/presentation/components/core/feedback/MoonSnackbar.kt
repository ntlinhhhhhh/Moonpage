package com.diary.moonpage.presentation.components.core.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diary.moonpage.core.theme.MoonTheme

@Composable
fun MoonSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    topPadding: androidx.compose.ui.unit.Dp = 105.dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = hostState,
            modifier = modifier
                .align(Alignment.TopCenter)
                .padding(top = topPadding)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) { data ->
            val isError = data.visuals.message.contains("future", ignoreCase = true) ||
                    data.visuals.message.contains("Failed", ignoreCase = true) ||
                    data.visuals.message.contains("Please select", ignoreCase = true) ||
                    data.visuals.message.contains("error", ignoreCase = true) ||
                    data.visuals.message.contains("invalid", ignoreCase = true)
            
            val isSuccess = data.visuals.message.contains("success", ignoreCase = true) ||
                    data.visuals.message.contains("deleted", ignoreCase = true) ||
                    data.visuals.message.contains("recorded", ignoreCase = true) ||
                    data.visuals.message.contains("updated", ignoreCase = true) ||
                    data.visuals.message.contains("edited", ignoreCase = true) ||
                    data.visuals.message.contains("saved", ignoreCase = true) ||
                    data.visuals.message.contains("sent", ignoreCase = true)
            
            val isWarning = data.visuals.message.contains("already", ignoreCase = true) ||
                    data.visuals.message.contains("warning", ignoreCase = true)

            val iconColor = when {
                isSuccess -> MoonTheme.customColors.successColor
                isWarning -> MoonTheme.customColors.warningColor
                isError -> MoonTheme.customColors.errorColor
                else -> MoonTheme.customColors.snackbarOnBg
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MoonTheme.customColors.snackbarBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        isError -> Icons.Rounded.Error
                        isWarning -> Icons.Rounded.Warning
                        isSuccess -> Icons.Rounded.CheckCircle
                        else -> Icons.Rounded.Info
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = data.visuals.message,
                    color = MoonTheme.customColors.snackbarOnBg,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
