package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _signUpResult = MutableLiveData<AuthResult>()
    val signUpResult: LiveData<AuthResult> = _signUpResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Simple in-memory user storage (replace with database later)
    private val users = mutableMapOf<String, String>()

    init {
        // Add some demo users
        users["john.doe@blueroots.com"] = "password123"
        users["demo@blueroots.com"] = "demo123"
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate network delay
            delay(1500)

            when {
                email.isBlank() -> {
                    _loginResult.value = AuthResult.Error("Email cannot be empty")
                }
                password.isBlank() -> {
                    _loginResult.value = AuthResult.Error("Password cannot be empty")
                }
                !isValidEmail(email) -> {
                    _loginResult.value = AuthResult.Error("Please enter a valid email")
                }
                users[email] == password -> {
                    _isLoggedIn.value = true
                    _loginResult.value = AuthResult.Success("Login successful")
                }
                users.containsKey(email) -> {
                    _loginResult.value = AuthResult.Error("Incorrect password")
                }
                else -> {
                    _loginResult.value = AuthResult.Error("User not found. Please sign up.")
                }
            }

            _isLoading.value = false
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate network delay
            delay(1500)

            when {
                email.isBlank() -> {
                    _signUpResult.value = AuthResult.Error("Email cannot be empty")
                }
                password.isBlank() -> {
                    _signUpResult.value = AuthResult.Error("Password cannot be empty")
                }
                confirmPassword.isBlank() -> {
                    _signUpResult.value = AuthResult.Error("Please confirm your password")
                }
                !isValidEmail(email) -> {
                    _signUpResult.value = AuthResult.Error("Please enter a valid email")
                }
                password.length < 6 -> {
                    _signUpResult.value = AuthResult.Error("Password must be at least 6 characters")
                }
                password != confirmPassword -> {
                    _signUpResult.value = AuthResult.Error("Passwords do not match")
                }
                users.containsKey(email) -> {
                    _signUpResult.value = AuthResult.Error("User already exists. Please login.")
                }
                else -> {
                    users[email] = password
                    _isLoggedIn.value = true
                    _signUpResult.value = AuthResult.Success("Account created successfully")
                }
            }

            _isLoading.value = false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    sealed class AuthResult {
        data class Success(val message: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}
