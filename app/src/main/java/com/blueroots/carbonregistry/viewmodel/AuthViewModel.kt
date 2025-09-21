package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueroots.carbonregistry.data.models.*
import com.blueroots.carbonregistry.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _authState = MutableLiveData<AuthUser?>()
    val authState: LiveData<AuthUser?> = _authState

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _signUpResult = MutableLiveData<AuthResult>()
    val signUpResult: LiveData<AuthResult> = _signUpResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    init {
        checkAuthState()
        observeAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                _authState.value = currentUser
                _isLoggedIn.value = currentUser != null

                // Load user profile if authenticated
                if (currentUser != null) {
                    loadUserProfile()
                }
            } catch (e: Exception) {
                _authState.value = null
                _isLoggedIn.value = false
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                _authState.value = user
                _isLoggedIn.value = user != null

                if (user != null) {
                    loadUserProfile()
                } else {
                    _userProfile.value = null
                }
            }
        }
    }

    private suspend fun loadUserProfile() {
        try {
            val profile = authRepository.getUserProfile()
            _userProfile.value = profile
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validate input
                when {
                    email.isBlank() -> {
                        _loginResult.value = AuthResult.Error("Email cannot be empty")
                        return@launch
                    }
                    password.isBlank() -> {
                        _loginResult.value = AuthResult.Error("Password cannot be empty")
                    }
                    !isValidEmail(email) -> {
                        _loginResult.value = AuthResult.Error("Please enter a valid email")
                        return@launch
                    }
                    else -> {
                        val request = LoginRequest(email, password)
                        val response = authRepository.signIn(request)

                        if (response.success) {
                            _authState.value = response.user
                            _isLoggedIn.value = true
                            _loginResult.value = AuthResult.Success(response.message)
                        } else {
                            _loginResult.value = AuthResult.Error(response.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _loginResult.value = AuthResult.Error("Login failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String, fullName: String, organizationName: String, organizationType: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validate input
                when {
                    email.isBlank() -> {
                        _signUpResult.value = AuthResult.Error("Email cannot be empty")
                        return@launch
                    }
                    password.isBlank() -> {
                        _signUpResult.value = AuthResult.Error("Password cannot be empty")
                        return@launch
                    }
                    confirmPassword.isBlank() -> {
                        _signUpResult.value = AuthResult.Error("Please confirm your password")
                        return@launch
                    }
                    fullName.isBlank() -> {
                        _signUpResult.value = AuthResult.Error("Full name cannot be empty")
                        return@launch
                    }
                    !isValidEmail(email) -> {
                        _signUpResult.value = AuthResult.Error("Please enter a valid email")
                        return@launch
                    }
                    password.length < 6 -> {
                        _signUpResult.value = AuthResult.Error("Password must be at least 6 characters")
                        return@launch
                    }
                    password != confirmPassword -> {
                        _signUpResult.value = AuthResult.Error("Passwords do not match")
                        return@launch
                    }
                    else -> {
                        val request = RegisterRequest(
                            email = email,
                            password = password,
                            fullName = fullName,
                            organizationName = organizationName,
                            organizationType = organizationType
                        )

                        val response = authRepository.signUp(request)

                        if (response.success) {
                            _signUpResult.value = AuthResult.Success(response.message)
                            // Don't automatically log in - user needs to verify email
                        } else {
                            _signUpResult.value = AuthResult.Error(response.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _signUpResult.value = AuthResult.Error("Registration failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val response = authRepository.signOut()
                if (response.success) {
                    _authState.value = null
                    _isLoggedIn.value = false
                    _userProfile.value = null
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            loadUserProfile()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    sealed class AuthResult {
        data class Success(val message: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}
