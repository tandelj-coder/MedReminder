package ca.sheridancollege.medreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dosageAmount: Double,
    val dosageUnit: String,
    val medicationType: String = "TABLET",
    val timesJson: String, // List<MedicationTime> serialized
    val days: String,      // List<DayOfWeek> codes
    val startDate: Long,
    val endDate: Long? = null,
    val instructions: String? = null,
    val isAsNeeded: Boolean = false,
    val maxPerDay: Int? = null,
    val isActive: Boolean = true,
    val notes: String = "",
    val pillColor: String = "#2196F3",
    val pillShape: String = "ROUND",
    val stockQuantity: Int = 0,
    val remainingQuantity: Int = 0,
    val refillThreshold: Int = 5,
    val nfcTagId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
