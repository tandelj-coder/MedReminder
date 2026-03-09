package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.Medication
import javax.inject.Inject

sealed class MarkAsTakenResult {
    object Success : MarkAsTakenResult()
    data class DoubleDoseWarning(
        val medication: Medication,
        val lastTakenAt: Long,
        val minutesSinceLastDose: Long
    ) : MarkAsTakenResult()
    object AlreadyTaken : MarkAsTakenResult()
}

class MarkAsTakenUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(
        medication: Medication,
        forceConfirm: Boolean = false
    ): MarkAsTakenResult {
        val now = System.currentTimeMillis()

        if (medication.isTakenToday && !forceConfirm) {
            return MarkAsTakenResult.AlreadyTaken
        }

        val lastLog = repository.getLastIntakeForMedication(medication.id)
        if (lastLog != null && !forceConfirm) {
            val minutesSince = (now - lastLog.takenAt) / 60000
            if (minutesSince < 120) {
                return MarkAsTakenResult.DoubleDoseWarning(
                    medication = medication,
                    lastTakenAt = lastLog.takenAt,
                    minutesSinceLastDose = minutesSince
                )
            }
        }

        repository.markAsTaken(
            medicationId = medication.id,
            timestamp = now,
            scheduledHour = medication.timeHour,
            scheduledMinute = medication.timeMinute
        )
        return MarkAsTakenResult.Success
    }
}