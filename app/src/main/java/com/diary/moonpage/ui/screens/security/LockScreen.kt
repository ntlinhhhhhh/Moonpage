package com.diary.moonpage.ui.screens.security

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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Stateful Component
 */
@Composable
fun LockRoute(
    onUnlockSuccess: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val latestOnUnlockSuccess by rememberUpdatedState(onUnlockSuccess)
    var passcode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBiometricPromptShowing by remember { mutableStateOf(false) }
    var hasUnlocked by remember { mutableStateOf(false) }

    val showBiometricPrompt = showBiometricPrompt@{
        val activity = context as? FragmentActivity
        if (
            activity == null ||
            hasUnlocked ||
            isBiometricPromptShowing ||
            !uiState.isBiometricEnabled ||
            !uiState.isBiometricAvailable
        ) {
            return@showBiometricPrompt
        }

        isBiometricPromptShowing = true
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isBiometricPromptShowing = false
                    hasUnlocked = true
                    scope.launch {
                        delay(250)
                        latestOnUnlockSuccess()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isBiometricPromptShowing = false
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

    val onNumberClick: (String) -> Unit = { digit ->
        if (passcode.length < 4) {
            passcode += digit
            if (passcode.length == 4) {
                if (viewModel.verifyPasscode(passcode)) {
                    hasUnlocked = true
                    scope.launch {
                        delay(250)
                        latestOnUnlockSuccess()
                    }
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
    val latestShowBiometricPrompt by rememberUpdatedState(showBiometricPrompt)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                latestShowBiometricPrompt()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.isBiometricEnabled, uiState.isBiometricAvailable) {
        if (uiState.isBiometricEnabled && uiState.isBiometricAvailable) {
            delay(150)
            showBiometricPrompt()
        }
    }

    LockScreen(
        passcodeLength = passcode.length,
        errorMessage = errorMessage,
        isBiometricEnabled = uiState.isBiometricEnabled,
        isBiometricAvailable = uiState.isBiometricAvailable,
        onNumberClick = onNumberClick,
        onDeleteClick = onDeleteClick,
        onBiometricClick = showBiometricPrompt
    )
}

/**
 * Stateless Component
 */
@Composable
fun LockScreen(
    passcodeLength: Int,
    errorMessage: String?,
    isBiometricEnabled: Boolean,
    isBiometricAvailable: Boolean,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit
) {
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

            PasscodeDots(passcodeLength)

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
                extraButton = if (isBiometricEnabled && isBiometricAvailable) {
                    {
                        KeypadButton(
                            icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Biometric") },
                            onClick = onBiometricClick
                        )
                    }
                } else null
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
