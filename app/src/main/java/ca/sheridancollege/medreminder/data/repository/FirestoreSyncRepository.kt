package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.model.PillShape
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
            "dosageAmount" to medication.dosageAmount,
            "dosageUnit" to medication.dosageUnit,
            "times" to medication.times.map { mapOf("hour" to it.hour, "minute" to it.minute) },
            "days" to medication.days.map { it.code },
            "notes" to medication.notes,
            "isActive" to medication.isActive,
            "pillColor" to medication.pillColor,
            "pillShape" to medication.pillShape.name,
            "stockQuantity" to medication.stockQuantity,
            "remainingQuantity" to medication.remainingQuantity,
            "refillThreshold" to medication.refillThreshold,
            "nfcTagId" to medication.nfcTagId,
            "updatedAt" to medication.updatedAt
        )
        
        docRef.set(data, SetOptions.merge()).await()
    }

    suspend fun uploadIntakeLog(log: IntakeLog) {
        val uid = userId ?: return
        val docRef = firestore.collection("users").document(uid)
            .collection("intakeLogs").document(log.id.toString())

        val data = hashMapOf(
            "id" to log.id,
            "medicationId" to log.medicationId,
            "medicationName" to log.medicationName,
            "takenAt" to log.takenAt,
            "scheduledHour" to log.scheduledHour,
            "scheduledMinute" to log.scheduledMinute,
            "wasOnTime" to log.wasOnTime,
            "snoozeReason" to log.snoozeReason,
            "wasDoubleDoseAttempt" to log.wasDoubleDoseAttempt,
            "updatedAt" to log.updatedAt
        )

        docRef.set(data, SetOptions.merge()).await()
    }

    suspend fun fetchMedications(): List<Medication> {
        val uid = userId ?: return emptyList()
        val snapshot = firestore.collection("users").document(uid)
            .collection("medications").get().await()
        
        return snapshot.documents.mapNotNull { doc ->
            try {
                Medication(
                    id = (doc.getLong("id") ?: 0L).toInt(),
                    name = doc.getString("name") ?: "",
                    dosageAmount = doc.getDouble("dosageAmount") ?: 0.0,
                    dosageUnit = doc.getString("dosageUnit") ?: "",
                    times = (doc.get("times") as? List<Map<String, Any>>)?.mapNotNull {
                        val hour = (it["hour"] as? Long)?.toInt()
                        val minute = (it["minute"] as? Long)?.toInt()
                        if (hour != null && minute != null) {
                            ca.sheridancollege.medreminder.domain.model.MedicationTime(hour, minute)
                        } else null
                    } ?: emptyList(),
                    days = (doc.get("days") as? List<String>)?.mapNotNull { DayOfWeek.fromCode(it) } ?: emptyList(),
                    isActive = doc.getBoolean("isActive") ?: true,
                    notes = doc.getString("notes") ?: "",
                    pillColor = doc.getString("pillColor") ?: "#2196F3",
                    pillShape = PillShape.valueOf(doc.getString("pillShape") ?: "ROUND"),
                    stockQuantity = (doc.getLong("stockQuantity") ?: 0L).toInt(),
                    remainingQuantity = (doc.getLong("remainingQuantity") ?: 0L).toInt(),
                    refillThreshold = (doc.getLong("refillThreshold") ?: 5L).toInt(),
                    nfcTagId = doc.getString("nfcTagId"),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun fetchIntakeLogs(): List<IntakeLog> {
        val uid = userId ?: return emptyList()
        val snapshot = firestore.collection("users").document(uid)
            .collection("intakeLogs").get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                IntakeLog(
                    id = (doc.getLong("id") ?: 0L).toInt(),
                    medicationId = (doc.getLong("medicationId") ?: 0L).toInt(),
                    medicationName = doc.getString("medicationName") ?: "",
                    takenAt = doc.getLong("takenAt") ?: 0L,
                    scheduledHour = (doc.getLong("scheduledHour") ?: 0L).toInt(),
                    scheduledMinute = (doc.getLong("scheduledMinute") ?: 0L).toInt(),
                    wasOnTime = doc.getBoolean("wasOnTime") ?: true,
                    snoozeReason = doc.getString("snoozeReason") ?: "",
                    wasDoubleDoseAttempt = doc.getBoolean("wasDoubleDoseAttempt") ?: false,
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun deleteMedication(medicationId: Int) {
        val uid = userId ?: return
        firestore.collection("users").document(uid)
            .collection("medications").document(medicationId.toString())
            .delete().await()
    }
}
