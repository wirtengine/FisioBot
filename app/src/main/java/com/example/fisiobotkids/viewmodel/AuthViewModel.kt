package com.example.fisiobotkids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val userEmail: String? = FirebaseAuth.getInstance().currentUser?.email,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {

                auth.signInWithEmailAndPassword(
                    email,
                    password
                ).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userEmail = auth.currentUser?.email
                )

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun register(email: String, password: String) {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {

                auth.createUserWithEmailAndPassword(
                    email,
                    password
                ).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userEmail = auth.currentUser?.email
                )

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun logout() {

        auth.signOut()

        _uiState.value = AuthUiState()
    }

    fun clearError() {

        _uiState.value = _uiState.value.copy(
            error = null
        )
    }

    fun registerChild(
        email: String,
        password: String,
        onSuccess: (uid: String) -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {

                val authResult = auth
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                val uid = authResult.user?.uid
                    ?: throw Exception("UID no obtenido")

                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )

                onSuccess(uid)

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )

                onError(
                    e.localizedMessage ?: "Error desconocido"
                )
            }
        }
    }
}