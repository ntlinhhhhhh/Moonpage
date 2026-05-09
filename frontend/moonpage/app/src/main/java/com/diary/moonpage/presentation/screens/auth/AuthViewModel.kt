package com.diary.moonpage.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.R
import com.diary.moonpage.core.util.TokenManager
import com.diary.moonpage.core.util.UiText
import com.diary.moonpage.core.util.OnboardingPrefsManager
import com.diary.moonpage.data.remote.dto.auth.LoginRequestDTO
import com.diary.moonpage.data.remote.dto.auth.RegisterRequestDTO
import com.diary.moonpage.domain.model.User
import com.diary.moonpage.domain.usecase.auth.*
import com.diary.moonpage.domain.usecase.validation.ValidateEmail
import com.diary.moonpage.domain.usecase.validation.ValidatePassword
import com.diary.moonpage.domain.usecase.validation.ValidateUsername
import dagger.hilt.android.lifecycle.HiltViewModel
import com.diary.moonpage.domain.repository.UserRepository
import com.diary.moonpage.domain.repository.ActivityRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor (
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUserCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword,
    private val validateUsername: ValidateUsername,
    private val tokenManager: TokenManager,
    private val onboardingPrefsManager: OnboardingPrefsManager,
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    private val _uiEvent = Channel<AuthUiEvent>()

    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    val uiEvent = _uiEvent.receiveAsFlow()

    val tokenFlow = tokenManager.getToken()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailInput = email)}
    }

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(usernameInput = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPasswordInput = confirmPassword)}
    }

    fun onOtpCodeChange(otpCode: String) {
        _uiState.update { it.copy(otpCodeInput = otpCode, otpCodeError = null) }
        if (otpCode.length == 6) {
            verifyOtp()
        }
    }

    /** Dùng bởi LoadingScreen: kiểm tra user hiện tại đã hoàn thành onboarding chưa */
    suspend fun checkOnboardingForCurrentUser(): Boolean {
        val userId = tokenManager.getUserId() ?: return false
        return onboardingPrefsManager.checkOnboardingCompleted(userId)
    }

    fun loadInitialAppResources(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Start with a small progress so it's not empty at first
            val initialProgress = 0.15f
            _uiState.update { it.copy(loadingProgress = initialProgress) }
            
            val jobs = listOf(
                async { userRepository.getCurrentUser() },
                async { userRepository.getMyThemes() },
                async { activityRepository.syncActivities() }
            )

            val remainingProgress = 1f - initialProgress
            val step = remainingProgress / jobs.size
            jobs.forEach { job ->
                job.await()
                _uiState.update { it.copy(loadingProgress = (it.loadingProgress + step).coerceAtMost(1f)) }
            }

            _uiState.update { it.copy(loadingProgress = 1f) }
            onComplete()
        }
    }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(emailError = null, passwordError = null) }
            
            val emailResult = validateEmail.execute(uiState.value.emailInput)
            val passwordResult = validatePassword.execute(uiState.value.passwordInput)

            if (!emailResult.successful || !passwordResult.successful) {
                _uiState.update { it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage
                ) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                val loginRequest = LoginRequestDTO(uiState.value.emailInput, uiState.value.passwordInput)
                val result: Result<User> = loginUseCase(loginRequest)

                result.onSuccess { user ->
                    tokenManager.saveToken(user.token)
                    tokenManager.saveUserId(user.userId)
                    tokenManager.saveUserName(user.name)
                    val isOnboarded = onboardingPrefsManager.checkOnboardingCompleted(user.userId)
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.send(AuthUiEvent.LoginSuccess(user.token, user.userId, isNewUser = !isOnboarded))
                }.onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false) }
                    handleAuthError(exception.message)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(AuthUiEvent.ShowSnackBar(UiText.StringResource(R.string.error_connection)))
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            val state = uiState.value
            _uiState.update {
                it.copy(
                    emailError = null,
                    usernameError = null,
                    passwordError = null,
                    confirmPasswordError = null
                )
            }

            val emailResult = validateEmail.execute(state.emailInput)
            val passwordResult = validatePassword.execute(state.passwordInput)
            val usernameResult = validateUsername.execute(state.usernameInput)
            
            val passwordsMatch = state.passwordInput == state.confirmPasswordInput
            val confirmPasswordError = if (!passwordsMatch) {
                UiText.StringResource(R.string.error_passwords_not_match)
            } else null

            val hasError = listOf(emailResult, passwordResult, usernameResult).any { !it.successful } || !passwordsMatch

            if (hasError) {
                _uiState.update { it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage,
                    usernameError = usernameResult.errorMessage,
                    confirmPasswordError = confirmPasswordError
                ) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = RegisterRequestDTO(
                    email = state.emailInput,
                    name = state.usernameInput,
                    password = state.passwordInput
                )
                val result: Result<User> = registerUseCase(request)

                result.onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.send(AuthUiEvent.RegisterSuccess("Registration successful. Please log in."))
                }.onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false) }
                    handleAuthError(exception.message)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(AuthUiEvent.ShowSnackBar(UiText.StringResource(R.string.error_connection)))
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = googleLoginUseCase(idToken)

                result.onSuccess { user ->
                    tokenManager.saveToken(user.token)
                    tokenManager.saveUserId(user.userId)
                    tokenManager.saveUserName(user.name)
                    val isOnboarded = onboardingPrefsManager.checkOnboardingCompleted(user.userId)
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.send(AuthUiEvent.LoginSuccess(user.token, user.userId, isNewUser = !isOnboarded))
                }.onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.send(AuthUiEvent.ShowSnackBar(UiText.DynamicString(exception.message ?: "Google login failed.")))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(AuthUiEvent.ShowSnackBar(UiText.StringResource(R.string.error_connection)))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _uiState.update { AuthUiState() }
            _uiEvent.send(AuthUiEvent.NavigateToLogin)
        }
    }

    fun forgotPassword() {
        val emailInput = uiState.value.emailInput.trim()
        val emailResult = validateEmail.execute(emailInput)
        
        if (!emailResult.successful) {
            _uiState.update { it.copy(emailError = emailResult.errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = forgotPasswordUseCase(emailInput)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, savedEmailForOtp = emailInput) }
                _uiEvent.send(AuthUiEvent.NavigateToVerifyOtp(emailInput))
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false) }
                handleAuthError(exception.message)
            }
        }
    }

    fun verifyOtp() {
        val email = uiState.value.savedEmailForOtp
        val otpCode = uiState.value.otpCodeInput.trim()

        if (otpCode.isBlank()) {
            _uiState.update { it.copy(otpCodeError = UiText.DynamicString("OTP required")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = verifyOtpUseCase(email, otpCode)

            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resetToken = response.resetToken
                    )
                }
                _uiEvent.send(AuthUiEvent.NavigateToResetPassword(email, response.resetToken))
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false) }
                handleAuthError(exception.message)
            }
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            val state = uiState.value
            val email = state.savedEmailForOtp
            val resetToken = state.resetToken
            val newPassword = state.passwordInput

            val passwordResult = validatePassword.execute(newPassword)
            val passwordsMatch = newPassword == state.confirmPasswordInput

            if (!passwordResult.successful || !passwordsMatch) {
                _uiState.update { it.copy(
                    passwordError = passwordResult.errorMessage,
                    confirmPasswordError = if (!passwordsMatch) UiText.StringResource(R.string.error_passwords_not_match) else null
                ) }
                return@launch
            }

            if (resetToken.isNullOrBlank()) {
                _uiEvent.send(AuthUiEvent.ShowSnackBar(UiText.DynamicString("Invalid or expired session. Please try again.")))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            val result = resetPasswordUseCase(email, resetToken, newPassword)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        passwordInput = "",
                        confirmPasswordInput = "",
                        otpCodeInput = "",
                        resetToken = "",
                        emailInput = it.savedEmailForOtp
                    )
                }
                _uiEvent.send(AuthUiEvent.ResetPasswordSuccess("Password reset successful! Please login."))
                _uiEvent.send(AuthUiEvent.NavigateToLogin)
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false) }
                handleAuthError(exception.message)
            }
        }
    }

    private fun handleAuthError(message: String?) {
        val error = message ?: "An unknown error occurred"

        _uiState.update { it.copy(emailError = null, passwordError = null, usernameError = null, confirmPasswordError = null) }
        when {
            error.contains("Email", ignoreCase = true) || error.contains("User", ignoreCase = true) -> {
                _uiState.update { it.copy(emailError = UiText.DynamicString(error)) }
            }
            error.contains("Password", ignoreCase = true) -> {
                _uiState.update { it.copy(passwordError = UiText.DynamicString(error)) }
            }
            error.contains("Username", ignoreCase = true) || error.contains("Name", ignoreCase = true) -> {
                _uiState.update { it.copy(usernameError = UiText.DynamicString(error)) }
            }
            else -> {
                viewModelScope.launch {
                    _uiEvent.send(AuthUiEvent.ShowSnackBar(UiText.DynamicString(error)))
                }
            }
        }
    }
}
