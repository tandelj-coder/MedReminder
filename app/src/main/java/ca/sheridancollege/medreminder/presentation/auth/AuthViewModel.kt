package ca.sheridancollege.medreminder.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.repository.AuthRepository
import ca.sheridancollege.medreminder.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    fun onSignInResult(idToken: String?) {
        if (idToken == null) {
            _state.update { it.copy(error = "Google Sign In failed") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _state.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, password)
                .onSuccess { user ->
                    _state.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _state.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun updateProfile(
        name: String,
        phone: String,
        height: String,
        weight: String,
        illness: String,
        photoUrl: String? = null
    ) {
        val currentUser = _state.value.user ?: return
        val updatedUser = currentUser.copy(
            displayName = name,
            phoneNumber = phone,
            height = height,
            weight = weight,
            illness = illness,
            photoUrl = photoUrl ?: currentUser.photoUrl,
            isProfileComplete = true
        )

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.updateProfile(updatedUser)
                .onSuccess {
                    _state.update { it.copy(user = updatedUser, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
