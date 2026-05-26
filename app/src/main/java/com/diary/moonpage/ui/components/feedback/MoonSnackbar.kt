package com.diary.moonpage.ui.components.feedback

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diary.moonpage.core.theme.MoonTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    topPadding: Dp = 60.dp
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .padding(horizontal = 16.dp)
    ) { data ->
        val appVisuals = data.visuals as? AppSnackbarVisuals
        val type = appVisuals?.type ?: inferType(data.visuals.message)
        val message = appVisuals?.uiText?.asString() ?: data.visuals.message
        val backgroundColor = MoonTheme.customColors.snackbarBg
        val contentColor = MoonTheme.customColors.snackbarOnBg
        val (icon, iconTint) = snackbarIconStyle(type)

        if (appVisuals == null && message.isBlank()) {
            Snackbar(snackbarData = data)
            return@SnackbarHost
        }

        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                    data.dismiss()
                    true
                } else {
                    false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
private fun snackbarIconStyle(type: SnackbarType): Pair<ImageVector, Color> {
    return when (type) {
        SnackbarType.SUCCESS -> Icons.Rounded.CheckCircle to MoonTheme.customColors.successColor
        SnackbarType.ERROR -> Icons.Rounded.Error to MoonTheme.customColors.errorColor
        SnackbarType.WARNING -> Icons.Rounded.Warning to MoonTheme.customColors.warningColor
        SnackbarType.INFO -> Icons.Rounded.Info to MoonTheme.customColors.snackbarOnBg
    }
}
