package com.diary.moonpage.ui.screens.auth

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diary.moonpage.core.theme.MoonPageTheme
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreensInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testForgotPasswordScreen_rendersAndTriggersSendOtpClick() {
        var emailInput = ""
        var sendOtpClicked = false
        val testUiState = AuthUiState(
            emailInput = "test@user.com",
            isLoading = false
        )

        composeTestRule.setContent {
            MoonPageTheme {
                ForgotPasswordScreen(
                    uiState = testUiState,
                    uiEvent = emptyFlow(),
                    onEmailChange = { emailInput = it },
                    onSendOtpClick = { sendOtpClicked = true },
                    onNavigateBack = {},
                    onNavigateToVerifyOtp = {}
                )
            }
        }

        // Check if forgot password texts and components exist
        // Note: Check strings.xml for accurate Vietnamese resources:
        // "Quên mật khẩu", "Gửi OTP"
        composeTestRule.onNodeWithText("Quên mật khẩu").assertExists()
        composeTestRule.onNodeWithText("Gửi OTP").assertExists()

        // Verify the pre-filled email exists
        composeTestRule.onNodeWithText("test@user.com").assertExists()

        // Perform click on Send OTP button
        composeTestRule.onNodeWithText("Gửi OTP").performClick()

        // Verify send OTP click callback is triggered
        assertTrue(sendOtpClicked)
    }

    @Test
    fun testForgotPasswordScreen_emailInputUpdate() {
        var emailInput = ""
        val testUiState = AuthUiState(
            emailInput = "",
            isLoading = false
        )

        composeTestRule.setContent {
            MoonPageTheme {
                ForgotPasswordScreen(
                    uiState = testUiState,
                    uiEvent = emptyFlow(),
                    onEmailChange = { emailInput = it },
                    onSendOtpClick = {},
                    onNavigateBack = {},
                    onNavigateToVerifyOtp = {}
                )
            }
        }

        // Perform text input on the email text field
        // Since MoonTextField renders the label text outside of the TextField, 
        // we target the input field using hasSetTextAction().
        composeTestRule.onNode(hasSetTextAction()).performTextInput("newemail@domain.com")
        assertEquals("newemail@domain.com", emailInput)
    }
}

