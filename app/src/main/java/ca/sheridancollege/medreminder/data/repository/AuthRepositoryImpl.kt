package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firestoreSyncRepository: FirestoreSyncRepository
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Fetch full details from Firestore
                val listener = firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        
                        val user = if (snapshot != null && snapshot.exists()) {
                            User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email,
                                displayName = snapshot.getString("displayName") ?: firebaseUser.displayName,
                                photoUrl = snapshot.getString("photoUrl") ?: firebaseUser.photoUrl?.toString(),
                                phoneNumber = snapshot.getString("phoneNumber"),
                                height = snapshot.getString("height"),
                                weight = snapshot.getString("weight"),
                                illness = snapshot.getString("illness"),
                                isProfileComplete = snapshot.getBoolean("isProfileComplete") ?: false
                            )
                        } else {
                            User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email,
                                displayName = firebaseUser.displayName,
                                photoUrl = firebaseUser.photoUrl?.toString(),
                                isProfileComplete = false
                            )
                        }
                        trySend(user)
                    }
                // Note: In a production app, you'd manage this listener better
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Sign in failed")
            
            // Check if user exists in Firestore
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (doc.exists()) {
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = doc.getString("displayName") ?: firebaseUser.displayName,
                    photoUrl = doc.getString("photoUrl") ?: firebaseUser.photoUrl?.toString(),
                    phoneNumber = doc.getString("phoneNumber"),
                    height = doc.getString("height"),
                    weight = doc.getString("weight"),
                    illness = doc.getString("illness"),
                    isProfileComplete = doc.getBoolean("isProfileComplete") ?: false
                )
            } else {
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    isProfileComplete = false
                )
                firestoreSyncRepository.saveUserInfo(newUser)
                newUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Sign up failed")
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.email?.substringBefore("@"),
                photoUrl = null,
                isProfileComplete = false
            )
            firestoreSyncRepository.saveUserInfo(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Sign in failed")
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (doc.exists()) {
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = doc.getString("displayName") ?: firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@"),
                    photoUrl = doc.getString("photoUrl") ?: firebaseUser.photoUrl?.toString(),
                    phoneNumber = doc.getString("phoneNumber"),
                    height = doc.getString("height"),
                    weight = doc.getString("weight"),
                    illness = doc.getString("illness"),
                    isProfileComplete = doc.getBoolean("isProfileComplete") ?: false
                )
            } else {
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@"),
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    isProfileComplete = false
                )
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            val data = hashMapOf(
                "displayName" to user.displayName,
                "phoneNumber" to user.phoneNumber,
                "height" to user.height,
                "weight" to user.weight,
                "illness" to user.illness,
                "photoUrl" to user.photoUrl,
                "isProfileComplete" to true
            )
            firestore.collection("users").document(user.uid)
                .set(data as Map<String, Any>, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
            val uid = user.uid
            // Delete all Firestore data
            val medsRef = firestore.collection("users").document(uid).collection("medications").get().await()
            for (doc in medsRef.documents) { doc.reference.delete().await() }
            firestore.collection("users").document(uid).delete().await()
            // Delete Firebase Auth account
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isUserSignedIn(): Boolean = firebaseAuth.currentUser != null
}
