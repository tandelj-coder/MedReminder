package ca.sheridancollege.medreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_events",
    foreignKeys = [ForeignKey(
        entity = MedicationEntity::class,
        parentColumns = ["id"],
        childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["medicationId"]),
        Index(value = ["scheduledTime"]),
        Index(value = ["status"])
    ]
)
data class DoseEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationId: Int,
    val medicationName: String,
    val scheduledTime: Long,
    val status: String,
    val takenAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class DoseStatus {
    SCHEDULED,
    TAKEN,
    MISSED,
    SNOOZED
}
