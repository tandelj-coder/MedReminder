package ca.sheridancollege.medreminder.domain.model

data class DoseEvent(
    val id: Int,
    val medicationId: Int,
    val medicationName: String,
    val scheduledTime: Long,
    val status: DoseStatus,
    val takenAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun isTaken(): Boolean = status == DoseStatus.TAKEN
    fun isScheduled(): Boolean = status == DoseStatus.SCHEDULED
    fun isMissed(): Boolean = status == DoseStatus.MISSED
    fun isSnoozed(): Boolean = status == DoseStatus.SNOOZED

    fun minutesUntilDue(now: Long = System.currentTimeMillis()): Long =
        (scheduledTime - now) / 60000

    fun isOverdue(now: Long = System.currentTimeMillis()): Boolean =
        scheduledTime <= now && !isTaken()
}

enum class DoseStatus {
    SCHEDULED,
    TAKEN,
    MISSED,
    SNOOZED
}
