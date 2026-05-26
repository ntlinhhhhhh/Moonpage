package com.diary.moonpage.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
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
import com.diary.moonpage.ui.screens.auth.components.AuthFooter
import com.diary.moonpage.ui.screens.auth.components.AuthHeader
import com.diary.moonpage.ui.components.buttons.MoonPrimaryButton
import com.diary.moonpage.ui.components.feedback.GlobalSnackbarManager
import com.diary.moonpage.ui.components.inputs.MoonTextField
import com.diary.moonpage.ui.components.navigation.TopCircularIcon
import com.diary.moonpage.core.theme.*
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Stateful Component (Smart Component)
 * Handles ViewModel, state observation, and event collection.
 */
@Composable
fun ForgotPasswordRoute(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToVerifyOtp: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordScreen(
        uiState = uiState,
        uiEvent = viewModel.uiEvent,
        onEmailChange = viewModel::onEmailChange,
        onSendOtpClick = {
            viewModel.forgotPassword()
        },
        onNavigateBack = onNavigateBack,
        onNavigateToVerifyOtp = onNavigateToVerifyOtp
    )
}

/**
 * Stateless Component (Dumb Component)
 * Purely presentational. Doesn't know about ViewModel or API logic.
 */
@Composable
fun ForgotPasswordScreen(
    uiState: AuthUiState,
    uiEvent: Flow<AuthUiEvent>,
    onEmailChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToVerifyOtp: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val screenBgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardBgColor = MaterialTheme.colorScheme.surface
    val iconColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.NavigateToVerifyOtp -> {
                    onNavigateToVerifyOtp(event.email)
                }
                is AuthUiEvent.ShowSnackBar -> {
                    launch {
                        GlobalSnackbarManager.show(
                            message = event.message.asString(context),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = screenBgColor
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
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

            // Custom Top Bar with Back Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.graphicsLayer { translationX = -12.dp.toPx() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.back),
                        tint = iconColor
                    )
                }
            }

            TopCircularIcon()

            Spacer(modifier = Modifier.height(24.dp))

            AuthHeader(
                title = stringResource(R.string.forgot_password_title),
                subtitle = stringResource(R.string.forgot_password_subtitle)
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
                        value = uiState.emailInput,
                        onValueChange = onEmailChange,
                        label = stringResource(R.string.email_address),
                        placeholderText = stringResource(R.string.placeholder_email_example),
                        iconVector = Icons.Outlined.Email,
                        errorText = uiState.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onSendOtpClick()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MoonPrimaryButton(
                        text = stringResource(R.string.send_otp),
                        enabled = !uiState.isLoading,
                        onClick = {
                            keyboardController?.hide()
                            onSendOtpClick()
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AuthFooter(
                questionText = stringResource(R.string.suddenly_remembered),
                actionText = stringResource(R.string.sign_in),
                onActionClick = onNavigateBack
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Footer Security Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(modifier = Modifier.width(40.dp), color = textColor.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = stringResource(R.string.content_desc_secure),
                    modifier = Modifier.size(14.dp),
                    tint = textColor.copy(alpha = 0.3f)
                )
                Text(
                    text = " ${stringResource(R.string.secure_sanctuary)} ",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = textColor.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                HorizontalDivider(modifier = Modifier.width(40.dp), color = textColor.copy(alpha = 0.1f))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Loading Overlay
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
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    MoonPageTheme {
        ForgotPasswordScreen(
            uiState = AuthUiState(),
            uiEvent = MutableSharedFlow<AuthUiEvent>().asSharedFlow(),
            onEmailChange = {},
            onSendOtpClick = {},
            onNavigateBack = {},
            onNavigateToVerifyOtp = {}
        )
    }
}
