package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String? get() = auth.currentUser?.uid

    suspend fun saveUserInfo(user: User) {
        val data = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl,
            "lastLogin" to System.currentTimeMillis()
        )
        firestore.collection("users").document(user.uid)
            .set(data, SetOptions.merge()).await()
    }

    suspend fun uploadMedication(medication: Medication) {
        val uid = userId ?: return
        val docRef = firestore.collection("users").document(uid)
            .collection("medications").document(medication.id.toString())
        
        val data = hashMapOf(
            "id" to medication.id,
            "name" to medication.name,
            "dosage" to medication.dosage,
            "timeHour" to medication.timeHour,
            "timeMinute" to medication.timeMinute,
            "days" to medication.days.map { it.code },
            "notes" to medication.notes,
            "isActive" to medication.isActive,
            "lastUpdated" to System.currentTimeMillis()
        )
        
        docRef.set(data, SetOptions.merge()).await()
    }

    suspend fun deleteMedication(medicationId: Int) {
        val uid = userId ?: return
        firestore.collection("users").document(uid)
            .collection("medications").document(medicationId.toString())
            .delete().await()
    }
}
