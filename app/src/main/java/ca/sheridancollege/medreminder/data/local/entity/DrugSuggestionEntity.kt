package ca.sheridancollege.medreminder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drug_suggestions",
    indices = [
        Index(value = ["searchQuery"], unique = false),
        Index(value = ["drugName"], unique = false)
    ]
)
data class DrugSuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val searchQuery: String,
    val drugName: String,
    val cachedAt: Long = System.currentTimeMillis()
)
