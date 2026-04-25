package ca.sheridancollege.medreminder.domain.model

data class Medication(
    val id: Int = 0,
    val name: String,
    val dosageAmount: Double,
    val dosageUnit: String, // mg, ml, tablet
    val medicationType: MedicationType = MedicationType.TABLET,
    val times: List<MedicationTime>,
    val days: List<DayOfWeek>,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val instructions: String? = null,
    val isAsNeeded: Boolean = false,
    val maxPerDay: Int? = null, // For PRN
    val isActive: Boolean = true,
    val notes: String = "",
    
    // Visual Pill Features
    val pillColor: String = "#2196F3",
    val pillShape: PillShape = PillShape.ROUND,
    
    // Inventory Tracking
    val stockQuantity: Int = 0,
    val remainingQuantity: Int = 0,
    val refillThreshold: Int = 5,
    
    // Sync and Meta
    val nfcTagId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Deprecated fields for transition or compatibility if needed
    // val timeHour: Int, val timeMinute: Int, val dosage: String 
    
    fun isScheduledForToday(): Boolean {
        if (!isActive) return false
        val now = System.currentTimeMillis()
        if (now < startDate) return false
        if (endDate != null && now > endDate) return false
        
        val today = java.util.Calendar.getInstance()
            .get(java.util.Calendar.DAY_OF_WEEK)
        return days.any { it.calendarValue == today }
    }
}

data class MedicationTime(
    val hour: Int,
    val minute: Int
) {
    fun formatted(): String {
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val m = String.format("%02d", minute)
        val amPm = if (hour < 12) "AM" else "PM"
        return "$h:$m $amPm"
    }
}

enum class MedicationType(val displayName: String) {
    TABLET("Tablet"),
    CAPSULE("Capsule"),
    SYRUP("Syrup"),
    INJECTION("Injection"),
    CREAM("Cream"),
    OTHER("Other")
}

enum class PillShape(val displayName: String) {
    ROUND("Round"),
    CAPSULE("Capsule"),
    OVAL("Oval"),
    SQUARE("Square")
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
            if (codes.isBlank()) emptyList() else codes.split(",").mapNotNull { fromCode(it.trim()) }
        fun toCodes(days: List<DayOfWeek>): String =
            days.joinToString(",") { it.code }
    }
}
