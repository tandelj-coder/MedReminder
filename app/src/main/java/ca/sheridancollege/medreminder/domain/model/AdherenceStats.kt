package ca.sheridancollege.medreminder.domain.model

data class AdherenceStats(
    val totalScheduled: Int,
    val totalTaken: Int,
    val currentStreak: Int,
    val adherencePercentage: Float = if (totalScheduled > 0)
        (totalTaken.toFloat() / totalScheduled) * 100f else 0f
)