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
import androidx.compose.material.icons.rounded.ArrowBackIosNew
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.moonpage.R
import com.diary.moonpage.ui.screens.auth.components.AuthFooter
import com.diary.moonpage.ui.screens.auth.components.AuthHeader
import com.diary.moonpage.ui.screens.auth.components.SocialLoginButton
import com.diary.moonpage.ui.components.buttons.MoonPrimaryButton
import com.diary.moonpage.ui.components.feedback.MoonSnackbarHost
import com.diary.moonpage.ui.components.inputs.MoonTextField
import com.diary.moonpage.ui.components.layout.MoonDivider
import com.diary.moonpage.core.theme.MoonPageTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (String, Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.LoginSuccess -> onLoginSuccess(event.token, event.isNewUser)
                else -> Unit
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        uiEvent = viewModel.uiEvent,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onGoogleLoginClick = viewModel::loginWithGoogle,
        onNavigateBack = onNavigateBack,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword
    )
}

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    uiEvent: Flow<AuthUiEvent>,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleLoginClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val googleWebClientId = stringResource(R.string.default_web_client_id)

    val screenBgColor = MaterialTheme.colorScheme.background
    val backIconColor = MaterialTheme.colorScheme.onSurface
    val cardBgColor = MaterialTheme.colorScheme.surface

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
        containerColor = screenBgColor,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .pointerInput(Unit) {
                        detectTapGestures { focusManager.clearFocus() }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = backIconColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        ),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AuthHeader(
                            title = stringResource(R.string.sign_in),
                            subtitle = stringResource(R.string.login_subtitle)
                        )

                        MoonTextField(
                            value = uiState.emailInput,
                            onValueChange = onEmailChange,
                            label = stringResource(R.string.email_address),
                            placeholderText = stringResource(R.string.placeholder_email),
                            iconVector = Icons.Outlined.Email,
                            errorText = uiState.emailError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        MoonTextField(
                            value = uiState.passwordInput,
                            onValueChange = onPasswordChange,
                            label = stringResource(R.string.password_label),
                            isPassword = true,
                            trailingLabel = stringResource(R.string.forgot_password_label),
                            placeholderText = stringResource(R.string.placeholder_password),
                            errorText = uiState.passwordError,
                            onTrailingClick = { onNavigateToForgotPassword() },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    onLoginClick()
                                }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MoonPrimaryButton(
                            text = stringResource(R.string.login),
                            enabled = !uiState.isLoading,
                            onClick = {
                                keyboardController?.hide()
                                onLoginClick()
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        MoonDivider(text = stringResource(R.string.or_continue_with))

                        Spacer(modifier = Modifier.height(32.dp))

                        SocialLoginButton(
                            text = stringResource(R.string.login_with_google),
                            iconResId = R.drawable.ic_google,
                            onClick = {
                                scope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(context)

                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId(googleWebClientId)
                                            .setAutoSelectEnabled(false)
                                            .build()

                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        val result = credentialManager.getCredential(context, request)

                                        val credential = result.credential
                                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                                            onGoogleLoginClick(googleIdToken.idToken)
                                        } else {
                                            snackBarHostState.showSnackbar(context.getString(R.string.google_login_failed))
                                        }
                                    } catch (e: GetCredentialCancellationException) {
                                        // User cancelled the Google account picker.
                                    } catch (e: NoCredentialException) {
                                        snackBarHostState.showSnackbar(context.getString(R.string.google_sign_in_required))
                                    } catch (e: Exception) {
                                        val reason = e.localizedMessage ?: e::class.java.simpleName
                                        snackBarHostState.showSnackbar(
                                            context.getString(R.string.google_sign_in_failed_with_reason, reason)
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        AuthFooter(
                            questionText = stringResource(R.string.dont_have_account),
                            actionText = stringResource(R.string.sign_up_free),
                            onActionClick = onNavigateToRegister
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            MoonSnackbarHost(hostState = snackBarHostState, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MoonPageTheme {
        LoginScreen(
            uiState = AuthUiState(),
            uiEvent = MutableSharedFlow<AuthUiEvent>().asSharedFlow(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onGoogleLoginClick = {},
            onNavigateBack = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {}
        )
    }
}
