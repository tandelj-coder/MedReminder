package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import javax.inject.Inject

sealed class MarkAsTakenResult {
    object Success : MarkAsTakenResult()
    data class DoubleDoseWarning(
        val lastTakenAt: Long,
        val minutesSinceLastDose: Long
    ) : MarkAsTakenResult()
}

class MarkAsTakenUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(
        doseEvent: DoseEvent,
        forceConfirm: Boolean = false
    ): MarkAsTakenResult {
        val now = System.currentTimeMillis()

        if (!forceConfirm) {
            val lastLog = repository.getLastIntakeForMedication(doseEvent.medicationId)
            if (lastLog != null) {
                val minutesSince = (now - lastLog.takenAt) / 60000
                if (minutesSince < 120) {
                    return MarkAsTakenResult.DoubleDoseWarning(
                        lastTakenAt = lastLog.takenAt,
                        minutesSinceLastDose = minutesSince
                    )
                }
            }
        }

        val cal = java.util.Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }
        repository.markAsTaken(
            medicationId    = doseEvent.medicationId,
            timestamp       = now,
            scheduledHour   = cal.get(java.util.Calendar.HOUR_OF_DAY),
            scheduledMinute = cal.get(java.util.Calendar.MINUTE),
            doseEventId     = doseEvent.id
        )
        return MarkAsTakenResult.Success
    }
}
