package com.example.flow.ui.screens.auth

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flow.data.model.AuthResult
import com.example.flow.data.repository.AuthRepository
import com.example.flow.ui.screens.time.TimeRecordsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val resultChannel = Channel<AuthResult<Unit>>()
    val authResults = resultChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            authenticate()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            val result = authRepository.login(email, password)
            resultChannel.send(result)
            _state.update {
                it.copy(loading = false)
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            val result = authRepository.signUp(email, password)
            resultChannel.send(result)
            _state.update {
                it.copy(loading = false)
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            val result = authRepository.logout()
            resultChannel.send(result)
            _state.update {
                it.copy(loading = false)
            }
        }
    }

    private fun authenticate() {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            val result = authRepository.authenticate()
            resultChannel.send(result)
            _state.update {
                it.copy(loading = false)
            }
        }
    }

}