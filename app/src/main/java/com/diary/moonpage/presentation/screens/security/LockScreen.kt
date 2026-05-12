package com.diary.moonpage.presentation.screens.security

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.R

@Composable
fun LockScreen(
    onUnlockSuccess: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var passcode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    val onNumberClick: (String) -> Unit = { digit ->
        if (passcode.length < 4) {
            passcode += digit
            if (passcode.length == 4) {
                if (viewModel.verifyPasscode(passcode)) {
                    onUnlockSuccess()
                } else {
                    errorMessage = context.getString(R.string.wrong_passcode)
                    passcode = ""
                }
            }
        }
    }

    val onDeleteClick: () -> Unit = {
        if (passcode.isNotEmpty()) {
            passcode = passcode.dropLast(1)
        }
        errorMessage = null
    }

    val showBiometricPrompt = {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            context as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onUnlockSuccess()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.unlock_app))
            .setSubtitle(context.getString(R.string.use_biometric))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled && viewModel.isBiometricAvailable()) {
            showBiometricPrompt()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = stringResource(R.string.enter_passcode),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            PasscodeDots(passcode.length)

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            NumericKeypad(
                onNumberClick = onNumberClick,
                onDeleteClick = onDeleteClick,
                extraButton = if (isBiometricEnabled && viewModel.isBiometricAvailable()) {
                    {
                        KeypadButton(
                            icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Biometric") },
                            onClick = showBiometricPrompt
                        )
                    }
                } else null
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
