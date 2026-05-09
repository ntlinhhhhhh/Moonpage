package com.diary.moonpage.presentation.screens.auth

import android.util.Log
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
import com.diary.moonpage.presentation.components.auth.AuthFooter
import com.diary.moonpage.presentation.components.auth.AuthHeader
import com.diary.moonpage.presentation.components.auth.SocialLoginButton
import com.diary.moonpage.presentation.components.core.buttons.MoonPrimaryButton
import com.diary.moonpage.presentation.components.core.feedback.MoonSnackbarHost
import com.diary.moonpage.presentation.components.core.inputs.MoonTextField
import com.diary.moonpage.presentation.components.core.layout.MoonDivider
import com.diary.moonpage.presentation.theme.MoonPageTheme
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

    val googleWebClientId = stringResource(R.string.google_web_client_id)

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
        snackbarHost = { MoonSnackbarHost(hostState = snackBarHostState) },
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
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
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
                            title = "Welcome Back",
                            subtitle = "Continue your journey of self-\nreflection and mindful awareness."
                        )

                        MoonTextField(
                            value = uiState.emailInput,
                            onValueChange = onEmailChange,
                            label = "Email address",
                            placeholderText = "Enter your email",
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
                            label = "Password",
                            isPassword = true,
                            trailingLabel = "Forgot Password?",
                            placeholderText = "Enter your password",
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
                            text = "Login",
                            enabled = !uiState.isLoading,
                            onClick = {
                                keyboardController?.hide()
                                onLoginClick()
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        MoonDivider(text = "OR CONTINUE WITH")

                        Spacer(modifier = Modifier.height(32.dp))

                        SocialLoginButton(
                            text = "Login with Google",
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
                                        }
                                    } catch (e: GetCredentialCancellationException) {
                                        Log.d("Auth", "User cancelled")
                                    } catch (e: NoCredentialException) {
                                        snackBarHostState.showSnackbar("Please sign in to a Google account.")
                                    } catch (e: Exception) {
                                        Log.e("Auth", "Error: ${e.message}")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        AuthFooter(
                            questionText = "Don't have an account? ",
                            actionText = "Sign up for free",
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