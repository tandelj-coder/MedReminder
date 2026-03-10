package ca.sheridancollege.medreminder.presentation.auth

import ca.sheridancollege.medreminder.domain.model.User

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
