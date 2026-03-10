package ca.sheridancollege.medreminder.domain.model

data class Medication(
    val id: Int = 0,
    val name: String,
    val dosage: String,
    val timeHour: Int,
    val timeMinute: Int,
    val days: List<DayOfWeek>,
    val isTakenToday: Boolean = false,
    val takenTimestamp: Long? = null,
    val isActive: Boolean = true,
    val notes: String = ""
) {
    fun formattedTime(): String {
        val hour = if (timeHour == 0) 12 else if (timeHour > 12) timeHour - 12 else timeHour
        val minute = String.format("%02d", timeMinute)
        val amPm = if (timeHour < 12) "AM" else "PM"
        return "$hour:$minute $amPm"
    }

    fun isScheduledForToday(): Boolean {
        val today = java.util.Calendar.getInstance()
            .get(java.util.Calendar.DAY_OF_WEEK)
        return days.any { it.calendarValue == today }
    }
}

enum class DayOfWeek(
    val code: String,
    val displayName: String,
    val calendarValue: Int
) {
    MON("MON", "Mon", java.util.Calendar.MONDAY),
    TUE("TUE", "Tue", java.util.Calendar.TUESDAY),
    WED("WED", "Wed", java.util.Calendar.WEDNESDAY),
    THU("THU", "Thu", java.util.Calendar.THURSDAY),
    FRI("FRI", "Fri", java.util.Calendar.FRIDAY),
    SAT("SAT", "Sat", java.util.Calendar.SATURDAY),
    SUN("SUN", "Sun", java.util.Calendar.SUNDAY);

    companion object {
        fun fromCode(code: String): DayOfWeek? = values().find { it.code == code }
        fun fromCodes(codes: String): List<DayOfWeek> =
            codes.split(",").mapNotNull { fromCode(it.trim()) }
        fun toCodes(days: List<DayOfWeek>): String =
            days.joinToString(",") { it.code }
    }
}