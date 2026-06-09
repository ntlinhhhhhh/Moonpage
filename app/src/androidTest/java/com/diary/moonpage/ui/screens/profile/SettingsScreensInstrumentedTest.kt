package com.diary.moonpage.ui.screens.profile

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diary.moonpage.core.theme.MoonPageTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreensInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun hasRole(role: Role): SemanticsMatcher {
        return SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }

    @Test
    fun testSettingsScreen_rendersMenuOptions() {
        val testUiState = SettingsUiState(
            language = "vi",
            isDarkMode = false,
            isPasscodeEnabled = true,
            isBiometricEnabled = false,
            isReminderEnabled = true,
            reminderTime = "22:00",
            authProvider = "Password"
        )

        composeTestRule.setContent {
            MoonPageTheme {
                SettingsScreen(
                    uiState = testUiState,
                    onNavigateBack = {},
                    onLanguageClick = {},
                    onThemeToggle = {},
                    onPasscodeToggle = {},
                    onBiometricToggle = {},
                    onReminderToggle = {},
                    onReminderTimeClick = { _, _ -> },
                    onBatteryOptimizationClick = {},
                    onExactAlarmClick = {},
                    onChangePasswordClick = {},
                    onDeleteAccountClick = {},
                    onCustomizeBlocksClick = {},
                    onClearCacheClick = {}
                )
            }
        }

        // Check if settings sections and titles render correctly
        // Look up strings.xml for Vietnamese values:
        // preferences -> "Tùy chỉnh"
        // language -> "Ngôn ngữ"
        // daily_reminder -> "Nhắc nhở ghi nhật ký"
        // passcode_lock -> "Khóa mã PIN"
        // biometric_auth -> "Xác thực vân tay"
        composeTestRule.onNodeWithText("Tùy chỉnh").assertExists()
        composeTestRule.onNodeWithText("Ngôn ngữ").assertExists()
        composeTestRule.onNodeWithText("Nhắc nhở ghi nhật ký").assertExists()
        composeTestRule.onNodeWithText("Khóa mã PIN").assertExists()
        composeTestRule.onNodeWithText("Xác thực vân tay").assertExists()
    }

    @Test
    fun testSettingsScreen_clickCallbacksTriggered() {
        var languageClicked = false
        var passcodeToggled = false
        val testUiState = SettingsUiState(
            language = "en",
            isDarkMode = false,
            isPasscodeEnabled = false,
            isBiometricEnabled = false,
            isReminderEnabled = false,
            reminderTime = "21:00",
            authProvider = "Password"
        )

        composeTestRule.setContent {
            MoonPageTheme {
                SettingsScreen(
                    uiState = testUiState,
                    onNavigateBack = {},
                    onLanguageClick = { languageClicked = true },
                    onThemeToggle = {},
                    onPasscodeToggle = { passcodeToggled = true },
                    onBiometricToggle = {},
                    onReminderToggle = {},
                    onReminderTimeClick = { _, _ -> },
                    onBatteryOptimizationClick = {},
                    onExactAlarmClick = {},
                    onChangePasswordClick = {},
                    onDeleteAccountClick = {},
                    onCustomizeBlocksClick = {},
                    onClearCacheClick = {}
                )
            }
        }

        // Click Language menu option
        composeTestRule.onNodeWithText("Ngôn ngữ").performClick()
        assertTrue(languageClicked)

        // Toggle passcode lock switch
        composeTestRule.onNode(hasAnySibling(hasText("Khóa mã PIN")) and hasRole(Role.Switch))
            .performScrollTo()
            .performClick()
        assertTrue(passcodeToggled)
    }
}



