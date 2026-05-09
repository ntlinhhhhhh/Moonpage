package com.diary.moonpage.presentation.components.core.inputs

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diary.moonpage.presentation.theme.MoonPageTheme

@Composable
fun MoonOtpField(
    label: String,
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    otpCount: Int = 6,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                color = textColor.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        OtpInputField(
            otpText = otpText,
            onOtpTextChange = onOtpTextChange,
            otpCount = otpCount,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

@Composable
fun OtpInputField(
    otpText: String, 
    onOtpTextChange: (String) -> Unit, 
    otpCount: Int = 6,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    BasicTextField(
        value = otpText,
        onValueChange = { if (it.length <= otpCount) onOtpTextChange(it) },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                repeat(otpCount) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> "•"
                    }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(46.dp)
                            .background(bgColor, RoundedCornerShape(18.dp))
                            .border(0.5.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge.copy(color = textColor, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    )
}

@Preview(name = "Light Mode")
@Composable
fun MoonOtpFieldLightPreview() {
    MoonPageTheme {
        Surface {
            MoonOtpField(
                label = "Verification Code",
                otpText = "123",
                onOtpTextChange = {},
            )
        }
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MoonOtpFieldDarkPreview() {
    MoonPageTheme {
        Surface {
            MoonOtpField(
                label = "Verification Code",
                otpText = "123",
                onOtpTextChange = {},
            )
        }
    }
}
