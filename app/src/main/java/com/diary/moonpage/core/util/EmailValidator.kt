package com.diary.moonpage.core.util

object EmailValidator {
    private val localAllowedChars = setOf(
        '!', '#', '$', '%', '&', '\'', '*', '+', '/', '=',
        '?', '^', '_', '`', '{', '|', '}', '~', '-', '.'
    )

    fun isValid(emailInput: String): Boolean {
        val email = emailInput.trim()
        if (email.isEmpty() || email.length > 254 || email.any { it.isWhitespace() }) return false

        val parts = email.split("@")
        if (parts.size != 2) return false

        val local = parts[0]
        val domain = parts[1]
        if (local.isEmpty() || local.length > 64 || domain.isEmpty() || domain.length > 253) return false
        if (local.startsWith(".") || local.endsWith(".") || local.contains("..")) return false
        if (!local.all { it.isAsciiLetterOrDigit() || localAllowedChars.contains(it) }) return false

        if (domain.startsWith(".") || domain.endsWith(".") || domain.contains("..")) return false
        val labels = domain.split(".")
        if (labels.size < 2) return false
        if (labels.last().length !in 2..63 || !labels.last().all { it.isAsciiLetter() }) return false

        return labels.all { label ->
            label.length in 1..63 &&
                !label.startsWith("-") &&
                !label.endsWith("-") &&
                label.all { it.isAsciiLetterOrDigit() || it == '-' }
        }
    }

    private fun Char.isAsciiLetter(): Boolean = this in 'A'..'Z' || this in 'a'..'z'

    private fun Char.isAsciiLetterOrDigit(): Boolean = isAsciiLetter() || this in '0'..'9'
}
