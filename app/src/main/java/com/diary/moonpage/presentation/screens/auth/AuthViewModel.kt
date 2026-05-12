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
    private val activityRepository: ActivityRepository,
    private val statisticsRepository: com.diary.moonpage.domain.repository.StatisticsRepository,
    private val themeRepository: com.diary.moonpage.domain.repository.ThemeRepository,
    private val dailyLogRepository: com.diary.moonpage.domain.repository.DailyLogRepository,
    private val momentRepository: com.diary.moonpage.domain.repository.MomentRepository,
    private val authApi: com.diary.moonpage.data.remote.api.AuthApi,
    private val themePreferencesManager: com.diary.moonpage.core.util.ThemePreferencesManager
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

    private var isResourcesLoading = false

    fun loadInitialAppResources(onComplete: () -> Unit) {
        // If already loading, wait for it to finish and then call onComplete
        if (isResourcesLoading) {
            viewModelScope.launch {
                _uiState.map { it.loadingProgress }.filter { it >= 1f }.first()
                onComplete()
            }
            return
        }
        
        // If already loaded successfully in a previous call
        if (_uiState.value.loadingProgress >= 1f) {
            onComplete()
            return
        }

        isResourcesLoading = true
        viewModelScope.launch {
            try {
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
                    try {
                        val result = job.await()
                        // If result is from getMyThemes, check for active theme
                        if (result is Result<*> && result.isSuccess) {
                            val data = result.getOrNull()
                            if (data is List<*> && data.isNotEmpty() && data.first() is com.diary.moonpage.domain.model.Theme) {
                                val themes = data as List<com.diary.moonpage.domain.model.Theme>
                                val activeTheme = themes.find { it.isActive }
                                if (activeTheme != null) {
                                    try {
                                        val cleanId = activeTheme.id.replace("theme_", "").uppercase()
                                        val themeType = com.diary.moonpage.core.theme.MoonThemeType.valueOf(cleanId)
                                        themePreferencesManager.setThemeType(themeType)
                                        
                                        // Also set dark mode if theme category suggests it
                                        if (activeTheme.category == "DARK") {
                                            themePreferencesManager.setDarkMode(true)
                                        } else if (activeTheme.category == "LIGHT") {
                                            themePreferencesManager.setDarkMode(false)
                                        }
                                    } catch (e: Exception) {
                                        // Ignore if theme name doesn't match enum
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    _uiState.update { it.copy(loadingProgress = (it.loadingProgress + step).coerceAtMost(1f)) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(loadingProgress = 1f) }
                isResourcesLoading = false
                onComplete()
            }
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
                    // After register: pre-fill both email + password for Login screen
                    _uiState.update {
                        AuthUiState(
                            emailInput = state.emailInput,
                            passwordInput = state.passwordInput,
                            prefillPassword = state.passwordInput
                        )
                    }
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
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Call backend logout (best effort)
            try {
                authApi.logout()
            } catch (e: Exception) {
                // Ignore backend logout failures as we're clearing locally anyway
            }

            // 2. Clear all local session data and caches
            tokenManager.clearAll()
            userRepository.clearCache()
            statisticsRepository.clearCache()
            themeRepository.clearCache()
            dailyLogRepository.clearCache()
            momentRepository.clearCache()
            activityRepository.clearCache()
            themePreferencesManager.clearAll()

            // 3. Reset UI state and navigate
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
                // Save email for OTP flow, clear password fields to avoid confusion
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        savedEmailForOtp = emailInput,
                        passwordInput = "",
                        confirmPasswordInput = "",
                        passwordError = null
                    )
                }
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

    fun resetPassword(email: String, resetToken: String) {
        viewModelScope.launch {
            val state = uiState.value
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
                // Keep only email for Login pre-fill, clear everything else
                _uiState.update {
                    AuthUiState(
                        emailInput = email,
                        prefillPassword = "" // Do NOT pre-fill password after reset
                    )
                }
                // Navigate immediately without waiting for snackbar
                _uiEvent.send(AuthUiEvent.NavigateToLogin)
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false) }
                handleAuthError(exception.message)
            }
        }
    }

    /** Called when navigating TO ForgotPassword from Login, to pre-fill email if already typed */
    fun prepareForgotPassword() {
        val currentEmail = uiState.value.emailInput
        // Clear password fields but preserve the email the user may have typed in Login
        _uiState.update {
            it.copy(
                passwordInput = "",
                confirmPasswordInput = "",
                passwordError = null,
                confirmPasswordError = null,
                prefillPassword = ""
            )
        }
    }

    /** Called when navigating away from screens to clear form fields */
    fun clearAuthFields() {
        _uiState.update {
            AuthUiState() // Full reset
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
