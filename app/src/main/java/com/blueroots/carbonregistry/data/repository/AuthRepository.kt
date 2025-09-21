package com.blueroots.carbonregistry.data.repository

import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.data.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    private val supabase = SupabaseClient.client

    suspend fun signUp(request: RegisterRequest): AuthResponse = try {
        supabase.auth.signUpWith(Email) {
            email = request.email
            password = request.password
            data = buildJsonObject {
                put("full_name", request.fullName)
                put("organization_name", request.organizationName)
                put("organization_type", request.organizationType)
            }
        }

        AuthResponse(
            success = true,
            message = "Registration successful! Please check your email to verify your account."
        )
    } catch (e: Exception) {
        AuthResponse(
            success = false,
            message = "Registration failed: ${e.message}",
            errors = listOf(e.message ?: "Unknown error")
        )
    }

    suspend fun signIn(request: LoginRequest): AuthResponse = try {
        supabase.auth.signInWith(Email) {
            email = request.email
            password = request.password
        }

        val currentUser = getCurrentUser()

        AuthResponse(
            success = true,
            message = "Login successful!",
            user = currentUser
        )
    } catch (e: Exception) {
        AuthResponse(
            success = false,
            message = "Login failed: ${e.message}",
            errors = listOf(e.message ?: "Invalid credentials")
        )
    }

    suspend fun signOut(): LogoutResponse = try {
        supabase.auth.signOut()
        LogoutResponse(
            success = true,
            message = "Signed out successfully"
        )
    } catch (e: Exception) {
        LogoutResponse(
            success = false,
            message = "Sign out failed: ${e.message}"
        )
    }

    suspend fun getCurrentUser(): AuthUser? = try {
        val user = supabase.auth.currentUserOrNull()
        user?.let { mapToAuthUser(it) }
    } catch (e: Exception) {
        null
    }

    // Simplified profile management using SharedPreferences for now
    suspend fun getUserProfile(): UserProfile? = try {
        val user = getCurrentUser()
        user?.let {
            UserProfile(
                id = it.id,
                email = it.email,
                fullName = it.fullName,
                organizationName = it.organizationName,
                organizationType = it.organizationType,
                isVerified = it.isVerified,
                createdAt = it.createdAt
            )
        }
    } catch (e: Exception) {
        null
    }

    suspend fun updateUserProfile(profile: UserProfile): Boolean {
        // TODO: Implement when database operations are working
        return true
    }

    fun getAuthStateFlow(): Flow<AuthUser?> = flow {
        try {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is io.github.jan.supabase.gotrue.SessionStatus.Authenticated -> {
                        emit(getCurrentUser())
                    }
                    is io.github.jan.supabase.gotrue.SessionStatus.NotAuthenticated -> {
                        emit(null)
                    }
                    else -> {
                        emit(getCurrentUser())
                    }
                }
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    private fun mapToAuthUser(user: UserInfo): AuthUser {
        val metadata = user.userMetadata
        return AuthUser(
            id = user.id,
            email = user.email ?: "",
            fullName = metadata?.get("full_name")?.toString() ?: "",
            organizationName = metadata?.get("organization_name")?.toString() ?: "",
            organizationType = metadata?.get("organization_type")?.toString() ?: "",
            isVerified = user.emailConfirmedAt != null,
            avatarUrl = metadata?.get("avatar_url")?.toString(),
            createdAt = user.createdAt.toString(),
            emailConfirmedAt = user.emailConfirmedAt?.toString()
        )
    }
}
