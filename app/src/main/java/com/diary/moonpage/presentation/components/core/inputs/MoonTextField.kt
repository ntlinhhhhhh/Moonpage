package com.diary.moonpage.presentation.components.core.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.diary.moonpage.core.util.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MoonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    trailingLabel: String? = null,
    placeholderText: String = "",
    iconVector: ImageVector? = null,
    iconRes: Int? = null,
    errorText: UiText? = null,
    onTrailingClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val inputBgColor = MaterialTheme.colorScheme.surfaceVariant
    val linkColor = MaterialTheme.colorScheme.tertiary

    var passwordVisible by remember { mutableStateOf(false) }
    val hasTrailingIcon = isPassword || iconVector != null || iconRes != null

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    var columnHeight by remember { mutableFloatStateOf(0f) }
    var columnWidth by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                columnHeight = coords.size.height.toFloat()
                columnWidth = coords.size.width.toFloat()
            }
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = textColor.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold
                )
            )
            if (trailingLabel != null && onTrailingClick != null) {
                Text(
                    text = trailingLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = linkColor.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTrailingClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            isError = errorText != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            delay(150)
                            if (columnHeight > 0) {
                                val extraSpace = with(density) { 85.dp.toPx() }
                                bringIntoViewRequester.bringIntoView(
                                    Rect(0f, 0f, columnWidth, columnHeight + extraSpace)
                                )
                            }
                        }
                    }
                },
            placeholder = {
                if (placeholderText.isNotEmpty()) {
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor.copy(alpha = 0.4f)
                    )
                }
            },
            trailingIcon = if (hasTrailingIcon) {
                {
                    if (isPassword) {
                        val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = textColor.copy(alpha = 0.5f))
                        }
                    }
                    else if (iconVector != null) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    else if (iconRes != null) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = inputBgColor,
                unfocusedContainerColor = inputBgColor,
                errorContainerColor = inputBgColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = textColor,
                errorCursorColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(25.dp),
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor.copy(alpha = 0.75f)),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )

        if (errorText != null) {
            Text(
                text = errorText.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
