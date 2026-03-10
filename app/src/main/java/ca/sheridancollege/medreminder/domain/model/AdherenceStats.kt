package ca.sheridancollege.medreminder.domain.model

data class DayAdherence(
    val label: String,       // "M", "T", "W" etc
    val hadMedications: Boolean,
    val tookAll: Boolean,
    val isToday: Boolean
)

data class AdherenceStats(
    val totalScheduled: Int,
    val totalTaken: Int,
    val currentStreak: Int,
    val weeklyAdherence: List<DayAdherence> = emptyList(),
    val adherencePercentage: Float = if (totalScheduled > 0)
        (totalTaken.toFloat() / totalScheduled) * 100f else 0f
)
