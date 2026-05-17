package com.diary.moonpage.ui.screens.security

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diary.moonpage.R
import kotlinx.coroutines.launch

/**
 * Stateful Component
 */
@Composable
fun CreatePasscodeRoute(
    onNavigateBack: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val onNumberClick: (String) -> Unit = { digit ->
        if (isConfirming) {
            if (confirmPasscode.length < 4) {
                confirmPasscode += digit
                if (confirmPasscode.length == 4) {
                    if (confirmPasscode == passcode) {
                        errorMessage = null
                        coroutineScope.launch {
                            viewModel.setPasscode(passcode)
                            onNavigateBack()
                        }
                    } else {
                        errorMessage = "Passcodes do not match"
                        confirmPasscode = ""
                    }
                }
            }
        } else {
            if (passcode.length < 4) {
                passcode += digit
                if (passcode.length == 4) {
                    isConfirming = true
                }
            }
        }
    }

    val onDeleteClick: () -> Unit = {
        if (isConfirming) {
            if (confirmPasscode.isNotEmpty()) {
                confirmPasscode = confirmPasscode.dropLast(1)
            } else {
                isConfirming = false
                passcode = passcode.dropLast(1)
            }
        } else {
            if (passcode.isNotEmpty()) {
                passcode = passcode.dropLast(1)
            }
        }
        errorMessage = null
    }

    CreatePasscodeScreen(
        isConfirming = isConfirming,
        currentPasscodeLength = if (isConfirming) confirmPasscode.length else passcode.length,
        errorMessage = errorMessage,
        onNavigateBack = onNavigateBack,
        onNumberClick = onNumberClick,
        onDeleteClick = onDeleteClick
    )
}

/**
 * Stateless Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePasscodeScreen(
    isConfirming: Boolean,
    currentPasscodeLength: Int,
    errorMessage: String?,
    onNavigateBack: () -> Unit,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = if (isConfirming) stringResource(R.string.confirm_passcode) else stringResource(R.string.create_passcode),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            PasscodeDots(currentPasscodeLength)

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = stringResource(R.string.passcodes_do_not_match),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            NumericKeypad(onNumberClick, onDeleteClick)

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
