package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun updateProfile(user: User): Result<Unit>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
    fun isUserSignedIn(): Boolean
}
