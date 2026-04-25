package ca.sheridancollege.medreminder.domain.model

import java.text.SimpleDateFormat
import java.util.*

data class IntakeLog(
    val id: Int = 0,
    val medicationId: Int,
    val medicationName: String,
    val takenAt: Long,
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val wasOnTime: Boolean,
    val snoozeReason: String = "",
    val wasDoubleDoseAttempt: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun formattedTime(): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(takenAt))

    fun formattedDate(): String =
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(takenAt))

    fun minutesLate(): Long {
        val scheduledMillis = Calendar.getInstance().apply {
            timeInMillis = takenAt
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        return (takenAt - scheduledMillis) / 60000
    }
}