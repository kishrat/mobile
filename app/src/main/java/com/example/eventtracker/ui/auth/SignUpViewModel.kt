package com.example.eventtracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventtracker.data.model.User
import com.example.eventtracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val user: User) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    fun signUp(email: String, password: String, displayName: String, isOrganizer: Boolean) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading

            val result = authRepository.signUp(email, password, displayName, isOrganizer)

            _signUpState.value = if (result.isSuccess) {
                SignUpState.Success(result.getOrNull()!!)
            } else {
                SignUpState.Error(
                    result.exceptionOrNull()?.message ?: "Sign up failed"
                )
            }
        }
    }
}