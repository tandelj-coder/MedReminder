package ca.sheridancollege.medreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "intake_logs",
    foreignKeys = [ForeignKey(
        entity = MedicationEntity::class,
        parentColumns = ["id"],
        childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class IntakeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationId: Int,
    val medicationName: String,
    val takenAt: Long,
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val wasOnTime: Boolean,
    val snoozeReason: String = "",
    val wasDoubleDoseAttempt: Boolean = false
)