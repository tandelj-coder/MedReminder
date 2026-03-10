package ca.sheridancollege.medreminder.domain.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val phoneNumber: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val illness: String? = null,
    val isProfileComplete: Boolean = false
)
