package com.example.bm_mobile.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bm_mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUsernameChange(v: String) { username = v }
    fun onPasswordChange(v: String) { password = v }

    fun login() {
        if (username.isBlank() || password.isBlank()) {
            _state.value = LoginUiState.Error("Podaj login i hasło")
            return
        }
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            repo.login(username, password).fold(
                onSuccess = { _state.value = LoginUiState.Success },
                onFailure = { _state.value = LoginUiState.Error(it.message ?: "Błąd logowania") }
            )
        }
    }

    companion object {
        fun factory(repo: AuthRepository) = viewModelFactory {
            initializer { LoginViewModel(repo) }
        }
    }
}
