package com.blueroots.carbonregistry.data.models

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)

data class RegisterRequest(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val organizationName: String = "",
    val contactPerson: String = "",
    val organizationType: String = ""
)

data class AuthResponse(
    val success: Boolean = false,
    val message: String = "",
    val token: String = "",
    val refreshToken: String = "",
    val user: User? = null,
    val expiresIn: Long = 0L,
    val errors: List<String> = emptyList()
)

data class RefreshTokenRequest(
    val refreshToken: String = ""
)

data class LogoutResponse(
    val success: Boolean = false,
    val message: String = ""
)

data class User(
    val id: String = "",
    val email: String = "",
    val organizationName: String = "",
    val contactPerson: String = "",
    val organizationType: String = "",
    val isVerified: Boolean = false,
    val createdAt: String = "",
    val lastLoginAt: String = ""
)
