package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AuthViewModel : ViewModel() {
    private val _loginStatus = MutableLiveData<AuthStatus>()
    val loginStatus: LiveData<AuthStatus> = _loginStatus

    private val _signUpStatus = MutableLiveData<AuthStatus>()
    val signUpStatus: LiveData<AuthStatus> = _signUpStatus

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginStatus.value = AuthStatus.Loading
                // Simulate API call
                delay(2000)

                // Mock validation
                if (email.contains("@") && password.length >= 6) {
                    _loginStatus.value = AuthStatus.Success("Login successful")
                } else {
                    _loginStatus.value = AuthStatus.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _loginStatus.value = AuthStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _signUpStatus.value = AuthStatus.Loading
                // Simulate API call
                delay(2000)

                // Mock validation
                if (name.isNotEmpty() && email.contains("@") && password.length >= 6) {
                    _signUpStatus.value = AuthStatus.Success("Account created successfully")
                } else {
                    _signUpStatus.value = AuthStatus.Error("Please check all fields")
                }
            } catch (e: Exception) {
                _signUpStatus.value = AuthStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        // Handle logout logic
    }
}

sealed class AuthStatus {
    object Loading : AuthStatus()
    data class Success(val message: String) : AuthStatus()
    data class Error(val message: String) : AuthStatus()
}
