package com.osia.petsos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osia.petsos.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun onGoogleSignInResult(idToken: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                _loginState.value = LoginState.Success
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("LoginViewModel", "Google Sign-In failed: $errorMsg", result.exceptionOrNull())
                _loginState.value = LoginState.Error(errorMsg)
            }
        }
    }

    fun onError(message: String) {
        _loginState.value = LoginState.Error(message)
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

}