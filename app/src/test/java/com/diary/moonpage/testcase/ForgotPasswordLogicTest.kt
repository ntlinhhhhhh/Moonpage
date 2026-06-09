package com.diary.moonpage.testcase

import com.diary.moonpage.domain.usecase.validation.ValidateEmail
import com.diary.moonpage.domain.usecase.validation.ValidatePassword
import com.diary.moonpage.domain.usecase.validation.ValidateUsername
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests checking validation logic for Login, Registration, and Forgot Password flows.
 * Located in the testcase package under unit tests.
 */
class ForgotPasswordLogicTest {

    private val validateEmail = ValidateEmail()
    private val validatePassword = ValidatePassword()
    private val validateUsername = ValidateUsername()

    @Test
    fun testForgotPassword_EmailValidation() {
        // Valid email formats
        assertTrue(validateEmail.execute("user@example.com").successful)
        assertTrue(validateEmail.execute("john.doe@domain.co.uk").successful)
        assertTrue(validateEmail.execute("admin123@sub.domain.org").successful)

        // Invalid email formats
        assertFalse(validateEmail.execute("").successful)
        assertFalse(validateEmail.execute("  ").successful)
        assertFalse(validateEmail.execute("plainaddress").successful)
        assertFalse(validateEmail.execute("user@").successful)
        assertFalse(validateEmail.execute("user@domain").successful)
        assertFalse(validateEmail.execute("@domain.com").successful)
    }

    @Test
    fun testResetPassword_PasswordValidation() {
        // Valid passwords
        assertTrue(validatePassword.execute("123456").successful)
        assertTrue(validatePassword.execute("strong_password_123").successful)

        // Invalid passwords
        assertFalse(validatePassword.execute("").successful)
        assertFalse(validatePassword.execute("     ").successful)
        assertFalse(validatePassword.execute("12345").successful) // too short
    }

    @Test
    fun testRegistration_UsernameValidation() {
        // Valid usernames
        assertTrue(validateUsername.execute("John Doe").successful)
        assertTrue(validateUsername.execute("alice").successful)

        // Invalid usernames
        assertFalse(validateUsername.execute("").successful)
        assertFalse(validateUsername.execute("   ").successful)
    }

    @Test
    fun testOtpCodeFormatValidation() {
        // OTP must be a non-empty 6-digit numeric string
        val isValidOtp: (String) -> Boolean = { code ->
            code.length == 6 && code.all { it.isDigit() }
        }

        assertTrue(isValidOtp("123456"))
        assertTrue(isValidOtp("000000"))
        assertTrue(isValidOtp("999999"))

        assertFalse(isValidOtp(""))
        assertFalse(isValidOtp("12345"))     // 5 digits
        assertFalse(isValidOtp("1234567"))    // 7 digits
        assertFalse(isValidOtp("123a56"))     // contains letter
        assertFalse(isValidOtp("abcdef"))     // non-numeric
    }
}
