package com.diary.moonpage.presentation.screens.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.diary.moonpage.presentation.navigation.Screen

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    this.navigate("auth_graph", navOptions)
}

fun NavGraphBuilder.authGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    onLoginSuccess: (String, Boolean) -> Unit,
    onRegisterSuccess: () -> Unit
) {
    navigation(
        startDestination = Screen.Login.route,
        route = "auth_graph"
    ) {
        composable(Screen.Login.route) {
            LoginRoute(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = onLoginSuccess
            )
        }

        composable(Screen.Register.route) {
            RegisterRoute(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = onRegisterSuccess,
                onLoginSuccess = onLoginSuccess
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerifyOtp = { email ->
                    navController.navigate(Screen.VerifyOtp.route)
                }
            )
        }

        composable(Screen.VerifyOtp.route) {
            VerifyOtpCodeScreen(
                email = authViewModel.uiState.value.savedEmailForOtp,
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResetPassword = { _, _ ->
                    navController.navigate(Screen.ResetPassword.route)
                }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                email = authViewModel.uiState.value.savedEmailForOtp,
                resetToken = authViewModel.uiState.value.resetToken,
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
