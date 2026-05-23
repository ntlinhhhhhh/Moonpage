package com.diary.moonpage.ui.components.inputs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonTimePicker(
    state: TimePickerState,
    modifier: Modifier = Modifier
) {
    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme
    val titleFontFamily = typography.titleLarge.fontFamily
    val primaryColor = colorScheme.primary

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography.copy(
            displayLarge = TextStyle(
                fontFamily = titleFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                lineHeight = typography.displayLarge.lineHeight,
                letterSpacing = typography.displayLarge.letterSpacing,
                fontStyle = FontStyle.Normal
            ),
            displayMedium = typography.displayMedium.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            displaySmall = typography.displaySmall.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            headlineLarge = typography.headlineLarge.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            headlineMedium = typography.headlineMedium.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            headlineSmall = typography.headlineSmall.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            titleLarge = typography.titleLarge.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            titleMedium = typography.titleMedium.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            titleSmall = typography.titleSmall.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            bodyLarge = typography.bodyLarge.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            bodyMedium = typography.bodyMedium.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            bodySmall = typography.bodySmall.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            labelLarge = typography.labelLarge.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            labelMedium = typography.labelMedium.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal),
            labelSmall = typography.labelSmall.copy(fontFamily = titleFontFamily, fontStyle = FontStyle.Normal)
        )
    ) {
        TimePicker(
            state = state,
            modifier = modifier,
            colors = TimePickerDefaults.colors(
                clockDialSelectedContentColor = Color.White,
                clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                clockDialColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                selectorColor = primaryColor,
                periodSelectorSelectedContainerColor = primaryColor.copy(alpha = 0.2f),
                periodSelectorSelectedContentColor = primaryColor,
                periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                timeSelectorSelectedContainerColor = primaryColor.copy(alpha = 0.2f),
                timeSelectorSelectedContentColor = primaryColor,
                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
