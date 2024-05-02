package com.example.flow.ui.screens.auth

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flow.data.model.AuthResult
import com.example.flow.data.model.User
import com.example.flow.data.repository.AuthRepository
import com.example.flow.ui.screens.time.TimeRecordsState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityScoped
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
    val error: String = "",
    val currentUser: User? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val resultChannel = Channel<AuthResult<User>>()
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
            _state.update {
                it.copy(
                    loading = false,
                    currentUser = result.data
                )
            }
            resultChannel.send(result)
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            val result = authRepository.signUp(email, password)
            _state.update {
                it.copy(
                    loading = false,
                    currentUser = result.data
                )
            }
            resultChannel.send(result)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            val result = authRepository.logout()
            _state.update {
                it.copy(loading = false)
            }
            resultChannel.send(result)
        }
    }

    private fun authenticate() {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }

            val result = authRepository.authenticate()

            Log.i("AuthViewModel", "authenticate: ${result.data?.username}")
            _state.update {
                it.copy(
                    loading = false,
                    currentUser = result.data
                )
            }
            resultChannel.send(result)
        }
    }

    fun setTimewHook(enabled: Boolean) {
        viewModelScope.launch {
            val result = authRepository.setTimewHook(enabled)

            _state.update {
                it.copy(
                    currentUser = it.currentUser?.copy(timewHook = result)
                )
            }
        }
    }
}