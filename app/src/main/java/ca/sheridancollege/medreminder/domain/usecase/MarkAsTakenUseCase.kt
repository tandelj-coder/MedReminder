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

        // Assuming current time to verify if taken, logic needs adjustment based on MedicationTime list
        // For now, checking if any log exists for today.
        // medication.isTakenToday needs to be implemented or handled.
        // repository.markAsTaken requires updated parameters.
        
        repository.markAsTaken(
            medicationId = medication.id,
            timestamp = now,
            // Assuming we take the first scheduled time or handle differently
            scheduledHour = medication.times.firstOrNull()?.hour ?: 0,
            scheduledMinute = medication.times.firstOrNull()?.minute ?: 0
        )
        return MarkAsTakenResult.Success
    }
}