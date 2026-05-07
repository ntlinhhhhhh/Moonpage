package com.diary.moonpage.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.presentation.components.auth.AuthFooter
import com.diary.moonpage.presentation.components.auth.AuthHeader
import com.diary.moonpage.presentation.components.core.buttons.MoonPrimaryButton
import com.diary.moonpage.presentation.components.core.inputs.MoonTextField
import com.diary.moonpage.presentation.components.core.navigation.TopCircularIcon
import com.diary.moonpage.presentation.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    email: String,
    resetToken: String,
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AuthUiEvent.NavigateToLogin -> onNavigateToLogin()
                else -> Unit
            }
        }
    }

    ResetPasswordScreenContent(
        uiState = uiState,
        uiEvent = viewModel.uiEvent,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onResetClick = viewModel::resetPassword,
        onNavigateBack = onNavigateBack,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun ResetPasswordScreenContent(
    uiState: AuthUiState,
    uiEvent: Flow<AuthUiEvent>,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val screenBgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardBgColor = MaterialTheme.colorScheme.surface
    val iconColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            if (event is AuthUiEvent.ShowSnackBar) {
                launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = screenBgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = iconColor
                    )
                }
            }

            TopCircularIcon()

            Spacer(modifier = Modifier.height(24.dp))

            AuthHeader(
                title = "New Password",
                subtitle = "Set a strong password to protect\nyour personal sanctuary."
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    ),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MoonTextField(
                        value = uiState.passwordInput,
                        onValueChange = onPasswordChange,
                        label = "New Password",
                        placeholderText = "At least 8 characters",
                        isPassword = true,
                        errorText = uiState.passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MoonTextField(
                        value = uiState.confirmPasswordInput,
                        onValueChange = onConfirmPasswordChange,
                        label = "Confirm Password",
                        placeholderText = "Repeat new password",
                        isPassword = true,
                        errorText = uiState.confirmPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onResetClick()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MoonPrimaryButton(
                        text = "Reset Password",
                        enabled = !uiState.isLoading,
                        onClick = {
                            keyboardController?.hide()
                            onResetClick()
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AuthFooter(
                questionText = "Go back to ",
                actionText = "Sign In",
                onActionClick = onNavigateToLogin
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
