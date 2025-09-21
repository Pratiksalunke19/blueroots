package com.blueroots.carbonregistry.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class LoginRequest(
    val email: String = "",
    val password: String = ""
)

@Serializable
data class RegisterRequest(
    val email: String = "",
    val password: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    @SerialName("organization_name")
    val organizationName: String = "",
    @SerialName("organization_type")
    val organizationType: String = ""
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String = ""
)

data class AuthResponse(
    val success: Boolean = false,
    val message: String = "",
    val user: AuthUser? = null,
    val session: AuthSession? = null,
    val errors: List<String> = emptyList()
)

data class AuthUser(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val organizationName: String = "",
    val organizationType: String = "",
    val contactPerson: String = "",
    val isVerified: Boolean = false,
    val avatarUrl: String? = null,
    val createdAt: String = "",
    val emailConfirmedAt: String? = null
)

data class AuthSession(
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresIn: Long = 0L,
    val tokenType: String = "bearer"
)

@Serializable
data class UserProfile(
    val id: String = "",
    val email: String = "",
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("organization_name")
    val organizationName: String? = null,
    @SerialName("organization_type")
    val organizationType: String? = null,
    @SerialName("contact_person")
    val contactPerson: String? = null,
    val phone: String? = null,
    val address: String? = null,
    @SerialName("is_verified")
    val isVerified: Boolean = false,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

data class LogoutResponse(
    val success: Boolean = false,
    val message: String = ""
)
