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
    val notes: String = "",
    // Unique features
    val pillColor: String = "#2196F3",
    val pillShape: String = "ROUND",
    val stockQuantity: Int = 0,
    val remainingQuantity: Int = 0,
    val refillThreshold: Int = 5,
    val nfcTagId: String? = null
)
