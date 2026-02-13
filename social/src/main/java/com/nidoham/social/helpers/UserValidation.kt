package com.nidoham.social.helpers

import com.nidoham.social.domain.profile.Profile

object UserValidation {

    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.Error("Username cannot be empty")
            username.length < 3 -> ValidationResult.Error("Username must be at least 3 characters")
            username.length > 30 -> ValidationResult.Error("Username must be at most 30 characters")
            !username.matches("^[a-zA-Z0-9._]+$".toRegex()) ->
                ValidationResult.Error("Username can only contain letters, numbers, dots and underscores")
            username.startsWith(".") || username.startsWith("_") ->
                ValidationResult.Error("Username cannot start with dot or underscore")
            username.endsWith(".") || username.endsWith("_") ->
                ValidationResult.Error("Username cannot end with dot or underscore")
            username.contains("..") || username.contains("__") ->
                ValidationResult.Error("Username cannot have consecutive dots or underscores")
            !ProfileFetch.available(username) ->
                ValidationResult.Error("Username is already taken")
            else -> ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email cannot be empty")
            !email.contains("@") -> ValidationResult.Error("Email must contain @")
            !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()) ->
                ValidationResult.Error("Invalid email format")
            ProfileFetch.byEmail(email) != null ->
                ValidationResult.Error("Email is already registered")
            else -> ValidationResult.Success
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) return ValidationResult.Success

        return when {
            !phone.matches("^\\+?[1-9]\\d{1,14}$".toRegex()) ->
                ValidationResult.Error("Invalid phone number format")
            else -> ValidationResult.Success
        }
    }

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Name cannot be empty")
            name.length < 2 -> ValidationResult.Error("Name must be at least 2 characters")
            name.length > 50 -> ValidationResult.Error("Name must be at most 50 characters")
            !name.matches("^[a-zA-Z\\s'-]+$".toRegex()) ->
                ValidationResult.Error("Name can only contain letters, spaces, hyphens and apostrophes")
            else -> ValidationResult.Success
        }
    }

    fun validateBio(bio: String): ValidationResult {
        return when {
            bio.length > 500 -> ValidationResult.Error("Bio must be at most 500 characters")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.length < 8 -> ValidationResult.Error("Password must be at least 8 characters")
            password.length > 128 -> ValidationResult.Error("Password must be at most 128 characters")
            !password.any { it.isUpperCase() } ->
                ValidationResult.Error("Password must contain at least one uppercase letter")
            !password.any { it.isLowerCase() } ->
                ValidationResult.Error("Password must contain at least one lowercase letter")
            !password.any { it.isDigit() } ->
                ValidationResult.Error("Password must contain at least one digit")
            !password.any { !it.isLetterOrDigit() } ->
                ValidationResult.Error("Password must contain at least one special character")
            else -> ValidationResult.Success
        }
    }

    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return if (password == confirmPassword) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Passwords do not match")
        }
    }

    fun validateUrl(url: String): ValidationResult {
        if (url.isBlank()) return ValidationResult.Success

        return if (url.matches("^https?://.*".toRegex())) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Invalid URL format")
        }
    }

    fun validateGender(gender: String): ValidationResult {
        val validGenders = listOf("male", "female", "other", "prefer_not_to_say", "")
        return if (gender in validGenders) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Invalid gender value")
        }
    }

    fun validateProfile(profile: Profile): Map<String, ValidationResult> {
        return mapOf(
            "name" to validateName(profile.name),
            "username" to validateUsername(profile.username),
            "email" to validateEmail(profile.email),
            "phone" to validatePhone(profile.phone),
            "bio" to validateBio(profile.bio),
            "gender" to validateGender(profile.gender)
        )
    }

    fun isProfileValid(profile: Profile): Boolean {
        return validateProfile(profile).all { it.value is ValidationResult.Success }
    }

    fun suggestUsernames(baseName: String, count: Int = 5): List<String> {
        val suggestions = mutableListOf<String>()
        val cleanName = baseName.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(20)

        if (ProfileFetch.available(cleanName)) {
            suggestions.add(cleanName)
        }

        repeat(count * 2) {
            val suggestion = when {
                suggestions.size < count / 2 -> "${cleanName}${(100..999).random()}"
                else -> "${cleanName}_${(10..99).random()}"
            }

            if (ProfileFetch.available(suggestion) && suggestion !in suggestions) {
                suggestions.add(suggestion)
            }

            if (suggestions.size >= count) return@repeat
        }

        return suggestions.take(count)
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun errorMessage(): String? = (this as? Error)?.message
}