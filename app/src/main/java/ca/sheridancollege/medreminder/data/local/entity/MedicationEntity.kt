package ca.sheridancollege.medreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dosage: String,
    val timeHour: Int,
    val timeMinute: Int,
    val days: String,
    val isTakenToday: Boolean = false,
    val takenTimestamp: Long? = null,
    val isActive: Boolean = true,
    val notes: String = ""
)